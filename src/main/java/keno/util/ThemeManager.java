package keno.util;


import javafx.scene.Parent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;


public class ThemeManager {
    public static void applyDefault(Parent root) {
        if (root instanceof Region r) r.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
    }
    public static void applyAlt(Parent root) {
        if (root instanceof Region r) r.setBackground(new Background(new BackgroundFill(Color.web("#f5f7ff"), null, null)));
        root.setStyle("-fx-font-size: 14px; -fx-font-family: 'Arial';");
    }
}