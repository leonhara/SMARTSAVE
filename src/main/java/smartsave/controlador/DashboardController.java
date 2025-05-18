package smartsave.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import smartsave.modelo.Transaccion;
import smartsave.servicio.NavegacionServicio;
import smartsave.utilidad.EstilosApp;

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

    // Servicio de navegación
    private final NavegacionServicio navegacionServicio = NavegacionServicio.getInstancia();

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

    // Métodos de gestión de navegación simplificados usando NavegacionServicio

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
        Stage escenarioActual = (Stage) transactionsButton.getScene().getWindow();
        navegacionServicio.navegarATransacciones(escenarioActual);
    }

    @FXML
    private void handleNutritionAction(ActionEvent evento) {
        Stage escenarioActual = (Stage) nutritionButton.getScene().getWindow();
        navegacionServicio.navegarANutricion(escenarioActual);
    }

    @FXML
    private void handleShoppingAction(ActionEvent evento) {
        Stage escenarioActual = (Stage) shoppingButton.getScene().getWindow();
        navegacionServicio.navegarACompras(escenarioActual);
    }

    @FXML
    private void handleSavingsAction(ActionEvent evento) {
        Stage escenarioActual = (Stage) savingsButton.getScene().getWindow();
        navegacionServicio.navegarAAhorro(escenarioActual);
    }

    @FXML
    private void handleReportsAction(ActionEvent evento) {
        // Cambiar a la vista de informes
        activarBoton(reportsButton);
        navegacionServicio.mostrarAlertaNoImplementado("Informes");
    }

    @FXML
    private void handleSettingsAction(ActionEvent evento) {
        Stage escenarioActual = (Stage) settingsButton.getScene().getWindow();
        navegacionServicio.navegarAConfiguracion(escenarioActual);
    }

    @FXML
    private void handleProfileAction(ActionEvent evento) {
        Stage escenarioActual = (Stage) profileButton.getScene().getWindow();
        navegacionServicio.navegarAPerfil(escenarioActual);
    }

    @FXML
    private void handleLogoutAction(ActionEvent evento) {
        Stage escenarioActual = (Stage) logoutButton.getScene().getWindow();
        navegacionServicio.confirmarCerrarSesion(escenarioActual);
    }

    @FXML
    private void handleViewAllTransactionsAction(ActionEvent evento) {
        navegacionServicio.mostrarAlertaNoImplementado("Ver Todas las Transacciones");
    }

    @FXML
    private void handleAddGoalAction(ActionEvent evento) {
        navegacionServicio.mostrarAlertaNoImplementado("Añadir Objetivo");
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
}