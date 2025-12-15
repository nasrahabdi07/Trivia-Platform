package client;

import client.network.ClientConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class GameRoomController {

    @FXML
    private Label roomLabel;

    private ClientConnection conn;

    public void initialize() {
        conn = ClientConnection.getInstance();

        String name = SceneSwitcher.getCurrentRoomName();
        if (name != null) {
            roomLabel.setText("Room: " + name);
        }

        conn.setMessageListener(msg -> {
            if (msg.equals("GAME_START")) {
                Platform.runLater(() -> SceneSwitcher.switchTo("Quiz.fxml"));
            }
        });
    }

    public void startGame() {
        conn.send("START_GAME");
    }
}

