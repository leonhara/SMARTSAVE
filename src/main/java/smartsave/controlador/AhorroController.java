// src/main/java/smartsave/controlador/AhorroController.java
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
import smartsave.modelo.ModalidadAhorro;
import smartsave.servicio.ModalidadAhorroServicio;
import smartsave.servicio.TransaccionServicio;
import smartsave.utilidad.EstilosApp;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AhorroController implements Initializable {

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

    // Referencias a elementos específicos de la pantalla de ahorro
    @FXML private ListView<ModalidadAhorro> modalidadesListView;
    @FXML private Label tituloModalidadLabel;
    @FXML private TextArea descripcionModalidadTextArea;
    @FXML private Label factorPresupuestoLabel;
    @FXML private Label prioridadPrecioLabel;
    @FXML private Label prioridadNutricionLabel;
    @FXML private ProgressBar prioridadPrecioProgress;
    @FXML private ProgressBar prioridadNutricionProgress;
    @FXML private Label ahorroEstimadoLabel;
    @FXML private Label presupuestoOriginalLabel;
    @FXML private Label presupuestoAjustadoLabel;
    @FXML private Button aplicarModalidadButton;
    @FXML private ListView<String> consejosListView;
    @FXML private VBox ejemploCalculoPane;
    @FXML private TextField presupuestoEjemploField;
    @FXML private Button calcularButton;
    @FXML private VBox resultadoCalculoPane;
    @FXML private Label presupuestoOriginalResultadoLabel;
    @FXML private Label presupuestoAjustadoResultadoLabel;
    @FXML private Label ahorroEstimadoResultadoLabel;
    @FXML private PieChart distribucionGastosChart;

    // Servicios
    private ModalidadAhorroServicio modalidadServicio = new ModalidadAhorroServicio();
    private TransaccionServicio transaccionServicio = new TransaccionServicio();

    // Variables de estado
    private Long usuarioIdActual = 1L; // Simulado, en un caso real vendría de la sesión
    private ModalidadAhorro modalidadSeleccionada = null;
    private boolean modalidadAplicada = false;

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

        // Inicializar pantalla de modalidades de ahorro
        inicializarPantallaAhorro();

        // Cargar datos
        cargarModalidades();
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

        // Destacar el botón de ahorros como seleccionado
        savingsButton.getStyleClass().add("selected");

        // Aplicar estilos a los botones principales
        EstilosApp.aplicarEstiloBotonPrimario(aplicarModalidadButton);
        EstilosApp.aplicarEstiloBotonPrimario(calcularButton);

        // Aplicar estilos a ListView y TextArea
        modalidadesListView.setStyle(
                "-fx-background-color: rgba(30, 30, 40, 0.7); " +
                        "-fx-background-radius: 5px; " +
                        "-fx-border-color: rgba(80, 80, 120, 0.5); " +
                        "-fx-border-radius: 5px; " +
                        "-fx-text-fill: white;"
        );

        consejosListView.setStyle(
                "-fx-background-color: rgba(30, 30, 40, 0.7); " +
                        "-fx-background-radius: 5px; " +
                        "-fx-border-color: rgba(80, 80, 120, 0.5); " +
                        "-fx-border-radius: 5px; " +
                        "-fx-text-fill: white;"
        );

        descripcionModalidadTextArea.setStyle(
                "-fx-background-color: rgba(40, 40, 50, 0.7); " +
                        "-fx-text-fill: rgb(230, 230, 250); " +
                        "-fx-border-color: rgba(80, 80, 120, 0.5); " +
                        "-fx-border-radius: 5px;"
        );

        // Aplicar estilos a los campos de texto
        EstilosApp.aplicarEstiloCampoTexto(presupuestoEjemploField);

        // Aplicar estilos al gráfico
        EstilosApp.aplicarEstiloGrafico(distribucionGastosChart);
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

    private void inicializarPantallaAhorro() {
        // Configurar la selección de modalidad
        modalidadesListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        mostrarDetalleModalidad(newValue);
                    }
                });

        // Configurar celda personalizada para ListView de modalidades
        modalidadesListView.setCellFactory(param -> new ListCell<ModalidadAhorro>() {
            @Override
            protected void updateItem(ModalidadAhorro item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    // Crear contenido de la celda
                    VBox contenido = new VBox(5);

                    // Nombre de la modalidad
                    Label nombreLabel = new Label(item.getNombre());
                    nombreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

                    // Factor de presupuesto
                    Label factorLabel = new Label(String.format("Factor: %.0f%%", item.getFactorPresupuesto() * 100));
                    factorLabel.setStyle("-fx-text-fill: rgb(200, 200, 220);");

                    // Prioridades
                    HBox prioridades = new HBox(10);
                    Label precioLabel = new Label("Precio: " + item.getPrioridadPrecio() + "/10");
                    precioLabel.setStyle("-fx-text-fill: rgb(100, 200, 255);");

                    Label nutricionLabel = new Label("Nutrición: " + item.getPrioridadNutricion() + "/10");
                    nutricionLabel.setStyle("-fx-text-fill: rgb(100, 220, 100);");

                    prioridades.getChildren().addAll(precioLabel, nutricionLabel);

                    contenido.getChildren().addAll(nombreLabel, factorLabel, prioridades);

                    // Configurar estilo de la celda
                    setGraphic(contenido);

                    // Estilo de selección
                    selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                        if (isNowSelected) {
                            setStyle("-fx-background-color: rgba(80, 80, 140, 0.7); -fx-background-radius: 5px;");
                        } else {
                            setStyle("-fx-background-color: transparent;");
                        }
                    });

                    if (isSelected()) {
                        setStyle("-fx-background-color: rgba(80, 80, 140, 0.7); -fx-background-radius: 5px;");
                    } else {
                        setStyle("-fx-background-color: transparent;");
                    }
                }
            }
        });

        // Configurar celda personalizada para ListView de consejos
        consejosListView.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText("• " + item);
                    setTextFill(Color.rgb(230, 230, 250));
                }
            }
        });

        // Configurar validación para el campo de presupuesto ejemplo
        presupuestoEjemploField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                presupuestoEjemploField.setText(oldValue);
            }
        });

        // Configurar acción del botón calcular
        calcularButton.setOnAction(this::handleCalcularEjemplo);

        // Configurar acción del botón aplicar modalidad
        aplicarModalidadButton.setOnAction(this::handleAplicarModalidad);

        // Ocultar el panel de resultado de cálculo inicialmente
        resultadoCalculoPane.setVisible(false);
        resultadoCalculoPane.setManaged(false);
    }

    private void cargarModalidades() {
        // Obtener todas las modalidades
        List<ModalidadAhorro> modalidades = modalidadServicio.obtenerTodasModalidades();

        // Cargar en el ListView
        modalidadesListView.setItems(FXCollections.observableArrayList(modalidades));

        // Seleccionar la primera modalidad
        if (!modalidades.isEmpty()) {
            modalidadesListView.getSelectionModel().select(0);
        }
    }

    private void mostrarDetalleModalidad(ModalidadAhorro modalidad) {
        modalidadSeleccionada = modalidad;

        // Actualizar la interfaz con los detalles de la modalidad
        tituloModalidadLabel.setText("Modalidad: " + modalidad.getNombre());
        descripcionModalidadTextArea.setText(modalidad.getDescripcion());

        // Actualizar etiquetas y barras de progreso
        factorPresupuestoLabel.setText(String.format("%.0f%%", modalidad.getFactorPresupuesto() * 100));

        prioridadPrecioLabel.setText(modalidad.getPrioridadPrecio() + "/10");
        prioridadPrecioProgress.setProgress(modalidad.getPrioridadPrecio() / 10.0);

        prioridadNutricionLabel.setText(modalidad.getPrioridadNutricion() + "/10");
        prioridadNutricionProgress.setProgress(modalidad.getPrioridadNutricion() / 10.0);

        // Colorear barras de progreso
        String colorPrecio;
        if (modalidad.getPrioridadPrecio() >= 7) {
            colorPrecio = "rgb(100, 200, 255)"; // Azul
        } else if (modalidad.getPrioridadPrecio() >= 4) {
            colorPrecio = "rgb(200, 200, 0)"; // Amarillo
        } else {
            colorPrecio = "rgb(200, 100, 100)"; // Rojo
        }
        prioridadPrecioProgress.setStyle("-fx-accent: " + colorPrecio + ";");

        String colorNutricion;
        if (modalidad.getPrioridadNutricion() >= 7) {
            colorNutricion = "rgb(100, 220, 100)"; // Verde
        } else if (modalidad.getPrioridadNutricion() >= 4) {
            colorNutricion = "rgb(200, 200, 0)"; // Amarillo
        } else {
            colorNutricion = "rgb(200, 100, 100)"; // Rojo
        }
        prioridadNutricionProgress.setStyle("-fx-accent: " + colorNutricion + ";");

        // Cargar consejos
        List<String> consejos = modalidadServicio.obtenerConsejosAhorro(modalidad);
        consejosListView.setItems(FXCollections.observableArrayList(consejos));

        // Calcular ejemplo con un presupuesto predeterminado
        double presupuestoEjemplo = 200.0; // € 200 como ejemplo
        double presupuestoAjustado = modalidadServicio.calcularPresupuestoAjustado(presupuestoEjemplo, modalidad);
        double ahorroEstimado = presupuestoEjemplo - presupuestoAjustado;

        // Actualizar etiquetas de ejemplo
        presupuestoOriginalLabel.setText(String.format("€%.2f", presupuestoEjemplo));
        presupuestoAjustadoLabel.setText(String.format("€%.2f", presupuestoAjustado));
        ahorroEstimadoLabel.setText(String.format("€%.2f", ahorroEstimado));

        // Actualizar estado del botón
        aplicarModalidadButton.setText(modalidadAplicada ? "Modalidad Aplicada" : "Aplicar Modalidad");
        aplicarModalidadButton.setDisable(modalidadAplicada);
    }

    @FXML
    private void handleCalcularEjemplo(ActionEvent event) {
        if (modalidadSeleccionada == null) {
            return;
        }

        try {
            // Obtener el presupuesto ingresado
            String presupuestoTexto = presupuestoEjemploField.getText().trim();
            if (presupuestoTexto.isEmpty()) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Por favor, ingresa un presupuesto válido.");
                return;
            }

            double presupuesto = Double.parseDouble(presupuestoTexto);
            if (presupuesto <= 0) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "El presupuesto debe ser mayor que cero.");
                return;
            }

            // Calcular presupuesto ajustado
            double presupuestoAjustado = modalidadServicio.calcularPresupuestoAjustado(presupuesto, modalidadSeleccionada);
            double ahorroEstimado = presupuesto - presupuestoAjustado;

            // Mostrar resultados
            presupuestoOriginalResultadoLabel.setText(String.format("€%.2f", presupuesto));
            presupuestoAjustadoResultadoLabel.setText(String.format("€%.2f", presupuestoAjustado));
            ahorroEstimadoResultadoLabel.setText(String.format("€%.2f", ahorroEstimado));

            // Actualizar gráfico
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Gasto", presupuestoAjustado),
                    new PieChart.Data("Ahorro", ahorroEstimado)
            );
            distribucionGastosChart.setData(pieChartData);

            // Colorear secciones del gráfico
            pieChartData.get(0).getNode().setStyle("-fx-pie-color: rgb(100, 170, 255);"); // Azul para gastos
            pieChartData.get(1).getNode().setStyle("-fx-pie-color: rgb(100, 220, 100);"); // Verde para ahorro

            // Mostrar panel de resultados
            resultadoCalculoPane.setVisible(true);
            resultadoCalculoPane.setManaged(true);

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Por favor, ingresa un valor numérico válido.");
        }
    }

    @FXML
    private void handleAplicarModalidad(ActionEvent event) {
        if (modalidadSeleccionada == null) {
            return;
        }

        // Aquí se implementaría la lógica para aplicar la modalidad al usuario
        // Por ejemplo, guardar la preferencia en la base de datos

        // Simulación de aplicación exitosa
        modalidadAplicada = true;
        aplicarModalidadButton.setText("Modalidad Aplicada");
        aplicarModalidadButton.setDisable(true);

        mostrarAlerta(Alert.AlertType.INFORMATION, "Modalidad Aplicada",
                "La modalidad de ahorro '" + modalidadSeleccionada.getNombre() +
                        "' ha sido aplicada correctamente. Se utilizará en tus recomendaciones de compra y presupuesto.");
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
        // Ya estamos en la vista de modalidades de ahorro, solo activamos el botón
        activarBoton(savingsButton);
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
        });

        alerta.showAndWait();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
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
        });

        alerta.showAndWait();
    }
}