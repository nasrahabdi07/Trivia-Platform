package client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ProfileController {

    @FXML
    private Label avatarLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label winsLabel;

    @FXML
    private Label gamesPlayedLabel;

    @FXML
    private Label accuracyLabel;

    @FXML
    private Label avgScoreLabel;

    @FXML
    private Label totalPointsLabel;

    @FXML
    private Label correctAnswersLabel;

    @FXML
    private Label gamesHostedLabel;

    public void initialize() {
        String username = SceneSwitcher.getCurrentUsername();

        // Set username
        usernameLabel.setText(username);

        // Set avatar initial (first letter of username)
        if (username != null && !username.isEmpty()) {
            avatarLabel.setText(username.substring(0, 1).toUpperCase());
        }

        // Set email (you can make this dynamic later)
        emailLabel.setText(username.toLowerCase() + "@example.com");

        client.network.ClientConnection conn = client.network.ClientConnection.getInstance();
        conn.setMessageListener(msg -> {
            if (msg.startsWith("STATS")) {
                String[] parts = msg.split(" ");
                if (parts.length >= 7) {
                    try {
                        int gamesPlayed = Integer.parseInt(parts[1]);
                        int wins = Integer.parseInt(parts[2]);
                        int totalScore = Integer.parseInt(parts[3]);
                        int correctAnswers = Integer.parseInt(parts[4]);
                        int totalQuestions = Integer.parseInt(parts[5]);
                        int gamesHosted = Integer.parseInt(parts[6]);

                        double accuracy = totalQuestions > 0 ? (double) correctAnswers / totalQuestions * 100 : 0;
                        int avgScore = gamesPlayed > 0 ? totalScore / gamesPlayed : 0;

                        javafx.application.Platform.runLater(() -> {
                            winsLabel.setText(String.valueOf(wins));
                            gamesPlayedLabel.setText(String.valueOf(gamesPlayed));
                            accuracyLabel.setText(String.format("%.1f%%", accuracy));
                            avgScoreLabel.setText(String.valueOf(avgScore));
                            totalPointsLabel.setText(String.valueOf(totalScore));
                            correctAnswersLabel.setText(String.valueOf(correctAnswers));
                            gamesHostedLabel.setText(String.valueOf(gamesHosted));
                        });
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        conn.send("GET_STATS");
    }

    public void goBack() {
        SceneSwitcher.switchTo("Home.fxml");
    }

    public void goHome() {
        SceneSwitcher.switchTo("Home.fxml");
    }

    public void goPlay() {
        SceneSwitcher.switchTo("Lobby.fxml");
    }

    public void logout() {
        SceneSwitcher.setCurrentUsername(null);
        SceneSwitcher.switchTo("Login.fxml");
    }
}
