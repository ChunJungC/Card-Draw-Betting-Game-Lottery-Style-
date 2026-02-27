package keno.ui;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import keno.SceneRouter;
import keno.logic.*;
import keno.util.ThemeManager;
import keno.util.Validators;


import java.util.*;
import java.util.stream.Collectors;

public class GameController {
    private final BorderPane root = new BorderPane();


    // Left controls
    private final ToggleGroup spotsGroup = new ToggleGroup();
    private final RadioButton spot1 = mkSpot("1",1);
    private final RadioButton spot4 = mkSpot("4",4);
    private final RadioButton spot8 = mkSpot("8",8);
    private final RadioButton spot10 = mkSpot("10",10);
    private final ComboBox<Integer> drawingsBox = new ComboBox<>(FXCollections.observableArrayList(1,2,3,4));
    private final Button quickPickBtn = new Button("Quick Pick");
    private final Button startBtn = new Button("Start Drawings");
    private final Button continueBtn = new Button("Continue");
    private final Button resetBtn = new Button("Reset Bet");


    // Right grid (bet card)
    private final GridPane grid = new GridPane();
    private final Button[] numberButtons = new Button[81]; // 1..80


    // Status + log
    private final Label selectedLabel = new Label("Selected: 0 / 0");
    private final Label drawLabel = new Label("Draw: 0 / 0");
    private final Label lastWinLabel = new Label("Last Win: $0");
    private final Label totalWinLabel = new Label("Total: $0");
    private final ListView<String> log = new ListView<>();


    // Game state
    private final BetCard betCard = new BetCard();
    private final DrawingEngine engine = new DrawingEngine();
    private final KenoGame game = new KenoGame();
    private final StatsTracker stats = new StatsTracker();


    private Timeline revealTimeline;

    public GameController(SceneRouter router) {
        // Top menu
        MenuBar mb = new MenuBar();
        Menu menu = new Menu("Menu");
        MenuItem rules = new MenuItem("Rules of the Game");
        MenuItem odds = new MenuItem("Odds of Winning");
        MenuItem newLook = new MenuItem("New Look");
        MenuItem exit = new MenuItem("Exit");
        menu.getItems().addAll(rules, odds, newLook, new SeparatorMenuItem(), exit);
        mb.getMenus().add(menu);
        root.setTop(mb);


        // Left controls pane
        VBox left = new VBox(10); left.setPadding(new Insets(10));
        Label spotsLabel = new Label("Spots");
        HBox spotRow = new HBox(8, spot1, spot4, spot8, spot10);
        drawingsBox.getSelectionModel().selectFirst();
        HBox drawingsRow = new HBox(8, new Label("Drawings"), drawingsBox);
        Button enableGridBtn = new Button("Enable Grid");
        enableGridBtn.setOnAction(e -> enableGridIfReady());
        quickPickBtn.setOnAction(e -> quickPick());
        startBtn.setOnAction(e -> startDrawings());
        continueBtn.setOnAction(e -> continueDrawing());
        resetBtn.setOnAction(e -> resetBet());
        startBtn.setDisable(true);
        continueBtn.setDisable(true);
        left.getChildren().addAll(spotsLabel, spotRow, drawingsRow, enableGridBtn, quickPickBtn, startBtn, continueBtn, resetBtn,
                new Separator(), selectedLabel, drawLabel, lastWinLabel, totalWinLabel,
                new Label("Log:"), log);
        left.setPrefWidth(320);
        root.setLeft(left);


        // Grid
        grid.setHgap(4); grid.setVgap(4); grid.setPadding(new Insets(10));
        int n = 1;
        for (int r=0;r<8;r++)
            for (int c=0;c<10;c++) {
                Button b = new Button(String.valueOf(n));
                b.setPrefSize(60,40);
                final int num = n;
                b.setOnAction(e -> togglePick(num));
                b.setDisable(true);
                numberButtons[n] = b;
                grid.add(b,c,r);
                n++;
            }
        root.setCenter(grid);


        // Menu actions
        rules.setOnAction(e -> WelcomeController.Dialogs.showRules());
        odds.setOnAction(e -> WelcomeController.Dialogs.showOdds());
        newLook.setOnAction(e -> ThemeManager.applyAlt(root));
        exit.setOnAction(e -> router.getStage().close());


        ThemeManager.applyDefault(root);
    }

    private RadioButton mkSpot(String label, int value) {
        RadioButton rb = new RadioButton(label);
        rb.setToggleGroup(spotsGroup);
        rb.setOnAction(e -> { betCard.setSpots(value); updateSelectedLabel(); });
        return rb;
    }


