package smartsave.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import smartsave.modelo.Transaccion;
import smartsave.servicio.TransaccionServicio;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de Dashboard
 * Extiende de BaseController para heredar funcionalidad común
 */
public class DashboardController extends BaseController {

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

    // Botones específicos del dashboard
    @FXML private Button viewAllTransactionsButton;
    @FXML private Button addGoalButton;

    // Servicio para datos de transacciones
    private final TransaccionServicio transaccionServicio = new TransaccionServicio();

    /**
     * Inicialización específica del dashboard
     * Implementa el método abstracto de BaseController
     */
    @Override
    protected void inicializarControlador() {
        // Destacar botón activo en la navegación
        activarBoton(dashboardButton);

        // Configurar tabla de transacciones
        configurarTablaTransacciones();

        // Cargar datos de ejemplo
        cargarDatosDeMuestra();
    }

    /**
     * Configura las columnas y formato de la tabla de transacciones
     */
    private void configurarTablaTransacciones() {
        // Configurar columnas de la tabla
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("monto"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("tipo"));

        // Formatear celdas de fecha y monto con colores según tipo
        formatearCeldasTabla();
    }

    /**
     * Aplica formato visual a las celdas de la tabla
     */
    private void formatearCeldasTabla() {
        // Formato de fecha
        dateColumn.setCellFactory(column -> new TableCell<>() {
            private final java.time.format.DateTimeFormatter formatter =
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        // Formato de monto con colores según tipo (ingreso/gasto)
        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("€%.2f", item));

                    // Colorear según sea ingreso o gasto
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        Transaccion transaccion = getTableView().getItems().get(index);
                        if ("Ingreso".equals(transaccion.getTipo())) {
                            setTextFill(javafx.scene.paint.Color.rgb(100, 220, 100)); // Verde para ingresos
                        } else {
                            setTextFill(javafx.scene.paint.Color.rgb(220, 100, 100)); // Rojo para gastos
                        }
                    }
                }
            }
        });
    }

    /**
     * Carga datos de muestra para el dashboard
     */
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
        cargarGraficoEvolucion();

        // Cargar datos para la tabla de transacciones
        cargarTransaccionesRecientes();
    }

    /**
     * Carga el gráfico de evolución con series de datos
     */
    private void cargarGraficoEvolucion() {
        // Serie de ingresos
        XYChart.Series<String, Number> serieIngresos = new XYChart.Series<>();
        serieIngresos.setName("Ingresos");
        serieIngresos.getData().addAll(
                new XYChart.Data<>("Ene", 2000),
                new XYChart.Data<>("Feb", 2000),
                new XYChart.Data<>("Mar", 2200),
                new XYChart.Data<>("Abr", 2200),
                new XYChart.Data<>("May", 2200),
                new XYChart.Data<>("Jun", 2500)
        );

        // Serie de gastos
        XYChart.Series<String, Number> serieGastos = new XYChart.Series<>();
        serieGastos.setName("Gastos");
        serieGastos.getData().addAll(
                new XYChart.Data<>("Ene", 1800),
                new XYChart.Data<>("Feb", 1700),
                new XYChart.Data<>("Mar", 1900),
                new XYChart.Data<>("Abr", 1600),
                new XYChart.Data<>("May", 1550),
                new XYChart.Data<>("Jun", 1500)
        );

        // Serie de ahorros
        XYChart.Series<String, Number> serieAhorros = new XYChart.Series<>();
        serieAhorros.setName("Ahorros");
        serieAhorros.getData().addAll(
                new XYChart.Data<>("Ene", 200),
                new XYChart.Data<>("Feb", 300),
                new XYChart.Data<>("Mar", 300),
                new XYChart.Data<>("Abr", 600),
                new XYChart.Data<>("May", 650),
                new XYChart.Data<>("Jun", 1000)
        );

        // Agregar todas las series al gráfico
        evolutionLineChart.getData().addAll(serieIngresos, serieGastos, serieAhorros);
    }

    /**
     * Carga transacciones recientes para la tabla
     */
    private void cargarTransaccionesRecientes() {
        // Datos de ejemplo para la tabla
        ObservableList<Transaccion> transacciones = FXCollections.observableArrayList(
                new Transaccion(LocalDate.now().minusDays(1), "Supermercado El Corte Inglés", "Alimentos", 75.32, "Gasto"),
                new Transaccion(LocalDate.now().minusDays(3), "Transferencia Nómina", "Salario", 2200.00, "Ingreso"),
                new Transaccion(LocalDate.now().minusDays(5), "Netflix", "Entretenimiento", 12.99, "Gasto"),
                new Transaccion(LocalDate.now().minusDays(8), "Gasolinera Repsol", "Transporte", 50.00, "Gasto"),
                new Transaccion(LocalDate.now().minusDays(12), "Dividendos Acciones", "Inversiones", 125.75, "Ingreso")
        );

        // Cargar la tabla
        transactionsTable.setItems(transacciones);
    }

    /**
     * Sobrescribe el método de navegación al dashboard para no hacer nada
     * Ya que estamos en el dashboard
     */
    @Override
    public void handleDashboardAction(ActionEvent evento) {
        // Ya estamos en el dashboard, solo refrescamos la vista si es necesario
        activarBoton(dashboardButton);
        cargarDatosDeMuestra();
    }

    /**
     * Manejador para ver todas las transacciones
     */
    @FXML
    private void handleViewAllTransactionsAction(ActionEvent evento) {
        navegacionServicio.navegarATransacciones(obtenerEscenarioActual());
    }

    /**
     * Manejador para añadir un nuevo objetivo
     */
    @FXML
    private void handleAddGoalAction(ActionEvent evento) {
        navegacionServicio.mostrarAlertaNoImplementado("Añadir Objetivo");
    }

    /**
     * Método auxiliar para obtener el escenario actual
     */
    private javafx.stage.Stage obtenerEscenarioActual() {
        return (javafx.stage.Stage) mainPane.getScene().getWindow();
    }
}