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
import javafx.scene.paint.Color;
import smartsave.modelo.PerfilNutricional;
import smartsave.modelo.Transaccion;
import smartsave.servicio.PerfilNutricionalServicio;
import smartsave.servicio.TransaccionServicio;
import smartsave.utilidad.EstilosApp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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

    // Referencias a botones de acción
    @FXML private Button viewAllTransactionsButton;

    // Servicios
    private final TransaccionServicio transaccionServicio = new TransaccionServicio();
    private final PerfilNutricionalServicio perfilNutricionalServicio = new PerfilNutricionalServicio();

    // Variables de estado
    private Long usuarioIdActual = 1L; // Simulado, en un caso real vendría de la sesión

    /**
     * Inicialización específica del dashboard
     * Implementa el método abstracto de BaseController
     */
    @Override
    protected void inicializarControlador() {
        // Destacar botón activo en la navegación
        activarBoton(dashboardButton);

        // Configurar tabla de transacciones y cargar datos
        configurarTablaTransacciones();
        cargarDatosReales();

        // Aplicar estilo al botón "Ver Todas" igual que el botón "Nueva Transacción"
        EstilosApp.aplicarEstiloBotonPrimario(viewAllTransactionsButton);
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

        // Formatear celda de fecha
        dateColumn.setCellFactory(column -> new TableCell<Transaccion, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(""); // Limpia estilo para heredar de la tabla
                } else {
                    setText(formatter.format(item));
                    // Aplica el color de texto base de tu tema
                    setStyle("-fx-text-fill: " + EstilosApp.toRgbString(EstilosApp.TEXTO_CLARO) + ";");
                }
            }
        });

        // Formato de monto con colores según tipo
        amountColumn.setCellFactory(column -> new TableCell<Transaccion, Double>() {
            private final String colorIngreso = "-fx-text-fill: rgb(100, 220, 100);"; // Verde
            private final String colorGasto = "-fx-text-fill: rgb(220, 100, 100);";   // Rojo
            private final String colorDefecto = "-fx-text-fill: " + EstilosApp.toRgbString(EstilosApp.TEXTO_CLARO) + ";";

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle(colorDefecto);
                } else {
                    setText(String.format("€%.2f", item));

                    Transaccion transaccion = getTableRow() != null ? (Transaccion) getTableRow().getItem() : null;
                    if (transaccion == null && getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        transaccion = getTableView().getItems().get(getIndex());
                    }

                    if (transaccion != null) {
                        if ("Ingreso".equals(transaccion.getTipo())) {
                            setStyle(colorIngreso);
                        } else if ("Gasto".equals(transaccion.getTipo())) {
                            setStyle(colorGasto);
                        } else {
                            setStyle(colorDefecto);
                        }
                    } else {
                        setStyle(colorDefecto);
                    }
                }
            }
        });

        // Formato para columna de tipo con colores
        typeColumn.setCellFactory(column -> new TableCell<Transaccion, String>() {
            private final String colorIngreso = "-fx-text-fill: rgb(100, 220, 100);";
            private final String colorGasto = "-fx-text-fill: rgb(220, 100, 100);";
            private final String colorDefecto = "-fx-text-fill: " + EstilosApp.toRgbString(EstilosApp.TEXTO_CLARO) + ";";

            @Override
            protected void updateItem(String tipo, boolean empty) {
                super.updateItem(tipo, empty);

                if (empty || tipo == null) {
                    setText(null);
                    setStyle(colorDefecto);
                } else {
                    setText(tipo);
                    if ("Ingreso".equals(tipo)) {
                        setStyle(colorIngreso);
                    } else if ("Gasto".equals(tipo)) {
                        setStyle(colorGasto);
                    } else {
                        setStyle(colorDefecto);
                    }
                }
            }
        });

        EstilosApp.aplicarEstiloTabla(transactionsTable);
        EstilosApp.aplicarEstiloCabecerasTabla(transactionsTable);
    }

    /**
     * Carga todos los datos reales para el dashboard
     */
    private void cargarDatosReales() {
        cargarTransaccionesRecientes();
        cargarResumenFinanciero();
        cargarDatosNutricionales();
        cargarGraficosGastos();
        cargarGraficoEvolucion();
    }

    /**
     * Carga transacciones recientes para la tabla
     */
    private void cargarTransaccionesRecientes() {
        List<Transaccion> transacciones = transaccionServicio.obtenerTransaccionesPorPeriodo(
                usuarioIdActual, LocalDate.now().minusDays(30), LocalDate.now());

        // Limitar a las 5 más recientes
        if (transacciones.size() > 5) {
            transacciones = transacciones.subList(0, 5);
        }

        transactionsTable.setItems(FXCollections.observableArrayList(transacciones));
    }

    /**
     * Carga los datos financieros del usuario
     */
    private void cargarResumenFinanciero() {
        LocalDate fechaActual = LocalDate.now();
        LocalDate inicioMesActual = fechaActual.withDayOfMonth(1);
        LocalDate inicioMesAnterior = fechaActual.minusMonths(1).withDayOfMonth(1);
        LocalDate finMesAnterior = inicioMesActual.minusDays(1);

        // Datos financieros actuales
        double ingresosMesActual = transaccionServicio.obtenerTotalIngresos(usuarioIdActual, inicioMesActual, fechaActual);
        double gastosMesActual = transaccionServicio.obtenerTotalGastos(usuarioIdActual, inicioMesActual, fechaActual);
        double balanceActual = transaccionServicio.obtenerBalance(usuarioIdActual, null, fechaActual);

        // Datos del mes anterior para comparación
        double ingresosMesAnterior = transaccionServicio.obtenerTotalIngresos(usuarioIdActual, inicioMesAnterior, finMesAnterior);
        double gastosMesAnterior = transaccionServicio.obtenerTotalGastos(usuarioIdActual, inicioMesAnterior, finMesAnterior);

        // Calcular porcentajes de cambio
        double cambioIngresos = calcularPorcentajeCambio(ingresosMesActual, ingresosMesAnterior);
        double cambioGastos = calcularPorcentajeCambio(gastosMesActual, gastosMesAnterior);
        double ahorros = balanceActual * 0.3; // Simular ahorros como 30% del balance
        double cambioAhorros = 10.0; // Valor ejemplo para simular cambio en ahorros

        // Actualizar UI
        balanceAmount.setText(String.format("€%.2f", balanceActual));
        expensesAmount.setText(String.format("€%.2f", gastosMesActual));
        savingsAmount.setText(String.format("€%.2f", ahorros));

        // Actualizar indicadores de cambio
        balanceChange.setText(formatearCambio(cambioIngresos - cambioGastos));
        expensesChange.setText(formatearCambio(-cambioGastos));
        savingsChange.setText(formatearCambio(cambioAhorros));

        // Colorear etiquetas
        balanceChange.setTextFill((cambioIngresos - cambioGastos) >= 0 ? Color.rgb(100, 220, 100) : Color.rgb(220, 100, 100));
        expensesChange.setTextFill(-cambioGastos >= 0 ? Color.rgb(100, 220, 100) : Color.rgb(220, 100, 100));
        savingsChange.setTextFill(cambioAhorros >= 0 ? Color.rgb(100, 220, 100) : Color.rgb(220, 100, 100));
    }

    /**
     * Métodos auxiliares para cálculos y formato
     */
    private String formatearCambio(double porcentaje) {
        String signo = porcentaje >= 0 ? "+" : "";
        return String.format("%s€%.2f (%.1f%%)", signo, Math.abs(porcentaje), porcentaje);
    }

    private double calcularPorcentajeCambio(double valorActual, double valorAnterior) {
        return valorAnterior == 0 ? 0 : ((valorActual - valorAnterior) / valorAnterior) * 100;
    }

    /**
     * Carga los datos nutricionales del perfil del usuario
     */
    private void cargarDatosNutricionales() {
        if (perfilNutricionalServicio.tienePerfil(usuarioIdActual)) {
            PerfilNutricional perfil = perfilNutricionalServicio.obtenerPerfilPorUsuario(usuarioIdActual);

            if (perfil != null) {
                int puntuacion = perfilNutricionalServicio.calcularPuntuacionNutricional(perfil);
                nutritionScore.setText(puntuacion + "/100");

                // Determinar estado según puntuación
                String estado;
                Color color;

                if (puntuacion >= 80) {
                    estado = "Excelente";
                    color = Color.rgb(100, 220, 100);
                } else if (puntuacion >= 60) {
                    estado = "Bueno";
                    color = Color.rgb(180, 220, 100);
                } else if (puntuacion >= 40) {
                    estado = "Regular";
                    color = Color.rgb(255, 200, 0);
                } else {
                    estado = "Mejorable";
                    color = Color.rgb(220, 100, 100);
                }

                nutritionStatus.setText(estado);
                nutritionStatus.setTextFill(color);
            } else {
                nutritionScore.setText("--/100");
                nutritionStatus.setText("No definido");
            }
        } else {
            nutritionScore.setText("--/100");
            nutritionStatus.setText("Crea tu perfil");
        }
    }

    /**
     * Carga datos para los gráficos de gastos
     */
    private void cargarGraficosGastos() {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate hoy = LocalDate.now();

        Map<String, Double> gastosPorCategoria = transaccionServicio.obtenerGastosPorCategoria(
                usuarioIdActual, inicioMes, hoy);

        // Crear datos para el gráfico
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            pieChartData.add(new PieChart.Data(
                    entry.getKey() + " - €" + String.format("%.2f", entry.getValue()),
                    entry.getValue()));
        }

        // Si no hay datos, mostrar mensaje
        if (pieChartData.isEmpty()) {
            pieChartData.add(new PieChart.Data("Sin gastos en el período", 1));
        }

        // Actualizar gráfico
        expensesPieChart.setData(pieChartData);
        EstilosApp.aplicarEstiloGrafico(expensesPieChart);

        // Aplicar efecto de brillo a las secciones
        expensesPieChart.getData().forEach(data -> {
            data.getNode().setEffect(new javafx.scene.effect.Glow(0.3));
        });
    }

    /**
     * Carga el gráfico de evolución con datos reales
     */
    private void cargarGraficoEvolucion() {
        // Limpiar series existentes
        evolutionLineChart.getData().clear();

        // Crear series para ingresos, gastos y ahorros
        XYChart.Series<String, Number> serieIngresos = new XYChart.Series<>();
        XYChart.Series<String, Number> serieGastos = new XYChart.Series<>();
        XYChart.Series<String, Number> serieAhorros = new XYChart.Series<>();

        serieIngresos.setName("Ingresos");
        serieGastos.setName("Gastos");
        serieAhorros.setName("Ahorros");

        // Obtener datos de los últimos 6 meses
        LocalDate hoy = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate mesActual = hoy.minusMonths(i);
            LocalDate inicioMes = mesActual.withDayOfMonth(1);
            LocalDate finMes = mesActual.plusMonths(1).withDayOfMonth(1).minusDays(1);

            // Nombres de los meses abreviados
            String nombreMes = mesActual.getMonth().toString().substring(0, 3);

            // Datos financieros del mes
            double ingresos = transaccionServicio.obtenerTotalIngresos(usuarioIdActual, inicioMes, finMes);
            double gastos = transaccionServicio.obtenerTotalGastos(usuarioIdActual, inicioMes, finMes);
            double ahorros = (ingresos - gastos) * 0.3; // Simulamos ahorros como 30% del balance

            // Agregar puntos a las series
            serieIngresos.getData().add(new XYChart.Data<>(nombreMes, ingresos));
            serieGastos.getData().add(new XYChart.Data<>(nombreMes, gastos));
            serieAhorros.getData().add(new XYChart.Data<>(nombreMes, ahorros));
        }

        // Agregar series al gráfico
        evolutionLineChart.getData().addAll(serieIngresos, serieGastos, serieAhorros);

        // Aplicar colores a las series
        serieIngresos.getNode().setStyle("-fx-stroke: rgb(100, 220, 100);");
        serieGastos.getNode().setStyle("-fx-stroke: rgb(220, 100, 100);");
        serieAhorros.getNode().setStyle("-fx-stroke: rgb(100, 100, 220);");
    }

    /**
     * Sobrescribe el método de navegación al dashboard
     */
    @Override
    public void handleDashboardAction(ActionEvent evento) {
        // Ya estamos en el dashboard, solo refrescamos la vista
        activarBoton(dashboardButton);
        cargarDatosReales();
    }

    /**
     * Manejador para ver todas las transacciones
     */
    @FXML
    private void handleViewAllTransactionsAction(ActionEvent evento) {
        navegacionServicio.navegarATransacciones((javafx.stage.Stage) mainPane.getScene().getWindow());
    }
}