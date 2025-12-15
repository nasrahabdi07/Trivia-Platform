package client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomeController {
    @FXML
    private Label tagline;

    public void joinGame() {
        SceneSwitcher.switchTo("Lobby.fxml");
    }

    public void goPlay() {
        SceneSwitcher.switchTo("Lobby.fxml");
    }

    public void goProfile() {
        SceneSwitcher.switchTo("Profile.fxml");
    }

    public void logout() {
        SceneSwitcher.switchTo("Login.fxml");
    }
}