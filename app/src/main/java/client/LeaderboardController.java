package client;

import client.network.ClientConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardController {

    @FXML
    private VBox leaderboardContainer;

    private ClientConnection conn;

    private String globalLeaderboardData = "";

    public void initialize() {
        conn = ClientConnection.getInstance();
        String payload = SceneSwitcher.getLastLeaderboard();
        if (payload != null) {
            populate(payload);
        }

        conn.setMessageListener(msg -> {
            if (msg.startsWith("LEADERBOARD ")) {
                String pl = msg.replace("LEADERBOARD ", "");
                Platform.runLater(() -> populate(pl));
            } else if (msg.startsWith("GLOBAL_LEADERBOARD")) {
                String gl = msg.replace("GLOBAL_LEADERBOARD ", "");
                Platform.runLater(() -> {
                    globalLeaderboardData = gl;
                    System.out.println("Global leaderboard received: " + gl);
                });
            }
        });

        // Request global leaderboard after a short delay
        new Thread(() -> {
            try {
                Thread.sleep(500);
                conn.send("GET_GLOBAL_LEADERBOARD");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void populate(String payload) {
        leaderboardContainer.getChildren().clear();
        String[] entries = payload.isEmpty() ? new String[] {} : payload.split(",");

        List<PlayerScore> scores = new ArrayList<>();
        for (String e : entries) {
            String[] parts = e.split(":");
            if (parts.length >= 2) {
                try {
                    int sc = Integer.parseInt(parts[1]);
                    double avgTime = parts.length >= 3 ? Double.parseDouble(parts[2]) : 0.0;
                    scores.add(new PlayerScore(parts[0], sc, avgTime));
                } catch (Exception ignored) {
                }
            }
        }

        // Sort by score descending
        scores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

        // Create entries
        for (int i = 0; i < scores.size(); i++) {
            PlayerScore ps = scores.get(i);
            HBox entry = createLeaderboardEntry(i + 1, ps.getName(), ps.getScore(), ps.getAvgTime());
            leaderboardContainer.getChildren().add(entry);
        }
    }

    private HBox createLeaderboardEntry(int rank, String playerName, int score, double avgTime) {
        HBox entry = new HBox(20);
        entry.setAlignment(Pos.CENTER_LEFT);
        entry.setPadding(new Insets(16));

        // Different styling for top 3
        if (rank == 1) {
            entry.getStyleClass().add("player-card-active");
            entry.setStyle("-fx-background-color: rgba(251, 191, 36, 0.2); -fx-border-color: rgba(251, 191, 36, 0.6);");
        } else if (rank == 2) {
            entry.getStyleClass().add("player-card");
            entry.setStyle(
                    "-fx-background-color: rgba(192, 192, 192, 0.15); -fx-border-color: rgba(192, 192, 192, 0.4);");
        } else if (rank == 3) {
            entry.getStyleClass().add("player-card");
            entry.setStyle(
                    "-fx-background-color: rgba(205, 127, 50, 0.15); -fx-border-color: rgba(205, 127, 50, 0.4);");
        } else {
            entry.getStyleClass().add("leaderboard-entry");
        }

        // Rank medal
        String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "#" + rank;
        Label rankLabel = new Label(medal);
        rankLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-min-width: 50px;");

        // Avatar
        VBox avatar = new VBox();
        avatar.setAlignment(Pos.CENTER);
        avatar.getStyleClass().add("avatar");
        avatar.setStyle("-fx-pref-width: 50px; -fx-pref-height: 50px;");
        Label avatarText = new Label(playerName.substring(0, 1).toUpperCase());
        avatarText.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");
        avatar.getChildren().add(avatarText);

        // Player name and stats
        VBox nameBox = new VBox(5);
        Label nameLabel = new Label(playerName);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label timeLabel = new Label(String.format("⏱ %.1fs avg", avgTime));
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #b8b4d0;");
        nameBox.getChildren().addAll(nameLabel, timeLabel);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Score
        VBox scoreBox = new VBox(3);
        scoreBox.setAlignment(Pos.CENTER_RIGHT);
        Label scoreValue = new Label(String.valueOf(score));
        scoreValue.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label scoreLabel = new Label("points");
        scoreLabel.getStyleClass().add("subtitle");
        scoreLabel.setStyle("-fx-font-size: 12px;");
        scoreBox.getChildren().addAll(scoreValue, scoreLabel);

        entry.getChildren().addAll(rankLabel, avatar, nameBox, spacer, scoreBox);
        return entry;
    }

    public void backToLobby() {
        SceneSwitcher.switchTo("Lobby.fxml");
    }

    public void playAgain() {
        SceneSwitcher.switchTo("Lobby.fxml");
    }

    public void goHome() {
        SceneSwitcher.switchTo("Home.fxml");
    }

    public void goPlay() {
        SceneSwitcher.switchTo("Lobby.fxml");
    }

    public void goProfile() {
        SceneSwitcher.switchTo("Profile.fxml");
    }

    public void logout() {
        SceneSwitcher.setCurrentUsername(null);
        SceneSwitcher.switchTo("Login.fxml");
    }

    public void showGlobalLeaderboard() {
        System.out.println("=== GLOBAL RANKINGS BUTTON CLICKED ===");
        System.out
                .println("Global data available: " + (globalLeaderboardData != null ? globalLeaderboardData : "NULL"));

        // Always request fresh data
        conn.send("GET_GLOBAL_LEADERBOARD");

        // Show loading message
        leaderboardContainer.getChildren().clear();
        VBox loadingBox = new VBox(15);
        loadingBox.setAlignment(javafx.geometry.Pos.CENTER);

        Label loading = new Label("Loading Global Rankings...");
        loading.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        Label subtitle = new Label("All-time cumulative scores");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #b8b4d0;");

        loadingBox.getChildren().addAll(loading, subtitle);
        leaderboardContainer.getChildren().add(loadingBox);

        // Wait for response and display
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                Platform.runLater(() -> {
                    System.out.println("After wait, global data: " + globalLeaderboardData);
                    if (globalLeaderboardData != null && !globalLeaderboardData.isEmpty()) {
                        populate(globalLeaderboardData);
                    } else {
                        leaderboardContainer.getChildren().clear();
                        VBox errorBox = new VBox(15);
                        errorBox.setAlignment(javafx.geometry.Pos.CENTER);

                        Label error = new Label("No Global Rankings Yet");
                        error.setStyle("-fx-font-size: 24px; -fx-text-fill: #ef4444; -fx-font-weight: bold;");

                        Label hint = new Label("Play some games to build the global leaderboard!");
                        hint.setStyle("-fx-font-size: 16px; -fx-text-fill: #b8b4d0;");

                        errorBox.getChildren().addAll(error, hint);
                        leaderboardContainer.getChildren().add(errorBox);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
