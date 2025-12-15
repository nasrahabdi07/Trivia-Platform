package client;

import client.network.ClientConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private Label statusLabel;

    private ClientConnection conn;

    public void initialize() {
        conn = ClientConnection.getInstance();
    }

    public void handleRegister() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            statusLabel.setText("Username is required!");
            return;
        }
        statusLabel.setText("Connecting...");
        statusLabel.setStyle("-fx-text-fill: blue; -fx-font-size: 12px;");
        try {
            boolean connected = conn.isConnected();
            if (!connected) {
                connected = conn.connect();
                if (!connected) {
                    statusLabel.setText("Could not connect. Is server running?");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                    return;
                }
            }
            conn.setMessageListener(msg -> {
                Platform.runLater(() -> {
                    if (msg.startsWith("REGISTER_OK")) {
                        SceneSwitcher.setCurrentUsername(username);
                        statusLabel.setText("Account created!");
                        statusLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
                        new Thread(() -> {
                            try {
                                Thread.sleep(500);
                                Platform.runLater(() -> SceneSwitcher.switchTo("Home.fxml"));
                            } catch (InterruptedException ignored) {}
                        }).start();
                    } else if (msg.startsWith("ERROR")) {
                        if (msg.contains("UsernameTaken")) {
                            statusLabel.setText("Username already exists.");
                        } else if (msg.contains("UsernameAlreadyInUse")) {
                            statusLabel.setText("User already signed in.");
                        } else {
                            statusLabel.setText("Error: " + msg.replace("ERROR ", ""));
                        }
                        statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                    }
                });
            });
            Thread.sleep(150);
            conn.send("REGISTER " + username);
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        }
    }

    public void goToLogin() {
        SceneSwitcher.switchTo("Login.fxml");
    }
}