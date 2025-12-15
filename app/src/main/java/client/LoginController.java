package client;

import client.network.ClientConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private Label statusLabel;

    private ClientConnection conn;

    public void initialize() {
        conn = ClientConnection.getInstance();
    }

    public void handleLogin() {
        String username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            statusLabel.setText("Username is required!");
            return;
        }

        // Disable the login button and show connecting status
        statusLabel.setText("Connecting to server...");
        statusLabel.setStyle("-fx-text-fill: blue; -fx-font-size: 12px;");
        
        try {
            // Connect to server if not already connected
            boolean connected = conn.isConnected();
            if (!connected) {
                statusLabel.setText("Connecting to server...");
                System.out.println("Attempting to connect to server...");
                connected = conn.connect();
                
                if (!connected) {
                    statusLabel.setText("Error: Could not connect to server. Make sure server is running on port 5000!");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                    System.err.println("Connection failed!");
                    return;
                }
            }
            
            conn.setMessageListener(msg -> {
                Platform.runLater(() -> {
                    if (msg.startsWith("LOGIN_OK") && msg.contains("WelcomeBack")) {
                        SceneSwitcher.setCurrentUsername(username);
                        statusLabel.setText("Welcome back!");
                        statusLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
                        new Thread(() -> {
                            try {
                                Thread.sleep(500);
                                Platform.runLater(() -> SceneSwitcher.switchTo("Home.fxml"));
                            } catch (InterruptedException ignored) {}
                        }).start();
                    } else if (msg.startsWith("ERROR")) {
                        if (msg.contains("NotRegistered")) {
                            statusLabel.setText("Account not found. Please sign up.");
                        } else if (msg.contains("UsernameAlreadyInUse")) {
                            statusLabel.setText("Username already signed in.");
                        } else {
                            statusLabel.setText("Error: " + msg.replace("ERROR ", ""));
                        }
                        statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                    }
                });
            });
            
            // Give the listener thread a moment to be ready
            Thread.sleep(150);
            
            // Send login command
            System.out.println("=== Sending LOGIN command for: " + username + " ===");
            conn.send("LOGIN " + username);
            
            // Set a timeout to check if we got a response
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> {
                        if (statusLabel.getText().equals("Connecting to server...")) {
                            statusLabel.setText("Error: No response from server. Is the server running?");
                            statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                        }
                    });
                } catch (InterruptedException e) {
                    // Ignore
                }
            }).start();
            
        } catch (Exception e) {
            statusLabel.setText("Error: Connection failed - " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
            e.printStackTrace();
        }
    }

    public void goToRegister() {
        SceneSwitcher.switchTo("Register.fxml");
    }
}

