package smartsave.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import smartsave.servicio.SessionManager;
import smartsave.utilidad.EstilosApp;
import smartsave.servicio.UsuarioServicio;
import smartsave.modelo.Usuario;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private BorderPane mainPane;
    @FXML private HBox titleBar;
    @FXML private VBox loginPane;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button minimizeButton;
    @FXML private Button maximizeButton;
    @FXML private Button closeButton;
    @FXML private Hyperlink registroLink;
    @FXML private Label emailLabel;
    @FXML private Label passwordLabel;

    private double offsetX = 0;
    private double offsetY = 0;

    private UsuarioServicio usuarioServicio = new UsuarioServicio();

    @Override
    public void initialize(URL ubicacion, ResourceBundle recursos) {
        configurarBotonesVentana();

        aplicarEstilos();

        configurarVentanaArrastrable();

        configurarValidacion();
    }

    private void aplicarEstilos() {
        EstilosApp.aplicarEstiloPanelPrincipal(mainPane);
        EstilosApp.aplicarEstiloBarraTitulo(titleBar);
        EstilosApp.aplicarEstiloPanelContenido(loginPane);

        EstilosApp.aplicarEstiloBotonVentana(minimizeButton);
        EstilosApp.aplicarEstiloBotonVentana(maximizeButton);
        EstilosApp.aplicarEstiloBotonVentana(closeButton);

        EstilosApp.aplicarEstiloTitulo(titleLabel);
        EstilosApp.aplicarEstiloSubtitulo(subtitleLabel);

        EstilosApp.aplicarEstiloCampoTexto(emailField);
        EstilosApp.aplicarEstiloCampoContrasena(passwordField);

        EstilosApp.aplicarEstiloBotonPrimario(loginButton);

        if (emailLabel != null) EstilosApp.aplicarEstiloEtiqueta(emailLabel);
        if (passwordLabel != null) EstilosApp.aplicarEstiloEtiqueta(passwordLabel);

        if (registroLink != null) EstilosApp.aplicarEstiloHipervinculo(registroLink);

        for (javafx.scene.Node nodo : mainPane.lookupAll("Label")) {
            if (nodo instanceof Label && nodo != titleLabel && nodo != subtitleLabel
                    && nodo != emailLabel && nodo != passwordLabel) {
                EstilosApp.aplicarEstiloEtiqueta((Label) nodo);
            }
        }

        for (javafx.scene.Node nodo : mainPane.lookupAll("Hyperlink")) {
            if (nodo instanceof Hyperlink && nodo != registroLink) {
                EstilosApp.aplicarEstiloHipervinculo((Hyperlink) nodo);
            }
        }
    }

    private void configurarBotonesVentana() {
        minimizeButton.setText("—");
        maximizeButton.setText("□");
        closeButton.setText("✕");


        minimizeButton.setStyle("-fx-font-family: 'Arial Unicode MS', 'Segoe UI Symbol';");
        maximizeButton.setStyle("-fx-font-family: 'Arial Unicode MS', 'Segoe UI Symbol';");
        closeButton.setStyle("-fx-font-family: 'Arial Unicode MS', 'Segoe UI Symbol';");

        minimizeButton.setMinWidth(20);
        maximizeButton.setMinWidth(40);
        closeButton.setMinWidth(20);
    }

    private void configurarValidacion() {
        emailField.textProperty().addListener((observable, valorAnterior, valorNuevo) -> {
            if (!valorNuevo.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$") && !valorNuevo.isEmpty()) {
                emailField.setBorder(new Border(new BorderStroke(
                        Color.rgb(255, 50, 50, 0.8),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1.5)
                )));

                DropShadow sombraError = new DropShadow();
                sombraError.setColor(Color.rgb(255, 0, 0, 0.5));
                sombraError.setRadius(10);
                sombraError.setSpread(0.1);
                emailField.setEffect(sombraError);
            } else {
                EstilosApp.aplicarEstiloCampoTexto(emailField);
            }
        });

        passwordField.textProperty().addListener((observable, valorAnterior, valorNuevo) -> {
            if (valorNuevo.length() < 6 && !valorNuevo.isEmpty()) {
                passwordField.setBorder(new Border(new BorderStroke(
                        Color.rgb(255, 50, 50, 0.8),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1.5)
                )));

                DropShadow sombraError = new DropShadow();
                sombraError.setColor(Color.rgb(255, 0, 0, 0.5));
                sombraError.setRadius(10);
                sombraError.setSpread(0.1);
                passwordField.setEffect(sombraError);
            } else {
                EstilosApp.aplicarEstiloCampoContrasena(passwordField);
            }
        });
    }

    private void configurarVentanaArrastrable() {
        titleBar.setOnMousePressed(evento -> {
            offsetX = evento.getSceneX();
            offsetY = evento.getSceneY();
        });

        titleBar.setOnMouseDragged(evento -> {
            Stage escenario = (Stage) titleBar.getScene().getWindow();
            escenario.setX(evento.getScreenX() - offsetX);
            escenario.setY(evento.getScreenY() - offsetY);
        });
    }

    @FXML
    private void handleMinimizeAction(ActionEvent evento) {
        Stage escenario = (Stage) ((Button) evento.getSource()).getScene().getWindow();
        escenario.setIconified(true);
    }

    @FXML
    private void handleMaximizeAction(ActionEvent evento) {
        Stage escenario = (Stage) ((Button) evento.getSource()).getScene().getWindow();
        escenario.setMaximized(!escenario.isMaximized());

        if (escenario.isMaximized()) {
            maximizeButton.setText("❐");
        } else {
            maximizeButton.setText("□");
        }
    }

    @FXML
    private void handleCloseAction(ActionEvent evento) {
        Stage escenario = (Stage) ((Button) evento.getSource()).getScene().getWindow();
        escenario.close();
    }

    @FXML
    private void handleLoginButtonAction(ActionEvent evento) {
        String email = emailField.getText().trim();
        String contrasena = passwordField.getText();

        if ("1".equals(email) && "1".equals(contrasena)) {
            try {
                Usuario usuarioPrueba = usuarioServicio.obtenerUsuarioPorId(1L);
                if (usuarioPrueba != null) {
                    SessionManager.getInstancia().setUsuarioActual(usuarioPrueba);
                    abrirPanelPrincipal();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo cargar el usuario de prueba (ID: 1).");
                }
                return;
            } catch (IOException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al cargar el panel principal: " + e.getMessage());
                return;
            }
        }

        if (email.isEmpty() || contrasena.isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Por favor, complete todos los campos.");
            return;
        }

        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Por favor, ingrese un email válido.");
            return;
        }

        if (contrasena.length() < 6) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        Usuario usuario = usuarioServicio.verificarCredenciales(email, contrasena);

        if (usuario != null) {
            SessionManager.getInstancia().setUsuarioActual(usuario);
            try {
                abrirPanelPrincipal();
            } catch (IOException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error",
                        "Error al cargar la pantalla principal: " + e.getMessage());
            }
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "Email o contraseña incorrectos. Por favor, intente nuevamente.");
        }
    }

    private void abrirPanelPrincipal() throws IOException {
        FXMLLoader cargador = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
        Parent raizPanel = cargador.load();

        Scene escenaPanel = new Scene(raizPanel);
        escenaPanel.setFill(Color.TRANSPARENT);

        Stage escenarioActual = (Stage) loginButton.getScene().getWindow();

        boolean eraMaximizado = escenarioActual.isMaximized();

        double nuevoAnchoDashboard = 1200;
        double nuevoAltoDashboard = 800;

        escenarioActual.setScene(escenaPanel);
        escenarioActual.setTitle("SmartSave - Panel Principal");

        if (eraMaximizado) {

            escenarioActual.setMaximized(true);
        } else {
            escenarioActual.setWidth(nuevoAnchoDashboard);
            escenarioActual.setHeight(nuevoAltoDashboard);
        }

        escenarioActual.centerOnScreen();
    }

    @FXML
    private void handleRegistroLinkAction(ActionEvent evento) {
        try {
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/fxml/registro.fxml"));
            Parent raizRegistro = cargador.load();

            Scene escenaRegistro = new Scene(raizRegistro);
            escenaRegistro.setFill(Color.TRANSPARENT);

            Stage escenarioActual = (Stage) registroLink.getScene().getWindow();

            escenarioActual.setScene(escenaRegistro);
            escenarioActual.setTitle("SmartSave - Registro de Usuario");

            escenarioActual.centerOnScreen();

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "Error al cargar la pantalla de registro: " + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        DialogPane panelDialogo = alerta.getDialogPane();

        panelDialogo.setBackground(new Background(new BackgroundFill(
                Color.rgb(25, 25, 35, 0.95),
                new CornerRadii(10),
                null
        )));

        panelDialogo.setBorder(new Border(new BorderStroke(
                Color.rgb(255, 0, 255, 0.7),
                BorderStrokeStyle.SOLID,
                new CornerRadii(10),
                new BorderWidths(1.5)
        )));

        panelDialogo.lookup(".content.label").setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        for (ButtonType tipoBoton : alerta.getButtonTypes()) {
            Button boton = (Button) panelDialogo.lookupButton(tipoBoton);

            boton.setBackground(new Background(new BackgroundFill(
                    Color.rgb(40, 40, 50, 1.0),
                    new CornerRadii(5),
                    null
            )));

            boton.setBorder(new Border(new BorderStroke(
                    Color.rgb(160, 100, 255, 0.8),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(5),
                    new BorderWidths(1)
            )));

            boton.setTextFill(Color.WHITE);

            Glow resplandor = new Glow();
            resplandor.setLevel(0.3);
            boton.setEffect(resplandor);

            boton.setOnMouseEntered(e -> {
                boton.setBackground(new Background(new BackgroundFill(
                        Color.rgb(60, 60, 70, 1.0),
                        new CornerRadii(5),
                        null
                )));

                DropShadow sombra = new DropShadow();
                sombra.setColor(Color.rgb(180, 100, 255, 0.8));
                sombra.setRadius(15);
                sombra.setSpread(0.2);
                boton.setEffect(sombra);
            });

            boton.setOnMouseExited(e -> {
                boton.setBackground(new Background(new BackgroundFill(
                        Color.rgb(40, 40, 50, 1.0),
                        new CornerRadii(5),
                        null
                )));

                Glow resplandorOriginal = new Glow();
                resplandorOriginal.setLevel(0.3);
                boton.setEffect(resplandorOriginal);
            });
        }

        DropShadow sombraAlerta = new DropShadow();
        sombraAlerta.setColor(Color.rgb(0, 0, 0, 0.7));
        sombraAlerta.setRadius(20);
        sombraAlerta.setSpread(0.1);
        panelDialogo.setEffect(sombraAlerta);

        alerta.showAndWait();
    }
}