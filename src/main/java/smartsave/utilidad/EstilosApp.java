package smartsave.utilidad;

import javafx.application.Platform;
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

public class EstilosApp {

    //Este será el tema de mi app con colores de tema oscuro con cosas de neon
    public static final Color FONDO_OSCURO = Color.rgb(15, 15, 25, 1.0);
    public static final Color PANEL_OSCURO = Color.rgb(25, 25, 35, 1.0);
    public static final Color NEON_ROSA = Color.rgb(255, 0, 255, 1.0);
    public static final Color NEON_AZUL = Color.rgb(80, 145, 255, 1.0);
    public static final Color NEON_MORADO = Color.rgb(160, 100, 255, 1.0);
    public static final Color TEXTO_CLARO = Color.rgb(230, 230, 250, 1.0);
    public static final Color FONDO_CLARO_CONTROLES = Color.rgb(45, 45, 60, 0.95);
    public static final Color BORDE_FOCO_CONTROLES = NEON_MORADO;
    public static final Color BORDE_SUTIL_CONTROLES = Color.rgb(120, 100, 200, 0.5);

    private static final LinearGradient GRADIENTE_NEON = crearGradienteNeon();

    public static final String FUENTE_MODERNA = "Segoe UI";

    private static final Map<String, String> cssCache = new HashMap<>();

    private static final Map<String, Object> efectosCache = new HashMap<>();

    static {
        efectosCache.put("glow-subtle", crearGlowEffect(0.3));
        efectosCache.put("glow-medium", crearGlowEffect(0.5));
        efectosCache.put("glow-strong", crearGlowEffect(0.7));
        efectosCache.put("shadow-subtle", crearShadowEffect(10, 0.1, Color.rgb(0, 0, 0, 0.5)));
        efectosCache.put("shadow-neon", crearShadowEffect(10, 0.1, Color.rgb(180, 70, 255, 0.7)));
        efectosCache.put("shadow-hover", crearShadowEffect(15, 0.2, Color.rgb(200, 100, 255, 0.9)));
        efectosCache.put("shadow-pressed", crearShadowEffect(5, 0.05, Color.rgb(180, 70, 255, 0.5)));
    }

