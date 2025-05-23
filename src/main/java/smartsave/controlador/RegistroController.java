package smartsave.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import smartsave.modelo.Usuario;
import smartsave.servicio.NavegacionServicio;
import smartsave.servicio.UsuarioServicio;
import smartsave.utilidad.EstilosApp;
import smartsave.utilidad.ValidacionUtil;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de Registro de Usuario
 * No extiende BaseController porque la pantalla de registro es diferente estructuralmente
 */
public class RegistroController implements Initializable {

    // Referencias FXML
    @FXML private BorderPane mainPane;
    @FXML private HBox titleBar;
    @FXML private VBox registroPane;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextField nombreField;
    @FXML private TextField apellidosField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registroButton;
    @FXML private Button minimizeButton;
    @FXML private Button maximizeButton;
    @FXML private Button closeButton;
    @FXML private Hyperlink loginLink;

    // Etiquetas para los campos
    @FXML private Label nombreLabel;
    @FXML private Label apellidosLabel;
    @FXML private Label emailLabel;
    @FXML private Label passwordLabel;
    @FXML private Label confirmPasswordLabel;

    // Servicios
    private final UsuarioServicio usuarioServicio = new UsuarioServicio();
    private final NavegacionServicio navegacionServicio = NavegacionServicio.getInstancia();

    // Variables para permitir el arrastre de la ventana
    private double offsetX = 0;
    private double offsetY = 0;

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

    /**
     * Aplica estilos a todos los componentes de la pantalla
     */
    private void aplicarEstilos() {
        // Aplicar estilos al tema oscuro con neón
        EstilosApp.aplicarEstiloPanelPrincipal(mainPane);
        EstilosApp.aplicarEstiloBarraTitulo(titleBar);
        EstilosApp.aplicarEstiloPanelContenido(registroPane);

        // Aplicar estilos a los botones de la ventana
        EstilosApp.aplicarEstiloBotonVentana(minimizeButton);
        EstilosApp.aplicarEstiloBotonVentana(maximizeButton);
        EstilosApp.aplicarEstiloBotonVentana(closeButton);

        // Aplicar estilos a las etiquetas
        EstilosApp.aplicarEstiloTitulo(titleLabel);
        EstilosApp.aplicarEstiloSubtitulo(subtitleLabel);

        // Aplicar estilos a los campos de entrada
        EstilosApp.aplicarEstiloCampoTexto(nombreField);
        EstilosApp.aplicarEstiloCampoTexto(apellidosField);
        EstilosApp.aplicarEstiloCampoTexto(emailField);
        EstilosApp.aplicarEstiloCampoContrasena(passwordField);
        EstilosApp.aplicarEstiloCampoContrasena(confirmPasswordField);

        // Aplicar estilo al botón de registro
        EstilosApp.aplicarEstiloBotonPrimario(registroButton);

        // Para las etiquetas adicionales
        EstilosApp.aplicarEstiloEtiqueta(nombreLabel);
        EstilosApp.aplicarEstiloEtiqueta(apellidosLabel);
        EstilosApp.aplicarEstiloEtiqueta(emailLabel);
        EstilosApp.aplicarEstiloEtiqueta(passwordLabel);
        EstilosApp.aplicarEstiloEtiqueta(confirmPasswordLabel);

        // Para hipervínculos
        EstilosApp.aplicarEstiloHipervinculo(loginLink);

        for (javafx.scene.Node nodo : mainPane.lookupAll("Label")) {
            if (nodo instanceof Label && nodo != titleLabel && nodo != subtitleLabel
                    && nodo != nombreLabel && nodo != apellidosLabel
                    && nodo != emailLabel && nodo != passwordLabel
                    && nodo != confirmPasswordLabel) {
                EstilosApp.aplicarEstiloEtiqueta((Label) nodo);
            }
        }
    }

    /**
     * Configura los botones de la ventana
     */
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

