package client;

import client.network.ClientConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.geometry.Insets;

public class LobbyController {

    @FXML
    private TextField searchField;

    @FXML
    private TextField roomCodeField;

    @FXML
    private TextField searchRoomsField;

    @FXML
    private VBox roomsContainer;

    private ClientConnection conn;

    public void initialize() {
        conn = ClientConnection.getInstance();

        conn.setMessageListener(msg -> {
            if (msg.startsWith("ROOM_CREATED")) {
                String room = msg.replace("ROOM_CREATED ", "");
                Platform.runLater(() -> {
                    SceneSwitcher.setCurrentRoomName(room);
                    SceneSwitcher.switchTo("GameRoom.fxml");
                });
            } else if (msg.startsWith("ROOM_JOINED")) {
                String room = msg.replace("ROOM_JOINED ", "");
                Platform.runLater(() -> {
                    SceneSwitcher.setCurrentRoomName(room);
                    SceneSwitcher.switchTo("GameRoom.fxml");
                });
            } else if (msg.startsWith("ROOMS")) {
                String data = msg.replace("ROOMS ", "");
                String[] names = data.isEmpty() ? new String[] {} : data.split(",");
                Platform.runLater(() -> {
                    displayRooms(names);
                });
            } else if (msg.startsWith("ERROR")) {
                String error = msg.replace("ERROR ", "");
                Platform.runLater(() -> {
                    showError(error);
                });
            }
        });
        conn.send("LIST_ROOMS");
    }

    private void displayRooms(String[] rooms) {
        roomsContainer.getChildren().clear();

        if (rooms.length == 0) {
            // Show empty state
            VBox emptyState = new VBox(20);
            emptyState.setAlignment(javafx.geometry.Pos.CENTER);

            Label icon = new Label("👥");
            icon.setStyle("-fx-font-size: 64px;");

            Label title = new Label("No rooms available");
            title.getStyleClass().add("title");
            title.setStyle("-fx-font-size: 28px;");

            Label subtitle = new Label("Be the first to create a game!");
            subtitle.getStyleClass().add("subtitle");
            subtitle.setStyle("-fx-font-size: 16px;");

            Button createBtn = new Button("+ Create Room");
            createBtn.getStyleClass().add("cta-button");
            createBtn.setOnAction(e -> createRoom());

            emptyState.getChildren().addAll(icon, title, subtitle, createBtn);
            roomsContainer.getChildren().add(emptyState);
        } else {
            // Show rooms list
            for (String room : rooms) {
                VBox roomCard = createRoomCard(room);
                roomsContainer.getChildren().add(roomCard);
            }
        }
    }

    private VBox createRoomCard(String roomName) {
        VBox card = new VBox(10);
        card.getStyleClass().add("player-card");
        card.setPadding(new Insets(16));
        card.setMaxWidth(850);

        Label nameLabel = new Label(roomName);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Button joinBtn = new Button("Join Room");
        joinBtn.getStyleClass().add("cta-button");
        joinBtn.setOnAction(e -> joinRoom(roomName));

        card.getChildren().addAll(nameLabel, joinBtn);
        return card;
    }

    public void createRoom() {
        SceneSwitcher.switchTo("CreateRoom.fxml");
    }

    public void joinRoom(String roomName) {
        conn.send("JOIN_ROOM " + roomName);
    }

    public void joinPrivateRoom() {
        String code = roomCodeField.getText().trim();
        if (!code.isEmpty()) {
            conn.send("JOIN_ROOM " + code);
        }
    }

    public void goBack() {
        SceneSwitcher.switchTo("Home.fxml");
    }

    public void goHome() {
        SceneSwitcher.switchTo("Home.fxml");
    }

    public void goProfile() {
        SceneSwitcher.switchTo("Profile.fxml");
    }

    public void logout() {
        SceneSwitcher.switchTo("Login.fxml");
    }

    public void filterAll() {
        conn.send("LIST_ROOMS");
    }

    public void filterGeneral() {
        // Filter by General category
        conn.send("LIST_ROOMS");
    }

    public void filterScience() {
        // Filter by Science category
        conn.send("LIST_ROOMS");
    }

    public void filterHistory() {
        // Filter by History category
        conn.send("LIST_ROOMS");
    }

    public void filterSports() {
        // Filter by Sports category
        conn.send("LIST_ROOMS");
    }

    public void playSolo() {
        // Create a single player room automatically
        String roomName = "Solo_" + SceneSwitcher.getCurrentUsername() + "_" + System.currentTimeMillis();
        conn.send("CREATE_ROOM " + roomName + " 10 15 1 General_Knowledge Medium 0");
    }

    private void showError(String error) {
        // Display error in the rooms container
        roomsContainer.getChildren().clear();

        VBox errorState = new VBox(20);
        errorState.setAlignment(javafx.geometry.Pos.CENTER);

        Label icon = new Label("⚠️");
        icon.setStyle("-fx-font-size: 64px;");

        Label title = new Label("Error");
        title.getStyleClass().add("title");
        title.setStyle("-fx-font-size: 28px; -fx-text-fill: #ef4444;");

        Label message = new Label(getErrorMessage(error));
        message.getStyleClass().add("subtitle");
        message.setStyle("-fx-font-size: 16px;");
        message.setWrapText(true);
        message.setMaxWidth(600);

        Button retryBtn = new Button("Back to Lobby");
        retryBtn.getStyleClass().add("cta-button");
        retryBtn.setOnAction(e -> {
            conn.send("LIST_ROOMS");
        });

        errorState.getChildren().addAll(icon, title, message, retryBtn);
        roomsContainer.getChildren().add(errorState);
    }

    private String getErrorMessage(String error) {
        switch (error) {
            case "RoomNotFound":
                return "Room not found. Please check the room code and try again.";
            case "RoomFull":
                return "This room is full. Please try another room.";
            case "NotLoggedIn":
                return "You must be logged in to join a room.";
            default:
                return "An error occurred: " + error;
        }
    }
}
