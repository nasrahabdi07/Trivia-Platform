package admin;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminMonitor extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(AdminMonitor.class.getResource("/AdminMonitor.fxml"));
        Parent root = loader.load();

        scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Trivia Server Monitor");
        stage.show();

        AdminMonitorController controller = loader.getController();
        stage.setOnCloseRequest(e -> controller.stop());
    }

    public static void main(String[] args) {
        launch();
    }
}
