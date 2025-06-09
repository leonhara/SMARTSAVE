package smartsave.controlador;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import smartsave.modelo.Transaccion;
import smartsave.servicio.SessionManager; 
import smartsave.servicio.TransaccionServicio;
import smartsave.utilidad.EstilosApp;
import smartsave.modelo.Usuario; 

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class TransaccionesController extends BaseController {

    @FXML private Label ingresosTotalLabel;
    @FXML private Label gastosTotalLabel;
    @FXML private Label balanceLabel;
    @FXML private ComboBox<String> periodoComboBox;
    @FXML private ComboBox<String> tipoFiltroComboBox;
    @FXML private ComboBox<String> categoriaFiltroComboBox;
    @FXML private Button nuevaTransaccionButton;
    @FXML private TableView<Transaccion> transaccionesTable;
    @FXML private TableColumn<Transaccion, LocalDate> fechaColumn;
    @FXML private TableColumn<Transaccion, String> descripcionColumn;
    @FXML private TableColumn<Transaccion, String> categoriaColumn;
    @FXML private TableColumn<Transaccion, Double> montoColumn;
    @FXML private TableColumn<Transaccion, String> tipoColumn;
    @FXML private TableColumn<Transaccion, Void> accionesColumn;
    @FXML private PieChart gastosPorCategoriaChart;
    @FXML private VBox transaccionFormPanel;
    @FXML private Label formTitleLabel;
    @FXML private ComboBox<String> tipoComboBox;
    @FXML private DatePicker fechaPicker;
    @FXML private TextField descripcionField;
    @FXML private ComboBox<String> categoriaComboBox;
    @FXML private TextField montoField;
    @FXML private Button guardarButton;
    @FXML private Button cancelarButton;

    private final TransaccionServicio transaccionServicio = new TransaccionServicio();

    private Long usuarioIdActualLocal; 
    private Usuario usuarioActualLocal; 
    private Transaccion transaccionEnEdicion = null;
    private boolean modoEdicion = false;

    @Override
    protected void inicializarControlador() {
        this.usuarioActualLocal = SessionManager.getInstancia().getUsuarioActual();

        if (this.usuarioActualLocal == null) {
            System.err.println("Error crítico: No hay usuario en sesión en TransaccionesController.");
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

        activarBoton(transactionsButton);
        inicializarFiltros();
        configurarTablaTransacciones();
        inicializarFormularioTransaccion();
        cargarDatos();
        mostrarFormularioTransaccion(false);
        aplicarEstilosComponentes();
    }

    private void disableUIComponents() {
        
        if (periodoComboBox != null) periodoComboBox.setDisable(true);
        if (tipoFiltroComboBox != null) tipoFiltroComboBox.setDisable(true);
        if (categoriaFiltroComboBox != null) categoriaFiltroComboBox.setDisable(true);
        if (nuevaTransaccionButton != null) nuevaTransaccionButton.setDisable(true);
        if (transaccionesTable != null) transaccionesTable.setItems(FXCollections.observableArrayList());
        if (gastosPorCategoriaChart != null) gastosPorCategoriaChart.setData(FXCollections.observableArrayList());
        if (ingresosTotalLabel != null) ingresosTotalLabel.setText("€0.00");
        if (gastosTotalLabel != null) gastosTotalLabel.setText("€0.00");
        if (balanceLabel != null) balanceLabel.setText("€0.00");
    }

    private void aplicarEstilosComponentes() {
        if (ingresosTotalLabel != null && ingresosTotalLabel.getParent() instanceof Pane) EstilosApp.aplicarEstiloTarjeta((Pane)ingresosTotalLabel.getParent()); 
        if (gastosTotalLabel != null && gastosTotalLabel.getParent() instanceof Pane) EstilosApp.aplicarEstiloTarjeta((Pane)gastosTotalLabel.getParent()); 
        if (balanceLabel != null && balanceLabel.getParent() instanceof Pane) EstilosApp.aplicarEstiloTarjeta((Pane)balanceLabel.getParent()); 
        if (periodoComboBox != null) EstilosApp.aplicarEstiloComboBox(periodoComboBox); 
        if (tipoFiltroComboBox != null) EstilosApp.aplicarEstiloComboBox(tipoFiltroComboBox); 
        if (categoriaFiltroComboBox != null) EstilosApp.aplicarEstiloComboBox(categoriaFiltroComboBox); 
        if (tipoComboBox != null) EstilosApp.aplicarEstiloComboBox(tipoComboBox); 
        if (categoriaComboBox != null) EstilosApp.aplicarEstiloComboBox(categoriaComboBox); 
        if (descripcionField != null) EstilosApp.aplicarEstiloCampoTexto(descripcionField); 
        if (montoField != null) EstilosApp.aplicarEstiloCampoTexto(montoField); 
        if (fechaPicker != null) EstilosApp.aplicarEstiloDatePicker(fechaPicker); 
        if (nuevaTransaccionButton != null) EstilosApp.aplicarEstiloBotonPrimario(nuevaTransaccionButton); 
        if (guardarButton != null) EstilosApp.aplicarEstiloBotonPrimario(guardarButton); 
        if (gastosPorCategoriaChart != null) EstilosApp.aplicarEstiloGrafico(gastosPorCategoriaChart); 
        if (transaccionFormPanel != null && transaccionFormPanel.isVisible()) {
            EstilosApp.aplicarEstiloTarjeta(transaccionFormPanel); 
        }
    }

    private void inicializarFiltros() {
        if (periodoComboBox != null) {
            periodoComboBox.setItems(FXCollections.observableArrayList("Todos", "Este Mes", "Mes Anterior", "Últimos 3 Meses", "Este Año"));
            periodoComboBox.getSelectionModel().selectFirst();
        }
        if (tipoFiltroComboBox != null) {
            tipoFiltroComboBox.setItems(FXCollections.observableArrayList("Todos", "Ingresos", "Gastos"));
            tipoFiltroComboBox.getSelectionModel().selectFirst();
        }
        if (categoriaFiltroComboBox != null) {
            categoriaFiltroComboBox.setItems(FXCollections.observableArrayList("Todas"));
            categoriaFiltroComboBox.getItems().addAll(transaccionServicio.obtenerCategoriasGastos()); 
            categoriaFiltroComboBox.getItems().addAll(transaccionServicio.obtenerCategoriasIngresos()); 
            categoriaFiltroComboBox.getSelectionModel().selectFirst();
        }
    }

    private void configurarColumnasBasicas() {
        if (fechaColumn == null || descripcionColumn == null || categoriaColumn == null || montoColumn == null || tipoColumn == null) return;

        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        fechaColumn.setCellFactory(column -> new TableCell<Transaccion, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(formatter.format(item));
                    setStyle("-fx-text-fill: " + EstilosApp.toRgbString(EstilosApp.TEXTO_CLARO) + ";");
                }
            }
        });

        descripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        descripcionColumn.setCellFactory(col -> new TableCell<Transaccion, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle(empty ? "" : "-fx-text-fill: " + EstilosApp.toRgbString(EstilosApp.TEXTO_CLARO) + ";");
            }
        });

        categoriaColumn.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        categoriaColumn.setCellFactory(col -> new TableCell<Transaccion, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle(empty ? "" : "-fx-text-fill: " + EstilosApp.toRgbString(EstilosApp.TEXTO_CLARO) + ";");
            }
        });

        montoColumn.setCellValueFactory(new PropertyValueFactory<>("monto"));
        montoColumn.setCellFactory(column -> new TableCell<Transaccion, Double>() {
            private final String colorIngreso = "-fx-text-fill: rgb(100, 220, 100);";
            private final String colorGasto = "-fx-text-fill: rgb(220, 100, 100);";
            private final String colorDefecto = "-fx-text-fill: " + EstilosApp.toRgbString(EstilosApp.TEXTO_CLARO) + ";";
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle(colorDefecto);
                } else {
                    setText(String.format("€%.2f", item));
                    Transaccion transaccion = getTableRow() != null ? getTableRow().getItem() : null;
                    if (transaccion == null && getIndex() >= 0 && getIndex() < getTableView().getItems().size()) { 
                        transaccion = getTableView().getItems().get(getIndex()); 
                    }
                    if (transaccion != null) {
                        setStyle("Ingreso".equals(transaccion.getTipo()) ? colorIngreso : ("Gasto".equals(transaccion.getTipo()) ? colorGasto : colorDefecto));
                    } else {
                        setStyle(colorDefecto);
                    }
                }
            }
        });

        tipoColumn.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        tipoColumn.setCellFactory(column -> new TableCell<Transaccion, String>() {
            private final String colorIngreso = "-fx-text-fill: rgb(100, 220, 100);";
            private final String colorGasto = "-fx-text-fill: rgb(220, 100, 100);";
            private final String colorDefecto = "-fx-text-fill: " + EstilosApp.toRgbString(EstilosApp.TEXTO_CLARO) + ";";
            @Override
            protected void updateItem(String tipo, boolean empty) {
                super.updateItem(tipo, empty);
                if (empty || tipo == null) {
                    setText(null); setStyle(colorDefecto);
                } else {
                    setText(tipo);
                    setStyle("Ingreso".equals(tipo) ? colorIngreso : ("Gasto".equals(tipo) ? colorGasto : colorDefecto));
                }
            }
        });
    }

    private void configurarTablaTransacciones() {
        if (transaccionesTable == null) return;
        configurarColumnasBasicas();
        configurarColumnaAcciones();
        EstilosApp.aplicarEstiloTabla(transaccionesTable); 
        EstilosApp.aplicarEstiloCabecerasTabla(transaccionesTable); 
    }

    private void configurarColumnaAcciones() {
        if (accionesColumn == null) return;
        accionesColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnEliminar = new Button("🗑️");
            private final HBox pane = new HBox(5, btnEditar, btnEliminar);
            {
                btnEditar.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2;");
                btnEliminar.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2;");
                btnEditar.setOnMouseEntered(e -> btnEditar.setStyle("-fx-background-color: rgba(80, 80, 130, 0.3); -fx-cursor: hand; -fx-padding: 2;"));
                btnEditar.setOnMouseExited(e -> btnEditar.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2;"));
                btnEliminar.setOnMouseEntered(e -> btnEliminar.setStyle("-fx-background-color: rgba(130, 80, 80, 0.3); -fx-cursor: hand; -fx-padding: 2;"));
                btnEliminar.setOnMouseExited(e -> btnEliminar.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2;"));
                btnEditar.setOnAction(event -> {
                    Transaccion transaccion = getTableView().getItems().get(getIndex());
                    editarTransaccion(transaccion);
                });
                btnEliminar.setOnAction(event -> {
                    Transaccion transaccion = getTableView().getItems().get(getIndex());
                    eliminarTransaccion(transaccion);
                });
                pane.setAlignment(javafx.geometry.Pos.CENTER);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void inicializarFormularioTransaccion() {
        if (tipoComboBox != null) {
            tipoComboBox.setItems(FXCollections.observableArrayList("Ingreso", "Gasto"));
            tipoComboBox.getSelectionModel().selectFirst();
        }
        if (categoriaComboBox != null && transaccionServicio != null) {
            categoriaComboBox.setItems(FXCollections.observableArrayList(transaccionServicio.obtenerCategoriasGastos())); 
        }
        if (montoField != null) {
            montoField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*(\\.\\d*)?")) {
                    montoField.setText(oldValue);
                }
            });
        }
        if (fechaPicker != null) fechaPicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleTipoSeleccionado(ActionEvent event) {
        if (tipoComboBox == null || categoriaComboBox == null || transaccionServicio == null) return;
        String tipoSeleccionado = tipoComboBox.getValue();
        if ("Ingreso".equals(tipoSeleccionado)) {
            categoriaComboBox.setItems(FXCollections.observableArrayList(transaccionServicio.obtenerCategoriasIngresos())); 
        } else {
            categoriaComboBox.setItems(FXCollections.observableArrayList(transaccionServicio.obtenerCategoriasGastos())); 
        }
        categoriaComboBox.getSelectionModel().selectFirst();
    }

    private void cargarDatos() {
        if (usuarioIdActualLocal == null) { 
            disableUIComponents(); 
            return;
        }
        if (periodoComboBox == null || tipoFiltroComboBox == null || categoriaFiltroComboBox == null || transaccionesTable == null) return;

        LocalDate fechaInicio = obtenerFechaInicio();
        LocalDate fechaFin = LocalDate.now();
        String tipoSeleccionado = tipoFiltroComboBox.getValue();
        String categoriaSeleccionada = categoriaFiltroComboBox.getValue();

        List<Transaccion> transacciones = filtrarTransacciones(fechaInicio, fechaFin, tipoSeleccionado, categoriaSeleccionada);
        transaccionesTable.setItems(FXCollections.observableArrayList(transacciones));
        actualizarResumenFinanciero(fechaInicio, fechaFin);
        actualizarGraficoDistribucion(fechaInicio, fechaFin);
    }

    private LocalDate obtenerFechaInicio() {
        if (periodoComboBox == null || periodoComboBox.getValue() == null) return LocalDate.of(2000, 1, 1);
        String periodoSeleccionado = periodoComboBox.getValue();
        switch (periodoSeleccionado) {
            case "Este Mes": return LocalDate.now().withDayOfMonth(1);
            case "Mes Anterior": return LocalDate.now().minusMonths(1).withDayOfMonth(1);
            case "Últimos 3 Meses": return LocalDate.now().minusMonths(3);
            case "Este Año": return LocalDate.now().withDayOfYear(1);
            default: return LocalDate.of(2000, 1, 1);
        }
    }

    private List<Transaccion> filtrarTransacciones(LocalDate fechaInicio, LocalDate fechaFin, String tipoSeleccionado, String categoriaSeleccionada) {
        List<Transaccion> transacciones;
        if ("Todas".equals(categoriaSeleccionada)) {
            if ("Todos".equals(tipoSeleccionado)) {
                transacciones = transaccionServicio.obtenerTransaccionesPorPeriodo(usuarioIdActualLocal, fechaInicio, fechaFin); 
            } else {
                String tipo = "Ingresos".equals(tipoSeleccionado) ? "Ingreso" : "Gasto";
                transacciones = transaccionServicio.obtenerTransaccionesPorTipo(usuarioIdActualLocal, tipo); 
                transacciones = transacciones.stream().filter(t -> !t.getFecha().isBefore(fechaInicio) && !t.getFecha().isAfter(fechaFin)).toList();
            }
        } else {
            transacciones = transaccionServicio.obtenerTransaccionesPorCategoria(usuarioIdActualLocal, categoriaSeleccionada); 
            final String tipoFiltro = determinarTipoFiltro(tipoSeleccionado);
            transacciones = transacciones.stream().filter(t -> !t.getFecha().isBefore(fechaInicio) && !t.getFecha().isAfter(fechaFin))
                    .filter(t -> tipoFiltro == null || t.getTipo().equals(tipoFiltro)).toList();
        }
        return transacciones;
    }

    private String determinarTipoFiltro(String tipoSeleccionado) {
        if ("Ingresos".equals(tipoSeleccionado)) return "Ingreso";
        else if ("Gastos".equals(tipoSeleccionado)) return "Gasto";
        else return null;
    }

    private void actualizarResumenFinanciero(LocalDate fechaInicio, LocalDate fechaFin) {
        if (ingresosTotalLabel == null || gastosTotalLabel == null || balanceLabel == null) return;
        double totalIngresos = transaccionServicio.obtenerTotalIngresos(usuarioIdActualLocal, fechaInicio, fechaFin); 
        double totalGastos = transaccionServicio.obtenerTotalGastos(usuarioIdActualLocal, fechaInicio, fechaFin); 
        double balance = totalIngresos - totalGastos;
        ingresosTotalLabel.setText(String.format("€%.2f", totalIngresos));
        gastosTotalLabel.setText(String.format("€%.2f", totalGastos));
        balanceLabel.setText(String.format("€%.2f", balance));
        balanceLabel.setTextFill(balance >= 0 ? Color.rgb(100, 220, 100) : Color.rgb(220, 100, 100));
    }

    private void actualizarGraficoDistribucion(LocalDate fechaInicio, LocalDate fechaFin) {
        if (gastosPorCategoriaChart == null) return;
        Map<String, Double> gastosPorCategoria = transaccionServicio.obtenerGastosPorCategoria(usuarioIdActualLocal, fechaInicio, fechaFin); 
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey() + " - €" + String.format("%.2f", entry.getValue()), entry.getValue()));
        }
        if (pieChartData.isEmpty()) {
            pieChartData.add(new PieChart.Data("Sin gastos en el período", 1));
        }
        gastosPorCategoriaChart.setData(pieChartData);
        gastosPorCategoriaChart.getData().forEach(data -> data.getNode().setEffect(new javafx.scene.effect.Glow(0.3)));
    }

    @FXML private void handlePeriodoSeleccionado(ActionEvent event) { cargarDatos(); }
    @FXML private void handleTipoFiltroSeleccionado(ActionEvent event) { cargarDatos(); }
    @FXML private void handleCategoriaFiltroSeleccionada(ActionEvent event) { cargarDatos(); }

    @FXML
    private void handleNuevaTransaccion(ActionEvent event) {
        if (formTitleLabel == null || tipoComboBox == null || fechaPicker == null || descripcionField == null || categoriaComboBox == null || montoField == null || transaccionFormPanel == null) return;
        modoEdicion = false;
        transaccionEnEdicion = null;
        formTitleLabel.setText("Nueva Transacción");
        tipoComboBox.getSelectionModel().selectFirst();
        fechaPicker.setValue(LocalDate.now());
        descripcionField.clear();
        categoriaComboBox.getSelectionModel().selectFirst();
        montoField.clear();
        mostrarFormularioTransaccion(true);
        EstilosApp.aplicarEstiloTarjeta(transaccionFormPanel); 
    }

    private void editarTransaccion(Transaccion transaccion) {
        if (formTitleLabel == null || tipoComboBox == null || fechaPicker == null || descripcionField == null || categoriaComboBox == null || montoField == null || transaccionFormPanel == null) return;
        modoEdicion = true;
        transaccionEnEdicion = transaccion;
        formTitleLabel.setText("Editar Transacción");
        tipoComboBox.setValue(transaccion.getTipo());
        fechaPicker.setValue(transaccion.getFecha());
        descripcionField.setText(transaccion.getDescripcion());
        if ("Ingreso".equals(transaccion.getTipo())) {
            categoriaComboBox.setItems(FXCollections.observableArrayList(transaccionServicio.obtenerCategoriasIngresos())); 
        } else {
            categoriaComboBox.setItems(FXCollections.observableArrayList(transaccionServicio.obtenerCategoriasGastos())); 
        }
        categoriaComboBox.setValue(transaccion.getCategoria());
        montoField.setText(String.format("%.2f", transaccion.getMonto()));
        mostrarFormularioTransaccion(true);
        EstilosApp.aplicarEstiloTarjeta(transaccionFormPanel); 
    }

    private void eliminarTransaccion(Transaccion transaccion) {
        if (navegacionServicio.confirmarEliminarTransaccion()) { 
            boolean eliminada = transaccionServicio.eliminarTransaccion(transaccion.getId(), usuarioIdActualLocal); 
            if (eliminada) {
                cargarDatos();
                navegacionServicio.mostrarAlertaInformacion("Transacción eliminada", "La transacción ha sido eliminada correctamente."); 
            } else {
                navegacionServicio.mostrarAlertaError("Error al eliminar", "No se pudo eliminar la transacción. Inténtalo de nuevo."); 
            }
        }
    }

    @FXML
    private void handleGuardarTransaccion(ActionEvent event) {
        if (!validarCamposTransaccion()) return;
        String tipo = tipoComboBox.getValue();
        LocalDate fecha = fechaPicker.getValue();
        String descripcion = descripcionField.getText().trim();
        String categoria = categoriaComboBox.getValue();
        double monto = Double.parseDouble(montoField.getText().trim());

        if (modoEdicion && transaccionEnEdicion != null) {
            transaccionEnEdicion.setTipo(tipo); 
            transaccionEnEdicion.setFecha(fecha); 
            transaccionEnEdicion.setDescripcion(descripcion); 
            transaccionEnEdicion.setCategoria(categoria); 
            transaccionEnEdicion.setMonto(monto); 
            boolean actualizada = transaccionServicio.actualizarTransaccion(transaccionEnEdicion); 
            if (actualizada) {
                navegacionServicio.mostrarAlertaInformacion("Transacción actualizada", "La transacción ha sido actualizada correctamente."); 
            } else {
                navegacionServicio.mostrarAlertaError("Error al actualizar", "No se pudo actualizar la transacción. Inténtalo de nuevo."); 
            }
        } else {
            Transaccion nuevaTransaccion = new Transaccion(fecha, descripcion, categoria, monto, tipo); 
            transaccionServicio.agregarTransaccion(nuevaTransaccion, usuarioIdActualLocal); 
            navegacionServicio.mostrarAlertaInformacion("Transacción registrada", "La transacción ha sido registrada correctamente."); 
        }
        mostrarFormularioTransaccion(false);
        cargarDatos();
    }

    private boolean validarCamposTransaccion() {
        if (tipoComboBox == null || fechaPicker == null || descripcionField == null || categoriaComboBox == null || montoField == null ||
                tipoComboBox.getValue() == null || fechaPicker.getValue() == null || descripcionField.getText().trim().isEmpty() ||
                categoriaComboBox.getValue() == null || montoField.getText().trim().isEmpty()) {
            navegacionServicio.mostrarAlertaError("Campos incompletos", "Por favor, completa todos los campos."); 
            return false;
        }
        try {
            double monto = Double.parseDouble(montoField.getText().trim());
            if (monto <= 0) {
                navegacionServicio.mostrarAlertaError("Monto inválido", "El monto debe ser mayor que cero."); 
                return false;
            }
        } catch (NumberFormatException e) {
            navegacionServicio.mostrarAlertaError("Monto inválido", "Por favor, ingresa un valor numérico válido."); 
            return false;
        }
        return true;
    }

    @FXML private void handleCancelarTransaccion(ActionEvent event) { mostrarFormularioTransaccion(false); }

    private void mostrarFormularioTransaccion(boolean mostrar) {
        if (transaccionFormPanel != null) {
            transaccionFormPanel.setVisible(mostrar);
            transaccionFormPanel.setManaged(mostrar);
        }
    }

    @Override
    public void handleTransactionsAction(ActionEvent evento) {
        this.usuarioActualLocal = SessionManager.getInstancia().getUsuarioActual(); 
        if (this.usuarioActualLocal == null) {
            System.err.println("Error crítico: No hay usuario en sesión al re-navegar a Transacciones.");
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
        activarBoton(transactionsButton);
        cargarDatos();
    }
}