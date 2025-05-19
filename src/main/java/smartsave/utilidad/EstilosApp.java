package smartsave.utilidad;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase de estilos mejorada con tema oscuro y acentos neón
 * Versión optimizada con sistema de caché y aplicación recursiva
 */
public class EstilosApp {

    // Colores del tema oscuro con acentos neón
    public static final Color FONDO_OSCURO = Color.rgb(15, 15, 25, 1.0);
    public static final Color PANEL_OSCURO = Color.rgb(25, 25, 35, 1.0);
    public static final Color NEON_ROSA = Color.rgb(255, 0, 255, 1.0);
    public static final Color NEON_AZUL = Color.rgb(80, 145, 255, 1.0);
    public static final Color NEON_MORADO = Color.rgb(160, 100, 255, 1.0);
    public static final Color TEXTO_CLARO = Color.rgb(230, 230, 250, 1.0);

    // Gradientes pre-computados
    private static final LinearGradient GRADIENTE_NEON = crearGradienteNeon();

    // Fuente moderna
    public static final String FUENTE_MODERNA = "Segoe UI";

    // Cache para estilos CSS creados dinámicamente
    private static final Map<String, String> cssCache = new HashMap<>();

    // Cache para efectos visuales (evita crear nuevas instancias repetidamente)
    private static final Map<String, Object> efectosCache = new HashMap<>();

    /**
     * Inicializa la cache de efectos comunes
     */
    static {
        // Pre-cargar efectos comunes
        efectosCache.put("glow-subtle", crearGlowEffect(0.3));
        efectosCache.put("glow-medium", crearGlowEffect(0.5));
        efectosCache.put("glow-strong", crearGlowEffect(0.7));
        efectosCache.put("shadow-subtle", crearShadowEffect(10, 0.1, Color.rgb(0, 0, 0, 0.5)));
        efectosCache.put("shadow-neon", crearShadowEffect(10, 0.1, Color.rgb(180, 70, 255, 0.7)));
        efectosCache.put("shadow-hover", crearShadowEffect(15, 0.2, Color.rgb(200, 100, 255, 0.9)));
        efectosCache.put("shadow-pressed", crearShadowEffect(5, 0.05, Color.rgb(180, 70, 255, 0.5)));
    }

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
     * Crea un efecto de brillo (Glow)
     */
    private static Glow crearGlowEffect(double nivel) {
        Glow glow = new Glow();
        glow.setLevel(nivel);
        return glow;
    }

    /**
     * Crea un efecto de sombra (DropShadow)
     */
    private static DropShadow crearShadowEffect(double radio, double propagacion, Color color) {
        DropShadow sombra = new DropShadow();
        sombra.setRadius(radio);
        sombra.setSpread(propagacion);
        sombra.setColor(color);
        return sombra;
    }

    /**
     * Aplica estilos a un componente raíz y todos sus hijos recursivamente
     * @param root El componente raíz a partir del cual aplicar estilos
     */
    public static void aplicarEstilosRecursivamente(Parent root) {
        aplicarEstiloSegunTipo(root);

        for (Node nodo : root.getChildrenUnmodifiable()) {
            aplicarEstiloSegunTipo(nodo);

            if (nodo instanceof Parent) {
                aplicarEstilosRecursivamente((Parent) nodo);
            }
        }
    }

    /**
     * Aplica el estilo adecuado según el tipo de componente
     * @param nodo El nodo al que aplicar estilos
     */
    public static void aplicarEstiloSegunTipo(Node nodo) {
        if (nodo instanceof BorderPane) {
            aplicarEstiloPanelPrincipal((BorderPane) nodo);
        } else if (nodo instanceof HBox && "titleBar".equals(nodo.getId())) {
            aplicarEstiloBarraTitulo((HBox) nodo);
        } else if (nodo instanceof VBox && "sideMenu".equals(nodo.getId())) {
            aplicarEstiloMenuLateral((VBox) nodo);
        } else if (nodo instanceof Button) {
            aplicarEstiloSegunBoton((Button) nodo);
        } else if (nodo instanceof TextField) {
            aplicarEstiloCampoTexto((TextField) nodo);
        } else if (nodo instanceof PasswordField) {
            aplicarEstiloCampoContraseña((PasswordField) nodo);
        } else if (nodo instanceof Label) {
            aplicarEstiloSegunLabel((Label) nodo);
        } else if (nodo instanceof Hyperlink) {
            aplicarEstiloHipervinculo((Hyperlink) nodo);
        } else if (nodo instanceof Chart) {
            aplicarEstiloGrafico((Chart) nodo);
        } else if (nodo instanceof TableView) {
            aplicarEstiloTabla((TableView<?>) nodo);
        } else if (nodo instanceof VBox && nodo.getParent() instanceof BorderPane) {
            aplicarEstiloPanelContenido((VBox) nodo);
        }
    }

