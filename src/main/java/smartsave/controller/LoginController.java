package smartsave.controller;

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
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import smartsave.util.AppStyles;

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

    // Variables para permitir el arrastre de la ventana
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar los textos de los botones de la barra de título
        configureWindowButtons();

        // Aplicar estilos usando la clase de utilidad
        applyStyles();

        // Configurar el arrastre de la ventana
        setupDraggableWindow();

        // Configurar validación de campos
        setupValidation();
    }

    private void applyStyles() {
        // Aplicar estilos al tema oscuro con neón
        AppStyles.applyMainPaneStyle(mainPane);
        AppStyles.applyTitleBarStyle(titleBar);
        AppStyles.applyContentPaneStyle(loginPane);

        // Aplicar estilos a los botones de la ventana
        AppStyles.applyWindowButtonStyle(minimizeButton);
        AppStyles.applyWindowButtonStyle(maximizeButton);
        AppStyles.applyWindowButtonStyle(closeButton);

        // Aplicar estilos a las etiquetas
        AppStyles.applyTitleStyle(titleLabel);
        AppStyles.applySubtitleStyle(subtitleLabel);

        // Aplicar estilos a los campos de entrada
        AppStyles.applyTextFieldStyle(emailField);
        AppStyles.applyPasswordFieldStyle(passwordField);

        // Aplicar estilo al botón de inicio de sesión
        AppStyles.applyPrimaryButtonStyle(loginButton);

        // Para las etiquetas adicionales
        if (emailLabel != null) AppStyles.applyLabelStyle(emailLabel);
        if (passwordLabel != null) AppStyles.applyLabelStyle(passwordLabel);

        // Para hipervínculos
        if (registroLink != null) AppStyles.applyHyperlinkStyle(registroLink);

        // Aplicar estilos a todas las etiquetas que no sean título o subtítulo
        for (javafx.scene.Node node : mainPane.lookupAll("Label")) {
            if (node instanceof Label && node != titleLabel && node != subtitleLabel
                    && node != emailLabel && node != passwordLabel) {
                AppStyles.applyLabelStyle((Label) node);
            }
        }

        // Aplicar estilos a todos los hipervínculos
        for (javafx.scene.Node node : mainPane.lookupAll("Hyperlink")) {
            if (node instanceof Hyperlink && node != registroLink) {
                AppStyles.applyHyperlinkStyle((Hyperlink) node);
            }
        }
    }

    private void configureWindowButtons() {
        // Configurar los símbolos de los botones de ventana
        minimizeButton.setText("—");
        maximizeButton.setText("□");
        closeButton.setText("✕");

        // Asegurar que los botones sean del tamaño correcto
        minimizeButton.setMinWidth(30);
        maximizeButton.setMinWidth(30);
        closeButton.setMinWidth(30);
    }

    private void setupValidation() {
        // Validación simple en tiempo real
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$") && !newValue.isEmpty()) {
                // Resaltar con borde rojo cuando el email es inválido
                emailField.setBorder(new Border(new BorderStroke(
                        Color.rgb(255, 50, 50, 0.8),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1.5)
                )));

                // Efecto de resplandor rojo
                DropShadow errorShadow = new DropShadow();
                errorShadow.setColor(Color.rgb(255, 0, 0, 0.5));
                errorShadow.setRadius(10);
                errorShadow.setSpread(0.1);
                emailField.setEffect(errorShadow);
            } else {
                // Restaurar estilo normal
                AppStyles.applyTextFieldStyle(emailField);
            }
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() < 6 && !newValue.isEmpty()) {
                // Resaltar con borde rojo cuando la contraseña es demasiado corta
                passwordField.setBorder(new Border(new BorderStroke(
                        Color.rgb(255, 50, 50, 0.8),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(5),
                        new BorderWidths(1.5)
                )));

                // Efecto de resplandor rojo
                DropShadow errorShadow = new DropShadow();
                errorShadow.setColor(Color.rgb(255, 0, 0, 0.5));
                errorShadow.setRadius(10);
                errorShadow.setSpread(0.1);
                passwordField.setEffect(errorShadow);
            } else {
                // Restaurar estilo normal
                AppStyles.applyPasswordFieldStyle(passwordField);
            }
        });
    }

    private void setupDraggableWindow() {
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    @FXML
    private void handleMinimizeAction(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void handleMaximizeAction(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());

        // Cambiar el símbolo del botón según el estado
        if (stage.isMaximized()) {
            maximizeButton.setText("❐");  // Símbolo para restaurar
        } else {
            maximizeButton.setText("□");  // Símbolo para maximizar
        }
    }

    @FXML
    private void handleCloseAction(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validación básica
        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Por favor, complete todos los campos.");
            return;
        }

        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showAlert(Alert.AlertType.ERROR, "Error", "Por favor, ingrese un email válido.");
            return;
        }

        if (password.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        // Aquí iría la lógica de autenticación real
        // Por ahora, simulamos un login exitoso y abrimos la pantalla principal
        try {
            openMainDashboard();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Error al cargar la pantalla principal: " + e.getMessage());
        }
    }

    private void openMainDashboard() throws IOException {
        // Cargar la vista del dashboard principal
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
        Parent dashboardRoot = loader.load();

        // Configurar la nueva escena
        Scene dashboardScene = new Scene(dashboardRoot);
        dashboardScene.setFill(Color.TRANSPARENT);

        // Obtener el stage actual
        Stage currentStage = (Stage) loginButton.getScene().getWindow();

        // Establecer la nueva escena
        currentStage.setScene(dashboardScene);
        currentStage.setTitle("SmartSave - Panel Principal");

        // Centrar en pantalla (opcional)
        currentStage.centerOnScreen();
    }

    @FXML
    private void handleRegistroLinkAction(ActionEvent event) {
        try {
            // Cargar la vista de registro
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/registro.fxml"));
            Parent registroRoot = loader.load();

            // Configurar la nueva escena
            Scene registroScene = new Scene(registroRoot);
            registroScene.setFill(Color.TRANSPARENT);

            // Obtener el stage actual
            Stage currentStage = (Stage) registroLink.getScene().getWindow();

            // Establecer la nueva escena
            currentStage.setScene(registroScene);
            currentStage.setTitle("SmartSave - Registro de Usuario");

            // Centrar en pantalla (opcional)
            currentStage.centerOnScreen();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Error al cargar la pantalla de registro: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Estilizar la alerta con el tema oscuro
        DialogPane dialogPane = alert.getDialogPane();

        // Fondo oscuro
        dialogPane.setBackground(new Background(new BackgroundFill(
                Color.rgb(25, 25, 35, 0.95),
                new CornerRadii(10),
                null
        )));

        // Borde con efecto neón
        dialogPane.setBorder(new Border(new BorderStroke(
                Color.rgb(255, 0, 255, 0.7),
                BorderStrokeStyle.SOLID,
                new CornerRadii(10),
                new BorderWidths(1.5)
        )));

        // Color de texto
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // Botones con estilo neón
        for (ButtonType buttonType : alert.getButtonTypes()) {
            Button button = (Button) dialogPane.lookupButton(buttonType);

            // Fondo oscuro
            button.setBackground(new Background(new BackgroundFill(
                    Color.rgb(40, 40, 50, 1.0),
                    new CornerRadii(5),
                    null
            )));

            // Borde neón
            button.setBorder(new Border(new BorderStroke(
                    Color.rgb(160, 100, 255, 0.8),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(5),
                    new BorderWidths(1)
            )));

            // Texto claro
            button.setTextFill(Color.WHITE);

            // Efecto de brillo
            Glow glow = new Glow();
            glow.setLevel(0.3);
            button.setEffect(glow);

            // Eventos de hover
            button.setOnMouseEntered(e -> {
                button.setBackground(new Background(new BackgroundFill(
                        Color.rgb(60, 60, 70, 1.0),
                        new CornerRadii(5),
                        null
                )));

                DropShadow shadow = new DropShadow();
                shadow.setColor(Color.rgb(180, 100, 255, 0.8));
                shadow.setRadius(15);
                shadow.setSpread(0.2);
                button.setEffect(shadow);
            });

            button.setOnMouseExited(e -> {
                button.setBackground(new Background(new BackgroundFill(
                        Color.rgb(40, 40, 50, 1.0),
                        new CornerRadii(5),
                        null
                )));

                Glow originalGlow = new Glow();
                originalGlow.setLevel(0.3);
                button.setEffect(originalGlow);
            });
        }

        // Efecto de sombra para toda la alerta
        DropShadow alertShadow = new DropShadow();
        alertShadow.setColor(Color.rgb(0, 0, 0, 0.7));
        alertShadow.setRadius(20);
        alertShadow.setSpread(0.1);
        dialogPane.setEffect(alertShadow);

        // Mostrar y esperar
        alert.showAndWait();
    }
}