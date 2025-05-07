package smartsave.util;

import javafx.geometry.Pos;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

// Eliminado el import java.awt.* que puede estar causando conflictos

/**
 * Clase de estilos mejorada con tema oscuro y acentos neón
 */
public class AppStyles {

    // Colores del tema oscuro con acentos neón
    private static final Color DARK_BG = Color.rgb(15, 15, 25, 1.0);
    private static final Color DARK_PANEL = Color.rgb(25, 25, 35, 1.0);
    private static final Color NEON_PINK = Color.rgb(255, 0, 255, 1.0);
    private static final Color NEON_BLUE = Color.rgb(80, 145, 255, 1.0);
    private static final Color NEON_PURPLE = Color.rgb(160, 100, 255, 1.0);
    private static final Color TEXT_LIGHT = Color.rgb(230, 230, 250, 1.0);

    // Gradiente neón para botones y acentos
    private static final LinearGradient NEON_GRADIENT = createNeonGradient();

    // Fuente moderna
    private static final String MODERN_FONT = "Segoe UI";

    /**
     * Crea el gradiente neón principal
     */
    private static LinearGradient createNeonGradient() {
        Stop[] stops = new Stop[] {
                new Stop(0, NEON_PINK),
                new Stop(1, NEON_BLUE)
        };
        return new LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE, stops);
    }

    /**
     * Aplica el estilo al panel principal
     */
    public static void applyMainPaneStyle(BorderPane pane) {
        // Fondo oscuro sólido
        pane.setBackground(new Background(new BackgroundFill(
                DARK_BG,
                new CornerRadii(0),
                null
        )));

        // Sin borde
        pane.setBorder(Border.EMPTY);

        // Efecto sutil de sombra
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.7));
        shadow.setRadius(15);
        shadow.setSpread(0.1);
        pane.setEffect(shadow);
    }

    /**
     * Aplica estilo a la barra de título
     */
    public static void applyTitleBarStyle(HBox titleBar) {
        // Fondo oscuro para la barra de título
        titleBar.setBackground(new Background(new BackgroundFill(
                DARK_PANEL,
                new CornerRadii(0),
                null
        )));

        // Sin borde
        titleBar.setBorder(Border.EMPTY);

        // Tamaño y padding
        titleBar.setPrefHeight(40);
        titleBar.setMinHeight(40);
        titleBar.setMaxHeight(40);
        titleBar.setPadding(new javafx.geometry.Insets(5, 10, 5, 15));

        // Configuración de los hijos
        for (javafx.scene.Node node : titleBar.getChildren()) {
            if (node instanceof Label) {
                HBox.setHgrow(node, javafx.scene.layout.Priority.ALWAYS);
                ((Label) node).setMaxWidth(Double.MAX_VALUE);
                ((Label) node).setTextFill(TEXT_LIGHT);
                ((Label) node).setFont(Font.font(MODERN_FONT, FontWeight.BOLD, 14));

                // Efecto de brillo sutil
                Glow glow = new Glow();
                glow.setLevel(0.3);
                ((Label) node).setEffect(glow);
            } else if (node instanceof HBox) {
                HBox.setHgrow(node, javafx.scene.layout.Priority.NEVER);
                ((HBox) node).setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            }
        }
    }

    /**
     * Aplica estilo al panel de contenido
     */
    public static void applyContentPaneStyle(VBox contentPane) {
        // Fondo oscuro para el panel
        contentPane.setBackground(new Background(new BackgroundFill(
                DARK_PANEL,
                new CornerRadii(0),
                null
        )));

        // Sin borde
        contentPane.setBorder(Border.EMPTY);

        // Padding para el contenido
        contentPane.setPadding(new javafx.geometry.Insets(25));

        // Espaciado entre elementos
        contentPane.setSpacing(15);
    }

    /**
     * Aplica estilo a los botones de la ventana (minimizar, maximizar, cerrar)
     */
    public static void applyWindowButtonStyle(Button button) {
        button.setFont(Font.font(MODERN_FONT, FontWeight.BOLD, 11));
        button.setTextFill(TEXT_LIGHT);

        // Estilo base
        button.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 2 10; " +
                        "-fx-cursor: hand;"
        );

        // Efectos de hover
        button.setOnMouseEntered(e -> {
            if (button.getText().equals("✕")) {
                // Rojo para el botón de cerrar
                button.setStyle(
                        "-fx-background-color: rgba(255, 50, 50, 0.7); " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 2 10; " +
                                "-fx-cursor: hand;"
                );
            } else {
                // Normal para los otros botones
                button.setStyle(
                        "-fx-background-color: rgba(255, 255, 255, 0.1); " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 2 10; " +
                                "-fx-cursor: hand;"
                );
            }
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 2 10; " +
                            "-fx-cursor: hand;"
            );
        });

        // Tamaño consistente
        button.setMinSize(30, 25);
        button.setPrefSize(30, 25);
    }

    /**
     * Aplica estilo al título principal
     */
    public static void applyTitleStyle(Label label) {
        label.setFont(Font.font(MODERN_FONT, FontWeight.BOLD, 24));
        label.setTextFill(TEXT_LIGHT);

        // Efecto de brillo neón
        DropShadow glow = new DropShadow();
        glow.setColor(NEON_PURPLE);
        glow.setRadius(10);
        glow.setSpread(0.2);
        label.setEffect(glow);
    }

    /**
     * Aplica estilo al subtítulo
     */
    public static void applySubtitleStyle(Label label) {
        label.setFont(Font.font(MODERN_FONT, FontWeight.NORMAL, 14));
        label.setTextFill(TEXT_LIGHT);
    }

    /**
     * Aplica estilo a los botones principales
     */
    public static void applyPrimaryButtonStyle(Button button) {
        button.setFont(Font.font(MODERN_FONT, FontWeight.NORMAL, 13));

        // Fondo con gradiente neón
        Stop[] stops = new Stop[] {
                new Stop(0, NEON_PINK),
                new Stop(1, NEON_BLUE)
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE, stops);

        button.setBackground(new Background(new BackgroundFill(
                gradient,
                new CornerRadii(5),
                null
        )));

        // Sin borde
        button.setBorder(Border.EMPTY);

        // Texto blanco
        button.setTextFill(Color.WHITE);

        // Sombra para efecto 3D
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(180, 70, 255, 0.7));
        shadow.setRadius(10);
        shadow.setSpread(0.1);
        button.setEffect(shadow);

        // Padding
        button.setPadding(new javafx.geometry.Insets(8, 20, 8, 20));

        // Efectos interactivos
        button.setOnMouseEntered(e -> {
            // Aumentar brillo
            DropShadow hoverShadow = new DropShadow();
            hoverShadow.setColor(Color.rgb(200, 100, 255, 0.9));
            hoverShadow.setRadius(15);
            hoverShadow.setSpread(0.2);
            button.setEffect(hoverShadow);
        });

        button.setOnMouseExited(e -> {
            // Restaurar sombra original
            DropShadow originalShadow = new DropShadow();
            originalShadow.setColor(Color.rgb(180, 70, 255, 0.7));
            originalShadow.setRadius(10);
            originalShadow.setSpread(0.1);
            button.setEffect(originalShadow);
        });

        button.setOnMousePressed(e -> {
            // Efecto de presión
            DropShadow pressedShadow = new DropShadow();
            pressedShadow.setColor(Color.rgb(180, 70, 255, 0.5));
            pressedShadow.setRadius(5);
            pressedShadow.setSpread(0.05);
            button.setEffect(pressedShadow);
        });

        button.setOnMouseReleased(e -> {
            if (button.isHover()) {
                // Restaurar efecto hover
                DropShadow hoverShadow = new DropShadow();
                hoverShadow.setColor(Color.rgb(200, 100, 255, 0.9));
                hoverShadow.setRadius(15);
                hoverShadow.setSpread(0.2);
                button.setEffect(hoverShadow);
            } else {
                // Restaurar sombra original
                DropShadow originalShadow = new DropShadow();
                originalShadow.setColor(Color.rgb(180, 70, 255, 0.7));
                originalShadow.setRadius(10);
                originalShadow.setSpread(0.1);
                button.setEffect(originalShadow);
            }
        });
    }

    /**
     * Aplica estilo a los campos de texto
     */
    public static void applyTextFieldStyle(TextField textField) {
        textField.setFont(Font.font(MODERN_FONT, 13));

        // Fondo oscuro
        textField.setBackground(new Background(new BackgroundFill(
                Color.rgb(35, 35, 45, 1.0),
                new CornerRadii(5),
                null
        )));

        // Borde neón sutil
        textField.setBorder(new Border(new BorderStroke(
                Color.rgb(120, 100, 200, 0.5),
                BorderStrokeStyle.SOLID,
                new CornerRadii(5),
                new BorderWidths(1)
        )));

        // Texto claro
        textField.setStyle("-fx-text-fill: rgb(230, 230, 250);");

        // Padding
        textField.setPadding(new javafx.geometry.Insets(8, 10, 8, 10));

        // Efecto focus
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Cuando tiene foco - borde neón más brillante
                textField.setBorder(new Border(new BorderStroke(
                        NEON_PURPLE,
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1.5)
                )));

                // Brillo interior
                DropShadow focusShadow = new DropShadow();
                focusShadow.setColor(Color.rgb(160, 100, 255, 0.4));
                focusShadow.setRadius(10);
                focusShadow.setSpread(0.1);
                textField.setEffect(focusShadow);
            } else {
                // Cuando pierde el foco - borde original
                textField.setBorder(new Border(new BorderStroke(
                        Color.rgb(120, 100, 200, 0.5),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1)
                )));

                // Sin efecto
                textField.setEffect(null);
            }
        });
    }

    /**
     * Aplica estilo al campo de contraseña (igual que TextField pero para PasswordField)
     */
    public static void applyPasswordFieldStyle(PasswordField passwordField) {
        passwordField.setFont(Font.font(MODERN_FONT, 13));

        // Fondo oscuro
        passwordField.setBackground(new Background(new BackgroundFill(
                Color.rgb(35, 35, 45, 1.0),
                new CornerRadii(5),
                null
        )));

        // Borde neón sutil
        passwordField.setBorder(new Border(new BorderStroke(
                Color.rgb(120, 100, 200, 0.5),
                BorderStrokeStyle.SOLID,
                new CornerRadii(5),
                new BorderWidths(1)
        )));

        // Texto claro
        passwordField.setStyle("-fx-text-fill: rgb(230, 230, 250);");

        // Padding
        passwordField.setPadding(new javafx.geometry.Insets(8, 10, 8, 10));

        // Efecto focus
        passwordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Cuando tiene foco - borde neón más brillante
                passwordField.setBorder(new Border(new BorderStroke(
                        NEON_PURPLE,
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1.5)
                )));

                // Brillo interior
                DropShadow focusShadow = new DropShadow();
                focusShadow.setColor(Color.rgb(160, 100, 255, 0.4));
                focusShadow.setRadius(10);
                focusShadow.setSpread(0.1);
                passwordField.setEffect(focusShadow);
            } else {
                // Cuando pierde el foco - borde original
                passwordField.setBorder(new Border(new BorderStroke(
                        Color.rgb(120, 100, 200, 0.5),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1)
                )));

                // Sin efecto
                passwordField.setEffect(null);
            }
        });
    }

    /**
     * Aplica estilo a los hipervínculos
     */
    public static void applyHyperlinkStyle(Hyperlink hyperlink) {
        hyperlink.setFont(Font.font(MODERN_FONT, 13));
        hyperlink.setTextFill(NEON_PINK);
        hyperlink.setBorder(Border.EMPTY);
        hyperlink.setUnderline(false);

        // Efecto al pasar el ratón
        hyperlink.setOnMouseEntered(e -> {
            hyperlink.setTextFill(NEON_BLUE);
            hyperlink.setUnderline(true);

            // Efecto de brillo
            Glow glow = new Glow();
            glow.setLevel(0.5);
            hyperlink.setEffect(glow);
        });

        hyperlink.setOnMouseExited(e -> {
            hyperlink.setTextFill(NEON_PINK);
            hyperlink.setUnderline(false);
            hyperlink.setEffect(null);
        });
    }

    /**
     * Aplica estilo a etiquetas normales
     */
    public static void applyLabelStyle(Label label) {
        label.setFont(Font.font(MODERN_FONT, 13));
        label.setTextFill(TEXT_LIGHT);
    }

    /**
     * Aplica estilo al menú lateral
     */
    public static void applySideMenuStyle(VBox sideMenu) {
        // Fondo oscuro para el menú
        sideMenu.setBackground(new Background(new BackgroundFill(
                Color.rgb(20, 20, 30, 1.0),
                new CornerRadii(0),
                null
        )));

        // Sin borde
        sideMenu.setBorder(Border.EMPTY);

        // Efecto sutil de sombra interna
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setRadius(10);
        shadow.setSpread(0);
        sideMenu.setEffect(shadow);
    }

    /**
     * Aplica estilo a los botones de navegación
     */
    public static void applyNavigationButtonStyle(Button button) {
        button.setFont(Font.font(MODERN_FONT, FontWeight.NORMAL, 13));

        // Fondo transparente
        button.setBackground(new Background(new BackgroundFill(
                Color.TRANSPARENT,
                new CornerRadii(5),
                null
        )));

        // Borde sutil
        button.setBorder(new Border(new BorderStroke(
                Color.rgb(80, 80, 120, 0.3),
                BorderStrokeStyle.SOLID,
                new CornerRadii(5),
                new BorderWidths(1)
        )));

        // Texto claro
        button.setTextFill(Color.rgb(200, 200, 220, 1.0));

        // Padding
        button.setPadding(new javafx.geometry.Insets(8, 15, 8, 15));

        // Alineación izquierda con el icono
        button.setAlignment(Pos.CENTER_LEFT);
        button.setGraphicTextGap(10);

        // Añadir clase CSS para poder cambiar estilos fácilmente
        button.getStyleClass().add("nav-button");

        // Efectos de hover
        button.setOnMouseEntered(e -> {
            button.setBackground(new Background(new BackgroundFill(
                    Color.rgb(60, 60, 90, 0.3),
                    new CornerRadii(5),
                    null
            )));

            button.setBorder(new Border(new BorderStroke(
                    Color.rgb(150, 100, 255, 0.5),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(5),
                    new BorderWidths(1)
            )));

            button.setTextFill(Color.rgb(255, 255, 255, 1.0));
        });

        button.setOnMouseExited(e -> {
            if (!button.getStyleClass().contains("selected")) {
                button.setBackground(new Background(new BackgroundFill(
                        Color.TRANSPARENT,
                        new CornerRadii(5),
                        null
                )));

                button.setBorder(new Border(new BorderStroke(
                        Color.rgb(80, 80, 120, 0.3),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1)
                )));

                button.setTextFill(Color.rgb(200, 200, 220, 1.0));
            }
        });

        // Efecto para botón seleccionado
        if (button.getStyleClass().contains("selected")) {
            button.setBackground(new Background(new BackgroundFill(
                    Color.rgb(100, 80, 180, 0.3),
                    new CornerRadii(5),
                    null
            )));

            button.setBorder(new Border(new BorderStroke(
                    Color.rgb(255, 0, 255, 0.7),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(5),
                    new BorderWidths(1)
            )));

            button.setTextFill(Color.rgb(255, 255, 255, 1.0));
        }
    }

    /**
     * Aplica estilo a los gráficos
     */
    public static void applyChartStyle(Chart chart) {
        // Fondo transparente
        chart.setStyle("-fx-background-color: transparent;");

        // Color de texto claro
        chart.lookupAll(".chart-title").forEach(node ->
                node.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;")
        );

        chart.lookupAll(".axis-label").forEach(node ->
                node.setStyle("-fx-text-fill: rgb(200, 200, 220);")
        );

        chart.lookupAll(".chart-legend").forEach(node ->
                node.setStyle("-fx-background-color: transparent; -fx-text-fill: white;")
        );

        chart.lookupAll(".chart-legend-item").forEach(node ->
                node.setStyle("-fx-text-fill: white;")
        );

        // Estilos específicos para PieChart
        if (chart instanceof PieChart) {
            ((PieChart) chart).getData().forEach(data -> {
                // Añadir efecto neón a las secciones
                Glow glow = new Glow();
                glow.setLevel(0.3);
                data.getNode().setEffect(glow);
            });
        }

        // Estilos específicos para LineChart
        if (chart instanceof LineChart) {
            chart.lookupAll(".chart-series-line").forEach(node -> {
                // Añadir efecto neón a las líneas
                Glow glow = new Glow();
                glow.setLevel(0.5);
                node.setEffect(glow);
            });
        }
    }

    /**
     * Aplica estilo a las tablas
     */
    public static void applyTableStyle(TableView<?> table) {
        // Estilo general de la tabla
        table.setStyle(
                "-fx-background-color: rgba(30, 30, 40, 0.7); " +
                        "-fx-background-radius: 5px; " +
                        "-fx-border-color: rgba(80, 80, 120, 0.5); " +
                        "-fx-border-radius: 5px; " +
                        "-fx-border-width: 1px;"
        );

        // Cabeceras de columnas
        table.lookupAll(".column-header").forEach(node ->
                node.setStyle(
                        "-fx-background-color: rgba(60, 60, 80, 0.7); " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 8px; " +
                                "-fx-border-color: transparent;"
                )
        );

        // Celdas
        table.lookupAll(".table-row-cell").forEach(node ->
                node.setStyle(
                        "-fx-background-color: rgba(40, 40, 50, 0.5); " +
                                "-fx-text-fill: white; " +
                                "-fx-table-cell-border-color: transparent;"
                )
        );

        // CSS para filas alternadas y efectos de hover
        String css = ".table-row-cell:odd { -fx-background-color: rgba(50, 50, 60, 0.5); -fx-text-fill: white; }" +
                ".table-row-cell:even { -fx-background-color: rgba(40, 40, 50, 0.5); -fx-text-fill: white; }" +
                ".table-row-cell:hover { -fx-background-color: rgba(80, 70, 120, 0.5); -fx-text-fill: white; }";

        table.getStylesheets().add(createCSS(css));
    }

    /**
     * Método auxiliar para crear un stylesheet CSS desde una cadena
     */
    private static String createCSS(String css) {
        try {
            File temp = File.createTempFile("stylesTemp", ".css");
            temp.deleteOnExit();
            try (PrintWriter out = new PrintWriter(temp)) {
                out.println(css);
            }
            return temp.toURI().toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}