package smartsave.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import smartsave.modelo.PerfilNutricional;
import smartsave.servicio.PerfilNutricionalServicio;
import smartsave.utilidad.EstilosApp;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class NutricionController implements Initializable {

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

    // Referencias a elementos del formulario
    @FXML private TextField edadField;
    @FXML private TextField pesoField;
    @FXML private TextField alturaField;
    @FXML private RadioButton sexoMRadio;
    @FXML private RadioButton sexoFRadio;
    @FXML private ToggleGroup sexoGroup;
    @FXML private ComboBox<String> actividadComboBox;
    @FXML private VBox restriccionesPane;
    @FXML private Button guardarPerfilButton;

    // Referencias a elementos de resumen
    @FXML private Label imcValueLabel;
    @FXML private Label imcCategoryLabel;
    @FXML private Label caloriasLabel;
    @FXML private Label proteinasLabel;
    @FXML private Label carbosLabel;
    @FXML private Label grasasLabel;
    @FXML private Label puntuacionLabel;
    @FXML private ProgressBar puntuacionProgress;
    @FXML private TextArea recomendacionesArea;
    @FXML private PieChart macrosPieChart;

    // Servicios
    private PerfilNutricionalServicio perfilServicio = new PerfilNutricionalServicio();

    // Variables de estado
    private Long usuarioIdActual = 1L; // Simulado, en un caso real vendría de la sesión
    private List<CheckBox> restriccionesCheckboxes = new ArrayList<>();
    private PerfilNutricional perfilActual = null;

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

        // Inicializar elementos del formulario
        inicializarFormulario();

        // Cargar datos del perfil si existe
        cargarPerfilExistente();
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

        // Destacar el botón de nutrición como seleccionado
        nutritionButton.getStyleClass().add("selected");

        // Aplicar estilos a los gráficos
        EstilosApp.aplicarEstiloGrafico(macrosPieChart);

        // Aplicar estilos a los campos de texto
        EstilosApp.aplicarEstiloCampoTexto(edadField);
        EstilosApp.aplicarEstiloCampoTexto(pesoField);
        EstilosApp.aplicarEstiloCampoTexto(alturaField);

        // Aplicar estilos a TextArea
        recomendacionesArea.setStyle(
                "-fx-background-color: rgba(40, 40, 50, 0.7); " +
                        "-fx-text-fill: rgb(230, 230, 250); " +
                        "-fx-border-color: rgba(80, 80, 120, 0.5); " +
                        "-fx-border-radius: 5px;"
        );

        // Aplicar estilos al botón de guardar
        EstilosApp.aplicarEstiloBotonPrimario(guardarPerfilButton);
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

    private void inicializarFormulario() {
        // Configurar validación de campos numéricos
        configurarValidacionCampos();

        // Cargar niveles de actividad
        actividadComboBox.setItems(FXCollections.observableArrayList(
                perfilServicio.obtenerNivelesActividad()
        ));

        // Crear checkboxes para restricciones alimentarias
        List<String> restricciones = perfilServicio.obtenerRestriccionesAlimentarias();

        restriccionesPane.getChildren().clear();
        restriccionesCheckboxes.clear();

        // Crear checkboxes por fila (2 por fila)
        HBox filaActual = null;
        int contador = 0;

        for (String restriccion : restricciones) {
            if (contador % 2 == 0) {
                filaActual = new HBox(20);
                restriccionesPane.getChildren().add(filaActual);
            }

            CheckBox checkbox = new CheckBox(restriccion);
            checkbox.setTextFill(Color.rgb(230, 230, 250));

            restriccionesCheckboxes.add(checkbox);
            filaActual.getChildren().add(checkbox);

            contador++;
        }
    }

    private void configurarValidacionCampos() {
        // Permitir solo números en el campo de edad
        edadField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                edadField.setText(oldValue);
            }
        });

        // Permitir números y punto decimal en el campo de peso
        pesoField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                pesoField.setText(oldValue);
            }
        });

        // Permitir números y punto decimal en el campo de altura
        alturaField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                alturaField.setText(oldValue);
            }
        });
    }

    private void cargarPerfilExistente() {
        if (perfilServicio.tienePerfil(usuarioIdActual)) {
            perfilActual = perfilServicio.obtenerPerfilPorUsuario(usuarioIdActual);

            // Cargar datos en el formulario
            edadField.setText(String.valueOf(perfilActual.getEdad()));
            pesoField.setText(String.format("%.1f", perfilActual.getPeso()));
            alturaField.setText(String.format("%.1f", perfilActual.getAltura()));

            if ("M".equals(perfilActual.getSexo())) {
                sexoMRadio.setSelected(true);
            } else {
                sexoFRadio.setSelected(true);
            }

            actividadComboBox.setValue(perfilActual.getNivelActividad());

            // Marcar restricciones
            for (CheckBox checkbox : restriccionesCheckboxes) {
                checkbox.setSelected(perfilActual.getRestricciones().contains(checkbox.getText()));
            }

            // Actualizar resumen
            actualizarResumen(perfilActual);
        }
    }

    @FXML
    private void handleGuardarPerfil(ActionEvent event) {
        // Validar campos
        if (edadField.getText().trim().isEmpty() ||
                pesoField.getText().trim().isEmpty() ||
                alturaField.getText().trim().isEmpty() ||
                actividadComboBox.getValue() == null) {

            mostrarAlertaError("Campos incompletos", "Por favor, completa todos los campos obligatorios.");
            return;
        }

        try {
            // Obtener valores del formulario
            int edad = Integer.parseInt(edadField.getText().trim());
            double peso = Double.parseDouble(pesoField.getText().trim());
            double altura = Double.parseDouble(alturaField.getText().trim());
            String sexo = sexoMRadio.isSelected() ? "M" : "F";
            String nivelActividad = actividadComboBox.getValue();

            // Validar valores
            if (edad < 12 || edad > 120) {
                mostrarAlertaError("Valor incorrecto", "La edad debe estar entre 12 y 120 años.");
                return;
            }

            if (peso < 30 || peso > 300) {
                mostrarAlertaError("Valor incorrecto", "El peso debe estar entre 30 y 300 kg.");
                return;
            }

            if (altura < 100 || altura > 250) {
                mostrarAlertaError("Valor incorrecto", "La altura debe estar entre 100 y 250 cm.");
                return;
            }

            // Crear o actualizar perfil
            PerfilNutricional perfil;
            if (perfilActual != null) {
                perfil = perfilActual;
                perfil.setEdad(edad);
                perfil.setPeso(peso);
                perfil.setAltura(altura);
                perfil.setSexo(sexo);
                perfil.setNivelActividad(nivelActividad);
                perfil.getRestricciones().clear(); // Limpiar restricciones existentes
            } else {
                perfil = new PerfilNutricional(usuarioIdActual, edad, peso, altura, sexo, nivelActividad);
            }

            // Agregar restricciones seleccionadas
            for (CheckBox checkbox : restriccionesCheckboxes) {
                if (checkbox.isSelected()) {
                    perfil.agregarRestriccion(checkbox.getText());
                }
            }

            // Guardar perfil
            perfilActual = perfilServicio.guardarPerfil(perfil);

            // Actualizar resumen
            actualizarResumen(perfilActual);

            // Mostrar mensaje de éxito
            mostrarAlertaInformacion("Perfil guardado", "Tu perfil nutricional ha sido guardado correctamente.");

        } catch (NumberFormatException e) {
            mostrarAlertaError("Formato incorrecto", "Por favor, ingresa valores numéricos válidos.");
        }
    }

    private void actualizarResumen(PerfilNutricional perfil) {
        // Actualizar valores de IMC
        double imc = perfil.getImc();
        imcValueLabel.setText(String.format("%.1f", imc));
        String categoriaIMC = perfil.getCategoriaIMC();
        imcCategoryLabel.setText(categoriaIMC);

        // Colorear según categoría IMC
        switch (categoriaIMC) {
            case "Bajo peso":
                imcCategoryLabel.setTextFill(Color.rgb(255, 200, 0));
                break;
            case "Normal":
                imcCategoryLabel.setTextFill(Color.rgb(100, 220, 100));
                break;
            case "Sobrepeso":
                imcCategoryLabel.setTextFill(Color.rgb(255, 150, 0));
                break;
            case "Obesidad":
                imcCategoryLabel.setTextFill(Color.rgb(255, 80, 80));
                break;
        }

        // Actualizar calorías
        caloriasLabel.setText(perfil.getCaloriasDiarias() + " kcal");

        // Actualizar macronutrientes
        PerfilNutricional.MacronutrientesDiarios macros = perfil.getMacronutrientesDiarios();
        proteinasLabel.setText(macros.getProteinas() + " g");
        carbosLabel.setText(macros.getCarbohidratos() + " g");
        grasasLabel.setText(macros.getGrasas() + " g");

        // Actualizar puntuación
        int puntuacion = perfilServicio.calcularPuntuacionNutricional(perfil);
        puntuacionLabel.setText(puntuacion + "/100");
        puntuacionProgress.setProgress(puntuacion / 100.0);

        // Colorear puntuación según valor
        if (puntuacion >= 80) {
            puntuacionLabel.setTextFill(Color.rgb(100, 220, 100)); // Verde
        } else if (puntuacion >= 60) {
            puntuacionLabel.setTextFill(Color.rgb(200, 220, 100)); // Verde-amarillo
        } else if (puntuacion >= 40) {
            puntuacionLabel.setTextFill(Color.rgb(255, 200, 0)); // Amarillo
        } else {
            puntuacionLabel.setTextFill(Color.rgb(255, 100, 100)); // Rojo
        }

        // Actualizar recomendaciones
        recomendacionesArea.setText(perfilServicio.generarRecomendacionAlimentaria(perfil));

        // Actualizar gráfico de macronutrientes
        actualizarGraficoMacros(macros);
    }

    private void actualizarGraficoMacros(PerfilNutricional.MacronutrientesDiarios macros) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Proteínas (30%)", macros.getProteinas()),
                new PieChart.Data("Carbohidratos (40%)", macros.getCarbohidratos()),
                new PieChart.Data("Grasas (30%)", macros.getGrasas())
        );

        macrosPieChart.setData(pieChartData);

        // Aplicar colores a las secciones
        pieChartData.get(0).getNode().setStyle("-fx-pie-color: rgb(100, 220, 100);"); // Verde para proteínas
        pieChartData.get(1).getNode().setStyle("-fx-pie-color: rgb(100, 170, 255);"); // Azul para carbohidratos
        pieChartData.get(2).getNode().setStyle("-fx-pie-color: rgb(255, 170, 100);"); // Naranja para grasas
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
            mostrarAlertaError("Error de navegación", "Error al cargar el dashboard: " + e.getMessage());
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
            mostrarAlertaError("Error de navegación", "Error al cargar la pantalla de transacciones: " + e.getMessage());
        }
    }

    @FXML
    private void handleNutritionAction(ActionEvent evento) {
        // Ya estamos en la vista de nutrición, solo activamos el botón
        activarBoton(nutritionButton);
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
            mostrarAlertaError("Error de navegación", "Error al cargar la pantalla de plan de compras: " + e.getMessage());
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
            mostrarAlertaError("Error de navegación", "Error al cargar la pantalla de modalidades de ahorro: " + e.getMessage());
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
        // Cambiar a la vista de configuración
        activarBoton(settingsButton);
        mostrarAlertaNoImplementado("Configuración");
    }

    @FXML
    private void handleProfileAction(ActionEvent evento) {
        // Cambiar a la vista de perfil
        activarBoton(profileButton);
        mostrarAlertaNoImplementado("Mi Perfil");
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

            // Efectos de hover
            node.setOnMouseEntered(e ->
                    node.setStyle(
                            "-fx-background-color: #35354A; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-border-color: #FF00FF; " +
                                    "-fx-border-width: 1px;"
                    )
            );

            node.setOnMouseExited(e ->
                    node.setStyle(
                            "-fx-background-color: #25253A; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-border-color: #4050FF; " +
                                    "-fx-border-width: 1px;"
                    )
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
                    mostrarAlertaError("Error al volver a la pantalla de login", e.getMessage());
                }
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
        alerta.setTitle(caracteristica + " - En desarrollo");
        alerta.setHeaderText(null);
        alerta.setContentText("Esta funcionalidad aún no está implementada.");

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

            // Efectos de hover
            node.setOnMouseEntered(e ->
                    node.setStyle(
                            "-fx-background-color: #35354A; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-border-color: #FF00FF; " +
                                    "-fx-border-width: 1px;"
                    )
            );

            node.setOnMouseExited(e ->
                    node.setStyle(
                            "-fx-background-color: #25253A; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-border-color: #4050FF; " +
                                    "-fx-border-width: 1px;"
                    )
            );
        });

        alerta.showAndWait();
    }

    private void mostrarAlertaError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

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

            // Efectos de hover
            node.setOnMouseEntered(e ->
                    node.setStyle(
                            "-fx-background-color: #35354A; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-border-color: #FF00FF; " +
                                    "-fx-border-width: 1px;"
                    )
            );

            node.setOnMouseExited(e ->
                    node.setStyle(
                            "-fx-background-color: #25253A; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-border-color: #4050FF; " +
                                    "-fx-border-width: 1px;"
                    )
            );
        });

        alerta.showAndWait();
    }

    private void mostrarAlertaInformacion(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

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

            // Efectos de hover
            node.setOnMouseEntered(e ->
                    node.setStyle(
                            "-fx-background-color: #35354A; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-border-color: #FF00FF; " +
                                    "-fx-border-width: 1px;"
                    )
            );

            node.setOnMouseExited(e ->
                    node.setStyle(
                            "-fx-background-color: #25253A; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-border-color: #4050FF; " +
                                    "-fx-border-width: 1px;"
                    )
            );
        });

        alerta.showAndWait();
    }
}