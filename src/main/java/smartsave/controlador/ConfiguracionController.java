package smartsave.controlador;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import smartsave.modelo.Usuario;
import smartsave.servicio.SessionManager;
import smartsave.servicio.UsuarioServicio;
import smartsave.utilidad.EstilosApp;

public class ConfiguracionController extends BaseController {

    @FXML private ComboBox<String> temaComboBox;
    @FXML private ComboBox<String> idiomaComboBox;
    @FXML private Button cambiarContrasena;
    @FXML private Label versionLabel;
    @FXML private Label autorLabel;
    @FXML private Label fechaCompilacionLabel;
    @FXML private Label centroLabel;
    @FXML private Button licenciaButton;
    @FXML private Button privacidadButton;
    @FXML private Button acercaDeButton;

    private UsuarioServicio usuarioServicio = new UsuarioServicio();
    private Usuario usuarioActualLocal;
    private Long usuarioIdActualLocal;


    @Override
    protected void inicializarControlador() {
        this.usuarioActualLocal = SessionManager.getInstancia().getUsuarioActual();

        if (this.usuarioActualLocal == null) {
            System.err.println("Error crítico: No hay usuario en sesión en ConfiguracionController.");
            if (navegacionServicio != null) {
                navegacionServicio.mostrarAlertaError("Error de Sesión", "No se pudo identificar al usuario. Por favor, inicie sesión de nuevo.");
                if (mainPane != null && mainPane.getScene() != null && mainPane.getScene().getWindow() instanceof Stage) {
                    Stage stage = (Stage) mainPane.getScene().getWindow();
                    if (stage != null) {
                        navegacionServicio.navegarALogin(stage);
                    }
                }
            }
            if (cambiarContrasena != null) cambiarContrasena.setDisable(true);
        } else {
            this.usuarioIdActualLocal = this.usuarioActualLocal.getId();
            if (cambiarContrasena != null) cambiarContrasena.setDisable(false);
        }

        activarBoton(settingsButton);
        inicializarConfiguracion();
        cargarConfiguracionActual();
        aplicarEstilosComponentes();
    }

    private void aplicarEstilosComponentes() {
        if (temaComboBox != null) EstilosApp.aplicarEstiloComboBox(temaComboBox);
        if (idiomaComboBox != null) EstilosApp.aplicarEstiloComboBox(idiomaComboBox);
        if (cambiarContrasena != null) EstilosApp.aplicarEstiloBotonPrimario(cambiarContrasena);
        if (licenciaButton != null) EstilosApp.aplicarEstiloBotonPrimario(licenciaButton);
        if (privacidadButton != null) EstilosApp.aplicarEstiloBotonPrimario(privacidadButton);
        if (acercaDeButton != null) EstilosApp.aplicarEstiloBotonPrimario(acercaDeButton);
        estilizarEtiquetasInformacion();
        estilizarPanelesContenedores();
        aplicarEstiloScrollBarNeon();
    }

    private void estilizarEtiquetasInformacion() {
        String estiloEtiquetaInfo = "-fx-text-fill: rgb(180, 180, 220); -fx-font-size: 14px;";
        if (versionLabel != null) versionLabel.setStyle(estiloEtiquetaInfo);
        if (autorLabel != null) autorLabel.setStyle(estiloEtiquetaInfo);
        if (fechaCompilacionLabel != null) fechaCompilacionLabel.setStyle(estiloEtiquetaInfo);
        if (centroLabel != null) centroLabel.setStyle(estiloEtiquetaInfo);
    }

    private void estilizarPanelesContenedores() {
        if (mainPane == null) return;
        for (javafx.scene.Node nodo : mainPane.lookupAll("VBox")) {
            if (nodo instanceof VBox && nodo != sideMenu) {
                VBox panel = (VBox) nodo;
                if (!panel.getChildren().isEmpty() && panel.getChildren().get(0) instanceof Label) {
                    EstilosApp.aplicarEstiloTarjeta(panel);
                }
            }
        }
    }

    private void inicializarConfiguracion() {
        if (temaComboBox != null) {
            temaComboBox.getItems().addAll("Oscuro (Actual)", "Claro", "Automático");
            temaComboBox.setValue("Oscuro (Actual)");
        }
        if (idiomaComboBox != null) {
            idiomaComboBox.getItems().addAll("Español", "English", "Français");
            idiomaComboBox.setValue("Español");
        }
    }

    private void cargarConfiguracionActual() {
        if (versionLabel != null) versionLabel.setText("1.5.2");
        if (autorLabel != null) autorLabel.setText("Leonel Yupanqui Serrano");
        if (fechaCompilacionLabel != null) fechaCompilacionLabel.setText("18/05/2025");
        if (centroLabel != null) centroLabel.setText("Salesianos San Francisco De Sales El Buen Amigo");
    }

