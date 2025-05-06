package smartsave.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
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
        // Aplicar estilos usando la clase de utilidad
        applyStyles();

        // Configurar el arrastre de la ventana
        setupDraggableWindow();

        // Configurar los textos de los botones de la barra de título
        configureWindowButtons();

        // Configurar validación de campos
        setupValidation();
    }

    private void applyStyles() {
        // Aplicar estilos usando la clase AppStyles mejorada
        AppStyles.applyMainPaneStyle(mainPane);
        AppStyles.applyTitleBarStyle(titleBar);
        AppStyles.applyContentPaneStyle(loginPane);
        AppStyles.applyWindowButtonStyle(minimizeButton);
        AppStyles.applyWindowButtonStyle(maximizeButton);
        AppStyles.applyWindowButtonStyle(closeButton);
        AppStyles.applyTitleStyle(titleLabel);
        AppStyles.applySubtitleStyle(subtitleLabel);
        AppStyles.applyPrimaryButtonStyle(loginButton);
        AppStyles.applyTextFieldStyle(emailField);
        AppStyles.applyTextFieldStyle(passwordField);

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
        // Configurar los símbolos de los botones de ventana al estilo Windows
        minimizeButton.setText("—");
        maximizeButton.setText("□");
        closeButton.setText("✕");
    }

    private void setupValidation() {
        // Validación simple en tiempo real
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$") && !newValue.isEmpty()) {
                emailField.setStyle(emailField.getStyle() + "-fx-border-color: rgba(255, 80, 80, 0.7);");
            } else {
                AppStyles.applyTextFieldStyle(emailField);
            }
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() < 6 && !newValue.isEmpty()) {
                passwordField.setStyle(passwordField.getStyle() + "-fx-border-color: rgba(255, 80, 80, 0.7);");
            } else {
                AppStyles.applyTextFieldStyle(passwordField);
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
        dashboardScene.setFill(javafx.scene.paint.Color.TRANSPARENT);

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
            registroScene.setFill(javafx.scene.paint.Color.TRANSPARENT);

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

        // Aplicar estilo Aero a la alerta
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("aero-alert");

        // Configurar estilo manualmente (ya que no podemos usar CSS)
        Stop[] stops = new Stop[] {
                new Stop(0, Color.rgb(240, 240, 240, 0.9)),
                new Stop(1, Color.rgb(220, 220, 220, 0.9))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);

        dialogPane.setBackground(new Background(new BackgroundFill(
                gradient,
                new CornerRadii(8),
                null
        )));

        dialogPane.setBorder(new Border(new BorderStroke(
                Color.rgb(255, 255, 255, 0.8),
                BorderStrokeStyle.SOLID,
                new CornerRadii(8),
                new BorderWidths(1)
        )));

        // Aplicar efecto de sombra
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setRadius(10);
        dialogPane.setEffect(shadow);

        // Mostrar y esperar
        alert.showAndWait();
    }
}