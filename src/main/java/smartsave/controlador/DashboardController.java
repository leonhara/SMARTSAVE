package smartsave.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import smartsave.modelo.Transaccion;
import smartsave.utilidad.EstilosApp;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    // Referencias a elementos principales del layout (mantener nombres FXML originales)
    @FXML private BorderPane mainPane;
    @FXML private HBox titleBar;
    @FXML private VBox sideMenu;

    // Referencias a elementos del menú y controles
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

    // Referencias a los gráficos
    @FXML private PieChart expensesPieChart;
    @FXML private LineChart<String, Number> evolutionLineChart;

    // Referencias a tabla de transacciones
    @FXML private TableView<Transaccion> transactionsTable;
    @FXML private TableColumn<Transaccion, LocalDate> dateColumn;
    @FXML private TableColumn<Transaccion, String> descriptionColumn;
    @FXML private TableColumn<Transaccion, String> categoryColumn;
    @FXML private TableColumn<Transaccion, Double> amountColumn;
    @FXML private TableColumn<Transaccion, String> typeColumn;

    // Referencias a etiquetas de datos financieros
    @FXML private Label balanceAmount;
    @FXML private Label balanceChange;
    @FXML private Label expensesAmount;
    @FXML private Label expensesChange;
    @FXML private Label savingsAmount;
    @FXML private Label savingsChange;
    @FXML private Label nutritionScore;
    @FXML private Label nutritionStatus;

    // Referencias a las barras de progreso
    @FXML private ProgressBar goal1Progress;
    @FXML private ProgressBar goal2Progress;
    @FXML private ProgressBar goal3Progress;

    // Variables para permitir el arrastre de la ventana
    private double offsetX = 0;
    private double offsetY = 0;

    @Override
    public void initialize(URL ubicacion, ResourceBundle recursos) {
        // Aplicar estilos neón oscuros a los componentes
        aplicarEstilos();

        // Configurar el arrastre de la ventana
        configurarVentanaArrastrable();

        // Configurar botones de navegación
        configurarBotonesNavegacion();

        // Cargar datos de ejemplo
        cargarDatosDeMuestra();
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

        // Destacar el botón de dashboard como seleccionado
        dashboardButton.getStyleClass().add("selected");

        // Aplicar estilos a los gráficos
        EstilosApp.aplicarEstiloGrafico(expensesPieChart);
        EstilosApp.aplicarEstiloGrafico(evolutionLineChart);

        // Aplicar estilos a la tabla
        EstilosApp.aplicarEstiloTabla(transactionsTable);
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

    private void cargarDatosDeMuestra() {
        // Cargar datos para el gráfico de distribución de gastos
        ObservableList<PieChart.Data> datosGraficoTorta = FXCollections.observableArrayList(
                new PieChart.Data("Alimentos", 350),
                new PieChart.Data("Vivienda", 800),
                new PieChart.Data("Transporte", 150),
                new PieChart.Data("Entretenimiento", 200),
                new PieChart.Data("Otros", 100)
        );
        expensesPieChart.setData(datosGraficoTorta);

        // Cargar datos para el gráfico de evolución
        XYChart.Series<String, Number> serieIngresos = new XYChart.Series<>();
        serieIngresos.setName("Ingresos");
        serieIngresos.getData().add(new XYChart.Data<>("Ene", 2000));
        serieIngresos.getData().add(new XYChart.Data<>("Feb", 2000));
        serieIngresos.getData().add(new XYChart.Data<>("Mar", 2200));
        serieIngresos.getData().add(new XYChart.Data<>("Abr", 2200));
        serieIngresos.getData().add(new XYChart.Data<>("May", 2200));
        serieIngresos.getData().add(new XYChart.Data<>("Jun", 2500));

        XYChart.Series<String, Number> serieGastos = new XYChart.Series<>();
        serieGastos.setName("Gastos");
        serieGastos.getData().add(new XYChart.Data<>("Ene", 1800));
        serieGastos.getData().add(new XYChart.Data<>("Feb", 1700));
        serieGastos.getData().add(new XYChart.Data<>("Mar", 1900));
        serieGastos.getData().add(new XYChart.Data<>("Abr", 1600));
        serieGastos.getData().add(new XYChart.Data<>("May", 1550));
        serieGastos.getData().add(new XYChart.Data<>("Jun", 1500));

        XYChart.Series<String, Number> serieAhorros = new XYChart.Series<>();
        serieAhorros.setName("Ahorros");
        serieAhorros.getData().add(new XYChart.Data<>("Ene", 200));
        serieAhorros.getData().add(new XYChart.Data<>("Feb", 300));
        serieAhorros.getData().add(new XYChart.Data<>("Mar", 300));
        serieAhorros.getData().add(new XYChart.Data<>("Abr", 600));
        serieAhorros.getData().add(new XYChart.Data<>("May", 650));
        serieAhorros.getData().add(new XYChart.Data<>("Jun", 1000));

        evolutionLineChart.getData().addAll(serieIngresos, serieGastos, serieAhorros);

        // Configurar tabla de transacciones
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("monto"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("tipo"));

        // Datos de ejemplo para la tabla
        ObservableList<Transaccion> transacciones = FXCollections.observableArrayList(
                new Transaccion(LocalDate.now().minusDays(1), "Supermercado El Corte Inglés", "Alimentos", 75.32, "Gasto"),
                new Transaccion(LocalDate.now().minusDays(3), "Transferencia Nómina", "Salario", 2200.00, "Ingreso"),
                new Transaccion(LocalDate.now().minusDays(5), "Netflix", "Entretenimiento", 12.99, "Gasto"),
                new Transaccion(LocalDate.now().minusDays(8), "Gasolinera Repsol", "Transporte", 50.00, "Gasto"),
                new Transaccion(LocalDate.now().minusDays(12), "Dividendos Acciones", "Inversiones", 125.75, "Ingreso")
        );

        transactionsTable.setItems(transacciones);
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
        // Ya estamos en el dashboard, solo actualizamos el estilo
        activarBoton(dashboardButton);
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
            mostrarAlertaError("Error de navegación", "Error al cargar la pantalla de perfil nutricional: " + e.getMessage());
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
        try {
            // Cargar la vista de configuración
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/fxml/configuracion.fxml"));
            Parent raizConfiguracion = cargador.load();

            // Configurar la nueva escena
            Scene escenaConfiguracion = new Scene(raizConfiguracion);
            escenaConfiguracion.setFill(Color.TRANSPARENT);

            // Obtener el escenario actual
            Stage escenarioActual = (Stage) settingsButton.getScene().getWindow();

            // Establecer la nueva escena
            escenarioActual.setScene(escenaConfiguracion);
            escenarioActual.setTitle("SmartSave - Configuración");

        } catch (IOException e) {
            mostrarAlertaError("Error de navegación", "Error al cargar la pantalla de configuración: " + e.getMessage());
        }
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
            mostrarAlertaError("Error de navegación", "Error al cargar la pantalla de perfil: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogoutAction(ActionEvent evento) {
        // Mostrar confirmación antes de cerrar sesión
        Alert alerta = crearAlertaConEstilo(
                Alert.AlertType.CONFIRMATION,
                "Cerrar Sesión",
                "¿Estás seguro que deseas cerrar la sesión?"
        );

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

    @FXML
    private void handleViewAllTransactionsAction(ActionEvent evento) {
        mostrarAlertaNoImplementado("Ver Todas las Transacciones");
    }

    @FXML
    private void handleAddGoalAction(ActionEvent evento) {
        mostrarAlertaNoImplementado("Añadir Objetivo");
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
        Alert alerta = crearAlertaConEstilo(
                Alert.AlertType.INFORMATION,
                caracteristica + " - En desarrollo",
                "Esta funcionalidad aún no está implementada."
        );
        alerta.showAndWait();
    }

    private void mostrarAlertaError(String titulo, String mensaje) {
        Alert alerta = crearAlertaConEstilo(
                Alert.AlertType.ERROR,
                titulo,
                mensaje
        );
        alerta.showAndWait();
    }

    private Alert crearAlertaConEstilo(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        // Aplicar estilo al diálogo
        DialogPane panelDialogo = alerta.getDialogPane();

        // Fondo oscuro
        panelDialogo.setStyle(
                "-fx-background-color: #1A1A25; " +
                        "-fx-border-color: #FF00FF; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 5px;"
        );

        // Color de texto claro
        panelDialogo.lookupAll(".label").forEach(etiqueta ->
                etiqueta.setStyle("-fx-text-fill: white; -fx-font-size: 14px;")
        );

        // Estilo para los botones
        panelDialogo.lookupAll(".button").forEach(boton -> {
            boton.setStyle(
                    "-fx-background-color: #25253A; " +
                            "-fx-text-fill: white; " +
                            "-fx-border-color: #4050FF; " +
                            "-fx-border-width: 1px; " +
                            "-fx-border-radius: 3px;"
            );

            // Efecto hover
            boton.setOnMouseEntered(e ->
                    boton.setStyle(
                            "-fx-background-color: #35354A; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-border-color: #FF00FF; " +
                                    "-fx-border-width: 1px; " +
                                    "-fx-border-radius: 3px;"
                    )
            );

            boton.setOnMouseExited(e ->
                    boton.setStyle(
                            "-fx-background-color: #25253A; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-border-color: #4050FF; " +
                                    "-fx-border-width: 1px; " +
                                    "-fx-border-radius: 3px;"
                    )
            );
        });

        return alerta;
    }
}