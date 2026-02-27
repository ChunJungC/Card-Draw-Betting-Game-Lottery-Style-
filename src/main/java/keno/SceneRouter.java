package keno;


import javafx.scene.Scene;
import javafx.stage.Stage;
import keno.ui.WelcomeController;
import keno.ui.GameController;


public class SceneRouter {
    private final Stage stage;
    public SceneRouter(Stage stage) { this.stage = stage; }


    public void showWelcome() {
        WelcomeController wc = new WelcomeController(this);
        stage.setScene(new Scene(wc.getRoot(), 900, 600));
    }


    public void showGame() {
        GameController gc = new GameController(this);
        stage.setScene(new Scene(gc.getRoot(), 1100, 720));
    }


    public Stage getStage() { return stage; }
}