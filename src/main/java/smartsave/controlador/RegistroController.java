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
import org.mindrot.jbcrypt.BCrypt;

import java.net.URL;
import java.util.ResourceBundle;


public class RegistroController implements Initializable {

    
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

    
    @FXML private Label nombreLabel;
    @FXML private Label apellidosLabel;
    @FXML private Label emailLabel;
    @FXML private Label passwordLabel;
    @FXML private Label confirmPasswordLabel;

    
    private final UsuarioServicio usuarioServicio = new UsuarioServicio();
    private final NavegacionServicio navegacionServicio = NavegacionServicio.getInstancia();

    
    private double offsetX = 0;
    private double offsetY = 0;

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
        EstilosApp.aplicarEstiloPanelContenido(registroPane);

        
        EstilosApp.aplicarEstiloBotonVentana(minimizeButton);
        EstilosApp.aplicarEstiloBotonVentana(maximizeButton);
        EstilosApp.aplicarEstiloBotonVentana(closeButton);

        
        EstilosApp.aplicarEstiloTitulo(titleLabel);
        EstilosApp.aplicarEstiloSubtitulo(subtitleLabel);

        
        EstilosApp.aplicarEstiloCampoTexto(nombreField);
        EstilosApp.aplicarEstiloCampoTexto(apellidosField);
        EstilosApp.aplicarEstiloCampoTexto(emailField);
        EstilosApp.aplicarEstiloCampoContrasena(passwordField);
        EstilosApp.aplicarEstiloCampoContrasena(confirmPasswordField);

        
        EstilosApp.aplicarEstiloBotonPrimario(registroButton);

        
        EstilosApp.aplicarEstiloEtiqueta(nombreLabel);
        EstilosApp.aplicarEstiloEtiqueta(apellidosLabel);
        EstilosApp.aplicarEstiloEtiqueta(emailLabel);
        EstilosApp.aplicarEstiloEtiqueta(passwordLabel);
        EstilosApp.aplicarEstiloEtiqueta(confirmPasswordLabel);

        
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
        
        minimizeButton.setText("—");
        maximizeButton.setText("□");
        closeButton.setText("✕");

        
        minimizeButton.setMinWidth(30);
        maximizeButton.setMinWidth(30);
        closeButton.setMinWidth(30);
    }

    /**
     * Configura la validación de los campos del formulario
     */
    private void configurarValidacion() {
        
        emailField.textProperty().addListener((observable, valorAnterior, valorNuevo) -> {
            if (!ValidacionUtil.esEmailValido(valorNuevo) && !valorNuevo.isEmpty()) {
                aplicarEstiloError(emailField);
            } else {
                EstilosApp.aplicarEstiloCampoTexto(emailField);
            }
        });

        
        passwordField.textProperty().addListener((observable, valorAnterior, valorNuevo) -> {
            if (valorNuevo.length() < 6 && !valorNuevo.isEmpty()) {
                aplicarEstiloError(passwordField);
            } else {
                EstilosApp.aplicarEstiloCampoContrasena(passwordField);
            }
        });

        
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
        
        campo.setBorder(new Border(new BorderStroke(
                Color.rgb(255, 50, 50, 0.8),
                BorderStrokeStyle.SOLID,
                new CornerRadii(5),
                new BorderWidths(1.5)
        )));

        
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

        
        if (escenario.isMaximized()) {
            maximizeButton.setText("❐");  
        } else {
            maximizeButton.setText("□");  
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
        
        String nombre = nombreField.getText().trim();
        String apellidos = apellidosField.getText().trim();
        String email = emailField.getText().trim();
        String contrasena = passwordField.getText();
        String confirmarContrasena = confirmPasswordField.getText();

        
        if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || contrasena.isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de validación", "Todos los campos son obligatorios.");
            return;
        }

        
        if (!ValidacionUtil.esEmailValido(email)) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de validación", "El email ingresado no es válido.");
            return;
        }

        
        if (contrasena.length() < 6) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de validación",
                    "La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        
        if (!contrasena.equals(confirmarContrasena)) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de validación",
                    "Las contraseñas no coinciden.");
            return;
        }

        try {
            String contrasenaCifrada = BCrypt.hashpw(contrasena, BCrypt.gensalt());

            Usuario nuevoUsuario = new Usuario(email, nombre, apellidos, contrasenaCifrada);

            boolean registroExitoso = usuarioServicio.registrarUsuario(nuevoUsuario);

            if (registroExitoso) {
                
                mostrarAlerta(Alert.AlertType.INFORMATION, "Registro exitoso",
                        "Tu cuenta ha sido creada correctamente. Ahora puedes iniciar sesión.");

                
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