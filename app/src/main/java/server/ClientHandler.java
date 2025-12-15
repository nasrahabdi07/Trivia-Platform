package server;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private GameRoom currentRoom;
    public static final java.util.concurrent.atomic.AtomicLong totalBytesSent = new java.util.concurrent.atomic.AtomicLong(
            0);
    public static final java.util.concurrent.atomic.AtomicLong totalBytesReceived = new java.util.concurrent.atomic.AtomicLong(
            0);

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }

    public void send(String message) {
        out.println(message);
        totalBytesSent.addAndGet(message.length() + 1); // +1 for newline
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                totalBytesReceived.addAndGet(line.length() + 1); // +1 for newline
                System.out.println("Received: " + line);
                handleCommand(line);
            }
        } catch (Exception e) {
            System.out.println("Client disconnected: " + username);
            if (username != null) {
                TriviaServer.removeConnectedUser(username);
            }
        } finally {
            TriviaServer.removeClientHandler(this);
            close();
        }
    }

    private void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            // Ignore errors during cleanup
        }
    }

    private void handleCommand(String message) {
        String[] parts = message.split(" ");
        String cmd = parts[0];

        // Check for Admin Auth first
        if (cmd.startsWith("AUTH_ADMIN_")) {
            String secret = cmd.replace("AUTH_ADMIN_", "");
            if (TriviaServer.authenticateAdmin(this, secret)) {
                send("ADMIN_AUTH_OK");
            } else {
                send("ERROR InvalidAdminKey");
            }
            return;
        }

        switch (cmd) {
            case "LOGIN": {
                if (parts.length < 2) {
                    send("ERROR InvalidCommand");
                    break;
                }
                String requestedUsername = parts[1];

                // Check if username is already connected
                if (TriviaServer.isUserConnected(requestedUsername)) {
                    send("ERROR UsernameAlreadyInUse");
                    break;
                }
                // Login only allowed if already registered
                username = requestedUsername;
                if (!TriviaServer.registerUser(requestedUsername)) {
                    TriviaServer.addConnectedUser(username);
                    send("LOGIN_OK WelcomeBack");
                    System.out.println("User logged in: " + username);
                    send("ROOMS " + TriviaServer.roomsList());
                } else {
                    // registerUser returned true means newly added; treat as not previously
                    // registered for strict login
                    TriviaServer.removeConnectedUser(username);
                    send("ERROR NotRegistered");
                    username = null;
                }
                break;
            }
            case "REGISTER": {
                if (parts.length < 2) {
                    send("ERROR InvalidCommand");
                    break;
                }
                String requestedUsername = parts[1];
                if (TriviaServer.isUserConnected(requestedUsername)) {
                    send("ERROR UsernameAlreadyInUse");
                    break;
                }
                boolean created = TriviaServer.registerUser(requestedUsername);
                if (!created) {
                    send("ERROR UsernameTaken");
                    break;
                }
                username = requestedUsername;
                TriviaServer.addConnectedUser(username);
                send("REGISTER_OK AccountCreated");
                send("ROOMS " + TriviaServer.roomsList());
                break;
            }
            case "CREATE_ROOM": {
                if (username == null) {
                    send("ERROR NotLoggedIn");
                    break;
                }
                if (parts.length < 2) {
                    send("ERROR InvalidCommand");
                    break;
                }
                String roomName = parts[1];
                if (parts.length >= 8) {
                    try {
                        int rounds = Integer.parseInt(parts[2]);
                        int timeSec = Integer.parseInt(parts[3]);
                        int maxPlayers = Integer.parseInt(parts[4]);
                        String category = parts[5];
                        String difficulty = parts[6];
                        boolean isPrivate = Integer.parseInt(parts[7]) == 1;
                        currentRoom = TriviaServer.createRoom(roomName, maxPlayers, rounds, timeSec * 1000, category,
                                difficulty, isPrivate);
                    } catch (Exception e) {
                        currentRoom = TriviaServer.createRoom(roomName);
                    }
                } else {
                    currentRoom = TriviaServer.createRoom(roomName);
                }
                currentRoom.addPlayer(this);
                TriviaServer.recordGameHosted(username);
                send("ROOM_CREATED " + roomName);
                break;
            }
            case "JOIN_ROOM": {
                if (username == null) {
                    send("ERROR NotLoggedIn");
                    break;
                }
                if (parts.length < 2) {
                    send("ERROR InvalidCommand");
                    break;
                }
                String roomName = parts[1];
                currentRoom = TriviaServer.getRoom(roomName);
                if (currentRoom != null) {
                    currentRoom.addPlayer(this);
                    send("ROOM_JOINED " + roomName);
                } else {
                    send("ERROR RoomNotFound");
                }
                break;
            }
            case "START_GAME": {
                if (username == null) {
                    send("ERROR NotLoggedIn");
                    break;
                }
                if (currentRoom != null) {
                    new Thread(() -> currentRoom.startGame()).start();
                }
                break;
            }
            case "ANSWER": {
                if (username == null) {
                    send("ERROR NotLoggedIn");
                    break;
                }
                if (currentRoom != null) {
                    if (parts.length < 2) {
                        send("ERROR InvalidCommand");
                        break;
                    }
                    try {
                        int selectedIndex = Integer.parseInt(parts[1]);
                        currentRoom.submitAnswer(username, selectedIndex);
                    } catch (NumberFormatException nfe) {
                        send("ERROR InvalidAnswer");
                    }
                }
                break;
            }
            case "LIST_ROOMS": {
                send("ROOMS " + TriviaServer.roomsList());
                break;
            }
            case "GET_STATS": {
                if (username == null) {
                    send("ERROR NotLoggedIn");
                    break;
                }
                UserStats stats = TriviaServer.getUserStats(username);
                send("STATS " + stats.getGamesPlayed() + " " + stats.getWins() + " " + stats.getTotalScore() + " " +
                        stats.getCorrectAnswers() + " " + stats.getTotalQuestionsAnswered() + " "
                        + stats.getGamesHosted());
                break;
            }
            case "GET_GLOBAL_LEADERBOARD": {
                String globalLeaderboard = TriviaServer.getGlobalLeaderboard();
                if (globalLeaderboard.isEmpty()) {
                    send("GLOBAL_LEADERBOARD ");
                } else {
                    send("GLOBAL_LEADERBOARD " + globalLeaderboard);
                }
                break;
            }
            case "CHAT": {
                if (username == null) {
                    send("ERROR NotLoggedIn");
                    break;
                }
                if (currentRoom == null) {
                    send("ERROR NotInRoom");
                    break;
                }
                if (parts.length < 2) {
                    send("ERROR InvalidCommand");
                    break;
                }
                String chatMessage = message.substring(5); // Remove "CHAT "
                currentRoom.broadcastChat(username, chatMessage);
                break;
            }
            default: {
                send("ERROR UnknownCommand");
                break;
            }
        }
    }
}
