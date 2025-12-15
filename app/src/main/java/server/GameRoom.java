package server;

import shared.Question;

import java.util.*;

public class GameRoom {

    private String roomName;
    private List<ClientHandler> players = new ArrayList<>();
    private Map<String, Integer> scores = new HashMap<>();
    private Map<String, Integer> correctAnswersMap = new HashMap<>();
    private Map<String, Integer> questionsAnsweredMap = new HashMap<>();
    private Map<String, List<Long>> responseTimes = new HashMap<>();
    private Question currentQuestion;
    private long roundStart;
    private long roundDuration = 15000;
    private Set<String> answeredThisRound = new HashSet<>();
    private int maxPlayers = 8;
    private int totalRounds = 5;
    private String category = "General_Knowledge";
    private String difficulty = "Medium";
    private boolean isPrivate = false;

    public GameRoom(String roomName) {
        this.roomName = roomName;
    }

    public GameRoom(String roomName, int maxPlayers, int totalRounds, long roundDuration, String category,
            String difficulty, boolean isPrivate) {
        this.roomName = roomName;
        this.maxPlayers = maxPlayers;
        this.totalRounds = totalRounds;
        this.roundDuration = roundDuration;
        this.category = category;
        this.difficulty = difficulty;
        this.isPrivate = isPrivate;
    }

    public synchronized void addPlayer(ClientHandler handler) {
        String name = handler.getUsername();
        if (name == null) {
            return;
        }
        if (players.size() >= maxPlayers) {
            handler.send("ERROR RoomFull");
            return;
        }
        players.add(handler);
        scores.put(name, 0);
        streaks.put(name, 0);
        correctAnswersMap.put(name, 0);
        questionsAnsweredMap.put(name, 0);
        responseTimes.put(name, new ArrayList<>());
        broadcast("PLAYER_JOINED " + name);
        broadcast("PLAYER_COUNT " + players.size() + "/" + maxPlayers);
    }

    public int getPlayerCount() {
        return players.size();
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getCategory() {
        return category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void broadcast(String message) {
        for (ClientHandler player : players) {
            player.send(message);
        }
    }

    public void startGame() {
        broadcast("GAME_START");

        // Countdown before first question
        try {
            for (int countdown = 3; countdown > 0; countdown--) {
                broadcast("COUNTDOWN " + countdown);
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Get questions for the selected category
        List<Question> pool = QuestionBank.getByCategory(category.replace("_", " "));
        Collections.shuffle(pool, new Random(System.currentTimeMillis()));

        int rounds = Math.min(totalRounds, pool.size());

        for (int i = 0; i < rounds; i++) {
            currentQuestion = pool.get(i);

            // Broadcast question to all players
            broadcast("QUESTION " +
                    currentQuestion.getText() + "|" +
                    currentQuestion.getOptions().get(0) + "|" +
                    currentQuestion.getOptions().get(1) + "|" +
                    currentQuestion.getOptions().get(2) + "|" +
                    currentQuestion.getOptions().get(3));

            answeredThisRound.clear();
            roundStart = System.currentTimeMillis();

            // Wait for the full round duration
            try {
                Thread.sleep(roundDuration);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            broadcast("ROUND_END");

            // Small delay between rounds (2 seconds)
            if (i < rounds - 1) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        sendLeaderboard();
    }

    private Map<String, Integer> streaks = new HashMap<>();

    public synchronized void submitAnswer(String username, int selectedIndex) {
        if (username == null || !scores.containsKey(username) || currentQuestion == null) {
            return;
        }
        if (answeredThisRound.contains(username)) {
            return;
        }

        questionsAnsweredMap.put(username, questionsAnsweredMap.getOrDefault(username, 0) + 1);

        // Track response time
        long responseTime = System.currentTimeMillis() - roundStart;
        responseTimes.get(username).add(responseTime);

        boolean correct = selectedIndex == currentQuestion.getCorrectIndex();
        int correctIndex = currentQuestion.getCorrectIndex();

        // Find the player's handler to send individual feedback
        ClientHandler playerHandler = null;
        for (ClientHandler handler : players) {
            if (username.equals(handler.getUsername())) {
                playerHandler = handler;
                break;
            }
        }

        questionsAnsweredMap.put(username, questionsAnsweredMap.getOrDefault(username, 0) + 1);

        if (correct) {
            correctAnswersMap.put(username, correctAnswersMap.getOrDefault(username, 0) + 1);

            // Update streak
            int currentStreak = streaks.getOrDefault(username, 0) + 1;
            streaks.put(username, currentStreak);

            // Base points
            int points = 10;

            // Streak bonus: +5 points for every 3 consecutive correct answers
            if (currentStreak >= 3) {
                points += 5 * (currentStreak / 3);
            }

            // Speed bonus: up to 5 extra points for fast answers
            long elapsed = System.currentTimeMillis() - roundStart;
            long remaining = Math.max(0, roundDuration - elapsed);
            int speedBonus = (int) Math.floor((remaining / (double) roundDuration) * 5);
            points += speedBonus;

            int newScore = scores.get(username) + points;
            scores.put(username, newScore);

            // Send feedback to the player with streak info
            if (playerHandler != null) {
                playerHandler.send("ANSWER_RESULT CORRECT " + correctIndex + " " + points + " " + currentStreak);
            }

            // Broadcast updated score to all players
            broadcast("SCORE " + username + " " + newScore);
        } else {
            // Reset streak
            streaks.put(username, 0);

            // Send wrong answer feedback
            if (playerHandler != null) {
                playerHandler.send("ANSWER_RESULT WRONG " + correctIndex + " 0 0");
            }
        }

        answeredThisRound.add(username);
    }

    private void sendLeaderboard() {
        // Determine winner (highest score)
        int maxScore = -1;
        for (int s : scores.values()) {
            if (s > maxScore)
                maxScore = s;
        }

        // Update global leaderboard with scores from this game
        for (Map.Entry<String, Integer> e : scores.entrySet()) {
            String user = e.getKey();
            int score = e.getValue();
            boolean isWin = (score == maxScore && score > 0);
            int correct = correctAnswersMap.getOrDefault(user, 0);
            int answered = questionsAnsweredMap.getOrDefault(user, 0);

            TriviaServer.recordGameStats(user, score, isWin, correct, answered);
        }

        // Send game-specific leaderboard
        StringBuilder result = new StringBuilder("LEADERBOARD ");
        boolean first = true;
        for (Map.Entry<String, Integer> e : scores.entrySet()) {
            if (!first) {
                result.append(',');
            }
            first = false;

            // Calculate average response time
            String username = e.getKey();
            List<Long> times = responseTimes.get(username);
            double avgTime = 0;
            if (times != null && !times.isEmpty()) {
                long sum = 0;
                for (Long time : times) {
                    sum += time;
                }
                avgTime = sum / (double) times.size() / 1000.0; // Convert to seconds
            }

            result.append(username).append(':').append(e.getValue()).append(':').append(String.format("%.1f", avgTime));
        }
        broadcast(result.toString());

        // Also send global leaderboard
        String globalLeaderboard = TriviaServer.getGlobalLeaderboard();
        if (!globalLeaderboard.isEmpty()) {
            broadcast("GLOBAL_LEADERBOARD " + globalLeaderboard);
        }
    }

    public void broadcastChat(String username, String message) {
        broadcast("CHAT " + username + "|" + message);
    }
}
