package smartsave.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import smartsave.utilidad.EstilosApp;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ConfiguracionController implements Initializable {

    // Referencias a elementos principales del layout
    @FXML private BorderPane mainPane;
    @FXML private HBox titleBar;
    @FXML private VBox sideMenu;

    // Referencias a los elementos de menú
    @FXML private Button dashboardButton;
    @FXML private Button transactionsButton;
    @FXML private Button nutritionButton;
    @FXML private Button shoppingButton;
    @FXML private Button savingsButton;
    @FXML private Button reportsButton;
    @FXML private Button settingsButton;
    @FXML private Button profileButton;
    @FXML private Button logoutButton;
    @FXML private Button minimizeButton;
    @FXML private Button maximizeButton;
    @FXML private Button closeButton;

    // Referencias a los elementos de configuración
    @FXML private ComboBox<String> temaComboBox;
    @FXML private ComboBox<String> idiomaComboBox;
    @FXML private Slider tamanoFuenteSlider;
    @FXML private Label tamanoFuenteLabel;

    @FXML private CheckBox notificacionesGastosCheckBox;
    @FXML private CheckBox notificacionesIngresoCheckBox;
    @FXML private CheckBox notificacionesComprasCheckBox;
    @FXML private CheckBox notificacionesAhorroCheckBox;
    @FXML private CheckBox emailNotificacionesCheckBox;

    @FXML private Button copiaSeguridad;
    @FXML private Button exportarDatos;
    @FXML private Button cambiarContrasena;
    @FXML private Label fechaUltimaCopia;

    @FXML private ComboBox<String> monedaComboBox;
    @FXML private ComboBox<String> decimalesComboBox;
    @FXML private CheckBox modoDebugCheckBox;

    @FXML private Label versionLabel;
    @FXML private Label autorLabel;
    @FXML private Label fechaCompilaciónLabel;
    @FXML private Label centroLabel;
    @FXML private Button licenciaButton;
    @FXML private Button privacidadButton;
    @FXML private Button acercaDeButton;

    @FXML private Button guardarConfiguracion;
    @FXML private Button restablecerConfiguracion;

    // Variables para permitir el arrastre de la ventana
    private double offsetX = 0;
    private double offsetY = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Aplicar estilos
        aplicarEstilos();

        // Configurar el arrastre de la ventana
        configurarVentanaArrastrable();

        // Configurar botones de navegación
        configurarBotonesNavegacion();

        // Inicializar configuración
        inicializarConfiguracion();

        // Cargar configuración actual
        cargarConfiguracionActual();
    }

    private void aplicarEstilos() {
        // Aplicar estilos al tema oscuro con neón
        EstilosApp.aplicarEstiloPanelPrincipal(mainPane);
        EstilosApp.aplicarEstiloBarraTitulo(titleBar);
        EstilosApp.aplicarEstiloMenuLateral(sideMenu);

        // Aplicar estilos a los botones de la ventana
        EstilosApp.aplicarEstiloBotonVentana(minimizeButton);
        EstilosApp.aplicarEstiloBotonVentana(maximizeButton);
        EstilosApp.aplicarEstiloBotonVentana(closeButton);

        // Aplicar estilos a los botones de navegación
        EstilosApp.aplicarEstiloBotonNavegacion(dashboardButton);
        EstilosApp.aplicarEstiloBotonNavegacion(transactionsButton);
        EstilosApp.aplicarEstiloBotonNavegacion(nutritionButton);
        EstilosApp.aplicarEstiloBotonNavegacion(shoppingButton);
        EstilosApp.aplicarEstiloBotonNavegacion(savingsButton);
        EstilosApp.aplicarEstiloBotonNavegacion(reportsButton);
        EstilosApp.aplicarEstiloBotonNavegacion(settingsButton);
        EstilosApp.aplicarEstiloBotonNavegacion(profileButton);
        EstilosApp.aplicarEstiloBotonNavegacion(logoutButton);

        // Destacar el botón de configuración como seleccionado
        settingsButton.getStyleClass().add("selected");

        // Aplicar estilos a los botones principales
        EstilosApp.aplicarEstiloBotonPrimario(guardarConfiguracion);
        EstilosApp.aplicarEstiloBotonPrimario(restablecerConfiguracion);
        EstilosApp.aplicarEstiloBotonPrimario(copiaSeguridad);
        EstilosApp.aplicarEstiloBotonPrimario(exportarDatos);
        EstilosApp.aplicarEstiloBotonPrimario(cambiarContrasena);
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

    private void configurarBotonesNavegacion() {
        // Configurar acción al seleccionar botones del menú
        dashboardButton.setOnAction(this::handleDashboardAction);
        transactionsButton.setOnAction(this::handleTransactionsAction);
        nutritionButton.setOnAction(this::handleNutritionAction);
        shoppingButton.setOnAction(this::handleShoppingAction);
        savingsButton.setOnAction(this::handleSavingsAction);
        reportsButton.setOnAction(this::handleReportsAction);
        settingsButton.setOnAction(this::handleSettingsAction);
        profileButton.setOnAction(this::handleProfileAction);
        logoutButton.setOnAction(this::handleLogoutAction);
    }

    private void inicializarConfiguracion() {
        // Configurar listener para el slider de tamaño de fuente
        tamanoFuenteSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            tamanoFuenteLabel.setText(String.format("%.0fpx", newValue.doubleValue()));
        });

        // Configurar valores por defecto
        temaComboBox.getSelectionModel().select("OSCURO");
        idiomaComboBox.getSelectionModel().select("ES");
        monedaComboBox.getSelectionModel().select("EUR");
        decimalesComboBox.getSelectionModel().select("2");

        // Inicializar fecha de última copia
        fechaUltimaCopia.setText("No disponible");
    }

    private void cargarConfiguracionActual() {
        // Cargar configuración desde archivo de propiedades o base de datos
        // Por ahora usar valores por defecto

        // Configurar notificaciones
        notificacionesGastosCheckBox.setSelected(true);
        notificacionesIngresoCheckBox.setSelected(true);
        notificacionesComprasCheckBox.setSelected(false);
        notificacionesAhorroCheckBox.setSelected(true);
        emailNotificacionesCheckBox.setSelected(false);
        modoDebugCheckBox.setSelected(false);

        // Información sobre la aplicación
        versionLabel.setText("1.0.0");
        autorLabel.setText("Leonel Yupanqui Serrano");
        fechaCompilaciónLabel.setText("28/04/2025");
        centroLabel.setText("Salesianos San Francisco De Sales El Buen Amigo");
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
    private void handleDashboardAction(ActionEvent evento) {
        try {
            // Cargar la vista del dashboard
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent raizDashboard = cargador.load();

            // Configurar la nueva escena
            Scene escenaDashboard = new Scene(raizDashboard);
            escenaDashboard.setFill(Color.TRANSPARENT);

            // Obtener el escenario actual
            Stage escenarioActual = (Stage) dashboardButton.getScene().getWindow();

            // Establecer la nueva escena
            escenarioActual.setScene(escenaDashboard);
            escenarioActual.setTitle("SmartSave - Dashboard");

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de navegación", "Error al cargar el dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleTransactionsAction(ActionEvent evento) {
        try {
            // Cargar la vista de transacciones
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/fxml/transacciones.fxml"));
            Parent raizTransacciones = cargador.load();

            // Configurar la nueva escena
            Scene escenaTransacciones = new Scene(raizTransacciones);
            escenaTransacciones.setFill(Color.TRANSPARENT);

            // Obtener el escenario actual
            Stage escenarioActual = (Stage) transactionsButton.getScene().getWindow();

            // Establecer la nueva escena
            escenarioActual.setScene(escenaTransacciones);
            escenarioActual.setTitle("SmartSave - Gestión de Ingresos y Gastos");

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de navegación", "Error al cargar la pantalla de transacciones: " + e.getMessage());
        }
    }

    @FXML
    private void handleNutritionAction(ActionEvent evento) {
        try {
            // Cargar la vista de perfil nutricional
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/fxml/nutricion.fxml"));
            Parent raizNutricion = cargador.load();

            // Configurar la nueva escena
            Scene escenaNutricion = new Scene(raizNutricion);
            escenaNutricion.setFill(Color.TRANSPARENT);

            // Obtener el escenario actual
            Stage escenarioActual = (Stage) nutritionButton.getScene().getWindow();

            // Establecer la nueva escena
            escenarioActual.setScene(escenaNutricion);
            escenarioActual.setTitle("SmartSave - Perfil Nutricional");

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de navegación", "Error al cargar la pantalla de perfil nutricional: " + e.getMessage());
        }
    }

    @FXML
    private void handleShoppingAction(ActionEvent evento) {
        try {
            // Cargar la vista de plan de compras
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/fxml/compras.fxml"));
            Parent raizCompras = cargador.load();

            // Configurar la nueva escena
            Scene escenaCompras = new Scene(raizCompras);
            escenaCompras.setFill(Color.TRANSPARENT);

            // Obtener el escenario actual
            Stage escenarioActual = (Stage) shoppingButton.getScene().getWindow();

            // Establecer la nueva escena
            escenarioActual.setScene(escenaCompras);
            escenarioActual.setTitle("SmartSave - Plan de Compras");

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de navegación", "Error al cargar la pantalla de plan de compras: " + e.getMessage());
        }
    }

    @FXML
    private void handleSavingsAction(ActionEvent evento) {
        try {
            // Cargar la vista de modalidades de ahorro
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/fxml/ahorro.fxml"));
            Parent raizAhorro = cargador.load();

            // Configurar la nueva escena
            Scene escenaAhorro = new Scene(raizAhorro);
            escenaAhorro.setFill(Color.TRANSPARENT);

            // Obtener el escenario actual
            Stage escenarioActual = (Stage) savingsButton.getScene().getWindow();

            // Establecer la nueva escena
            escenarioActual.setScene(escenaAhorro);
            escenarioActual.setTitle("SmartSave - Modalidades de Ahorro");

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de navegación", "Error al cargar la pantalla de modalidades de ahorro: " + e.getMessage());
        }
    }

    @FXML
    private void handleReportsAction(ActionEvent evento) {
        // Cambiar a la vista de informes
        activarBoton(reportsButton);
        mostrarAlertaNoImplementado("Informes");
    }

    @FXML
    private void handleSettingsAction(ActionEvent evento) {
        // Ya estamos en la vista de configuración, solo activamos el botón
        activarBoton(settingsButton);
    }

    @FXML
    private void handleProfileAction(ActionEvent evento) {
        try {
            // Cargar la vista de perfil
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/fxml/perfil.fxml"));
            Parent raizPerfil = cargador.load();

            // Configurar la nueva escena
            Scene escenaPerfil = new Scene(raizPerfil);
            escenaPerfil.setFill(Color.TRANSPARENT);

            // Obtener el escenario actual
            Stage escenarioActual = (Stage) profileButton.getScene().getWindow();

            // Establecer la nueva escena
            escenarioActual.setScene(escenaPerfil);
            escenarioActual.setTitle("SmartSave - Mi Perfil");

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de navegación", "Error al cargar la pantalla de perfil: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogoutAction(ActionEvent evento) {
        // Mostrar confirmación antes de cerrar sesión
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Cerrar Sesión");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Estás seguro que deseas cerrar la sesión?");

        // Estilizar alerta
        DialogPane dialogPane = alerta.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #1A1A25; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-color: #FF00FF; " +
                        "-fx-border-width: 1px;"
        );

        dialogPane.lookupAll(".label").forEach(node ->
                node.setStyle("-fx-text-fill: white;")
        );

        dialogPane.lookupAll(".button").forEach(node -> {
            node.setStyle(
                    "-fx-background-color: #25253A; " +
                            "-fx-text-fill: white; " +
                            "-fx-border-color: #4050FF; " +
                            "-fx-border-width: 1px;"
            );
        });

        alerta.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    // Volver a la pantalla de login
                    FXMLLoader cargador = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                    Parent raizLogin = cargador.load();

                    Scene escenaLogin = new Scene(raizLogin);
                    escenaLogin.setFill(Color.TRANSPARENT);

                    Stage escenarioActual = (Stage) logoutButton.getScene().getWindow();
                    escenarioActual.setScene(escenaLogin);
                    escenarioActual.setTitle("SmartSave - Login");
                    escenarioActual.centerOnScreen();

                } catch (IOException e) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error al volver a la pantalla de login", e.getMessage());
                }
            }
        });
    }

    // Métodos para manejar la configuración

    @FXML
    private void handleCrearCopiaSeguridadAction(ActionEvent evento) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Copia de Seguridad");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivo de Copia de Seguridad", "*.backup")
            );

            Stage stage = (Stage) copiaSeguridad.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                // Simular creación de copia de seguridad
                // Aquí iría la lógica real de backup
                Thread.sleep(2000); // Simular proceso

                // Actualizar fecha de última copia
                fechaUltimaCopia.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

                mostrarAlerta(Alert.AlertType.INFORMATION, "Copia de Seguridad",
                        "La copia de seguridad se ha creado correctamente en:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al crear la copia de seguridad: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportarDatosAction(ActionEvent evento) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exportar Datos");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Archivo CSV", "*.csv"),
                    new FileChooser.ExtensionFilter("Archivo JSON", "*.json")
            );

            Stage stage = (Stage) exportarDatos.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                // Simular exportación de datos
                Thread.sleep(1500);

                mostrarAlerta(Alert.AlertType.INFORMATION, "Exportar Datos",
                        "Los datos se han exportado correctamente a:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al exportar los datos: " + e.getMessage());
        }
    }

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
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField contrasenaActual = new PasswordField();
        contrasenaActual.setPromptText("Contraseña actual");
        PasswordField nuevaContrasena = new PasswordField();
        nuevaContrasena.setPromptText("Nueva contraseña");
        PasswordField confirmarContrasena = new PasswordField();
        confirmarContrasena.setPromptText("Confirmar nueva contraseña");

        grid.add(new Label("Contraseña actual:"), 0, 0);
        grid.add(contrasenaActual, 1, 0);
        grid.add(new Label("Nueva contraseña:"), 0, 1);
        grid.add(nuevaContrasena, 1, 1);
        grid.add(new Label("Confirmar contraseña:"), 0, 2);
        grid.add(confirmarContrasena, 1, 2);

        dialog.getDialogPane().setContent(grid);
        contrasenaActual.requestFocus();

        // Estilizar el diálogo
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #1A1A25; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-color: #FF00FF; " +
                        "-fx-border-width: 1px;"
        );

        dialogPane.lookupAll(".label").forEach(node ->
                node.setStyle("-fx-text-fill: white;")
        );

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
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Todos los campos son obligatorios.");
                return;
            }

            if (!nueva.equals(confirmar)) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Las contraseñas nuevas no coinciden.");
                return;
            }

            if (nueva.length() < 6) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "La nueva contraseña debe tener al menos 6 caracteres.");
                return;
            }

            // Simular cambio de contraseña
            mostrarAlerta(Alert.AlertType.INFORMATION, "Contraseña Cambiada",
                    "Tu contraseña ha sido cambiada correctamente.");
        });
    }

    @FXML
    private void handleVerLicenciaAction(ActionEvent evento) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Licencia de Software");
        alerta.setHeaderText("SmartSave - Licencia MIT");
        alerta.setContentText(
                "Copyright © 2025 Leonel Yupanqui Serrano\n\n" +
                        "Por la presente se concede permiso, libre de cargos, a cualquier persona " +
                        "que obtenga una copia de este software y de los archivos de documentación " +
                        "asociados (el \"Software\"), a utilizar el Software sin restricción, " +
                        "incluyendo sin limitación los derechos a usar, copiar, modificar, fusionar, " +
                        "publicar, distribuir, sublicenciar, y/o vender copias del Software...\n\n" +
                        "EL SOFTWARE SE PROPORCIONA \"COMO ESTÁ\", SIN GARANTÍA DE NINGÚN TIPO."
        );

        estilizarAlerta(alerta);
        alerta.showAndWait();
    }

    @FXML
    private void handlePoliticaPrivacidadAction(ActionEvent evento) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Política de Privacidad");
        alerta.setHeaderText("SmartSave - Política de Privacidad");
        alerta.setContentText(
                "SmartSave respeta tu privacidad y se compromete a proteger tus datos personales.\n\n" +
                        "1. Los datos financieros se almacenan localmente en tu dispositivo.\n" +
                        "2. No compartimos tu información personal con terceros.\n" +
                        "3. Las funcionalidades de backup son opcionales y controladas por el usuario.\n" +
                        "4. Los datos nutricionales se procesan localmente para recomendaciones.\n\n" +
                        "Para más información, contacta al desarrollador."
        );

        estilizarAlerta(alerta);
        alerta.showAndWait();
    }

    @FXML
    private void handleAcercaDeAction(ActionEvent evento) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Acerca de SmartSave");
        alerta.setHeaderText("SmartSave v1.0.0");
        alerta.setContentText(
                "SmartSave es una aplicación de gestión financiera y nutricional desarrollada " +
                        "como proyecto de fin de grado.\n\n" +
                        "Desarrollador: Leonel Yupanqui Serrano\n" +
                        "Centro educativo: Salesianos San Francisco De Sales El Buen Amigo\n" +
                        "Año: 2025\n\n" +
                        "La aplicación combina la gestión de finanzas personales con recomendaciones " +
                        "nutricionales para ayudar a los usuarios a optimizar tanto su economía como su salud."
        );

        estilizarAlerta(alerta);
        alerta.showAndWait();
    }

    @FXML
    private void handleGuardarConfiguracionAction(ActionEvent evento) {
        try {
            // Simular guardado de configuración
            Thread.sleep(1000);

            // Aquí iría la lógica real para guardar la configuración
            // Podría usar Properties, JSON, o base de datos

            mostrarAlerta(Alert.AlertType.INFORMATION, "Configuración Guardada",
                    "La configuración se ha guardado correctamente.");
        } catch (InterruptedException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al guardar la configuración.");
        }
    }

    @FXML
    private void handleRestablecerConfiguracionAction(ActionEvent evento) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Restablecer Configuración");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Estás seguro de que deseas restablecer la configuración a los valores por defecto? Esta acción no se puede deshacer.");

        estilizarAlerta(confirmacion);

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                // Restablecer valores por defecto
                temaComboBox.getSelectionModel().select("OSCURO");
                idiomaComboBox.getSelectionModel().select("ES");
                tamanoFuenteSlider.setValue(14);

                notificacionesGastosCheckBox.setSelected(true);
                notificacionesIngresoCheckBox.setSelected(true);
                notificacionesComprasCheckBox.setSelected(false);
                notificacionesAhorroCheckBox.setSelected(true);
                emailNotificacionesCheckBox.setSelected(false);

                monedaComboBox.getSelectionModel().select("EUR");
                decimalesComboBox.getSelectionModel().select("2");
                modoDebugCheckBox.setSelected(false);

                mostrarAlerta(Alert.AlertType.INFORMATION, "Configuración Restablecida",
                        "La configuración ha sido restablecida a los valores por defecto.");
            }
        });
    }

    private void activarBoton(Button botonActivo) {
        // Quitar la clase 'selected' de todos los botones
        dashboardButton.getStyleClass().remove("selected");
        transactionsButton.getStyleClass().remove("selected");
        nutritionButton.getStyleClass().remove("selected");
        shoppingButton.getStyleClass().remove("selected");
        savingsButton.getStyleClass().remove("selected");
        reportsButton.getStyleClass().remove("selected");
        settingsButton.getStyleClass().remove("selected");
        profileButton.getStyleClass().remove("selected");

        // Añadir la clase 'selected' al botón activo
        botonActivo.getStyleClass().add("selected");
    }

    private void mostrarAlertaNoImplementado(String caracteristica) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(caracteristica + "