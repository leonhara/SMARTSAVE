package smartsave.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private VBox loginPane;

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Aplicar estilos usando Java
        applyStyles();
    }

    private void applyStyles() {
        // Estilo para el fondo
        loginPane.setStyle("-fx-background-color: #f4f4f4;");

        // Estilo para el título
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        // Estilo para el subtítulo
        subtitleLabel.setFont(Font.font("Segoe UI", 16));
        subtitleLabel.setTextFill(Color.web("#7f8c8d"));

        // Estilo para el botón de login
        loginButton.setStyle(
                "-fx-background-color: #3498db; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 20; " +
                        "-fx-background-radius: 5;"
        );

        // Estilos para los campos de texto
        String textFieldStyle =
                "-fx-padding: 8; " +
                        "-fx-background-radius: 4;";

        emailField.setStyle(textFieldStyle);
        passwordField.setStyle(textFieldStyle);
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