    private static LinearGradient crearGradienteNeon() {
        Stop[] stops = new Stop[] {
                new Stop(0, NEON_ROSA),
                new Stop(1, NEON_AZUL)
        };
        return new LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE, stops);
    }

    private static Glow crearGlowEffect(double nivel) {
        Glow glow = new Glow();
        glow.setLevel(nivel);
        return glow;
    }

    private static DropShadow crearShadowEffect(double radio, double propagacion, Color color) {
        DropShadow sombra = new DropShadow();
        sombra.setRadius(radio);
        sombra.setSpread(propagacion);
        sombra.setColor(color);
        return sombra;
    }

    public static void aplicarEstilosRecursivamente(Parent root) {
        aplicarEstiloSegunTipo(root);

        for (Node nodo : root.getChildrenUnmodifiable()) {
            aplicarEstiloSegunTipo(nodo);

            if (nodo instanceof Parent) {
                aplicarEstilosRecursivamente((Parent) nodo);
            }
        }
    }

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
            aplicarEstiloCampoContrasena((PasswordField) nodo);
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

    private static void aplicarEstiloSegunLabel(Label etiqueta) {
        if ("titleLabel".equals(etiqueta.getId())) {
            aplicarEstiloTitulo(etiqueta);
        } else if ("subtitleLabel".equals(etiqueta.getId())) {
            aplicarEstiloSubtitulo(etiqueta);
        } else {
            aplicarEstiloEtiqueta(etiqueta);
        }
    }

    public static void aplicarEstiloPanelPrincipal(BorderPane panel) {
        panel.setBackground(new Background(new BackgroundFill(
                FONDO_OSCURO,
                new CornerRadii(0),
                null
        )));

        panel.setBorder(Border.EMPTY);

        panel.setEffect((DropShadow) efectosCache.get("shadow-subtle"));
    }

    public static void aplicarEstiloBarraTitulo(HBox barraTitulo) {
        barraTitulo.setBackground(new Background(new BackgroundFill(
                PANEL_OSCURO,
                new CornerRadii(0),
                null
        )));

        barraTitulo.setBorder(Border.EMPTY);

        barraTitulo.setPrefHeight(40);
        barraTitulo.setMinHeight(40);
        barraTitulo.setMaxHeight(40);
        barraTitulo.setPadding(new Insets(5, 10, 5, 15));

        for (Node nodo : barraTitulo.getChildren()) {
            if (nodo instanceof Label) {
                HBox.setHgrow(nodo, Priority.ALWAYS);
                ((Label) nodo).setMaxWidth(Double.MAX_VALUE);
                ((Label) nodo).setTextFill(TEXTO_CLARO);
                ((Label) nodo).setFont(Font.font(FUENTE_MODERNA, FontWeight.BOLD, 14));

                nodo.setEffect((Glow) efectosCache.get("glow-subtle"));
            } else if (nodo instanceof HBox) {
                HBox.setHgrow(nodo, Priority.NEVER);
                ((HBox) nodo).setAlignment(Pos.CENTER_RIGHT);
            }
        }
    }

    public static void aplicarEstiloPanelContenido(VBox panelContenido) {
        panelContenido.setBackground(new Background(new BackgroundFill(
                PANEL_OSCURO,
                new CornerRadii(0),
                null
        )));

        panelContenido.setBorder(Border.EMPTY);

        panelContenido.setPadding(new Insets(25));

        panelContenido.setSpacing(15);
    }

    public static void aplicarEstiloBotonVentana(Button boton) {
        boton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Arial Unicode MS', 'Segoe UI Symbol'; " +
                        "-fx-cursor: hand;"
        );

        boton.setOnMouseEntered(e -> boton.setStyle(
                "-fx-background-color: rgba(100, 100, 150, 0.3); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Arial Unicode MS', 'Segoe UI Symbol'; " +
                        "-fx-cursor: hand;"
        ));

        boton.setOnMouseExited(e -> boton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Arial Unicode MS', 'Segoe UI Symbol'; " +
                        "-fx-cursor: hand;"
        ));
    }

    public static void aplicarEstiloTitulo(Label etiqueta) {
        etiqueta.setFont(Font.font(FUENTE_MODERNA, FontWeight.BOLD, 24));
        etiqueta.setTextFill(TEXTO_CLARO);

        DropShadow brillo = new DropShadow();
        brillo.setColor(NEON_MORADO);
        brillo.setRadius(10);
        brillo.setSpread(0.2);
        etiqueta.setEffect(brillo);
    }

    public static void aplicarEstiloSubtitulo(Label etiqueta) {
        etiqueta.setFont(Font.font(FUENTE_MODERNA, FontWeight.NORMAL, 14));
        etiqueta.setTextFill(TEXTO_CLARO);
    }

    public static void aplicarEstiloBotonPrimario(Button boton) {
        if (boton == null) {
            System.err.println("EstilosApp: Intento de aplicar estilo a un botón null.");
            return;
        }

        boton.setFont(Font.font(FUENTE_MODERNA, FontWeight.NORMAL, 13));
        boton.setTextFill(Color.WHITE);
        boton.setPadding(new Insets(8, 20, 8, 20));

        String colorRosaCSS = String.format("rgba(%d, %d, %d, %.2f)",
                (int) (NEON_ROSA.getRed() * 255),
                (int) (NEON_ROSA.getGreen() * 255),
                (int) (NEON_ROSA.getBlue() * 255),
                NEON_ROSA.getOpacity());

        String colorAzulCSS = String.format("rgba(%d, %d, %d, %.2f)",
                (int) (NEON_AZUL.getRed() * 255),
                (int) (NEON_AZUL.getGreen() * 255),
                (int) (NEON_AZUL.getBlue() * 255),
                NEON_AZUL.getOpacity());

        String gradienteCssString = String.format("-fx-background-color: linear-gradient(to right, %s, %s);",
                colorRosaCSS, colorAzulCSS);

        boton.setStyle(
                gradienteCssString +
                        " -fx-background-radius: 5px;" +
                        " -fx-border-color: transparent;" +
                        " -fx-border-width: 0;"
        );

        boton.setEffect((DropShadow) efectosCache.get("shadow-neon"));

        boton.setOnMouseEntered(e -> {
            boton.setEffect((DropShadow) efectosCache.get("shadow-hover"));
        });

        boton.setOnMouseExited(e -> {
            boton.setEffect((DropShadow) efectosCache.get("shadow-neon"));
        });

        boton.setOnMousePressed(e -> {
            boton.setEffect((DropShadow) efectosCache.get("shadow-pressed"));
        });

        boton.setOnMouseReleased(e -> {
            if (boton.isHover()) {
                boton.setEffect((DropShadow) efectosCache.get("shadow-hover"));
            } else {
                boton.setEffect((DropShadow) efectosCache.get("shadow-neon"));
            }
        });
    }

    public static void aplicarEstiloCampoTexto(TextField campoTexto) {
        campoTexto.setFont(Font.font(FUENTE_MODERNA, 13));
        campoTexto.setPadding(new Insets(8, 10, 8, 10));

        String estiloBase = String.format(
                "-fx-control-inner-background: %s; " +
                        "-fx-background-color: %s; " +
                        "-fx-text-fill: %s; " +
                        "-fx-border-color: %s; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-prompt-text-fill: %s;",
                toRgbString(FONDO_CLARO_CONTROLES),
                toRgbString(FONDO_CLARO_CONTROLES),
                toRgbString(TEXTO_CLARO),
                toRgbString(BORDE_SUTIL_CONTROLES),
                toRgbString(TEXTO_CLARO)
        );

        String estiloFoco = String.format(
                "-fx-control-inner-background: %s; " +
                        "-fx-background-color: %s; " +
                        "-fx-text-fill: %s; " +
                        "-fx-border-color: %s; " +
                        "-fx-border-width: 1.5px; " +
                        "-fx-border-radius: 5px;" +
                        "-fx-background-radius: 5px;",
                toRgbString(FONDO_CLARO_CONTROLES),
                toRgbString(FONDO_CLARO_CONTROLES),
                toRgbString(TEXTO_CLARO),
                toRgbString(BORDE_FOCO_CONTROLES)
        );

        campoTexto.setStyle(estiloBase);

        campoTexto.focusedProperty().addListener((observable, valorAnterior, valorNuevo) -> {
            if (valorNuevo) {
                campoTexto.setStyle(estiloFoco);
                DropShadow sombraFoco = new DropShadow();
                sombraFoco.setColor(Color.rgb(160, 100, 255, 0.4));
                sombraFoco.setRadius(10);
                sombraFoco.setSpread(0.1);
                campoTexto.setEffect(sombraFoco);
            } else {
                campoTexto.setStyle(estiloBase);
                campoTexto.setEffect(null);
            }
        });
    }

    public static void aplicarEstiloCampoContrasena(PasswordField campoContrasena) {
        campoContrasena.setFont(Font.font(FUENTE_MODERNA, 13));

        campoContrasena.setBackground(new Background(new BackgroundFill(
                Color.rgb(35, 35, 45, 1.0),
                new CornerRadii(5),
                null
        )));

        campoContrasena.setBorder(new Border(new BorderStroke(
                Color.rgb(120, 100, 200, 0.5),
                BorderStrokeStyle.SOLID,
                new CornerRadii(5),
                new BorderWidths(1)
        )));

        campoContrasena.setStyle("-fx-text-fill: rgb(230, 230, 250);");

        campoContrasena.setPadding(new Insets(8, 10, 8, 10));

        campoContrasena.focusedProperty().addListener((observable, valorAnterior, valorNuevo) -> {
            if (valorNuevo) {
                campoContrasena.setBorder(new Border(new BorderStroke(
                        NEON_MORADO,
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1.5)
                )));

                DropShadow sombraFoco = new DropShadow();
                sombraFoco.setColor(Color.rgb(160, 100, 255, 0.4));
                sombraFoco.setRadius(10);
                sombraFoco.setSpread(0.1);
                campoContrasena.setEffect(sombraFoco);
            } else {
                campoContrasena.setBorder(new Border(new BorderStroke(
                        Color.rgb(120, 100, 200, 0.5),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1)
                )));

                campoContrasena.setEffect(null);
            }
        });
    }

    public static void aplicarEstiloHipervinculo(Hyperlink hipervinculo) {
        hipervinculo.setFont(Font.font(FUENTE_MODERNA, 13));
        hipervinculo.setTextFill(NEON_ROSA);
        hipervinculo.setBorder(Border.EMPTY);
        hipervinculo.setUnderline(false);

        hipervinculo.setOnMouseEntered(e -> {
            hipervinculo.setTextFill(NEON_AZUL);
            hipervinculo.setUnderline(true);

            hipervinculo.setEffect((Glow) efectosCache.get("glow-medium"));
        });

        hipervinculo.setOnMouseExited(e -> {
            hipervinculo.setTextFill(NEON_ROSA);
            hipervinculo.setUnderline(false);
            hipervinculo.setEffect(null);
        });
    }

    public static void aplicarEstiloEtiqueta(Label etiqueta) {
        etiqueta.setFont(Font.font(FUENTE_MODERNA, 13));
        etiqueta.setTextFill(TEXTO_CLARO);
    }

    public static void aplicarEstiloMenuLateral(VBox menuLateral) {
        menuLateral.setBackground(new Background(new BackgroundFill(
                Color.rgb(20, 20, 30, 1.0),
                new CornerRadii(0),
                null
        )));

        menuLateral.setBorder(Border.EMPTY);

        menuLateral.setEffect((DropShadow) efectosCache.get("shadow-subtle"));
    }

    public static void aplicarEstiloBotonNavegacion(Button boton) {
        boton.setFont(Font.font(FUENTE_MODERNA, FontWeight.NORMAL, 13));

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

        boton.setPadding(new Insets(8, 15, 8, 15));

        boton.setAlignment(Pos.CENTER_LEFT);
        boton.setGraphicTextGap(10);

        boton.getStyleClass().add("nav-button");

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

    public static void aplicarEstiloGrafico(Chart grafico) {
        grafico.setStyle("-fx-background-color: transparent;");

        grafico.lookupAll(".chart-title").forEach(nodo -> {
            if (nodo instanceof Label) {
                ((Label) nodo).setTextFill(TEXTO_CLARO);
            }
        });

        grafico.lookupAll(".axis-label").forEach(nodo ->
                nodo.setStyle("-fx-text-fill: white;")
        );

        grafico.lookupAll(".chart-legend").forEach(nodo ->
                nodo.setStyle("-fx-background-color: transparent; -fx-text-fill: white;")
        );

        grafico.lookupAll(".chart-legend-item").forEach(nodo ->
                nodo.setStyle("-fx-text-fill: white;")
        );

        if (grafico instanceof PieChart) {
            javafx.application.Platform.runLater(() -> {
                grafico.lookupAll(".chart-pie-label").forEach(node -> {
                    if (node instanceof javafx.scene.text.Text) {
                        ((javafx.scene.text.Text) node).setFill(TEXTO_CLARO);
                    }
                });
            });

            ((PieChart) grafico).getData().forEach(dato -> {
                dato.getNode().setEffect(new javafx.scene.effect.Glow(0.3));
            });
        }

        if (grafico instanceof LineChart) {
            grafico.lookupAll(".chart-series-line").forEach(nodo -> {
                nodo.setEffect(new javafx.scene.effect.Glow(0.3));
            });
            grafico.lookupAll(".axis-label").forEach(labelNode -> {
                labelNode.setStyle("-fx-text-fill: white;");
            });
            grafico.lookupAll(".axis-tick-mark").forEach(tickNode -> {
                tickNode.setStyle("-fx-stroke: white;");
            });
            grafico.lookupAll(".axis-tick-label").forEach(tickLabelNode -> {
                tickLabelNode.setStyle("-fx-text-fill: white;");
            });
        }
    }

    public static void aplicarEstiloTabla(TableView<?> tabla) {
        tabla.setStyle(
                "-fx-background-color: rgba(30, 30, 40, 0.7); " +
                        "-fx-background-radius: 5px; " +
                        "-fx-border-color: rgba(80, 80, 120, 0.5); " +
                        "-fx-border-radius: 5px; " +
                        "-fx-border-width: 1px;"
        );

        tabla.lookupAll(".column-header").forEach(nodo ->
                nodo.setStyle(
                        "-fx-background-color: rgba(60, 60, 80, 0.7); " +
                                "-fx-text-fill: " + toRgbString(TEXTO_CLARO) + "; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 8px; " +
                                "-fx-border-color: transparent;"
                )
        );

        tabla.lookupAll(".table-cell").forEach(nodo ->
                nodo.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-text-fill: " + toRgbString(TEXTO_CLARO) + "; " +
                                "-fx-table-cell-border-color: transparent;"
                )
        );

        String cssClave = "tabla-estilo";
        String css = ".table-row-cell:odd { -fx-background-color: rgba(50, 50, 60, 0.5); }" +
                ".table-row-cell:even { -fx-background-color: rgba(40, 40, 50, 0.5); }" +
                ".table-row-cell:hover { -fx-background-color: rgba(80, 70, 120, 0.5); }" +
                ".table-row-cell:selected { -fx-background-color: rgba(100, 100, 200, 0.7); }" +
                ".table-cell { -fx-text-fill: " + toRgbString(TEXTO_CLARO) + "; }";

        if (!cssCache.containsKey(cssClave)) {
            cssCache.put(cssClave, crearCSS(css));
        }

        String cssUrl = cssCache.get(cssClave);
        if (tabla.getScene() != null && !tabla.getScene().getStylesheets().contains(cssUrl)) {
            tabla.getScene().getStylesheets().add(cssUrl);
        } else if (tabla.getScene() == null) {
            tabla.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null && !newScene.getStylesheets().contains(cssUrl)) {
                    newScene.getStylesheets().add(cssUrl);
                }
            });
        }
    }

    public static void aplicarEstiloLista(ListView<?> lista) {
        lista.setStyle(
                "-fx-background-color: rgba(30, 30, 40, 0.7); " +
                        "-fx-background-radius: 5px; " +
                        "-fx-border-color: rgba(80, 80, 120, 0.5); " +
                        "-fx-border-radius: 5px; " +
                        "-fx-border-width: 1px;"
        );

        String cssClave = "lista-estilo";
        String css = ".list-cell:odd { -fx-background-color: rgba(50, 50, 60, 0.5); }" +
                ".list-cell:even { -fx-background-color: rgba(40, 40, 50, 0.5); }" +
                ".list-cell:hover { -fx-background-color: rgba(80, 70, 120, 0.5); }" +
                ".list-cell:selected { -fx-background-color: rgba(100, 100, 200, 0.7); }" +
                ".list-cell { -fx-text-fill: " + toRgbString(TEXTO_CLARO) + "; }";

        if (!cssCache.containsKey(cssClave)) {
            cssCache.put(cssClave, crearCSS(css));
        }

        String cssUrl = cssCache.get(cssClave);
        if (lista.getScene() != null && !lista.getScene().getStylesheets().contains(cssUrl)) {
            lista.getScene().getStylesheets().add(cssUrl);
        } else if (lista.getScene() == null) {
            lista.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null && !newScene.getStylesheets().contains(cssUrl)) {
                    newScene.getStylesheets().add(cssUrl);
                }
            });
        }
    }

    public static void aplicarEstiloComboBox(ComboBox<?> combo) {
        String colorFondoCombo = toRgbString(FONDO_CLARO_CONTROLES);

        combo.setStyle(
                "-fx-background-color: " + colorFondoCombo + "; " +
                        "-fx-border-color: rgba(120, 100, 200, 0.5); " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-text-fill: " + toRgbString(TEXTO_CLARO) + ";"
        );

        String cssClave = "combo-estilo-popup-claro";
        String css = String.format(
                ".combo-box.%s .list-cell { -fx-text-fill: %s; -fx-background-color: %s; }" +
                        ".combo-box.%s .list-view { -fx-background-color: %s; -fx-border-color: %s; }" +
                        ".combo-box.%s .list-cell:hover { -fx-background-color: %s; }",
                cssClave, toRgbString(TEXTO_CLARO), toRgbString(PANEL_OSCURO),
                cssClave, toRgbString(Color.rgb(25, 25, 35, 0.95)), toRgbString(NEON_MORADO),
                cssClave, toRgbString(Color.rgb(60, 60, 90, 0.7))
        );

        String cssUrl = "";
        if (!cssCache.containsKey(cssClave)) {
            cssCache.put(cssClave, crearCSS(css));
        }
        cssUrl = cssCache.get(cssClave);

        if (!combo.getStyleClass().contains(cssClave)) {
            combo.getStyleClass().add(cssClave);
        }
        final String finalCssUrl = cssUrl;
        Platform.runLater(() -> {
            if (combo.getScene() != null && !combo.getScene().getStylesheets().contains(finalCssUrl)) {
                combo.getScene().getStylesheets().add(finalCssUrl);
            } else if (combo.getScene() == null) {
                combo.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null && !newScene.getStylesheets().contains(finalCssUrl)) {
                        newScene.getStylesheets().add(finalCssUrl);
                    }
                });
            }
        });
    }

    public static void aplicarEstiloDatePicker(DatePicker datePicker) {
        datePicker.setStyle(
                "-fx-background-color: rgba(35, 35, 45, 1.0); " +
                        "-fx-border-color: rgba(120, 100, 200, 0.5); " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-text-fill: white;"
        );

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

    public static void aplicarEstiloTextArea(TextArea textArea) {
        String colorFondoTextArea = toRgbString(FONDO_CLARO_CONTROLES);
        String colorTextoTextArea = toRgbString(TEXTO_CLARO);
        String colorBordeTextArea = toRgbString(Color.rgb(120, 100, 200, 0.7));
        String colorBordeFocoTextArea = toRgbString(NEON_MORADO);

        String estiloBase = String.format(
                "-fx-background-color: %s; " +
                        "-fx-control-inner-background: %s; " +
                        "-fx-border-color: %s; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-text-fill: %s;" +
                        "-fx-prompt-text-fill: %s;",
                colorFondoTextArea,
                colorFondoTextArea,
                colorBordeTextArea,
                colorTextoTextArea,
                toRgbString(Color.rgb(150, 150, 170))
        );

        String estiloFoco = String.format(
                "-fx-background-color: %s; " +
                        "-fx-control-inner-background: %s; " +
                        "-fx-border-color: %s; " +
                        "-fx-border-width: 1.5px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-text-fill: %s;" +
                        "-fx-prompt-text-fill: %s;",
                colorFondoTextArea,
                colorFondoTextArea,
                colorBordeFocoTextArea,
                colorTextoTextArea,
                toRgbString(Color.rgb(150, 150, 170))
        );

        textArea.setStyle(estiloBase);

        textArea.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                textArea.setStyle(estiloFoco);
            } else {
                textArea.setStyle(estiloBase);
            }
        });
    }

    public static void aplicarEstiloScrollPane(ScrollPane scrollPane) {
        scrollPane.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-background: transparent; " +
                        "-fx-border-color: transparent;"
        );

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

    public static void aplicarEstiloDialogPane(DialogPane dialogPane) {
        dialogPane.setStyle(
                "-fx-background-color: rgba(25, 25, 35, 0.95); " +
                        "-fx-text-fill: white; " +
                        "-fx-border-color: rgba(255, 0, 255, 0.7); " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px;"
        );

        dialogPane.lookupAll(".label").forEach(nodo ->
                nodo.setStyle("-fx-text-fill: white;")
        );

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

        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.rgb(0, 0, 0, 0.7));
        sombra.setRadius(20);
        sombra.setSpread(0.1);
        dialogPane.setEffect(sombra);
    }

    public static void aplicarEstiloTarjeta(Pane tarjeta) {
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

        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.rgb(0, 0, 0, 0.5));
        sombra.setRadius(10);
        sombra.setSpread(0.05);
        tarjeta.setEffect(sombra);

        tarjeta.setPadding(new Insets(15));
    }

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

    public static void limpiarCacheCSS() {
        cssCache.clear();
    }

    public static Border crearBordeNeon(Color color, double intensidad, double radio) {
        return new Border(new BorderStroke(
                color,
                BorderStrokeStyle.SOLID,
                new CornerRadii(radio),
                new BorderWidths(1.0)
        ));
    }

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

    public static DropShadow crearResplandorNeon(Color color, double intensidad) {
        DropShadow resplandor = new DropShadow();
        resplandor.setColor(color);
        resplandor.setRadius(15 * intensidad);
        resplandor.setSpread(0.2 * intensidad);
        return resplandor;
    }

    public static void aplicarEstiloCabecerasTabla(TableView<?> tabla) {
        Color moradoCabecera = Color.rgb(123, 104, 238);

        Platform.runLater(() -> {
            tabla.lookupAll(".column-header").forEach(nodo -> {
                nodo.setStyle(
                        "-fx-background-color: " + toRgbString(moradoCabecera) + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 8px; " +
                                "-fx-border-color: rgba(160, 100, 255, 0.5); " +
                                "-fx-border-width: 0 0 1px 0;"
                );
            });

            tabla.lookupAll(".column-header .label").forEach(nodo -> {
                nodo.setStyle(
                        "-fx-text-fill: white; " +
                                "-fx-font-weight: bold;"
                );
            });
        });
    }

    public static String toRgbString(Color color) {
        return String.format("rgb(%d, %d, %d)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }


}