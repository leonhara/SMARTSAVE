package smartsave.controlador;

import javafx.application.Platform;
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
import javafx.stage.Stage;
import smartsave.modelo.PerfilNutricional;
import smartsave.modelo.Transaccion;
import smartsave.servicio.PerfilNutricionalServicio;
import smartsave.servicio.SessionManager;
import smartsave.servicio.TransaccionServicio;
import smartsave.utilidad.EstilosApp;
import smartsave.modelo.Usuario;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DashboardController extends BaseController {

    @FXML private PieChart expensesPieChart;
    @FXML private LineChart<String, Number> evolutionLineChart;
    @FXML private TableView<Transaccion> transactionsTable;
    @FXML private TableColumn<Transaccion, LocalDate> dateColumn;
    @FXML private TableColumn<Transaccion, String> descriptionColumn;
    @FXML private TableColumn<Transaccion, String> categoryColumn;
    @FXML private TableColumn<Transaccion, Double> amountColumn;
    @FXML private TableColumn<Transaccion, String> typeColumn;
    @FXML private Label balanceAmount;
    @FXML private Label balanceChange;
    @FXML private Label expensesAmount;
    @FXML private Label expensesChange;
    @FXML private Label savingsAmount;
    @FXML private Label savingsChange;
    @FXML private Label nutritionScore;
    @FXML private Label nutritionStatus;
    @FXML private Button viewAllTransactionsButton;

    private final TransaccionServicio transaccionServicio = new TransaccionServicio();
    private final PerfilNutricionalServicio perfilNutricionalServicio = new PerfilNutricionalServicio();

    private Long usuarioIdActualLocal;
    private Usuario usuarioActualLocal;

    @Override
    protected void inicializarControlador() {
        this.usuarioActualLocal = SessionManager.getInstancia().getUsuarioActual();

        if (this.usuarioActualLocal == null) {
            System.err.println("Error crítico: No hay usuario en sesión en DashboardController.");
            if (navegacionServicio != null) {
                navegacionServicio.mostrarAlertaError("Error de Sesión", "No se pudo identificar al usuario. Por favor, inicie sesión de nuevo.");
                if (mainPane != null && mainPane.getScene() != null && mainPane.getScene().getWindow() instanceof Stage) {
                    Stage stage = (Stage) mainPane.getScene().getWindow();
                    if (stage != null) {
                        navegacionServicio.navegarALogin(stage);
                    }
                }
            }
            disableUIComponents();
            return;
        }
        this.usuarioIdActualLocal = this.usuarioActualLocal.getId();

        activarBoton(dashboardButton);
        configurarTablaTransacciones();
        cargarDatosReales();

        if (viewAllTransactionsButton != null) {
            EstilosApp.aplicarEstiloBotonPrimario(viewAllTransactionsButton);
        }
    }

    private void disableUIComponents() {
        
        if (balanceAmount != null) balanceAmount.setText("€0.00");
        if (balanceChange != null) balanceChange.setText("");
        if (expensesAmount != null) expensesAmount.setText("€0.00");
        if (expensesChange != null) expensesChange.setText("");
        if (savingsAmount != null) savingsAmount.setText("€0.00");
        if (savingsChange != null) savingsChange.setText("");
        if (nutritionScore != null) nutritionScore.setText("--/100");
        if (nutritionStatus != null) nutritionStatus.setText("N/A");
        if (transactionsTable != null) transactionsTable.setItems(FXCollections.observableArrayList());
        if (expensesPieChart != null) expensesPieChart.setData(FXCollections.observableArrayList());
        if (evolutionLineChart != null) evolutionLineChart.setData(FXCollections.observableArrayList());
        if (viewAllTransactionsButton != null) viewAllTransactionsButton.setDisable(true);
    }

    private void configurarTablaTransacciones() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("monto"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("tipo"));

        dateColumn.setCellFactory(column -> new TableCell<Transaccion, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(formatter.format(item));
                    setStyle("-fx-text-fill: " + EstilosApp.toRgbString(EstilosApp.TEXTO_CLARO) + ";");
                }
            }
        });

        amountColumn.setCellFactory(column -> new TableCell<Transaccion, Double>() {
            private final String colorIngreso = "-fx-text-fill: rgb(100, 220, 100);";
            private final String colorGasto = "-fx-text-fill: rgb(220, 100, 100);";
            private final String colorDefecto = "-fx-text-fill: " + EstilosApp.toRgbString(EstilosApp.TEXTO_CLARO) + ";";
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(colorDefecto);
                } else {
                    setText(String.format("€%.2f", item));
                    Transaccion transaccion = getTableRow() != null ? getTableRow().getItem() : null;
                    if (transaccion == null && getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        transaccion = getTableView().getItems().get(getIndex());
                    }
                    if (transaccion != null) {
                        setStyle("Ingreso".equals(transaccion.getTipo()) ? colorIngreso : colorGasto);
                    } else {
                        setStyle(colorDefecto);
                    }
                }
            }
        });

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
                    setStyle("Ingreso".equals(tipo) ? colorIngreso : ("Gasto".equals(tipo) ? colorGasto : colorDefecto));
                }
            }
        });
        if (transactionsTable != null) {
            EstilosApp.aplicarEstiloTabla(transactionsTable);
            EstilosApp.aplicarEstiloCabecerasTabla(transactionsTable);
        }
    }

    private void cargarDatosReales() {
        if (usuarioIdActualLocal == null) {
            disableUIComponents();
            return;
        }
        cargarTransaccionesRecientes();
        cargarResumenFinanciero();
        cargarDatosNutricionales();
        cargarGraficosGastos();
        cargarGraficoEvolucion();
    }

    private void cargarTransaccionesRecientes() {
        List<Transaccion> transacciones = transaccionServicio.obtenerTransaccionesPorPeriodo(
                usuarioIdActualLocal, LocalDate.now().minusDays(30), LocalDate.now());
        if (transacciones.size() > 5) {
            transacciones = transacciones.subList(0, 5);
        }
        if (transactionsTable != null) {
            transactionsTable.setItems(FXCollections.observableArrayList(transacciones));
        }
    }

    private void cargarResumenFinanciero() {
        LocalDate fechaActual = LocalDate.now();
        LocalDate inicioMesActual = fechaActual.withDayOfMonth(1);
        LocalDate inicioMesAnterior = fechaActual.minusMonths(1).withDayOfMonth(1);
        LocalDate finMesAnterior = inicioMesActual.minusDays(1);

        double ingresosMesActual = transaccionServicio.obtenerTotalIngresos(usuarioIdActualLocal, inicioMesActual, fechaActual);
        double gastosMesActual = transaccionServicio.obtenerTotalGastos(usuarioIdActualLocal, inicioMesActual, fechaActual);
        double balanceActual = transaccionServicio.obtenerBalance(usuarioIdActualLocal, null, fechaActual);
        double ingresosMesAnterior = transaccionServicio.obtenerTotalIngresos(usuarioIdActualLocal, inicioMesAnterior, finMesAnterior);
        double gastosMesAnterior = transaccionServicio.obtenerTotalGastos(usuarioIdActualLocal, inicioMesAnterior, finMesAnterior);
        double cambioIngresos = calcularPorcentajeCambio(ingresosMesActual, ingresosMesAnterior);
        double cambioGastos = calcularPorcentajeCambio(gastosMesActual, gastosMesAnterior);
        double ahorros = balanceActual * 0.3;
        double cambioAhorros = 10.0;

        if (balanceAmount != null) balanceAmount.setText(String.format("€%.2f", balanceActual));
        if (expensesAmount != null) expensesAmount.setText(String.format("€%.2f", gastosMesActual));
        if (savingsAmount != null) savingsAmount.setText(String.format("€%.2f", ahorros));
        if (balanceChange != null) {
            balanceChange.setText(formatearCambio(cambioIngresos - cambioGastos));
            balanceChange.setTextFill((cambioIngresos - cambioGastos) >= 0 ? Color.rgb(100, 220, 100) : Color.rgb(220, 100, 100));
        }
        if (expensesChange != null) {
            expensesChange.setText(formatearCambio(-cambioGastos));
            expensesChange.setTextFill(-cambioGastos >= 0 ? Color.rgb(100, 220, 100) : Color.rgb(220, 100, 100));
        }
        if (savingsChange != null) {
            savingsChange.setText(formatearCambio(cambioAhorros));
            savingsChange.setTextFill(cambioAhorros >= 0 ? Color.rgb(100, 220, 100) : Color.rgb(220, 100, 100));
        }
    }

    private String formatearCambio(double porcentaje) {
        String signo = porcentaje >= 0 ? "+" : "";
        return String.format("%s%.1f%%", signo, porcentaje);
    }

    private double calcularPorcentajeCambio(double valorActual, double valorAnterior) {
        if (valorAnterior == 0) {
            return valorActual > 0 ? 100.0 : (valorActual < 0 ? -100.0 : 0.0);
        }
        return ((valorActual - valorAnterior) / Math.abs(valorAnterior)) * 100;
    }

    private void cargarDatosNutricionales() {
        if (perfilNutricionalServicio.tienePerfil(usuarioIdActualLocal)) {
            PerfilNutricional perfil = perfilNutricionalServicio.obtenerPerfilPorUsuario(usuarioIdActualLocal);
            if (perfil != null) {
                int puntuacion = perfilNutricionalServicio.calcularPuntuacionNutricional(perfil);
                if (nutritionScore != null) nutritionScore.setText(puntuacion + "/100");
                String estado;
                Color color;
                if (puntuacion >= 80) { estado = "Excelente"; color = Color.rgb(100, 220, 100); }
                else if (puntuacion >= 60) { estado = "Bueno"; color = Color.rgb(180, 220, 100); }
                else if (puntuacion >= 40) { estado = "Regular"; color = Color.rgb(255, 200, 0); }
                else { estado = "Mejorable"; color = Color.rgb(220, 100, 100); }
                if (nutritionStatus != null) {
                    nutritionStatus.setText(estado);
                    nutritionStatus.setTextFill(color);
                }
            } else {
                if (nutritionScore != null) nutritionScore.setText("--/100");
                if (nutritionStatus != null) nutritionStatus.setText("No definido");
            }
        } else {
            if (nutritionScore != null) nutritionScore.setText("--/100");
            if (nutritionStatus != null) nutritionStatus.setText("Crea tu perfil");
        }
    }

    private void cargarGraficosGastos() {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate hoy = LocalDate.now();
        Map<String, Double> gastosPorCategoria = transaccionServicio.obtenerGastosPorCategoria(usuarioIdActualLocal, inicioMes, hoy);
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey() + " - €" + String.format("%.2f", entry.getValue()), entry.getValue()));
        }
        if (pieChartData.isEmpty()) {
            pieChartData.add(new PieChart.Data("Sin gastos en el período", 1));
        }
        if (expensesPieChart != null) {
            expensesPieChart.setData(pieChartData);
            EstilosApp.aplicarEstiloGrafico(expensesPieChart);
            expensesPieChart.getData().forEach(data -> data.getNode().setEffect(new javafx.scene.effect.Glow(0.3)));
        }
    }

    private void cargarGraficoEvolucion() {
        if (evolutionLineChart == null) return;
        evolutionLineChart.getData().clear();

        XYChart.Series<String, Number> serieIngresos = new XYChart.Series<>();
        serieIngresos.setName("Ingresos");

        XYChart.Series<String, Number> serieGastos = new XYChart.Series<>();
        serieGastos.setName("Gastos");

        XYChart.Series<String, Number> serieBalance = new XYChart.Series<>();
        serieBalance.setName("Balance");

        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");

        for (int i = 5; i >= 0; i--) {
            LocalDate mesActual = hoy.minusMonths(i);
            LocalDate inicioMes = mesActual.withDayOfMonth(1);
            LocalDate finMes = mesActual.plusMonths(1).withDayOfMonth(1).minusDays(1);
            String nombreMes = mesActual.format(formatter);

            double ingresos = transaccionServicio.obtenerTotalIngresos(usuarioIdActualLocal, inicioMes, finMes);
            double gastos = transaccionServicio.obtenerTotalGastos(usuarioIdActualLocal, inicioMes, finMes);
            double balance = ingresos - gastos;

            serieIngresos.getData().add(new XYChart.Data<>(nombreMes, ingresos));
            serieGastos.getData().add(new XYChart.Data<>(nombreMes, gastos));
            serieBalance.getData().add(new XYChart.Data<>(nombreMes, balance));
        }

        evolutionLineChart.getData().addAll(serieIngresos, serieGastos, serieBalance);

        Platform.runLater(() -> {
            String colorIngresos = "rgb(100, 220, 100)";
            String colorGastos = "rgb(220, 100, 100)";
            String colorBalance = "rgb(100, 100, 220)";

            if (serieIngresos.getNode() != null) {
                serieIngresos.getNode().setStyle("-fx-stroke: " + colorIngresos + "; -fx-stroke-width: 2.5px;");
            }
            if (serieGastos.getNode() != null) {
                serieGastos.getNode().setStyle("-fx-stroke: " + colorGastos + "; -fx-stroke-width: 2.5px;");
            }
            if (serieBalance.getNode() != null) {
                serieBalance.getNode().setStyle("-fx-stroke: " + colorBalance + "; -fx-stroke-width: 2.5px;");
            }
            try {

                evolutionLineChart.lookup(".default-color0.chart-legend-item-symbol").setStyle("-fx-background-color: " + colorIngresos + ";");
                evolutionLineChart.lookup(".default-color1.chart-legend-item-symbol").setStyle("-fx-background-color: " + colorGastos + ";");
                evolutionLineChart.lookup(".default-color2.chart-legend-item-symbol").setStyle("-fx-background-color: " + colorBalance + ";");
            } catch (Exception e) {
                System.err.println("Error al aplicar estilos a la leyenda del gráfico: " + e.getMessage());
            }
        });
    }

    @Override
    public void handleDashboardAction(ActionEvent evento) {
        this.usuarioActualLocal = SessionManager.getInstancia().getUsuarioActual();
        if (this.usuarioActualLocal == null) {
            System.err.println("Error crítico: No hay usuario en sesión al re-navegar al Dashboard.");
            if (navegacionServicio != null) {
                navegacionServicio.mostrarAlertaError("Error de Sesión", "No se pudo identificar al usuario. Por favor, inicie sesión de nuevo.");
                if (mainPane != null && mainPane.getScene() != null && mainPane.getScene().getWindow() instanceof Stage) {
                    Stage stage = (Stage) mainPane.getScene().getWindow();
                    if (stage != null) {
                        navegacionServicio.navegarALogin(stage);
                    }
                }
            }
            disableUIComponents();
            return;
        }
        this.usuarioIdActualLocal = this.usuarioActualLocal.getId();
        activarBoton(dashboardButton);
        cargarDatosReales();
    }

    @FXML
    private void handleViewAllTransactionsAction(ActionEvent evento) {
        if (mainPane != null && mainPane.getScene() != null) {
            navegacionServicio.navegarATransacciones((Stage) mainPane.getScene().getWindow());
        }
    }
}