    /**
     * Determina qué tipo de estilo aplicar a un botón según su contexto
     */
    private static void aplicarEstiloSegunBoton(Button boton) {
        Parent parent = boton.getParent();

        if (parent instanceof HBox && "titleBar".equals(parent.getId())) {
            aplicarEstiloBotonVentana(boton);
        } else if (parent instanceof VBox && "sideMenu".equals(parent.getId())) {
            aplicarEstiloBotonNavegacion(boton);
        } else {
            aplicarEstiloBotonPrimario(boton);
        }
    }

    /**
     * Determina qué tipo de estilo aplicar a una etiqueta según su contexto
     */
    private static void aplicarEstiloSegunLabel(Label etiqueta) {
        if ("titleLabel".equals(etiqueta.getId())) {
            aplicarEstiloTitulo(etiqueta);
        } else if ("subtitleLabel".equals(etiqueta.getId())) {
            aplicarEstiloSubtitulo(etiqueta);
        } else {
            aplicarEstiloEtiqueta(etiqueta);
        }
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
        panel.setEffect((DropShadow) efectosCache.get("shadow-subtle"));
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
        barraTitulo.setPadding(new Insets(5, 10, 5, 15));

        // Configuración de los hijos
        for (Node nodo : barraTitulo.getChildren()) {
            if (nodo instanceof Label) {
                HBox.setHgrow(nodo, Priority.ALWAYS);
                ((Label) nodo).setMaxWidth(Double.MAX_VALUE);
                ((Label) nodo).setTextFill(TEXTO_CLARO);
                ((Label) nodo).setFont(Font.font(FUENTE_MODERNA, FontWeight.BOLD, 14));

                // Efecto de brillo sutil
                nodo.setEffect((Glow) efectosCache.get("glow-subtle"));
            } else if (nodo instanceof HBox) {
                HBox.setHgrow(nodo, Priority.NEVER);
                ((HBox) nodo).setAlignment(Pos.CENTER_RIGHT);
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
        panelContenido.setPadding(new Insets(25));

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
        boton.setBackground(new Background(new BackgroundFill(
                GRADIENTE_NEON,
                new CornerRadii(5),
                null
        )));

        // Sin borde
        boton.setBorder(Border.EMPTY);

        // Texto blanco
        boton.setTextFill(Color.WHITE);

        // Efecto de sombra
        boton.setEffect((DropShadow) efectosCache.get("shadow-neon"));

        // Padding
        boton.setPadding(new Insets(8, 20, 8, 20));

        // Efectos interactivos
        boton.setOnMouseEntered(e -> {
            // Aumentar brillo al pasar el ratón
            boton.setEffect((DropShadow) efectosCache.get("shadow-hover"));
        });

        boton.setOnMouseExited(e -> {
            // Restaurar efecto original
            boton.setEffect((DropShadow) efectosCache.get("shadow-neon"));
        });

        boton.setOnMousePressed(e -> {
            // Efecto al presionar
            boton.setEffect((DropShadow) efectosCache.get("shadow-pressed"));
        });

        boton.setOnMouseReleased(e -> {
            if (boton.isHover()) {
                // Restaurar efecto hover
                boton.setEffect((DropShadow) efectosCache.get("shadow-hover"));
            } else {
                // Restaurar efecto original
                boton.setEffect((DropShadow) efectosCache.get("shadow-neon"));
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
        campoTexto.setPadding(new Insets(8, 10, 8, 10));

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
     * Aplica estilo al campo de contraseña
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
        campoContraseña.setPadding(new Insets(8, 10, 8, 10));

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
            hipervinculo.setEffect((Glow) efectosCache.get("glow-medium"));
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
        menuLateral.setEffect((DropShadow) efectosCache.get("shadow-subtle"));
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
        boton.setPadding(new Insets(8, 15, 8, 15));

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
     * Actualiza el estilo del botón de navegación cuando se selecciona
     */
    public static void actualizarEstiloBotonSeleccionado(Button boton, boolean seleccionado) {
        if (seleccionado) {
            boton.getStyleClass().add("selected");
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
        } else {
            boton.getStyleClass().remove("selected");
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
                dato.getNode().setEffect((Glow) efectosCache.get("glow-subtle"));
            });
        }

        // Estilos específicos para LineChart
        if (grafico instanceof LineChart) {
            grafico.lookupAll(".chart-series-line").forEach(nodo -> {
                // Añadir efecto neón a las líneas
                nodo.setEffect((Glow) efectosCache.get("glow-medium"));
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
        String cssClave = "tabla-estilo";
        String css = ".table-row-cell:odd { -fx-background-color: rgba(50, 50, 60, 0.5); -fx-text-fill: white; }" +
                ".table-row-cell:even { -fx-background-color: rgba(40, 40, 50, 0.5); -fx-text-fill: white; }" +
                ".table-row-cell:hover { -fx-background-color: rgba(80, 70, 120, 0.5); -fx-text-fill: white; }";

        // Usa la caché para no crear múltiples archivos CSS
        if (!cssCache.containsKey(cssClave)) {
            cssCache.put(cssClave, crearCSS(css));
        }

        String cssUrl = cssCache.get(cssClave);
        if (!tabla.getStylesheets().contains(cssUrl)) {
            tabla.getStylesheets().add(cssUrl);
        }
    }

    /**
     * Aplica estilos a un ComboBox
     */
    public static void aplicarEstiloComboBox(ComboBox<?> combo) {
        combo.setStyle(
                "-fx-background-color: rgba(35, 35, 45, 1.0); " +
                        "-fx-border-color: rgba(120, 100, 200, 0.5); " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-text-fill: white;"
        );

        // Añadir CSS para estilizar la lista desplegable
        String cssClave = "combo-estilo";
        String css = ".combo-box .list-cell { -fx-text-fill: white; -fx-background-color: rgba(35, 35, 45, 1.0); }" +
                ".combo-box .list-view { -fx-background-color: rgba(25, 25, 35, 0.95); -fx-border-color: rgba(160, 100, 255, 0.7); }" +
                ".combo-box .list-cell:hover { -fx-background-color: rgba(60, 60, 90, 0.7); }";

        if (!cssCache.containsKey(cssClave)) {
            cssCache.put(cssClave, crearCSS(css));
        }

        String cssUrl = cssCache.get(cssClave);
        if (!combo.getStylesheets().contains(cssUrl)) {
            combo.getStylesheets().add(cssUrl);
        }
    }

    /**
     * Aplica estilos a un DatePicker
     */
    public static void aplicarEstiloDatePicker(DatePicker datePicker) {
        datePicker.setStyle(
                "-fx-background-color: rgba(35, 35, 45, 1.0); " +
                        "-fx-border-color: rgba(120, 100, 200, 0.5); " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-text-fill: white;"
        );

        // Añadir CSS para estilizar el calendario
        String cssClave = "date-picker-estilo";
        String css = ".date-picker-popup { -fx-background-color: rgba(25, 25, 35, 0.95); -fx-border-color: rgba(160, 100, 255, 0.7); }" +
                ".date-picker-popup .month-year-pane { -fx-background-color: rgba(60, 60, 90, 0.7); }" +
                ".date-picker-popup .day-name-cell { -fx-text-fill: white; }" +
                ".date-picker-popup .day-cell { -fx-text-fill: white; -fx-background-color: rgba(35, 35, 45, 1.0); }" +
                ".date-picker-popup .day-cell:hover { -fx-background-color: rgba(60, 60, 90, 0.7); }" +
                ".date-picker-popup .selected { -fx-background-color: rgba(255, 0, 255, 0.3); -fx-text-fill: white; }";

        if (!cssCache.containsKey(cssClave)) {
            cssCache.put(cssClave, crearCSS(css));
        }

        String cssUrl = cssCache.get(cssClave);
        if (!datePicker.getStylesheets().contains(cssUrl)) {
            datePicker.getStylesheets().add(cssUrl);
        }
    }

    /**
     * Aplica estilos a un TextArea
     */
    public static void aplicarEstiloTextArea(TextArea textArea) {
        textArea.setStyle(
                "-fx-background-color: rgba(35, 35, 45, 1.0); " +
                        "-fx-border-color: rgba(120, 100, 200, 0.5); " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-text-fill: rgb(230, 230, 250);"
        );

        // Añadir CSS para estilizar el scrollbar
        String cssClave = "text-area-estilo";
        String css = ".text-area .scroll-bar:vertical { -fx-pref-width: 12px; -fx-background-color: transparent; }" +
                ".text-area .scroll-bar:vertical .track { -fx-background-color: rgba(25, 25, 35, 0.9); -fx-border-color: rgba(80, 80, 120, 0.5); }" +
                ".text-area .scroll-bar:vertical .thumb { -fx-background-color: rgba(120, 100, 200, 0.5); }";

        if (!cssCache.containsKey(cssClave)) {
            cssCache.put(cssClave, crearCSS(css));
        }

        String cssUrl = cssCache.get(cssClave);
        if (!textArea.getStylesheets().contains(cssUrl)) {
            textArea.getStylesheets().add(cssUrl);
        }
    }

    /**
     * Aplica estilos a un ScrollPane
     */
    public static void aplicarEstiloScrollPane(ScrollPane scrollPane) {
        scrollPane.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-background: transparent; " +
                        "-fx-border-color: transparent;"
        );

        // Añadir CSS para estilizar el scrollbar
        String cssClave = "scroll-pane-estilo";
        String css = ".scroll-pane .scroll-bar:vertical { -fx-pref-width: 12px; -fx-background-color: transparent; }" +
                ".scroll-pane .scroll-bar:vertical .track { -fx-background-color: rgba(25, 25, 35, 0.9); -fx-border-color: rgba(80, 80, 120, 0.5); }" +
                ".scroll-pane .scroll-bar:vertical .thumb { -fx-background-color: rgba(120, 100, 200, 0.5); }" +
                ".scroll-pane .corner { -fx-background-color: transparent; }";

        if (!cssCache.containsKey(cssClave)) {
            cssCache.put(cssClave, crearCSS(css));
        }

        String cssUrl = cssCache.get(cssClave);
        if (!scrollPane.getStylesheets().contains(cssUrl)) {
            scrollPane.getStylesheets().add(cssUrl);
        }
    }

    /**
     * Aplica estilos a una ventana de diálogo
     */
    public static void aplicarEstiloDialogPane(DialogPane dialogPane) {
        // Fondo oscuro
        dialogPane.setStyle(
                "-fx-background-color: rgba(25, 25, 35, 0.95); " +
                        "-fx-text-fill: white; " +
                        "-fx-border-color: rgba(255, 0, 255, 0.7); " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px;"
        );

        // Etiquetas con texto claro
        dialogPane.lookupAll(".label").forEach(nodo ->
                nodo.setStyle("-fx-text-fill: white;")
        );

        // Botones con estilo neón
        dialogPane.lookupAll(".button").forEach(nodo -> {
            nodo.setStyle(
                    "-fx-background-color: rgba(40, 40, 50, 1.0); " +
                            "-fx-text-fill: white; " +
                            "-fx-border-color: rgba(160, 100, 255, 0.8); " +
                            "-fx-border-width: 1px; " +
                            "-fx-border-radius: 5px; " +
                            "-fx-background-radius: 5px; " +
                            "-fx-cursor: hand;"
            );

            // Eventos de hover
            nodo.setOnMouseEntered(e ->
                    nodo.setStyle(
                            "-fx-background-color: rgba(60, 60, 80, 1.0); " +
                                    "-fx-text-fill: white; " +
                                    "-fx-border-color: rgba(255, 0, 255, 0.8); " +
                                    "-fx-border-width: 1px; " +
                                    "-fx-border-radius: 5px; " +
                                    "-fx-background-radius: 5px; " +
                                    "-fx-cursor: hand;"
                    )
            );

            nodo.setOnMouseExited(e ->
                    nodo.setStyle(
                            "-fx-background-color: rgba(40, 40, 50, 1.0); " +
                                    "-fx-text-fill: white; " +
                                    "-fx-border-color: rgba(160, 100, 255, 0.8); " +
                                    "-fx-border-width: 1px; " +
                                    "-fx-border-radius: 5px; " +
                                    "-fx-background-radius: 5px; " +
                                    "-fx-cursor: hand;"
                    )
            );
        });

        // Sombra para todo el diálogo
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.rgb(0, 0, 0, 0.7));
        sombra.setRadius(20);
        sombra.setSpread(0.1);
        dialogPane.setEffect(sombra);
    }

    /**
     * Aplica estilos específicos a un panel de tarjeta
     */
    public static void aplicarEstiloTarjeta(Pane tarjeta) {
        // Fondo oscuro con borde suave
        tarjeta.setBackground(new Background(new BackgroundFill(
                Color.rgb(35, 35, 50, 0.85),
                new CornerRadii(10),
                null
        )));

        tarjeta.setBorder(new Border(new BorderStroke(
                Color.rgb(100, 100, 200, 0.5),
                BorderStrokeStyle.SOLID,
                new CornerRadii(10),
                new BorderWidths(1)
        )));

        // Efecto de sombra
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.rgb(0, 0, 0, 0.5));
        sombra.setRadius(10);
        sombra.setSpread(0.05);
        tarjeta.setEffect(sombra);

        // Padding
        tarjeta.setPadding(new Insets(15));
    }

    /**
     * Método auxiliar para crear un stylesheet CSS desde una cadena
     * Versión mejorada que reutiliza los archivos CSS creados
     */
    private static String crearCSS(String css) {
        try {
            File temporal = File.createTempFile("estilosTemp", ".css");
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

    /**
     * Limpia la caché de CSS (útil para pruebas y cuando se modifica el tema)
     */
    public static void limpiarCacheCSS() {
        cssCache.clear();
    }

    /**
     * Crea un estilo personalizado de borde neón
     * @param color Color base del borde
     * @param intensidad Intensidad del efecto (0.0-1.0)
     * @param radio Radio de las esquinas del borde
     */
    public static Border crearBordeNeon(Color color, double intensidad, double radio) {
        return new Border(new BorderStroke(
                color,
                BorderStrokeStyle.SOLID,
                new CornerRadii(radio),
                new BorderWidths(1.0)
        ));
    }

    /**
     * Crea un estilo personalizado de fondo con gradiente
     * @param colorInicio Color inicial del gradiente
     * @param colorFin Color final del gradiente
     * @param radio Radio de las esquinas del fondo
     */
    public static Background crearFondoGradiente(Color colorInicio, Color colorFin, double radio) {
        Stop[] stops = new Stop[] {
                new Stop(0, colorInicio),
                new Stop(1, colorFin)
        };
        LinearGradient gradiente = new LinearGradient(0, 0, 1, 1, true,
                javafx.scene.paint.CycleMethod.NO_CYCLE, stops);

        return new Background(new BackgroundFill(
                gradiente,
                new CornerRadii(radio),
                null
        ));
    }

    /**
     * Genera un efecto de resplandor neón personalizado
     * @param color Color base del resplandor
     * @param intensidad Intensidad del efecto (0.0-1.0)
     */
    public static DropShadow crearResplandorNeon(Color color, double intensidad) {
        DropShadow resplandor = new DropShadow();
        resplandor.setColor(color);
        resplandor.setRadius(15 * intensidad);
        resplandor.setSpread(0.2 * intensidad);
        return resplandor;
    }
}