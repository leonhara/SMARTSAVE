package smartsave.util;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Hyperlink;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.Reflection;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class AppStyles {

    // Colores base para simular Windows 7 Aero en Windows 11
    private static final Color AERO_BLUE_TITLE = Color.rgb(80, 150, 220, 1.0);
    private static final Color AERO_SIMULATION = Color.rgb(225, 238, 255, 0.85);
    private static final Color AERO_BORDER = Color.rgb(200, 220, 255, 1.0);
    private static final Color TEXT_WHITE = Color.rgb(255, 255, 255, 1.0);
    private static final Color TEXT_DARK = Color.rgb(30, 50, 70, 1.0);

    // Fuentes
    private static final String SEGOE_UI = "Segoe UI";

    /**
     * Aplica el estilo de ventana tipo Windows 7 Aero simulado para Windows 11
     */
    public static void applyMainPaneStyle(BorderPane pane) {
        // Simulación de Aero Glass en Windows 11 - Colores exactos de Windows 7
        Stop[] stops = new Stop[] {
                new Stop(0, Color.rgb(226, 237, 254, 1.0)),
                new Stop(1, Color.rgb(210, 230, 250, 1.0))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);

        pane.setBackground(new Background(new BackgroundFill(
                gradient,
                new CornerRadii(7),
                null
        )));

        // Borde brillante característico de Windows 7
        pane.setBorder(new Border(new BorderStroke(
                Color.rgb(255, 255, 255, 0.9),
                BorderStrokeStyle.SOLID,
                new CornerRadii(7),
                new BorderWidths(1)
        )));

        // Sombra para efecto flotante
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.4));
        shadow.setRadius(12);
        shadow.setSpread(0.05);
        pane.setEffect(shadow);
    }

    /**
     * Aplica estilo Windows 7 a la barra de título (simulado para Windows 11)
     */
    public static void applyTitleBarStyle(HBox titleBar) {
        // Gradiente azul característico de Windows 7 para la barra de título - COLORES AJUSTADOS
        Stop[] stops = new Stop[] {
                new Stop(0, Color.rgb(105, 162, 222, 1.0)),
                new Stop(0.5, Color.rgb(80, 142, 210, 1.0)),
                new Stop(1, Color.rgb(60, 127, 195, 1.0))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);

        titleBar.setBackground(new Background(new BackgroundFill(
                gradient,
                new CornerRadii(7, 7, 0, 0, false),
                null
        )));

        // Borde superior e izquierdo/derecho más brillante
        titleBar.setBorder(new Border(new BorderStroke(
                Color.rgb(255, 255, 255, 0.9),
                BorderStrokeStyle.SOLID,
                new CornerRadii(7, 7, 0, 0, false),
                new BorderWidths(1, 1, 0, 1)
        )));

        // Reflejo menos intenso para que sea visible en Windows 11
        Reflection reflection = new Reflection();
        reflection.setFraction(0.2);
        reflection.setTopOpacity(0.5);
        reflection.setBottomOpacity(0);
        titleBar.setEffect(reflection);

        // Altura de la barra de título igual a Windows 7
        titleBar.setPrefHeight(30);
        titleBar.setMinHeight(30);
        titleBar.setMaxHeight(30);

        // Padding
        titleBar.setPadding(new javafx.geometry.Insets(5, 5, 5, 10));

        // Asegurar que los botones estén alineados correctamente
        for (javafx.scene.Node node : titleBar.getChildren()) {
            if (node instanceof Label) {
                HBox.setHgrow(node, javafx.scene.layout.Priority.ALWAYS);
                ((Label) node).setMaxWidth(Double.MAX_VALUE);
            } else if (node instanceof HBox) {
                HBox.setHgrow(node, javafx.scene.layout.Priority.NEVER);
                ((HBox) node).setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            }
        }
    }

    /**
     * Aplica estilo Windows 7 al panel de contenido (adaptado para Windows 11)
     */
    public static void applyContentPaneStyle(VBox contentPane) {
        // Fondo de cristal simulado
        contentPane.setBackground(new Background(new BackgroundFill(
                Color.rgb(245, 250, 255, 0.9),
                new CornerRadii(0, 0, 7, 7, false),
                null
        )));

        // Borde suave estilo Windows 7
        contentPane.setBorder(new Border(new BorderStroke(
                AERO_BORDER,
                BorderStrokeStyle.SOLID,
                new CornerRadii(0, 0, 7, 7, false),
                new BorderWidths(0, 1, 1, 1)
        )));

        // Efecto de brillo interior sutil
        InnerShadow innerGlow = new InnerShadow();
        innerGlow.setColor(Color.rgb(255, 255, 255, 0.7));
        innerGlow.setRadius(1);
        innerGlow.setChoke(0.1);
        innerGlow.setBlurType(BlurType.ONE_PASS_BOX);
        contentPane.setEffect(innerGlow);

        // Padding
        contentPane.setPadding(new javafx.geometry.Insets(20));
    }

    /**
     * Aplica estilo Windows 7 a los botones de la barra de título
     */
    public static void applyWindowButtonStyle(Button button) {
        button.setFont(Font.font(SEGOE_UI, FontWeight.BOLD, 10));

        // Mayor contraste y tamaño para los botones de la barra de título
        button.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: white; " +  // Texto más visible (blanco completo)
                        "-fx-padding: 1 5; " +      // Padding reducido
                        "-fx-cursor: hand; " +
                        "-fx-font-size: 12px;"      // Fuente más grande
        );

        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.3); " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 1 5; " +
                        "-fx-background-radius: 3; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-size: 12px;"
        ));

        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 1 5; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-size: 12px;"
        ));

        // Ajustar tamaño para que sean más visibles
        button.setMinSize(24, 18);
        button.setPrefSize(24, 18);
    }

    /**
     * Aplica estilo al título principal como Windows 7
     */
    public static void applyTitleStyle(Label label) {
        label.setFont(Font.font(SEGOE_UI, FontWeight.BOLD, 20));
        label.setTextFill(TEXT_DARK);

        // Efecto de sombra para el texto como en Windows 7
        DropShadow textShadow = new DropShadow();
        textShadow.setColor(Color.rgb(255, 255, 255, 0.8));
        textShadow.setRadius(1);
        textShadow.setOffsetY(1);
        textShadow.setOffsetX(0);
        textShadow.setSpread(0.2);
        label.setEffect(textShadow);
    }

    /**
     * Aplica estilo al subtítulo como Windows 7
     */
    public static void applySubtitleStyle(Label label) {
        label.setFont(Font.font(SEGOE_UI, FontWeight.NORMAL, 13));
        label.setTextFill(TEXT_DARK);

        // Efecto de sombra sutil para el texto
        DropShadow textShadow = new DropShadow();
        textShadow.setColor(Color.rgb(255, 255, 255, 0.7));
        textShadow.setRadius(0.5);
        textShadow.setOffsetY(0.5);
        textShadow.setSpread(0.1);
        label.setEffect(textShadow);
    }

    /**
     * Aplica estilo a botones principales como en Windows 7
     */
    public static void applyPrimaryButtonStyle(Button button) {
        button.setFont(Font.font(SEGOE_UI, FontWeight.NORMAL, 12));

        // Crear efecto de cristal azul característico de Windows 7
        Stop[] stops = new Stop[] {
                new Stop(0, Color.rgb(235, 244, 252, 1.0)),
                new Stop(0.5, Color.rgb(210, 230, 250, 1.0)),
                new Stop(1, Color.rgb(190, 215, 245, 1.0))
        };

        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);

        // Fondo con gradiente
        button.setBackground(new Background(new BackgroundFill(
                gradient,
                new CornerRadii(3),
                null
        )));

        // Borde estilo Windows 7
        button.setBorder(new Border(new BorderStroke(
                Color.rgb(170, 190, 210, 0.9),
                BorderStrokeStyle.SOLID,
                new CornerRadii(3),
                new BorderWidths(1)
        )));

        // Texto como en Windows 7
        button.setTextFill(TEXT_DARK);
        DropShadow textEffect = new DropShadow();
        textEffect.setColor(Color.rgb(255, 255, 255, 0.8));
        textEffect.setRadius(0.5);
        textEffect.setOffsetY(0.5);
        button.setEffect(textEffect);

        // Padding
        button.setPadding(new javafx.geometry.Insets(5, 12, 5, 12));

        // Efecto hover
        button.setOnMouseEntered(e -> {
            Stop[] hoverStops = new Stop[] {
                    new Stop(0, Color.rgb(250, 252, 255, 0.95)),
                    new Stop(0.5, Color.rgb(220, 240, 255, 0.9)),
                    new Stop(1, Color.rgb(200, 225, 250, 0.95))
            };

            LinearGradient hoverGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, hoverStops);

            button.setBackground(new Background(new BackgroundFill(
                    hoverGradient,
                    new CornerRadii(3),
                    null
            )));

            button.setBorder(new Border(new BorderStroke(
                    Color.rgb(140, 180, 210, 0.95),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(3),
                    new BorderWidths(1)
            )));
        });

        button.setOnMouseExited(e -> {
            button.setBackground(new Background(new BackgroundFill(
                    gradient,
                    new CornerRadii(3),
                    null
            )));

            button.setBorder(new Border(new BorderStroke(
                    Color.rgb(170, 190, 210, 0.9),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(3),
                    new BorderWidths(1)
            )));
        });

        // Efecto al presionar
        button.setOnMousePressed(e -> {
            Stop[] pressedStops = new Stop[] {
                    new Stop(0, Color.rgb(190, 210, 240, 0.9)),
                    new Stop(0.5, Color.rgb(210, 230, 250, 0.85)),
                    new Stop(1, Color.rgb(220, 240, 255, 0.9))
            };

            LinearGradient pressedGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, pressedStops);

            button.setBackground(new Background(new BackgroundFill(
                    pressedGradient,
                    new CornerRadii(3),
                    null
            )));

            button.setBorder(new Border(new BorderStroke(
                    Color.rgb(130, 170, 200, 0.95),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(3),
                    new BorderWidths(1)
            )));
        });

        button.setOnMouseReleased(e -> {
            if (button.isHover()) {
                Stop[] hoverStops = new Stop[] {
                        new Stop(0, Color.rgb(250, 252, 255, 0.95)),
                        new Stop(0.5, Color.rgb(220, 240, 255, 0.9)),
                        new Stop(1, Color.rgb(200, 225, 250, 0.95))
                };

                LinearGradient hoverGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, hoverStops);

                button.setBackground(new Background(new BackgroundFill(
                        hoverGradient,
                        new CornerRadii(3),
                        null
                )));

                button.setBorder(new Border(new BorderStroke(
                        Color.rgb(140, 180, 210, 0.95),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(3),
                        new BorderWidths(1)
                )));
            } else {
                button.setBackground(new Background(new BackgroundFill(
                        gradient,
                        new CornerRadii(3),
                        null
                )));

                button.setBorder(new Border(new BorderStroke(
                        Color.rgb(170, 190, 210, 0.9),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(3),
                        new BorderWidths(1)
                )));
            }
        });
    }

    /**
     * Aplica estilo a los campos de texto como en Windows 7
     */
    public static void applyTextFieldStyle(TextField textField) {
        textField.setFont(Font.font(SEGOE_UI, 12));

        // Fondo blanco como en Windows 7
        textField.setBackground(new Background(new BackgroundFill(
                Color.rgb(255, 255, 255, 1.0), // Blanco sólido
                new CornerRadii(2),
                null
        )));

        // Borde como en Windows 7
        textField.setBorder(new Border(new BorderStroke(
                Color.rgb(160, 180, 200, 0.8),
                BorderStrokeStyle.SOLID,
                new CornerRadii(2),
                new BorderWidths(1)
        )));

        // Sombra interior sutil
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setColor(Color.rgb(0, 0, 0, 0.1));
        innerShadow.setRadius(1);
        innerShadow.setOffsetX(0);
        innerShadow.setOffsetY(1);
        textField.setEffect(innerShadow);

        // Padding
        textField.setPadding(new javafx.geometry.Insets(4, 6, 4, 6));

        // Efecto focus
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Cuando tiene foco
                textField.setBorder(new Border(new BorderStroke(
                        Color.rgb(100, 160, 220, 0.8),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(2),
                        new BorderWidths(1)
                )));

                // Brillo interior azulado
                InnerShadow focusShadow = new InnerShadow();
                focusShadow.setColor(Color.rgb(100, 150, 230, 0.15));
                focusShadow.setRadius(2);
                focusShadow.setOffsetX(0);
                focusShadow.setOffsetY(1);
                textField.setEffect(focusShadow);
            } else {
                // Cuando pierde el foco
                textField.setBorder(new Border(new BorderStroke(
                        Color.rgb(160, 180, 200, 0.8),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(2),
                        new BorderWidths(1)
                )));

                // Restaurar sombra original
                InnerShadow originalShadow = new InnerShadow();
                originalShadow.setColor(Color.rgb(0, 0, 0, 0.1));
                originalShadow.setRadius(1);
                originalShadow.setOffsetX(0);
                originalShadow.setOffsetY(1);
                textField.setEffect(originalShadow);
            }
        });
    }

    /**
     * Aplica estilo a hipervínculos como en Windows 7
     */
    public static void applyHyperlinkStyle(Hyperlink hyperlink) {
        hyperlink.setFont(Font.font(SEGOE_UI, 12));
        hyperlink.setTextFill(Color.rgb(40, 100, 170, 1.0)); // Azul de Windows 7
        hyperlink.setBorder(Border.EMPTY);
        hyperlink.setUnderline(false);

        // Efecto al pasar el ratón como en Windows 7
        hyperlink.setOnMouseEntered(e -> {
            hyperlink.setTextFill(Color.rgb(65, 120, 190, 1.0));
            hyperlink.setUnderline(true);
        });

        hyperlink.setOnMouseExited(e -> {
            hyperlink.setTextFill(Color.rgb(40, 100, 170, 1.0));
            hyperlink.setUnderline(false);
        });
    }

    /**
     * Aplica estilo a etiquetas normales como en Windows 7
     */
    public static void applyLabelStyle(Label label) {
        label.setFont(Font.font(SEGOE_UI, 12));
        label.setTextFill(TEXT_DARK);

        // Sombra de texto muy sutil
        DropShadow labelShadow = new DropShadow();
        labelShadow.setColor(Color.rgb(255, 255, 255, 0.5));
        labelShadow.setRadius(0.2);
        labelShadow.setOffsetY(0.2);
        labelShadow.setSpread(0.1);
        label.setEffect(labelShadow);
    }
}