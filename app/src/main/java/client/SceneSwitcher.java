package client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneSwitcher {

    private static Stage primaryStage;
    private static String currentRoomName;
    private static String lastLeaderboard;
    private static String currentUsername;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void setCurrentUsername(String username) {
        currentUsername = username;
    }

    public static String getCurrentUsername() {
        return currentUsername != null ? currentUsername : "Guest";
    }

    private static double savedWidth = 1200;
    private static double savedHeight = 800;
    private static boolean savedMaximized = false;

    public static void switchTo(String fxml) {
        try {
            // Save current state
            if (primaryStage.getScene() != null) {
                savedWidth = primaryStage.getWidth();
                savedHeight = primaryStage.getHeight();
                savedMaximized = primaryStage.isMaximized();
            }

            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/client/" + fxml));
            Scene newScene = new Scene(loader.load());
            newScene.getStylesheets().add(SceneSwitcher.class.getResource("/client/styles.css").toExternalForm());

            // Set scene
            primaryStage.setScene(newScene);

            // Restore state if needed
            if (savedMaximized) {
                primaryStage.setMaximized(true);
            } else {
                primaryStage.setWidth(savedWidth);
                primaryStage.setHeight(savedHeight);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setCurrentRoomName(String name) {
        currentRoomName = name;
    }

    public static String getCurrentRoomName() {
        return currentRoomName;
    }

    public static void setLastLeaderboard(String payload) {
        lastLeaderboard = payload;
    }

    public static String getLastLeaderboard() {
        return lastLeaderboard;
    }
}