    /**
     * Configura la validación de los campos del formulario
     */
    private void configurarValidacion() {
        // Validación de email en tiempo real
        emailField.textProperty().addListener((observable, valorAnterior, valorNuevo) -> {
            if (!ValidacionUtil.esEmailValido(valorNuevo) && !valorNuevo.isEmpty()) {
                aplicarEstiloError(emailField);
            } else {
                EstilosApp.aplicarEstiloCampoTexto(emailField);
            }
        });

        // Validación de contraseña en tiempo real
        passwordField.textProperty().addListener((observable, valorAnterior, valorNuevo) -> {
            if (valorNuevo.length() < 6 && !valorNuevo.isEmpty()) {
                aplicarEstiloError(passwordField);
            } else {
                EstilosApp.aplicarEstiloCampoContrasena(passwordField);
            }
        });

        // Validar que las contraseñas coincidan
        confirmPasswordField.textProperty().addListener((observable, valorAnterior, valorNuevo) -> {
            if (!valorNuevo.equals(passwordField.getText()) && !valorNuevo.isEmpty()) {
                aplicarEstiloError(confirmPasswordField);
            } else {
                EstilosApp.aplicarEstiloCampoContrasena(confirmPasswordField);
            }
        });
    }

    /**
     * Aplica estilo de error a un campo
     */
    private void aplicarEstiloError(Control campo) {
        // Resaltar con borde rojo cuando hay error
        campo.setBorder(new Border(new BorderStroke(
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
        campo.setEffect(sombraError);
    }

    /**
     * Configura el arrastre de la ventana
     */
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

    /**
     * Maneja la acción de minimizar la ventana
     */
    @FXML
    private void handleMinimizeAction(ActionEvent evento) {
        Stage escenario = (Stage) ((Button) evento.getSource()).getScene().getWindow();
        escenario.setIconified(true);
    }

    /**
     * Maneja la acción de maximizar/restaurar la ventana
     */
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

    /**
     * Maneja la acción de cerrar la ventana
     */
    @FXML
    private void handleCloseAction(ActionEvent evento) {
        Stage escenario = (Stage) ((Button) evento.getSource()).getScene().getWindow();
        escenario.close();
    }

    /**
     * Maneja la acción del botón de registro
     */
    @FXML
    private void handleRegistroButtonAction(ActionEvent evento) {
        // Obtener valores de los campos
        String nombre = nombreField.getText().trim();
        String apellidos = apellidosField.getText().trim();
        String email = emailField.getText().trim();
        String contrasena = passwordField.getText();
        String confirmarContrasena = confirmPasswordField.getText();

        // Validar que no haya campos vacíos
        if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || contrasena.isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de validación", "Todos los campos son obligatorios.");
            return;
        }

        // Validar formato de email
        if (!ValidacionUtil.esEmailValido(email)) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de validación", "El email ingresado no es válido.");
            return;
        }

        // Validar longitud mínima de contraseña
        if (contrasena.length() < 6) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de validación",
                    "La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        // Validar que las contraseñas coincidan
        if (!contrasena.equals(confirmarContrasena)) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de validación",
                    "Las contraseñas no coinciden.");
            return;
        }

        try {
            // Crear nuevo usuario (en un entorno real, se haría hash de la contraseña)
            Usuario nuevoUsuario = new Usuario(email, nombre, apellidos, contrasena);

            // Guardar usuario en la base de datos (simulado)
            boolean registroExitoso = usuarioServicio.registrarUsuario(nuevoUsuario);

            if (registroExitoso) {
                // Mostrar mensaje de éxito
                mostrarAlerta(Alert.AlertType.INFORMATION, "Registro exitoso",
                        "Tu cuenta ha sido creada correctamente. Ahora puedes iniciar sesión.");

                // Redirigir a la pantalla de login
                irALogin();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de registro",
                        "No se pudo completar el registro. El email ya está en uso.");
            }
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de sistema",
                    "Ha ocurrido un error al procesar el registro: " + e.getMessage());
        }
    }

    /**
     * Maneja la acción del enlace para ir a login
     */
    @FXML
    private void handleLoginLinkAction(ActionEvent evento) {
        irALogin();
    }

    /**
     * Navega a la pantalla de login
     */
    private void irALogin() {
        try {
            Stage escenarioActual = (Stage) mainPane.getScene().getWindow();
            navegacionServicio.navegarALogin(escenarioActual);
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de navegación",
                    "Error al cargar la pantalla de login: " + e.getMessage());
        }
    }

    /**
     * Muestra una alerta estilizada
     */
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