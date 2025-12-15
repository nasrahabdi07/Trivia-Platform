package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class TriviaServer {

    private static final Map<String, GameRoom> rooms = new HashMap<>();
    private static final Set<String> registeredUsers = new HashSet<>();
    private static final Set<String> connectedUsers = new HashSet<>();
    private static final List<ClientHandler> clients = new ArrayList<>();
    private static final Map<String, Integer> globalLeaderboard = new HashMap<>(); // Track all-time scores
    private static final Map<String, UserStats> userStatsMap = new HashMap<>();
    private static ServerSocket serverSocket;
    private static final String ADMIN_SECRET = "SuperSecretAdmin2025";
    private static final Set<ClientHandler> adminClients = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final java.util.concurrent.ScheduledExecutorService statsExecutor = java.util.concurrent.Executors
            .newSingleThreadScheduledExecutor();
    private static long lastTotalBytesSent = 0;
    private static long lastTotalBytesReceived = 0;

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(5050);
            System.out.println("Trivia Server running on port 5050");

            // Start stats collector (2 seconds interval)
            statsExecutor.scheduleAtFixedRate(TriviaServer::broadcastStats, 2, 2,
                    java.util.concurrent.TimeUnit.SECONDS);

            // Add shutdown hook to close server socket gracefully
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                        System.out.println("Server socket closed.");
                    }
                } catch (Exception e) {
                    System.err.println("Error closing server socket: " + e.getMessage());
                }
            }));

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                addClientHandler(handler);
                handler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (Exception e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
    }

    public static synchronized GameRoom createRoom(String name) {
        GameRoom room = new GameRoom(name, 8, 5, 15000, "General_Knowledge", "Medium", false);
        rooms.put(name, room);
        broadcastRooms();
        return room;
    }

    public static synchronized GameRoom createRoom(String name, int maxPlayers, int rounds, int perQuestionMillis,
            String category, String difficulty, boolean isPrivate) {
        GameRoom room = new GameRoom(name, maxPlayers, rounds, perQuestionMillis, category, difficulty, isPrivate);
        rooms.put(name, room);
        broadcastRooms();
        return room;
    }

    public static synchronized GameRoom getRoom(String name) {
        return rooms.get(name);
    }

    public static synchronized boolean registerUser(String username) {
        if (registeredUsers.contains(username)) {
            return false; // User already exists
        }
        registeredUsers.add(username);
        return true; // New user registered
    }

    public static synchronized boolean isUserConnected(String username) {
        return connectedUsers.contains(username);
    }

    public static synchronized void addConnectedUser(String username) {
        connectedUsers.add(username);
    }

    public static synchronized void removeConnectedUser(String username) {
        connectedUsers.remove(username);
    }

    public static synchronized void addClientHandler(ClientHandler handler) {
        clients.add(handler);
    }

    public static synchronized void removeClientHandler(ClientHandler handler) {
        clients.remove(handler);
    }

    public static synchronized String roomsList() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String name : rooms.keySet()) {
            if (!first)
                sb.append(',');
            first = false;
            sb.append(name);
        }
        return sb.toString();
    }

    public static synchronized void broadcastRooms() {
        String payload = "ROOMS " + roomsList();
        for (ClientHandler ch : clients) {
            ch.send(payload);
        }
    }

    public static synchronized void updateGlobalScore(String username, int scoreToAdd) {
        int currentScore = globalLeaderboard.getOrDefault(username, 0);
        globalLeaderboard.put(username, currentScore + scoreToAdd);
    }

    public static synchronized String getGlobalLeaderboard() {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        // Sort by score descending
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(globalLeaderboard.entrySet());
        sortedEntries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        for (Map.Entry<String, Integer> entry : sortedEntries) {
            if (!first)
                result.append(',');
            first = false;
            result.append(entry.getKey()).append(':').append(entry.getValue());
        }
        return result.toString();
    }

    public static synchronized UserStats getUserStats(String username) {
        userStatsMap.putIfAbsent(username, new UserStats());
        return userStatsMap.get(username);
    }

    public static synchronized void recordGameStats(String username, int score, boolean isWin, int correctAnswers,
            int questionsAnswered) {
        UserStats stats = getUserStats(username);
        stats.addGamePlayed();
        stats.addScore(score);
        if (isWin)
            stats.addWin();
        for (int i = 0; i < correctAnswers; i++)
            stats.addCorrectAnswer();
        for (int i = 0; i < questionsAnswered; i++)
            stats.addQuestionAnswered();

        // Also update global leaderboard for backward compatibility or if used
        // elsewhere
        updateGlobalScore(username, score);
    }

    public static synchronized void recordGameHosted(String username) {
        getUserStats(username).addGameHosted();
    }

    public static boolean authenticateAdmin(ClientHandler handler, String secret) {
        if (ADMIN_SECRET.equals(secret)) {
            adminClients.add(handler);
            return true;
        }
        return false;
    }

    private static void broadcastStats() {
        try {
            // 1. Registered Users
            int regCount = registeredUsers.size();

            // 2. Online Users
            int onlineCount = connectedUsers.size();

            // 3. Memory Usage
            Runtime rt = Runtime.getRuntime();
            long usedMemMB = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
            long maxMemMB = rt.maxMemory() / (1024 * 1024);

            // 4. CPU Usage
            java.lang.management.OperatingSystemMXBean osBean = java.lang.management.ManagementFactory
                    .getPlatformMXBean(java.lang.management.OperatingSystemMXBean.class);
            // Default to 0 if not supported
            double cpuLoad = osBean.getSystemLoadAverage();
            // Try connection to specific implementation if available for more accurate %
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                cpuLoad = ((com.sun.management.OperatingSystemMXBean) osBean).getCpuLoad() * 100;
            } else if (cpuLoad < 0) {
                cpuLoad = 0;
            }

            // 5. Network Traffic
            long currentSent = ClientHandler.totalBytesSent.get();
            long currentReceived = ClientHandler.totalBytesReceived.get();
            long deltaSent = currentSent - lastTotalBytesSent;
            long deltaReceived = currentReceived - lastTotalBytesReceived;
            lastTotalBytesSent = currentSent;
            lastTotalBytesReceived = currentReceived;

            long totalTrafficKB = (deltaSent + deltaReceived) / (1024 * 2); // Divide by 2s interval to get rate/sec,
                                                                            // then 1024 for KB

            String statsMsg = String.format(Locale.US, "STATS|Reg:%d|Online:%d|Mem:%d/%dMB|CPU:%.1f%%|Net:%dKB/s",
                    regCount, onlineCount, usedMemMB, maxMemMB, cpuLoad, totalTrafficKB);

            for (ClientHandler admin : adminClients) {
                admin.send(statsMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