    @FXML
    private void handleCambiarContrasenaAction(ActionEvent evento) {
        if (usuarioActualLocal == null) {
            navegacionServicio.mostrarAlertaError("Error", "Debe iniciar sesión para cambiar la contraseña.");
            return;
        }

        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Cambiar Contraseña");
        dialog.setHeaderText("Ingresa tu nueva contraseña");
        ButtonType cambiarButtonType = new ButtonType("Cambiar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(cambiarButtonType, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        PasswordField contrasenaActualField = new PasswordField(); contrasenaActualField.setPromptText("Contraseña actual");
        PasswordField nuevaContrasenaField = new PasswordField(); nuevaContrasenaField.setPromptText("Nueva contraseña");
        PasswordField confirmarContrasenaField = new PasswordField(); confirmarContrasenaField.setPromptText("Confirmar nueva contraseña");
        EstilosApp.aplicarEstiloCampoContrasena(contrasenaActualField);
        EstilosApp.aplicarEstiloCampoContrasena(nuevaContrasenaField);
        EstilosApp.aplicarEstiloCampoContrasena(confirmarContrasenaField);
        grid.add(new Label("Contraseña actual:"), 0, 0); grid.add(contrasenaActualField, 1, 0);
        grid.add(new Label("Nueva contraseña:"), 0, 1); grid.add(nuevaContrasenaField, 1, 1);
        grid.add(new Label("Confirmar contraseña:"), 0, 2); grid.add(confirmarContrasenaField, 1, 2);
        dialog.getDialogPane().setContent(grid);
        contrasenaActualField.requestFocus();
        navegacionServicio.estilizarDialog(dialog);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == cambiarButtonType) {
                return new String[]{contrasenaActualField.getText(), nuevaContrasenaField.getText(), confirmarContrasenaField.getText()};
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String actual = result[0];
            String nueva = result[1];
            String confirmar = result[2];
            if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
                navegacionServicio.mostrarAlertaError("Error", "Todos los campos son obligatorios."); return;
            }
            if (!nueva.equals(confirmar)) {
                navegacionServicio.mostrarAlertaError("Error", "Las contraseñas nuevas no coinciden."); return;
            }
            if (nueva.length() < 6) {
                navegacionServicio.mostrarAlertaError("Error", "La nueva contraseña debe tener al menos 6 caracteres."); return;
            }

            if (!usuarioActualLocal.getContrasenaHash().equals(actual)) {
                navegacionServicio.mostrarAlertaError("Error", "La contraseña actual es incorrecta."); return;
            }

            usuarioActualLocal.setContrasenaHash(nueva);
            if (usuarioServicio.actualizarUsuario(usuarioActualLocal)) {
                navegacionServicio.mostrarAlertaInformacion("Contraseña Cambiada", "Tu contraseña ha sido cambiada correctamente.");
            } else {
                navegacionServicio.mostrarAlertaError("Error", "No se pudo actualizar la contraseña.");
            }
        });
    }

    @FXML
    private void handleVerLicenciaAction(ActionEvent evento) {
        navegacionServicio.mostrarAlertaInformacion("Licencia de Software",
                "SmartSave - Licencia MIT\n\n" +
                        "Copyright © 2025 Leonel Yupanqui Serrano\n\n" +
                        "Por la presente se concede permiso, libre de cargos, a cualquier persona " +
                        "que obtenga una copia de este software y de los archivos de documentación " +
                        "asociados (el \"Software\"), a utilizar el Software sin restricción, " +
                        "incluyendo sin limitación los derechos a usar, copiar, modificar, fusionar, " +
                        "publicar, distribuir, sublicenciar, y/o vender copias del Software...\n\n" +
                        "EL SOFTWARE SE PROPORCIONA \"COMO ESTÁ\", SIN GARANTÍA DE NINGÚN TIPO.");
    }

    @FXML
    private void handlePoliticaPrivacidadAction(ActionEvent evento) {
        navegacionServicio.mostrarAlertaInformacion("Política de Privacidad",
                "SmartSave - Política de Privacidad\n\n" +
                        "SmartSave respeta tu privacidad y se compromete a proteger tus datos personales.\n\n" +
                        "1. Los datos financieros se almacenan localmente en tu dispositivo.\n" +
                        "2. No compartimos tu información personal con terceros.\n" +
                        "3. Las funcionalidades de backup son opcionales y controladas por el usuario.\n" +
                        "4. Los datos nutricionales se procesan localmente para recomendaciones.\n\n" +
                        "Para más información, contacta al desarrollador.");
    }

    @FXML
    private void handleAcercaDeAction(ActionEvent evento) {
        navegacionServicio.mostrarAlertaInformacion("Acerca de SmartSave",
                "SmartSave v1.0.0\n\n" +
                        "SmartSave es una aplicación de gestión financiera y nutricional desarrollada " +
                        "como proyecto de fin de grado.\n\n" +
                        "Desarrollador: Leonel Yupanqui Serrano\n" +
                        "Centro educativo: Salesianos San Francisco De Sales El Buen Amigo\n" +
                        "Año: 2025\n\n" +
                        "La aplicación combina la gestión de finanzas personales con recomendaciones " +
                        "nutricionales para ayudar a los usuarios a optimizar tanto su economía como su salud.");
    }

    private void aplicarEstiloScrollBarNeon() {
        Platform.runLater(() -> {
            if (mainPane != null && mainPane.getCenter() instanceof ScrollPane) {
                ScrollPane scrollPane = (ScrollPane) mainPane.getCenter();
                EstilosApp.aplicarEstiloScrollPane(scrollPane);
            }
        });
    }

    @Override
    public void handleSettingsAction(ActionEvent evento) {
        this.usuarioActualLocal = SessionManager.getInstancia().getUsuarioActual();
        if (this.usuarioActualLocal == null) {
            if (cambiarContrasena != null) cambiarContrasena.setDisable(true);
        } else {
            this.usuarioIdActualLocal = this.usuarioActualLocal.getId();
            if (cambiarContrasena != null) cambiarContrasena.setDisable(false);
        }
        activarBoton(settingsButton);
    }
}