    private void updateSelectedLabel() {
        selectedLabel.setText("Selected: " + betCard.getPicks().size() + " / " + betCard.getSpots());
        startBtn.setDisable(!(betCard.isComplete()));
    }


    private void enableGridIfReady() {
        Integer d = drawingsBox.getValue();
        RadioButton sel = (RadioButton) spotsGroup.getSelectedToggle();
        if (sel == null || !Validators.validDrawings(d)) {
            alert("Choose spots and drawings first.");
            return;
        }
        betCard.setSpots(Integer.parseInt(sel.getText()));
        game.configure(betCard.getSpots(), d);
        drawLabel.setText("Draw: 0 / " + d);
        for (int i=1;i<=80;i++) numberButtons[i].setDisable(false);
    }


    private void togglePick(int num) {
        if (game.isRunning()) return;
        boolean added = betCard.togglePick(num);
        Button b = numberButtons[num];
        if (added) b.setStyle("-fx-background-color: #b6e3ff;");
        else b.setStyle("");
        updateSelectedLabel();
    }

    private void quickPick() {
        if (game.isRunning()) return;
        betCard.quickFill();
        // Update button styles
        for (int i=1;i<=80;i++) {
            numberButtons[i].setStyle(betCard.getPicks().contains(i)?"-fx-background-color: #b6e3ff;":"");
        }
        updateSelectedLabel();
    }


    private void startDrawings() {
        if (!betCard.isComplete()) { alert("Pick exactly " + betCard.getSpots() + " numbers."); return; }
        lockInputs();
        game.start();
        log.getItems().add("Starting drawings…");
        proceedOneDrawing();
    }


    private void continueDrawing() { proceedOneDrawing(); }

    private void proceedOneDrawing() {
        continueBtn.setDisable(true);
        Set<Integer> drawn = engine.draw20();
        ObservableList<Integer> order = FXCollections.observableArrayList(drawn);
        Collections.shuffle(order); // random reveal order
        log.getItems().add("Drawing " + (game.getCurrentDraw()+1) + "…");
        // clear previous highlights
        for (int i=1;i<=80;i++) numberButtons[i].setOpacity(1.0);


        revealTimeline = new Timeline();
        int step = 0;
        for (Integer v : order) {
            step++;
            revealTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(200 * step), e -> {
                numberButtons[v].setOpacity(0.35);
            }));
        }
        revealTimeline.setOnFinished(e -> {
            MatchResult mr = game.computeResult(betCard.getPicks(), drawn);
            stats.record(mr);
            // highlight hits
            for (Integer h : mr.hits()) numberButtons[h].setStyle("-fx-background-color: #8ef79b;");
            lastWinLabel.setText("Last Win: $" + mr.win());
            totalWinLabel.setText("Total: $" + stats.totalWins());
            drawLabel.setText("Draw: " + game.getCurrentDraw() + " / " + game.getDrawings());
            log.getItems().add("Hits: " + mr.k() + " → $" + mr.win());


            if (game.hasNext()) {
                continueBtn.setDisable(false);
            } else {
                log.getItems().add("All drawings complete.");
                unlockForNewRound();
            }
        });
        revealTimeline.play();
    }

    private void resetBet() {
        if (revealTimeline != null) revealTimeline.stop();
        for (int i=1;i<=80;i++) { numberButtons[i].setDisable(true); numberButtons[i].setStyle(""); numberButtons[i].setOpacity(1.0);}
        betCard.clear();
        spotsGroup.selectToggle(null);
        selectedLabel.setText("Selected: 0 / 0");
        drawLabel.setText("Draw: 0 / 0");
        lastWinLabel.setText("Last Win: $0");
        startBtn.setDisable(true);
        continueBtn.setDisable(true);
    }


    private void lockInputs() {
        for (int i=1;i<=80;i++) numberButtons[i].setDisable(true);
        quickPickBtn.setDisable(true);
        startBtn.setDisable(true);
        drawingsBox.setDisable(true);
        spot1.setDisable(true); spot4.setDisable(true); spot8.setDisable(true); spot10.setDisable(true);
    }


    private void unlockForNewRound() {
        drawingsBox.setDisable(false);
        spot1.setDisable(false); spot4.setDisable(false); spot8.setDisable(false); spot10.setDisable(false);
        quickPickBtn.setDisable(false);
        resetBtn.setDisable(false);
    }


    private void alert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }


    public BorderPane getRoot() { return root; }
}