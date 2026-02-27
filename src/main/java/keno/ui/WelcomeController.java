package keno.ui;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import keno.SceneRouter;
import keno.util.ThemeManager;
import keno.logic.PayoutTable;


public class WelcomeController {
    private final BorderPane root = new BorderPane();


    public WelcomeController(SceneRouter router) {
        // Menu
        MenuBar mb = new MenuBar();
        Menu menu = new Menu("Menu");
        MenuItem rules = new MenuItem("Rules of the Game");
        MenuItem odds = new MenuItem("Odds of Winning");
        MenuItem exit = new MenuItem("Exit");
        menu.getItems().addAll(rules, odds, new SeparatorMenuItem(), exit);
        mb.getMenus().add(menu);
        root.setTop(mb);


        // Center
        VBox center = new VBox(18);
        center.setPadding(new Insets(40));
        center.setAlignment(Pos.CENTER);
        Label title = new Label("K E N O");
        title.setStyle("-fx-font-size: 48px; -fx-font-weight: bold;");
        Button start = new Button("Start Playing");
        start.setPrefWidth(220);
        start.setOnAction(e -> router.showGame());
        center.getChildren().addAll(title, start,
                new Label("Tip: Use Menu to read rules or odds any time."));
        root.setCenter(center);


        // Actions
        rules.setOnAction(e -> Dialogs.showRules());
        odds.setOnAction(e -> Dialogs.showOdds());
        exit.setOnAction(e -> router.getStage().close());


        ThemeManager.applyDefault(root);
    }


    public BorderPane getRoot() { return root; }


    static class Dialogs {
        static void showRules() {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Rules");
            a.setHeaderText("How the game is played");
            a.setContentText("Choose 1, 4, 8, or 10 spots (numbers 1–80).\n" +
                    "20 unique numbers are drawn each drawing.\n" +
                    "You win based on how many of your picks match the draw.\n" +
                    "Play 1–4 drawings per bet card.");
            a.showAndWait();
        }
        static void showOdds() {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Odds / Payouts");
            a.setHeaderText("Spot 1, 4, 8, 10 payout table (NC Keno)");
            a.setContentText(PayoutTable.asTextTable());
            a.getDialogPane().setPrefWidth(520);
            a.showAndWait();
        }
    }
}