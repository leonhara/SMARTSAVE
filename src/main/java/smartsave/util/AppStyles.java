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

    // Colores base del estilo Aero
    private static final Color AERO_BLUE_LIGHT = Color.rgb(155, 205, 255, 0.7);
    private static final Color AERO_BLUE = Color.rgb(102, 152, 219, 0.7);
    private static final Color AERO_BLUE_DARK = Color.rgb(55, 110, 220, 0.7);
    private static final Color GLASS_WHITE = Color.rgb(255, 255, 255, 0.25);
    private static final Color GLASS_BORDER = Color.rgb(255, 255, 255, 0.7);
    private static final Color TEXT_WHITE = Color.rgb(240, 240, 240, 1.0);
    private static final Color TEXT_DARK = Color.rgb(30, 50, 70, 1.0);

    // Fuentes
    private static final String SEGOE_UI = "Segoe UI";

    /**
     * Aplica el estilo de ventana tipo Aero (Frutiger Aero) al panel principal
     */
    public static void applyMainPaneStyle(BorderPane pane) {
        // Fondo semitransparente con efecto de vidrio
        Stop[] stops = new Stop[] {
                new Stop(0, Color.rgb(190, 235, 255, 0.35)),
                new Stop(1, Color.rgb(220, 240, 255, 0.25))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);

        pane.setBackground(new Background(new BackgroundFill(
                gradient,
                new CornerRadii(8),
                null
        )));

        // Borde brillante característico de Aero
        pane.setBorder(new Border(new BorderStroke(
                GLASS_BORDER,
                BorderStrokeStyle.SOLID,
                new CornerRadii(8),
                new BorderWidths(1)
        )));

        // Sombra para efecto flotante
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setRadius(10);
        shadow.setSpread(0.1);
        pane.setEffect(shadow);
    }

    /**
     * Aplica estilo Aero a la barra de título
     */
    public static void applyTitleBarStyle(HBox titleBar) {
        // Gradiente azul característico de Aero para la barra de título
        Stop[] stops = new Stop[] {
                new Stop(0, Color.rgb(130, 180, 255, 0.85)),
                new Stop(0.5, Color.rgb(80, 130, 230, 0.8)),
                new Stop(1, Color.rgb(60, 100, 200, 0.85))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);

        titleBar.setBackground(new Background(new BackgroundFill(
                gradient,
                new CornerRadii(8, 8, 0, 0, false),
                null
        )));

        // Borde superior e izquierdo/derecho más brillante
        titleBar.setBorder(new Border(new BorderStroke(
                GLASS_BORDER,
                BorderStrokeStyle.SOLID,
                new CornerRadii(8, 8, 0, 0, false),
                new BorderWidths(1, 1, 0, 1)
        )));

        // Efecto reflejo suave característico de Aero
        Reflection reflection = new Reflection();
        reflection.setFraction(0.3);
        reflection.setTopOpacity(0.5);
        reflection.setBottomOpacity(0);
        titleBar.setEffect(reflection);

        // Altura de la barra de título
        titleBar.setPrefHeight(30);
        titleBar.setMinHeight(30);
        titleBar.setMaxHeight(30);

        // Padding
        titleBar.setPadding(new javafx.geometry.Insets(4, 8, 4, 8));
    }

    /**
     * Aplica estilo Aero al panel de contenido
     */
    public static void applyContentPaneStyle(VBox contentPane) {
        // Fondo de cristal semitransparente
        Stop[] stops = new Stop[] {
                new Stop(0, Color.rgb(245, 250, 255, 0.5)),
                new Stop(1, Color.rgb(225, 240, 255, 0.4))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);

        contentPane.setBackground(new Background(new BackgroundFill(
                gradient,
                new CornerRadii(0, 0, 8, 8, false),
                null
        )));

        // Borde suave
        contentPane.setBorder(new Border(new BorderStroke(
                GLASS_BORDER,
                BorderStrokeStyle.SOLID,
                new CornerRadii(0, 0, 8, 8, false),
                new BorderWidths(0, 1, 1, 1)
        )));

        // Efecto de brillo interior sutil
        InnerShadow innerGlow = new InnerShadow();
        innerGlow.setColor(Color.rgb(255, 255, 255, 0.5));
        innerGlow.setRadius(2);
        innerGlow.setChoke(0.1);
        innerGlow.setBlurType(BlurType.ONE_PASS_BOX);
        contentPane.setEffect(innerGlow);
    }

    /**
     * Aplica estilo Aero a los botones de la barra de título (minimizar, maximizar, cerrar)
     */
    public static void applyWindowButtonStyle(Button button) {
        button.setFont(Font.font(SEGOE_UI, FontWeight.BOLD, 10));
        button.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.0); " +
                        "-fx-text-fill: rgba(255, 255, 255, 0.9); " +
                        "-fx-padding: 2 8; " +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.3); " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 2 8; " +
                        "-fx-background-radius: 3; " +
                        "-fx-cursor: hand;"
        ));

        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.0); " +
                        "-fx-text-fill: rgba(255, 255, 255, 0.9); " +
                        "-fx-padding: 2 8; " +
                        "-fx-cursor: hand;"
        ));

        // Ajustar tamaño
        button.setMinSize(20, 20);
        button.setPrefSize(20, 20);
    }

    /**
     * Aplica estilo al título principal con estilo Aero
     */
    public static void applyTitleStyle(Label label) {
        label.setFont(Font.font(SEGOE_UI, FontWeight.BOLD, 22));
        label.setTextFill(AERO_BLUE_DARK);

        // Efecto de sombra para el texto
        DropShadow textShadow = new DropShadow();
        textShadow.setColor(Color.rgb(255, 255, 255, 0.8));
        textShadow.setRadius(1);
        textShadow.setOffsetY(1);
        textShadow.setOffsetX(0);
        textShadow.setSpread(0.3);
        label.setEffect(textShadow);
    }

    /**
     * Aplica estilo al subtítulo con estilo Aero
     */
    public static void applySubtitleStyle(Label label) {
        label.setFont(Font.font(SEGOE_UI, FontWeight.NORMAL, 14));
        label.setTextFill(TEXT_DARK);

        // Efecto de sombra sutil para el texto
        DropShadow textShadow = new DropShadow();
        textShadow.setColor(Color.rgb(255, 255, 255, 0.5));
        textShadow.setRadius(1);
        textShadow.setOffsetY(1);
        textShadow.setSpread(0.1);
        label.setEffect(textShadow);
    }

    /**
     * Aplica estilo a botones de acción principal con efecto de cristal Aero
     */
    public static void applyPrimaryButtonStyle(Button button) {
        button.setFont(Font.font(SEGOE_UI, FontWeight.BOLD, 12));

        // Crear efecto de cristal azul característico de Aero
        Stop[] stops = new Stop[] {
                new Stop(0, Color.rgb(180, 210, 255, 0.9)),
                new Stop(0.5, Color.rgb(130, 180, 240, 0.85)),
                new Stop(1, Color.rgb(100, 160, 230, 0.9))
        };

        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);

        // Fondo con gradiente
        button.setBackground(new Background(new BackgroundFill(
                gradient,
                new CornerRadii(5),
                null
        )));

        // Borde brillante
        button.setBorder(new Border(new BorderStroke(
                Color.rgb(200, 230, 255, 0.9),
                BorderStrokeStyle.SOLID,
                new CornerRadii(5),
                new BorderWidths(1)
        )));

        // Texto con sombra
        button.setTextFill(Color.WHITE);
        DropShadow textEffect = new DropShadow();
        textEffect.setColor(Color.rgb(0, 0, 0, 0.3));
        textEffect.setRadius(1);
        textEffect.setOffsetY(1);
        button.setEffect(textEffect);

        // Padding
        button.setPadding(new javafx.geometry.Insets(6, 14, 6, 14));

        // Efecto hover
        button.setOnMouseEntered(e -> {
            Stop[] hoverStops = new Stop[] {
                    new Stop(0, Color.rgb(200, 225, 255, 0.95)),
                    new Stop(0.5, Color.rgb(150, 195, 245, 0.9)),
                    new Stop(1, Color.rgb(120, 180, 240, 0.95))
            };

            LinearGradient hoverGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, hoverStops);

            button.setBackground(new Background(new BackgroundFill(
                    hoverGradient,
                    new CornerRadii(5),
                    null
            )));

            button.setBorder(new Border(new BorderStroke(
                    Color.rgb(220, 240, 255, 0.95),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(5),
                    new BorderWidths(1)
            )));

            // Aumentar sombra
            DropShadow hoverEffect = new DropShadow();
            hoverEffect.setColor(Color.rgb(0, 0, 0, 0.4));
            hoverEffect.setRadius(2);
            hoverEffect.setOffsetY(1);
            button.setEffect(hoverEffect);
        });

        button.setOnMouseExited(e -> {
            button.setBackground(new Background(new BackgroundFill(
                    gradient,
                    new CornerRadii(5),
                    null
            )));

            button.setBorder(new Border(new BorderStroke(
                    Color.rgb(200, 230, 255, 0.9),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(5),
                    new BorderWidths(1)
            )));

            // Restaurar sombra original
            DropShadow originalEffect = new DropShadow();
            originalEffect.setColor(Color.rgb(0, 0, 0, 0.3));
            originalEffect.setRadius(1);
            originalEffect.setOffsetY(1);
            button.setEffect(originalEffect);
        });

        // Efecto al presionar
        button.setOnMousePressed(e -> {
            Stop[] pressedStops = new Stop[] {
                    new Stop(0, Color.rgb(100, 160, 230, 0.9)),
                    new Stop(0.5, Color.rgb(130, 180, 240, 0.85)),
                    new Stop(1, Color.rgb(150, 200, 250, 0.9))
            };

            LinearGradient pressedGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, pressedStops);

            button.setBackground(new Background(new BackgroundFill(
                    pressedGradient,
                    new CornerRadii(5),
                    null
            )));

            // Quitar sombra temporalmente
            button.setEffect(null);
        });

        button.setOnMouseReleased(e -> {
            if (button.isHover()) {
                // Restaurar efecto hover
                Stop[] hoverStops = new Stop[] {
                        new Stop(0, Color.rgb(200, 225, 255, 0.95)),
                        new Stop(0.5, Color.rgb(150, 195, 245, 0.9)),
                        new Stop(1, Color.rgb(120, 180, 240, 0.95))
                };

                LinearGradient hoverGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, hoverStops);

                button.setBackground(new Background(new BackgroundFill(
                        hoverGradient,
                        new CornerRadii(5),
                        null
                )));

                // Restaurar sombra hover
                DropShadow hoverEffect = new DropShadow();
                hoverEffect.setColor(Color.rgb(0, 0, 0, 0.4));
                hoverEffect.setRadius(2);
                hoverEffect.setOffsetY(1);
                button.setEffect(hoverEffect);
            } else {
                // Restaurar estilo normal
                button.setBackground(new Background(new BackgroundFill(
                        gradient,
                        new CornerRadii(5),
                        null
                )));

                // Restaurar sombra original
                DropShadow originalEffect = new DropShadow();
                originalEffect.setColor(Color.rgb(0, 0, 0, 0.3));
                originalEffect.setRadius(1);
                originalEffect.setOffsetY(1);
                button.setEffect(originalEffect);
            }
        });
    }

    /**
     * Aplica estilo a los campos de texto con bordes redondeados y suave efecto Aero
     */
    public static void applyTextFieldStyle(TextField textField) {
        textField.setFont(Font.font(SEGOE_UI, 12));

        // Fondo ligeramente transparente
        textField.setBackground(new Background(new BackgroundFill(
                Color.rgb(255, 255, 255, 0.7),
                new CornerRadii(5),
                null
        )));

        // Borde azul claro
        textField.setBorder(new Border(new BorderStroke(
                Color.rgb(150, 190, 230, 0.7),
                BorderStrokeStyle.SOLID,
                new CornerRadii(5),
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
        textField.setPadding(new javafx.geometry.Insets(6, 8, 6, 8));

        // Efecto focus
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Cuando tiene foco
                textField.setBorder(new Border(new BorderStroke(
                        AERO_BLUE,
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1)
                )));

                // Sombra interior azulada
                InnerShadow focusShadow = new InnerShadow();
                focusShadow.setColor(Color.rgb(100, 150, 230, 0.2));
                focusShadow.setRadius(3);
                focusShadow.setOffsetX(0);
                focusShadow.setOffsetY(1);
                textField.setEffect(focusShadow);
            } else {
                // Cuando pierde el foco
                textField.setBorder(new Border(new BorderStroke(
                        Color.rgb(150, 190, 230, 0.7),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
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
     * Aplica estilo a hipervínculos con estilo Aero
     */
    public static void applyHyperlinkStyle(Hyperlink hyperlink) {
        hyperlink.setFont(Font.font(SEGOE_UI, 12));
        hyperlink.setTextFill(AERO_BLUE_DARK);
        hyperlink.setBorder(Border.EMPTY);
        hyperlink.setUnderline(false);

        // Efecto al pasar el ratón
        hyperlink.setOnMouseEntered(e -> {
            hyperlink.setTextFill(AERO_BLUE_LIGHT);
            hyperlink.setUnderline(true);
        });

        hyperlink.setOnMouseExited(e -> {
            hyperlink.setTextFill(AERO_BLUE_DARK);
            hyperlink.setUnderline(false);
        });
    }

    /**
     * Aplica estilo a etiquetas normales
     */
    public static void applyLabelStyle(Label label) {
        label.setFont(Font.font(SEGOE_UI, 12));
        label.setTextFill(TEXT_DARK);

        // Sombra de texto muy sutil
        DropShadow labelShadow = new DropShadow();
        labelShadow.setColor(Color.rgb(255, 255, 255, 0.5));
        labelShadow.setRadius(0.5);
        labelShadow.setOffsetY(0.5);
        labelShadow.setSpread(0.1);
        label.setEffect(labelShadow);
    }
}