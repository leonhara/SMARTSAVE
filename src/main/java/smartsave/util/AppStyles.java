package smartsave.util;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class AppStyles {

    // Colores base de la aplicación
    private static final Color PRIMARY_COLOR = Color.rgb(52, 152, 219, 1.0);       // Azul
    private static final Color SECONDARY_COLOR = Color.rgb(41, 128, 185, 1.0);     // Azul más oscuro
    private static final Color TRANSPARENT_WHITE = Color.rgb(255, 255, 255, 0.6);  // Blanco semitransparente
    private static final Color PANEL_BACKGROUND = Color.rgb(255, 255, 255, 0.7);   // Fondo del panel
    private static final Color TEXT_PRIMARY = Color.rgb(44, 62, 80, 1.0);          // Texto oscuro
    private static final Color TEXT_SECONDARY = Color.rgb(127, 140, 141, 1.0);     // Texto gris

    // Métodos para aplicar estilos a los componentes principales

    /**
     * Aplica el estilo de ventana transparente al panel principal
     */
    public static void applyMainPaneStyle(BorderPane pane) {
        // Fondo semitransparente con bordes redondeados
        pane.setBackground(new Background(new BackgroundFill(
                TRANSPARENT_WHITE,
                new CornerRadii(10),
                null
        )));

        // Sombra para efecto flotante
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setRadius(10);
        pane.setEffect(shadow);
    }

    /**
     * Aplica estilo a la barra de título
     */
    public static void applyTitleBarStyle(HBox titleBar) {
        // Gradiente para la barra de título
        Stop[] stops = new Stop[] {
                new Stop(0, Color.rgb(90, 140, 230, 0.8)),
                new Stop(1, Color.rgb(40, 80, 180, 0.8))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);

        titleBar.setBackground(new Background(new BackgroundFill(
                gradient,
                new CornerRadii(10, 10, 0, 0, false), // Redondeado solo en la parte superior
                null
        )));

        // Altura de la barra de título
        titleBar.setPrefHeight(40);
    }

    /**
     * Aplica estilo al panel de contenido
     */
    public static void applyContentPaneStyle(VBox contentPane) {
        contentPane.setBackground(new Background(new BackgroundFill(
                PANEL_BACKGROUND,
                new CornerRadii(0, 0, 10, 10, false), // Redondeado solo en la parte inferior
                null
        )));
    }

    /**
     * Aplica estilo a los botones de la barra de título (minimizar, maximizar, cerrar)
     */
    public static void applyWindowButtonStyle(Button button) {
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: transparent; -fx-text-fill: white;"));
    }

    /**
     * Aplica estilo al título principal
     */
    public static void applyTitleStyle(Label label) {
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        label.setTextFill(TEXT_PRIMARY);
    }

    /**
     * Aplica estilo al subtítulo
     */
    public static void applySubtitleStyle(Label label) {
        label.setFont(Font.font("Segoe UI", 16));
        label.setTextFill(TEXT_SECONDARY);
    }

    /**
     * Aplica estilo al botón de acción principal
     */
    public static void applyPrimaryButtonStyle(Button button) {
        button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #3498db, #2980b9); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 20; " +
                        "-fx-background-radius: 5;"
        );

        // Efectos hover
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4fa3e0, #3990c3); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 20; " +
                        "-fx-background-radius: 5;"
        ));

        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #3498db, #2980b9); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 20; " +
                        "-fx-background-radius: 5;"
        ));
    }

    /**
     * Aplica estilo a los campos de texto
     */
    public static void applyTextFieldStyle(TextField textField) {
        textField.setStyle(
                "-fx-padding: 8; " +
                        "-fx-background-radius: 4; " +
                        "-fx-background-color: rgba(255, 255, 255, 0.9);"
        );
    }
}