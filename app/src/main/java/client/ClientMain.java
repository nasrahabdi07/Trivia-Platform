package client;

import javafx.application.Application;
import javafx.stage.Stage;

public class ClientMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        SceneSwitcher.setStage(stage);
        
        // Set initial window size
        stage.setWidth(1200);
        stage.setHeight(800);
        
        // Make window resizable
        stage.setResizable(true);
        
        // Set minimum size
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        
        stage.setTitle("Trivia Arena");
        
        SceneSwitcher.switchTo("Login.fxml");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}