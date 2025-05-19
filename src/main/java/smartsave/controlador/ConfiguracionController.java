package smartsave.controlador;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import smartsave.utilidad.EstilosApp;

/**
 * Controlador para la vista de Configuración
 * Extiende BaseController para heredar funcionalidad común
 */
public class ConfiguracionController extends BaseController {

    // Referencias a los elementos de configuración
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

    /**
     * Inicialización específica del controlador de configuración
     * Implementación del método abstracto de BaseController
     */
    @Override
    protected void inicializarControlador() {
        // Destacar el botón de configuración como seleccionado
        activarBoton(settingsButton);

        // Inicializar configuración
        inicializarConfiguracion();

        // Cargar configuración actual
        cargarConfiguracionActual();

        // Aplicar estilos personalizados
        aplicarEstilosComponentes();
    }

    /**
     * Aplica estilos a los componentes específicos de esta pantalla
     */
    private void aplicarEstilosComponentes() {
        // Aplicar estilos a los ComboBox
        EstilosApp.aplicarEstiloComboBox(temaComboBox);
        EstilosApp.aplicarEstiloComboBox(idiomaComboBox);

        // Aplicar estilos a los botones
        EstilosApp.aplicarEstiloBotonPrimario(cambiarContrasena);
        EstilosApp.aplicarEstiloBotonPrimario(licenciaButton);
        EstilosApp.aplicarEstiloBotonPrimario(privacidadButton);
        EstilosApp.aplicarEstiloBotonPrimario(acercaDeButton);

        // Aplicar estilos a las etiquetas de información
        estilizarEtiquetasInformacion();

        // Aplicar estilos a los paneles contenedores
        estilizarPanelesContenedores();

        // Aplicar estilo al ScrollBar (si está disponible)
        aplicarEstiloScrollBarNeon();
    }

    /**
     * Aplica estilos a las etiquetas de información
     */
    private void estilizarEtiquetasInformacion() {
        // Estilo para etiquetas de información
        String estiloEtiquetaInfo = "-fx-text-fill: rgb(180, 180, 220); -fx-font-size: 14px;";

        // Aplicar el estilo a las etiquetas de información
        versionLabel.setStyle(estiloEtiquetaInfo);
        autorLabel.setStyle(estiloEtiquetaInfo);
        fechaCompilacionLabel.setStyle(estiloEtiquetaInfo);
        centroLabel.setStyle(estiloEtiquetaInfo);
    }

    /**
     * Aplica estilos a los paneles contenedores
     */
    private void estilizarPanelesContenedores() {
        // Buscar paneles VBox que contienen secciones de configuración
        for (javafx.scene.Node nodo : mainPane.lookupAll("VBox")) {
            if (nodo instanceof VBox && nodo != sideMenu) {
                VBox panel = (VBox) nodo;
                // Verificar si es un panel de sección buscando hijos específicos
                if (!panel.getChildren().isEmpty() && panel.getChildren().get(0) instanceof Label) {
                    EstilosApp.aplicarEstiloTarjeta(panel);
                }
            }
        }
    }

    /**
     * Inicializa los elementos de configuración
     */
    private void inicializarConfiguracion() {
        // PRIMERO agregar items a los ComboBox
        // Inicializar ComboBox de tema
        temaComboBox.getItems().addAll("Oscuro (Actual)", "Claro", "Automático");

        // Inicializar ComboBox de idioma
        idiomaComboBox.getItems().addAll("Español", "English", "Français");

        // DESPUÉS seleccionar valores por defecto
        temaComboBox.setValue("Oscuro (Actual)");
        idiomaComboBox.setValue("Español");
    }

    /**
     * Carga la configuración actual del sistema
     */
    private void cargarConfiguracionActual() {
        // Información sobre la aplicación
        versionLabel.setText("1.0.0");
        autorLabel.setText("Leonel Yupanqui Serrano");
        fechaCompilacionLabel.setText("28/04/2025");
        centroLabel.setText("Salesianos San Francisco De Sales El Buen Amigo");
    }

    /**
     * Manejador para el cambio de contraseña
     */
    @FXML
    private void handleCambiarContrasenaAction(ActionEvent evento) {
        // Crear diálogo para cambiar contraseña
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Cambiar Contraseña");
        dialog.setHeaderText("Ingresa tu nueva contraseña");

        // Configurar botones
        ButtonType cambiarButtonType = new ButtonType("Cambiar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(cambiarButtonType, ButtonType.CANCEL);

        // Crear el formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        PasswordField contrasenaActual = new PasswordField();
        contrasenaActual.setPromptText("Contraseña actual");
        PasswordField nuevaContrasena = new PasswordField();
        nuevaContrasena.setPromptText("Nueva contraseña");
        PasswordField confirmarContrasena = new PasswordField();
        confirmarContrasena.setPromptText("Confirmar nueva contraseña");

        // Aplicar estilos a los campos
        EstilosApp.aplicarEstiloCampoContraseña(contrasenaActual);
        EstilosApp.aplicarEstiloCampoContraseña(nuevaContrasena);
        EstilosApp.aplicarEstiloCampoContraseña(confirmarContrasena);

        grid.add(new Label("Contraseña actual:"), 0, 0);
        grid.add(contrasenaActual, 1, 0);
        grid.add(new Label("Nueva contraseña:"), 0, 1);
        grid.add(nuevaContrasena, 1, 1);
        grid.add(new Label("Confirmar contraseña:"), 0, 2);
        grid.add(confirmarContrasena, 1, 2);

        dialog.getDialogPane().setContent(grid);
        contrasenaActual.requestFocus();

        // Estilizar el diálogo
        navegacionServicio.estilizarDialog(dialog);

        // Convertir el resultado cuando se presiona el botón cambiar
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == cambiarButtonType) {
                return new String[]{contrasenaActual.getText(), nuevaContrasena.getText(), confirmarContrasena.getText()};
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String actual = result[0];
            String nueva = result[1];
            String confirmar = result[2];

            // Validar contraseñas
            if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
                navegacionServicio.mostrarAlertaError("Error", "Todos los campos son obligatorios.");
                return;
            }

            if (!nueva.equals(confirmar)) {
                navegacionServicio.mostrarAlertaError("Error", "Las contraseñas nuevas no coinciden.");
                return;
            }

            if (nueva.length() < 6) {
                navegacionServicio.mostrarAlertaError("Error", "La nueva contraseña debe tener al menos 6 caracteres.");
                return;
            }

            // Simular cambio de contraseña
            navegacionServicio.mostrarAlertaInformacion("Contraseña Cambiada",
                    "Tu contraseña ha sido cambiada correctamente.");
        });
    }

    /**
     * Manejador para ver la licencia del software
     */
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

    /**
     * Manejador para ver la política de privacidad
     */
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

    /**
     * Manejador para ver la información acerca de la aplicación
     */
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

    /**
     * Aplica estilos al ScrollBar
     */
    private void aplicarEstiloScrollBarNeon() {
        Platform.runLater(() -> {
            ScrollPane scrollPane = (ScrollPane) mainPane.getCenter();

            if (scrollPane != null) {
                // Aplicar estilos centralizados a través de EstilosApp
                EstilosApp.aplicarEstiloScrollPane(scrollPane);
            }
        });
    }

    /**
     * Sobrescribe el método de navegación a configuración
     * Ya que estamos en la vista de configuración
     */
    @Override
    public void handleSettingsAction(ActionEvent evento) {
        // Ya estamos en la vista de configuración, solo activamos el botón
        activarBoton(settingsButton);
    }
}