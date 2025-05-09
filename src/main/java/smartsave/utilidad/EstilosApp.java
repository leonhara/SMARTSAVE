package smartsave.utilidad;

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

/**
 * Clase de estilos mejorada con tema oscuro y acentos neón
 */
public class EstilosApp {

    // Colores del tema oscuro con acentos neón
    private static final Color FONDO_OSCURO = Color.rgb(15, 15, 25, 1.0);
    private static final Color PANEL_OSCURO = Color.rgb(25, 25, 35, 1.0);
    private static final Color NEON_ROSA = Color.rgb(255, 0, 255, 1.0);
    private static final Color NEON_AZUL = Color.rgb(80, 145, 255, 1.0);
    private static final Color NEON_MORADO = Color.rgb(160, 100, 255, 1.0);
    private static final Color TEXTO_CLARO = Color.rgb(230, 230, 250, 1.0);

    // Gradiente neón para botones y acentos
    private static final LinearGradient GRADIENTE_NEON = crearGradienteNeon();

    // Fuente moderna
    private static final String FUENTE_MODERNA = "Segoe UI";

    /**
     * Crea el gradiente neón principal
     */
    private static LinearGradient crearGradienteNeon() {
        Stop[] stops = new Stop[] {
                new Stop(0, NEON_ROSA),
                new Stop(1, NEON_AZUL)
        };
        return new LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE, stops);
    }

    /**
     * Aplica el estilo al panel principal
     */
    public static void aplicarEstiloPanelPrincipal(BorderPane panel) {
        // Fondo oscuro sólido
        panel.setBackground(new Background(new BackgroundFill(
                FONDO_OSCURO,
                new CornerRadii(0),
                null
        )));

        // Sin borde
        panel.setBorder(Border.EMPTY);

        // Efecto sutil de sombra
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.rgb(0, 0, 0, 0.7));
        sombra.setRadius(15);
        sombra.setSpread(0.1);
        panel.setEffect(sombra);
    }

    /**
     * Aplica estilo a la barra de título
     */
    public static void aplicarEstiloBarraTitulo(HBox barraTitulo) {
        // Fondo oscuro para la barra de título
        barraTitulo.setBackground(new Background(new BackgroundFill(
                PANEL_OSCURO,
                new CornerRadii(0),
                null
        )));

        // Sin borde
        barraTitulo.setBorder(Border.EMPTY);

        // Tamaño y padding
        barraTitulo.setPrefHeight(40);
        barraTitulo.setMinHeight(40);
        barraTitulo.setMaxHeight(40);
        barraTitulo.setPadding(new javafx.geometry.Insets(5, 10, 5, 15));

        // Configuración de los hijos
        for (javafx.scene.Node nodo : barraTitulo.getChildren()) {
            if (nodo instanceof Label) {
                HBox.setHgrow(nodo, javafx.scene.layout.Priority.ALWAYS);
                ((Label) nodo).setMaxWidth(Double.MAX_VALUE);
                ((Label) nodo).setTextFill(TEXTO_CLARO);
                ((Label) nodo).setFont(Font.font(FUENTE_MODERNA, FontWeight.BOLD, 14));

                // Efecto de brillo sutil
                Glow brillo = new Glow();
                brillo.setLevel(0.3);
                ((Label) nodo).setEffect(brillo);
            } else if (nodo instanceof HBox) {
                HBox.setHgrow(nodo, javafx.scene.layout.Priority.NEVER);
                ((HBox) nodo).setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            }
        }
    }

    /**
     * Aplica estilo al panel de contenido
     */
    public static void aplicarEstiloPanelContenido(VBox panelContenido) {
        // Fondo oscuro para el panel
        panelContenido.setBackground(new Background(new BackgroundFill(
                PANEL_OSCURO,
                new CornerRadii(0),
                null
        )));

        // Sin borde
        panelContenido.setBorder(Border.EMPTY);

        // Padding para el contenido
        panelContenido.setPadding(new javafx.geometry.Insets(25));

        // Espaciado entre elementos
        panelContenido.setSpacing(15);
    }

    /**
     * Aplica estilo a los botones de la ventana (minimizar, maximizar, cerrar)
     */
    public static void aplicarEstiloBotonVentana(Button boton) {
        boton.setFont(Font.font(FUENTE_MODERNA, FontWeight.BOLD, 11));
        boton.setTextFill(TEXTO_CLARO);

        // Estilo base
        boton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 2 10; " +
                        "-fx-cursor: hand;"
        );

        // Efectos de hover
        boton.setOnMouseEntered(e -> {
            if (boton.getText().equals("✕")) {
                // Rojo para el botón de cerrar
                boton.setStyle(
                        "-fx-background-color: rgba(255, 50, 50, 0.7); " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 2 10; " +
                                "-fx-cursor: hand;"
                );
            } else {
                // Normal para los otros botones
                boton.setStyle(
                        "-fx-background-color: rgba(255, 255, 255, 0.1); " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 2 10; " +
                                "-fx-cursor: hand;"
                );
            }
        });

        boton.setOnMouseExited(e -> {
            boton.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 2 10; " +
                            "-fx-cursor: hand;"
            );
        });

        // Tamaño consistente
        boton.setMinSize(30, 25);
        boton.setPrefSize(30, 25);
    }

    /**
     * Aplica estilo al título principal
     */
    public static void aplicarEstiloTitulo(Label etiqueta) {
        etiqueta.setFont(Font.font(FUENTE_MODERNA, FontWeight.BOLD, 24));
        etiqueta.setTextFill(TEXTO_CLARO);

        // Efecto de brillo neón
        DropShadow brillo = new DropShadow();
        brillo.setColor(NEON_MORADO);
        brillo.setRadius(10);
        brillo.setSpread(0.2);
        etiqueta.setEffect(brillo);
    }

    /**
     * Aplica estilo al subtítulo
     */
    public static void aplicarEstiloSubtitulo(Label etiqueta) {
        etiqueta.setFont(Font.font(FUENTE_MODERNA, FontWeight.NORMAL, 14));
        etiqueta.setTextFill(TEXTO_CLARO);
    }

    /**
     * Aplica estilo a los botones principales
     */
    public static void aplicarEstiloBotonPrimario(Button boton) {
        boton.setFont(Font.font(FUENTE_MODERNA, FontWeight.NORMAL, 13));

        // Fondo con gradiente neón
        Stop[] stops = new Stop[] {
                new Stop(0, NEON_ROSA),
                new Stop(1, NEON_AZUL)
        };
        LinearGradient gradiente = new LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE, stops);

        boton.setBackground(new Background(new BackgroundFill(
                gradiente,
                new CornerRadii(5),
                null
        )));

        // Sin borde
        boton.setBorder(Border.EMPTY);

        // Texto blanco
        boton.setTextFill(Color.WHITE);

        // Sombra para efecto 3D
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.rgb(180, 70, 255, 0.7));
        sombra.setRadius(10);
        sombra.setSpread(0.1);
        boton.setEffect(sombra);

        // Padding
        boton.setPadding(new javafx.geometry.Insets(8, 20, 8, 20));

        // Efectos interactivos
        boton.setOnMouseEntered(e -> {
            // Aumentar brillo
            DropShadow sombraHover = new DropShadow();
            sombraHover.setColor(Color.rgb(200, 100, 255, 0.9));
            sombraHover.setRadius(15);
            sombraHover.setSpread(0.2);
            boton.setEffect(sombraHover);
        });

        boton.setOnMouseExited(e -> {
            // Restaurar sombra original
            DropShadow sombraOriginal = new DropShadow();
            sombraOriginal.setColor(Color.rgb(180, 70, 255, 0.7));
            sombraOriginal.setRadius(10);
            sombraOriginal.setSpread(0.1);
            boton.setEffect(sombraOriginal);
        });

        boton.setOnMousePressed(e -> {
            // Efecto de presión
            DropShadow sombraPresionada = new DropShadow();
            sombraPresionada.setColor(Color.rgb(180, 70, 255, 0.5));
            sombraPresionada.setRadius(5);
            sombraPresionada.setSpread(0.05);
            boton.setEffect(sombraPresionada);
        });

        boton.setOnMouseReleased(e -> {
            if (boton.isHover()) {
                // Restaurar efecto hover
                DropShadow sombraHover = new DropShadow();
                sombraHover.setColor(Color.rgb(200, 100, 255, 0.9));
                sombraHover.setRadius(15);
                sombraHover.setSpread(0.2);
                boton.setEffect(sombraHover);
            } else {
                // Restaurar sombra original
                DropShadow sombraOriginal = new DropShadow();
                sombraOriginal.setColor(Color.rgb(180, 70, 255, 0.7));
                sombraOriginal.setRadius(10);
                sombraOriginal.setSpread(0.1);
                boton.setEffect(sombraOriginal);
            }
        });
    }

    /**
     * Aplica estilo a los campos de texto
     */
    public static void aplicarEstiloCampoTexto(TextField campoTexto) {
        campoTexto.setFont(Font.font(FUENTE_MODERNA, 13));

        // Fondo oscuro
        campoTexto.setBackground(new Background(new BackgroundFill(
                Color.rgb(35, 35, 45, 1.0),
                new CornerRadii(5),
                null
        )));

        // Borde neón sutil
        campoTexto.setBorder(new Border(new BorderStroke(
                Color.rgb(120, 100, 200, 0.5),
                BorderStrokeStyle.SOLID,
                new CornerRadii(5),
                new BorderWidths(1)
        )));

        // Texto claro
        campoTexto.setStyle("-fx-text-fill: rgb(230, 230, 250);");

        // Padding
        campoTexto.setPadding(new javafx.geometry.Insets(8, 10, 8, 10));

        // Efecto focus
        campoTexto.focusedProperty().addListener((observable, valorAnterior, valorNuevo) -> {
            if (valorNuevo) {
                // Cuando tiene foco - borde neón más brillante
                campoTexto.setBorder(new Border(new BorderStroke(
                        NEON_MORADO,
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1.5)
                )));

                // Brillo interior
                DropShadow sombraFoco = new DropShadow();
                sombraFoco.setColor(Color.rgb(160, 100, 255, 0.4));
                sombraFoco.setRadius(10);
                sombraFoco.setSpread(0.1);
                campoTexto.setEffect(sombraFoco);
            } else {
                // Cuando pierde el foco - borde original
                campoTexto.setBorder(new Border(new BorderStroke(
                        Color.rgb(120, 100, 200, 0.5),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1)
                )));

                // Sin efecto
                campoTexto.setEffect(null);
            }
        });
    }

    /**
     * Aplica estilo al campo de contraseña (igual que TextField pero para PasswordField)
     */
    public static void aplicarEstiloCampoContraseña(PasswordField campoContraseña) {
        campoContraseña.setFont(Font.font(FUENTE_MODERNA, 13));

        // Fondo oscuro
        campoContraseña.setBackground(new Background(new BackgroundFill(
                Color.rgb(35, 35, 45, 1.0),
                new CornerRadii(5),
                null
        )));

        // Borde neón sutil
        campoContraseña.setBorder(new Border(new BorderStroke(
                Color.rgb(120, 100, 200, 0.5),
                BorderStrokeStyle.SOLID,
                new CornerRadii(5),
                new BorderWidths(1)
        )));

        // Texto claro
        campoContraseña.setStyle("-fx-text-fill: rgb(230, 230, 250);");

        // Padding
        campoContraseña.setPadding(new javafx.geometry.Insets(8, 10, 8, 10));

        // Efecto focus
        campoContraseña.focusedProperty().addListener((observable, valorAnterior, valorNuevo) -> {
            if (valorNuevo) {
                // Cuando tiene foco - borde neón más brillante
                campoContraseña.setBorder(new Border(new BorderStroke(
                        NEON_MORADO,
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1.5)
                )));

                // Brillo interior
                DropShadow sombraFoco = new DropShadow();
                sombraFoco.setColor(Color.rgb(160, 100, 255, 0.4));
                sombraFoco.setRadius(10);
                sombraFoco.setSpread(0.1);
                campoContraseña.setEffect(sombraFoco);
            } else {
                // Cuando pierde el foco - borde original
                campoContraseña.setBorder(new Border(new BorderStroke(
                        Color.rgb(120, 100, 200, 0.5),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1)
                )));

                // Sin efecto
                campoContraseña.setEffect(null);
            }
        });
    }

    /**
     * Aplica estilo a los hipervínculos
     */
    public static void aplicarEstiloHipervinculo(Hyperlink hipervinculo) {
        hipervinculo.setFont(Font.font(FUENTE_MODERNA, 13));
        hipervinculo.setTextFill(NEON_ROSA);
        hipervinculo.setBorder(Border.EMPTY);
        hipervinculo.setUnderline(false);

        // Efecto al pasar el ratón
        hipervinculo.setOnMouseEntered(e -> {
            hipervinculo.setTextFill(NEON_AZUL);
            hipervinculo.setUnderline(true);

            // Efecto de brillo
            Glow brillo = new Glow();
            brillo.setLevel(0.5);
            hipervinculo.setEffect(brillo);
        });

        hipervinculo.setOnMouseExited(e -> {
            hipervinculo.setTextFill(NEON_ROSA);
            hipervinculo.setUnderline(false);
            hipervinculo.setEffect(null);
        });
    }

    /**
     * Aplica estilo a etiquetas normales
     */
    public static void aplicarEstiloEtiqueta(Label etiqueta) {
        etiqueta.setFont(Font.font(FUENTE_MODERNA, 13));
        etiqueta.setTextFill(TEXTO_CLARO);
    }

    /**
     * Aplica estilo al menú lateral
     */
    public static void aplicarEstiloMenuLateral(VBox menuLateral) {
        // Fondo oscuro para el menú
        menuLateral.setBackground(new Background(new BackgroundFill(
                Color.rgb(20, 20, 30, 1.0),
                new CornerRadii(0),
                null
        )));

        // Sin borde
        menuLateral.setBorder(Border.EMPTY);

        // Efecto sutil de sombra interna
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.rgb(0, 0, 0, 0.5));
        sombra.setRadius(10);
        sombra.setSpread(0);
        menuLateral.setEffect(sombra);
    }

    /**
     * Aplica estilo a los botones de navegación
     */
    public static void aplicarEstiloBotonNavegacion(Button boton) {
        boton.setFont(Font.font(FUENTE_MODERNA, FontWeight.NORMAL, 13));

        // Fondo transparente
        boton.setBackground(new Background(new BackgroundFill(
                Color.TRANSPARENT,
                new CornerRadii(5),
                null
        )));

        // Borde sutil
        boton.setBorder(new Border(new BorderStroke(
                Color.rgb(80, 80, 120, 0.3),
                BorderStrokeStyle.SOLID,
                new CornerRadii(5),
                new BorderWidths(1)
        )));

        // Texto claro
        boton.setTextFill(Color.rgb(200, 200, 220, 1.0));

        // Padding
        boton.setPadding(new javafx.geometry.Insets(8, 15, 8, 15));

        // Alineación izquierda con el icono
        boton.setAlignment(Pos.CENTER_LEFT);
        boton.setGraphicTextGap(10);

        // Añadir clase CSS para poder cambiar estilos fácilmente
        boton.getStyleClass().add("nav-button");

        // Efectos de hover
        boton.setOnMouseEntered(e -> {
            boton.setBackground(new Background(new BackgroundFill(
                    Color.rgb(60, 60, 90, 0.3),
                    new CornerRadii(5),
                    null
            )));

            boton.setBorder(new Border(new BorderStroke(
                    Color.rgb(150, 100, 255, 0.5),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(5),
                    new BorderWidths(1)
            )));

            boton.setTextFill(Color.rgb(255, 255, 255, 1.0));
        });

        boton.setOnMouseExited(e -> {
            if (!boton.getStyleClass().contains("selected")) {
                boton.setBackground(new Background(new BackgroundFill(
                        Color.TRANSPARENT,
                        new CornerRadii(5),
                        null
                )));

                boton.setBorder(new Border(new BorderStroke(
                        Color.rgb(80, 80, 120, 0.3),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1)
                )));

                boton.setTextFill(Color.rgb(200, 200, 220, 1.0));
            }
        });

        // Efecto para botón seleccionado
        if (boton.getStyleClass().contains("selected")) {
            boton.setBackground(new Background(new BackgroundFill(
                    Color.rgb(100, 80, 180, 0.3),
                    new CornerRadii(5),
                    null
            )));

            boton.setBorder(new Border(new BorderStroke(
                    Color.rgb(255, 0, 255, 0.7),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(5),
                    new BorderWidths(1)
            )));

            boton.setTextFill(Color.rgb(255, 255, 255, 1.0));
        }
    }

    /**
     * Aplica estilo a los gráficos
     */
    public static void aplicarEstiloGrafico(Chart grafico) {
        // Fondo transparente
        grafico.setStyle("-fx-background-color: transparent;");

        // Color de texto claro
        grafico.lookupAll(".chart-title").forEach(nodo ->
                nodo.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;")
        );

        grafico.lookupAll(".axis-label").forEach(nodo ->
                nodo.setStyle("-fx-text-fill: rgb(200, 200, 220);")
        );

        grafico.lookupAll(".chart-legend").forEach(nodo ->
                nodo.setStyle("-fx-background-color: transparent; -fx-text-fill: white;")
        );

        grafico.lookupAll(".chart-legend-item").forEach(nodo ->
                nodo.setStyle("-fx-text-fill: white;")
        );

        // Estilos específicos para PieChart
        if (grafico instanceof PieChart) {
            ((PieChart) grafico).getData().forEach(dato -> {
                // Añadir efecto neón a las secciones
                Glow brillo = new Glow();
                brillo.setLevel(0.3);
                dato.getNode().setEffect(brillo);
            });
        }

        // Estilos específicos para LineChart
        if (grafico instanceof LineChart) {
            grafico.lookupAll(".chart-series-line").forEach(nodo -> {
                // Añadir efecto neón a las líneas
                Glow brillo = new Glow();
                brillo.setLevel(0.5);
                nodo.setEffect(brillo);
            });
        }
    }

    /**
     * Aplica estilo a las tablas
     */
    public static void aplicarEstiloTabla(TableView<?> tabla) {
        // Estilo general de la tabla
        tabla.setStyle(
                "-fx-background-color: rgba(30, 30, 40, 0.7); " +
                        "-fx-background-radius: 5px; " +
                        "-fx-border-color: rgba(80, 80, 120, 0.5); " +
                        "-fx-border-radius: 5px; " +
                        "-fx-border-width: 1px;"
        );

        // Cabeceras de columnas
        tabla.lookupAll(".column-header").forEach(nodo ->
                nodo.setStyle(
                        "-fx-background-color: rgba(60, 60, 80, 0.7); " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 8px; " +
                                "-fx-border-color: transparent;"
                )
        );

        // Celdas
        tabla.lookupAll(".table-row-cell").forEach(nodo ->
                nodo.setStyle(
                        "-fx-background-color: rgba(40, 40, 50, 0.5); " +
                                "-fx-text-fill: white; " +
                                "-fx-table-cell-border-color: transparent;"
                )
        );

        // CSS para filas alternadas y efectos de hover
        String css = ".table-row-cell:odd { -fx-background-color: rgba(50, 50, 60, 0.5); -fx-text-fill: white; }" +
                ".table-row-cell:even { -fx-background-color: rgba(40, 40, 50, 0.5); -fx-text-fill: white; }" +
                ".table-row-cell:hover { -fx-background-color: rgba(80, 70, 120, 0.5); -fx-text-fill: white; }";

        tabla.getStylesheets().add(crearCSS(css));
    }

    /**
     * Método auxiliar para crear un stylesheet CSS desde una cadena
     */
    private static String crearCSS(String css) {
        try {
            File temporal = File.createTempFile("estilosTemporal", ".css");
            temporal.deleteOnExit();
            try (PrintWriter salida = new PrintWriter(temporal)) {
                salida.println(css);
            }
            return temporal.toURI().toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}