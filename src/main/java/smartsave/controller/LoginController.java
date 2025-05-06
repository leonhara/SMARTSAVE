package smartsave.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import smartsave.util.AppStyles;

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

    // Variables para permitir el arrastre de la ventana
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Aplicar estilos usando la clase de utilidad
        applyStyles();

        // Configurar el arrastre de la ventana
        setupDraggableWindow();
    }

    private void applyStyles() {
        // Aplicar estilos usando la clase AppStyles
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

        // Por ahora, solo mostraremos una alerta
        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Por favor, complete todos los campos.");
            return;
        }

        // Aquí iría la lógica de autenticación real
        showAlert("Información", "Intento de login con:\nEmail: " + email);
    }

    @FXML
    private void handleRegistroLinkAction(ActionEvent event) {
        // Aquí iría la navegación a la pantalla de registro
        showAlert("Registro", "Funcionalidad de registro pendiente de implementar");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}