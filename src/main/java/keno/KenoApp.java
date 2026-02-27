package keno;


import javafx.application.Application;
import javafx.stage.Stage;
import keno.ui.WelcomeController;


public class KenoApp extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("Keno");
        new SceneRouter(stage).showWelcome();
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}