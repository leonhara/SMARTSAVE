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
import smartsave.utilidad.EstilosApp;
import smartsave.servicio.UsuarioServicio;
import smartsave.modelo.Usuario;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    // Mantener referencias FXML con nombres originales
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

    // Variables para permitir el arrastre de la ventana
    private double offsetX = 0;
    private double offsetY = 0;

    // Servicio para operaciones con usuarios
    private UsuarioServicio usuarioServicio = new UsuarioServicio();

    @Override
    public void initialize(URL ubicacion, ResourceBundle recursos) {
        // Configurar los textos de los botones de la barra de título
        configurarBotonesVentana();

        // Aplicar estilos usando la clase de utilidad
        aplicarEstilos();

        // Configurar el arrastre de la ventana
        configurarVentanaArrastrable();

        // Configurar validación de campos
        configurarValidacion();
    }

    private void aplicarEstilos() {
        // Aplicar estilos al tema oscuro con neón
        EstilosApp.aplicarEstiloPanelPrincipal(mainPane);
        EstilosApp.aplicarEstiloBarraTitulo(titleBar);
        EstilosApp.aplicarEstiloPanelContenido(loginPane);

        // Aplicar estilos a los botones de la ventana
        EstilosApp.aplicarEstiloBotonVentana(minimizeButton);
        EstilosApp.aplicarEstiloBotonVentana(maximizeButton);
        EstilosApp.aplicarEstiloBotonVentana(closeButton);

        // Aplicar estilos a las etiquetas
        EstilosApp.aplicarEstiloTitulo(titleLabel);
        EstilosApp.aplicarEstiloSubtitulo(subtitleLabel);

        // Aplicar estilos a los campos de entrada
        EstilosApp.aplicarEstiloCampoTexto(emailField);
        EstilosApp.aplicarEstiloCampoContraseña(passwordField);

        // Aplicar estilo al botón de inicio de sesión
        EstilosApp.aplicarEstiloBotonPrimario(loginButton);

        // Para las etiquetas adicionales
        if (emailLabel != null) EstilosApp.aplicarEstiloEtiqueta(emailLabel);
        if (passwordLabel != null) EstilosApp.aplicarEstiloEtiqueta(passwordLabel);

        // Para hipervínculos
        if (registroLink != null) EstilosApp.aplicarEstiloHipervinculo(registroLink);

        // Aplicar estilos a todas las etiquetas que no sean título o subtítulo
        for (javafx.scene.Node nodo : mainPane.lookupAll("Label")) {
            if (nodo instanceof Label && nodo != titleLabel && nodo != subtitleLabel
                    && nodo != emailLabel && nodo != passwordLabel) {
                EstilosApp.aplicarEstiloEtiqueta((Label) nodo);
            }
        }

        // Aplicar estilos a todos los hipervínculos
        for (javafx.scene.Node nodo : mainPane.lookupAll("Hyperlink")) {
            if (nodo instanceof Hyperlink && nodo != registroLink) {
                EstilosApp.aplicarEstiloHipervinculo((Hyperlink) nodo);
            }
        }
    }

    private void configurarBotonesVentana() {
        // Configurar los símbolos de los botones de ventana
        minimizeButton.setText("—");
        maximizeButton.setText("□");
        closeButton.setText("✕");

        // Asegurar que los botones sean del tamaño correcto
        minimizeButton.setMinWidth(30);
        maximizeButton.setMinWidth(30);
        closeButton.setMinWidth(30);
    }

    private void configurarValidacion() {
        // Validación simple en tiempo real
        emailField.textProperty().addListener((observable, valorAnterior, valorNuevo) -> {
            if (!valorNuevo.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$") && !valorNuevo.isEmpty()) {
                // Resaltar con borde rojo cuando el email es inválido
                emailField.setBorder(new Border(new BorderStroke(
                        Color.rgb(255, 50, 50, 0.8),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1.5)
                )));

                // Efecto de resplandor rojo
                DropShadow sombraError = new DropShadow();
                sombraError.setColor(Color.rgb(255, 0, 0, 0.5));
                sombraError.setRadius(10);
                sombraError.setSpread(0.1);
                emailField.setEffect(sombraError);
            } else {
                // Restaurar estilo normal
                EstilosApp.aplicarEstiloCampoTexto(emailField);
            }
        });

        passwordField.textProperty().addListener((observable, valorAnterior, valorNuevo) -> {
            if (valorNuevo.length() < 6 && !valorNuevo.isEmpty()) {
                // Resaltar con borde rojo cuando la contraseña es demasiado corta
                passwordField.setBorder(new Border(new BorderStroke(
                        Color.rgb(255, 50, 50, 0.8),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1.5)
                )));

                // Efecto de resplandor rojo
                DropShadow sombraError = new DropShadow();
                sombraError.setColor(Color.rgb(255, 0, 0, 0.5));
                sombraError.setRadius(10);
                sombraError.setSpread(0.1);
                passwordField.setEffect(sombraError);
            } else {
                // Restaurar estilo normal
                EstilosApp.aplicarEstiloCampoContraseña(passwordField);
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

    // Mantener nombres de métodos de eventos FXML en inglés
    @FXML
    private void handleMinimizeAction(ActionEvent evento) {
        Stage escenario = (Stage) ((Button) evento.getSource()).getScene().getWindow();
        escenario.setIconified(true);
    }

    @FXML
    private void handleMaximizeAction(ActionEvent evento) {
        Stage escenario = (Stage) ((Button) evento.getSource()).getScene().getWindow();
        escenario.setMaximized(!escenario.isMaximized());

        // Cambiar el símbolo del botón según el estado
        if (escenario.isMaximized()) {
            maximizeButton.setText("❐");  // Símbolo para restaurar
        } else {
            maximizeButton.setText("□");  // Símbolo para maximizar
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

        // Acceso rápido para desarrollo
        if ("1".equals(email) && "1".equals(contrasena)) {
            try {
                abrirPanelPrincipal();
                return;
            } catch (IOException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al cargar el panel principal: " + e.getMessage());
                return;
            }
        }

        // Validación básica
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

        // Verificar credenciales usando el servicio
        Usuario usuario = usuarioServicio.verificarCredenciales(email, contrasena);

        if (usuario != null) {
            try {
                // Login exitoso, abrir panel principal
                abrirPanelPrincipal();
            } catch (IOException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error",
                        "Error al cargar la pantalla principal: " + e.getMessage());
            }
        } else {
            // Credenciales incorrectas
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "Email o contraseña incorrectos. Por favor, intente nuevamente.");
        }
    }

    private void abrirPanelPrincipal() throws IOException {
        // Cargar la vista del panel principal
        FXMLLoader cargador = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
        Parent raizPanel = cargador.load();

        // Configurar la nueva escena
        Scene escenaPanel = new Scene(raizPanel);
        escenaPanel.setFill(Color.TRANSPARENT);

        // Obtener el escenario actual
        Stage escenarioActual = (Stage) loginButton.getScene().getWindow();

        // Establecer la nueva escena
        escenarioActual.setScene(escenaPanel);
        escenarioActual.setTitle("SmartSave - Panel Principal");

        // Centrar en pantalla (opcional)
        escenarioActual.centerOnScreen();
    }

    @FXML
    private void handleRegistroLinkAction(ActionEvent evento) {
        try {
            // Cargar la vista de registro
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/fxml/registro.fxml"));
            Parent raizRegistro = cargador.load();

            // Configurar la nueva escena
            Scene escenaRegistro = new Scene(raizRegistro);
            escenaRegistro.setFill(Color.TRANSPARENT);

            // Obtener el escenario actual
            Stage escenarioActual = (Stage) registroLink.getScene().getWindow();

            // Establecer la nueva escena
            escenarioActual.setScene(escenaRegistro);
            escenarioActual.setTitle("SmartSave - Registro de Usuario");

            // Centrar en pantalla (opcional)
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

        // Estilizar la alerta con el tema oscuro
        DialogPane panelDialogo = alerta.getDialogPane();

        // Fondo oscuro
        panelDialogo.setBackground(new Background(new BackgroundFill(
                Color.rgb(25, 25, 35, 0.95),
                new CornerRadii(10),
                null
        )));

        // Borde con efecto neón
        panelDialogo.setBorder(new Border(new BorderStroke(
                Color.rgb(255, 0, 255, 0.7),
                BorderStrokeStyle.SOLID,
                new CornerRadii(10),
                new BorderWidths(1.5)
        )));

        // Color de texto
        panelDialogo.lookup(".content.label").setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // Botones con estilo neón
        for (ButtonType tipoBoton : alerta.getButtonTypes()) {
            Button boton = (Button) panelDialogo.lookupButton(tipoBoton);

            // Fondo oscuro
            boton.setBackground(new Background(new BackgroundFill(
                    Color.rgb(40, 40, 50, 1.0),
                    new CornerRadii(5),
                    null
            )));

            // Borde neón
            boton.setBorder(new Border(new BorderStroke(
                    Color.rgb(160, 100, 255, 0.8),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(5),
                    new BorderWidths(1)
            )));

            // Texto claro
            boton.setTextFill(Color.WHITE);

            // Efecto de brillo
            Glow resplandor = new Glow();
            resplandor.setLevel(0.3);
            boton.setEffect(resplandor);

            // Eventos de hover
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

        // Efecto de sombra para toda la alerta
        DropShadow sombraAlerta = new DropShadow();
        sombraAlerta.setColor(Color.rgb(0, 0, 0, 0.7));
        sombraAlerta.setRadius(20);
        sombraAlerta.setSpread(0.1);
        panelDialogo.setEffect(sombraAlerta);

        // Mostrar y esperar
        alerta.showAndWait();
    }
}