package client;

import client.network.ClientConnection;
import javafx.fxml.FXML;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class CreateRoomController {
    @FXML
    private TextField roomNameField;
    @FXML
    private ComboBox<String> categoryBox;
    @FXML
    private ComboBox<String> difficultyBox;
    @FXML
    private Slider maxPlayersSlider;
    @FXML
    private Slider questionsSlider;
    @FXML
    private Slider timeSlider;
    @FXML
    private CheckBox privateCheck;
    @FXML
    private Label statusLabel;
    @FXML
    private Label maxPlayersLabel;
    @FXML
    private Label questionsLabel;
    @FXML
    private Label timeLabel;

    private ClientConnection conn;

    public void initialize() {
        conn = ClientConnection.getInstance();

        // Set up message listener for room creation response
        conn.setMessageListener(msg -> {
            if (msg.startsWith("ROOM_CREATED")) {
                String room = msg.replace("ROOM_CREATED ", "");
                javafx.application.Platform.runLater(() -> {
                    SceneSwitcher.setCurrentRoomName(room);
                    SceneSwitcher.switchTo("GameRoom.fxml");
                });
            } else if (msg.startsWith("ERROR")) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Error: " + msg.replace("ERROR ", ""));
                    statusLabel.setStyle("-fx-text-fill: #ef4444;");
                });
            }
        });

        categoryBox.getItems().setAll("General Knowledge", "Science", "History", "Sports");
        categoryBox.getSelectionModel().selectFirst();
        difficultyBox.getItems().setAll("Easy", "Medium", "Hard");
        difficultyBox.getSelectionModel().select("Medium");

        maxPlayersSlider.setMin(2);
        maxPlayersSlider.setMax(16);
        maxPlayersSlider.setValue(8);
        maxPlayersSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            maxPlayersLabel.setText(String.valueOf(newVal.intValue()));
        });

        questionsSlider.setMin(5);
        questionsSlider.setMax(20);
        questionsSlider.setValue(10);
        questionsSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            questionsLabel.setText(String.valueOf(newVal.intValue()));
        });

        timeSlider.setMin(10);
        timeSlider.setMax(45);
        timeSlider.setValue(15);
        timeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            timeLabel.setText(newVal.intValue() + "s");
        });
    }

    public void createRoom() {
        String name = roomNameField.getText().trim();
        if (name.isEmpty()) {
            statusLabel.setText("Enter room name");
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        int maxPlayers = (int) Math.round(maxPlayersSlider.getValue());
        int totalQuestions = (int) Math.round(questionsSlider.getValue());
        int timePerQuestionSec = (int) Math.round(timeSlider.getValue());
        String category = categoryBox.getSelectionModel().getSelectedItem();
        String difficulty = difficultyBox.getSelectionModel().getSelectedItem();
        boolean isPrivate = privateCheck.isSelected();

        if (!conn.isConnected()) {
            statusLabel.setText("Connecting to server...");
            statusLabel.setStyle("-fx-text-fill: #60a5fa;");
            if (!conn.connect()) {
                statusLabel.setText("Server not available. Please start the server first.");
                statusLabel.setStyle("-fx-text-fill: #ef4444;");
                return;
            }
            // Give connection time to establish
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // Ignore
            }
        }

        statusLabel.setText("Creating room...");
        statusLabel.setStyle("-fx-text-fill: #a78bfa;");

        conn.send("CREATE_ROOM " + name + " " + totalQuestions + " " + timePerQuestionSec + " " + maxPlayers + " "
                + category.replace(" ", "_") + " " + difficulty + " " + (isPrivate ? 1 : 0));
    }

    public void backToLobby() {
        SceneSwitcher.switchTo("Lobby.fxml");
    }

    public void goHome() {
        SceneSwitcher.switchTo("Home.fxml");
    }

    public void goProfile() {
        SceneSwitcher.switchTo("Profile.fxml");
    }

    public void logout() {
        SceneSwitcher.setCurrentUsername(null);
        SceneSwitcher.switchTo("Login.fxml");
    }
}