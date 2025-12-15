package client;

import client.network.ClientConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class QuizController {

    @FXML
    private Label questionLabel;

    @FXML
    private Button optionA, optionB, optionC, optionD;

    private ClientConnection conn;
    @FXML
    private Label timerLabel;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label streakLabel;
    @FXML
    private Label pointsLabel;
    @FXML
    private javafx.scene.control.ProgressBar progressBar;
    @FXML
    private javafx.scene.control.ListView<String> chatListView;
    @FXML
    private javafx.scene.control.TextField chatTextField;

    private Timeline timeline;
    private int remaining;
    private SoundManager soundManager;

    public void initialize() {
        conn = ClientConnection.getInstance();
        soundManager = SoundManager.getInstance();

        // Hide everything until first question arrives
        questionLabel.setText("Waiting for game to start...");
        optionA.setVisible(false);
        optionB.setVisible(false);
        optionC.setVisible(false);
        optionD.setVisible(false);
        progressBar.setVisible(false);
        pointsLabel.setVisible(false);

        conn.setMessageListener(msg -> {
            if (msg.startsWith("QUESTION")) {
                String[] data = msg.split("\\|");
                String text = data[0].replace("QUESTION ", "");
                String a = data[1];
                String b = data[2];
                String c = data[3];
                String d = data[4];

                Platform.runLater(() -> {
                    // Show all elements on first question
                    optionA.setVisible(true);
                    optionB.setVisible(true);
                    optionC.setVisible(true);
                    optionD.setVisible(true);
                    progressBar.setVisible(true);
                    pointsLabel.setVisible(true);

                    // Reset button styles
                    resetButtonStyles();

                    questionLabel.setText(text);
                    optionA.setText(a);
                    optionB.setText(b);
                    optionC.setText(c);
                    optionD.setText(d);
                    optionA.setDisable(false);
                    optionB.setDisable(false);
                    optionC.setDisable(false);
                    optionD.setDisable(false);
                    startTimer(15);
                });
            } else if (msg.startsWith("ANSWER_RESULT")) {
                String[] parts = msg.split(" ");
                String result = parts[1]; // CORRECT or WRONG
                int correctIndex = Integer.parseInt(parts[2]);
                int pointsEarned = parts.length > 3 ? Integer.parseInt(parts[3]) : 0;
                int streak = parts.length > 4 ? Integer.parseInt(parts[4]) : 0;

                Platform.runLater(() -> {
                    showAnswerFeedback(result, correctIndex, pointsEarned, streak);
                });
            } else if (msg.startsWith("LEADERBOARD")) {
                String payload = msg.replace("LEADERBOARD ", "");
                SceneSwitcher.setLastLeaderboard(payload);
                Platform.runLater(() -> SceneSwitcher.switchTo("Leaderboard.fxml"));
            } else if (msg.startsWith("SCORE")) {
                String[] s = msg.split(" ");
                if (s.length >= 3) {
                    String username = s[1];
                    String val = s[2];
                    // Only update score if it's for this player
                    if (username.equals(SceneSwitcher.getCurrentUsername())) {
                        Platform.runLater(() -> {
                            scoreLabel.setText("Score: " + val);
                        });
                    }
                }
            } else if (msg.startsWith("ROUND_END")) {
                Platform.runLater(() -> stopTimer());
            } else if (msg.startsWith("COUNTDOWN")) {
                String count = msg.replace("COUNTDOWN ", "");
                Platform.runLater(() -> {
                    questionLabel.setText("Game starting in " + count + "...");
                    questionLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #fbbf24;");
                });
            } else if (msg.startsWith("CHAT")) {
                String[] parts = msg.split("\\|", 2);
                if (parts.length >= 2) {
                    String username = parts[0].replace("CHAT ", "");
                    String message = parts[1];
                    Platform.runLater(() -> {
                        chatListView.getItems().add(username + ": " + message);
                        chatListView.scrollTo(chatListView.getItems().size() - 1);
                    });
                }
            }
        });
    }

    private void resetButtonStyles() {
        optionA.setStyle("");
        optionB.setStyle("");
        optionC.setStyle("");
        optionD.setStyle("");
    }

    private void showAnswerFeedback(String result, int correctIndex, int pointsEarned, int streak) {
        Button correctButton = getButtonByIndex(correctIndex);

        if (result.equals("CORRECT")) {
            // Play correct sound
            soundManager.playCorrect();

            // Highlight the correct answer in green
            if (correctButton != null) {
                correctButton.setStyle(
                        "-fx-background-color: rgba(34, 197, 94, 0.4); -fx-border-color: #4ade80; -fx-border-width: 2px;");
            }

            // Show points earned
            String message = "+" + pointsEarned + " points!";
            if (streak >= 3) {
                message += " 🔥 " + streak + " streak!";
            }
            pointsLabel.setText(message);
            pointsLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #4ade80;");

            // Update streak display

            streakLabel.setText("Streak: " + streak);

            // Animate points label
            animatePointsLabel();

        } else {
            // Play wrong sound
            soundManager.playWrong();

            // Show correct answer in green
            if (correctButton != null) {
                correctButton.setStyle(
                        "-fx-background-color: rgba(34, 197, 94, 0.4); -fx-border-color: #4ade80; -fx-border-width: 2px;");
            }

            // Show wrong feedback
            pointsLabel.setText("Wrong! 😞");
            pointsLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");

            // Reset streak

            streakLabel.setText("Streak: 0");
        }
    }

    private void animatePointsLabel() {
        // Simple fade animation
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.seconds(0.5),
                pointsLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private Button getButtonByIndex(int index) {
        switch (index) {
            case 0:
                return optionA;
            case 1:
                return optionB;
            case 2:
                return optionC;
            case 3:
                return optionD;
            default:
                return null;
        }
    }

    private void sendAnswer(String choiceIndex) {
        conn.send("ANSWER " + choiceIndex);

        // Disable all buttons after answering
        Platform.runLater(() -> {
            optionA.setDisable(true);
            optionB.setDisable(true);
            optionC.setDisable(true);
            optionD.setDisable(true);
        });
    }

    public void selectA() {
        sendAnswer("0");
    }

    public void selectB() {
        sendAnswer("1");
    }

    public void selectC() {
        sendAnswer("2");
    }

    public void selectD() {
        sendAnswer("3");
    }

    private void startTimer(int seconds) {
        stopTimer();
        remaining = seconds;
        timerLabel.setText(remaining + "s");
        progressBar.setProgress(1.0);
        pointsLabel.setText(""); // Clear points label

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remaining--;
            timerLabel.setText(Math.max(remaining, 0) + "s");

            // Update progress bar
            double progress = (double) remaining / seconds;
            progressBar.setProgress(progress);

            // Change timer color when time is running out
            if (remaining <= 5) {
                timerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
            } else {
                timerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
            }
        }));
        timeline.setCycleCount(seconds);
        timeline.playFromStart();
    }

    private void stopTimer() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    public void sendChat() {
        String message = chatTextField.getText().trim();
        if (!message.isEmpty()) {
            conn.send("CHAT " + message);
            chatTextField.clear();
        }
    }
}
