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
import smartsave.modelo.Transaccion;
import smartsave.servicio.TransaccionServicio;
import smartsave.utilidad.EstilosApp;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controlador para la gestión de transacciones (ingresos y gastos)
 * Extiende BaseController para heredar funcionalidad común
 */
public class TransaccionesController extends BaseController {

    // Referencias a elementos de resumen financiero
    @FXML private Label ingresosTotalLabel;
    @FXML private Label gastosTotalLabel;
    @FXML private Label balanceLabel;

    // Referencias a elementos de filtros
    @FXML private ComboBox<String> periodoComboBox;
    @FXML private ComboBox<String> tipoFiltroComboBox;
    @FXML private ComboBox<String> categoriaFiltroComboBox;
    @FXML private Button nuevaTransaccionButton;

    // Referencias a la tabla de transacciones
    @FXML private TableView<Transaccion> transaccionesTable;
    @FXML private TableColumn<Transaccion, LocalDate> fechaColumn;
    @FXML private TableColumn<Transaccion, String> descripcionColumn;
    @FXML private TableColumn<Transaccion, String> categoriaColumn;
    @FXML private TableColumn<Transaccion, Double> montoColumn;
    @FXML private TableColumn<Transaccion, String> tipoColumn;
    @FXML private TableColumn<Transaccion, Void> accionesColumn;

    // Referencias al gráfico de distribución
    @FXML private PieChart gastosPorCategoriaChart;

    // Referencias al formulario de transacción
    @FXML private VBox transaccionFormPanel;
    @FXML private Label formTitleLabel;
    @FXML private ComboBox<String> tipoComboBox;
    @FXML private DatePicker fechaPicker;
    @FXML private TextField descripcionField;
    @FXML private ComboBox<String> categoriaComboBox;
    @FXML private TextField montoField;
    @FXML private Button guardarButton;
    @FXML private Button cancelarButton;

    // Servicio de transacciones
    private final TransaccionServicio transaccionServicio = new TransaccionServicio();

    // Variables de estado
    private Long usuarioIdActual = 1L; // Simulado, en un caso real vendría de la sesión
    private Transaccion transaccionEnEdicion = null;
    private boolean modoEdicion = false;

    /**
     * Inicialización específica del controlador de transacciones
     * Implementación del método abstracto de BaseController
     */
    @Override
    protected void inicializarControlador() {
        // Destacar botón activo
        activarBoton(transactionsButton);

        // Inicializar combos de filtros
        inicializarFiltros();

        // Configurar tabla de transacciones
        configurarTablaTransacciones();

        // Inicializar formulario de transacción
        inicializarFormularioTransaccion();

        // Cargar datos iniciales
        cargarDatos();

        // Ocultar formulario inicialmente
        mostrarFormularioTransaccion(false);

        // Aplicar estilos de manera centralizada
        aplicarEstilosComponentes();

    }

    /**
     * Aplica estilos a todos los componentes usando EstilosApp
     */
    private void aplicarEstilosComponentes() {
        // Aplicar estilos a las tarjetas de resumen
        EstilosApp.aplicarEstiloTarjeta((Pane)ingresosTotalLabel.getParent());
        EstilosApp.aplicarEstiloTarjeta((Pane)gastosTotalLabel.getParent());
        EstilosApp.aplicarEstiloTarjeta((Pane)balanceLabel.getParent());

        // Aplicar estilos a los ComboBox
        EstilosApp.aplicarEstiloComboBox(periodoComboBox);
        EstilosApp.aplicarEstiloComboBox(tipoFiltroComboBox);
        EstilosApp.aplicarEstiloComboBox(categoriaFiltroComboBox);
        EstilosApp.aplicarEstiloComboBox(tipoComboBox);
        EstilosApp.aplicarEstiloComboBox(categoriaComboBox);

        // Aplicar estilos a los campos de texto
        EstilosApp.aplicarEstiloCampoTexto(descripcionField);
        EstilosApp.aplicarEstiloCampoTexto(montoField);

        // Aplicar estilos al DatePicker
        EstilosApp.aplicarEstiloDatePicker(fechaPicker);

        // Aplicar estilos a los botones
        EstilosApp.aplicarEstiloBotonPrimario(nuevaTransaccionButton);
        EstilosApp.aplicarEstiloBotonPrimario(guardarButton);

        // Aplicar estilos al gráfico
        EstilosApp.aplicarEstiloGrafico(gastosPorCategoriaChart);

        // Aplicar estilos al panel de formulario
        if (transaccionFormPanel.isVisible()) {
            EstilosApp.aplicarEstiloTarjeta(transaccionFormPanel);
        }
    }

    /**
     * Inicializa los filtros de la pantalla
     */
    private void inicializarFiltros() {
        // Inicializar combo de períodos
        periodoComboBox.setItems(FXCollections.observableArrayList(
                "Todos", "Este Mes", "Mes Anterior", "Últimos 3 Meses", "Este Año"
        ));
        periodoComboBox.getSelectionModel().selectFirst();

        // Inicializar combo de tipo
        tipoFiltroComboBox.setItems(FXCollections.observableArrayList(
                "Todos", "Ingresos", "Gastos"
        ));
        tipoFiltroComboBox.getSelectionModel().selectFirst();

        // Inicializar combo de categoría
        categoriaFiltroComboBox.setItems(FXCollections.observableArrayList("Todas"));
        categoriaFiltroComboBox.getItems().addAll(transaccionServicio.obtenerCategoriasGastos());
        categoriaFiltroComboBox.getItems().addAll(transaccionServicio.obtenerCategoriasIngresos());
        categoriaFiltroComboBox.getSelectionModel().selectFirst();
    }

    /**
     * Configura las columnas básicas de la tabla
     */
    private void configurarColumnasBasicas() {
        // Columna de fecha
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        fechaColumn.setCellFactory(column -> new TableCell<Transaccion, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(""); // Limpia cualquier estilo en línea previo
                } else {
                    setText(formatter.format(item));
                    // Aplicar el color de texto base de tu tema si es necesario, o dejarlo vacío
                    // para que herede de EstilosApp.aplicarEstiloTabla
                    setStyle("-fx-text-fill: " + EstilosApp.toRgbString(EstilosApp.TEXTO_CLARO) + ";");
                }
            }
        });

        descripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        // Asegúrate de que las celdas de descripción y categoría también tengan un estilo de texto base
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


        // Columna de monto formateada
        montoColumn.setCellValueFactory(new PropertyValueFactory<>("monto"));
        montoColumn.setCellFactory(column -> new TableCell<Transaccion, Double>() {
            // Define los colores como constantes para evitar crearlos en cada llamada
            private final String colorIngreso = "-fx-text-fill: rgb(100, 220, 100);"; // Verde
            private final String colorGasto = "-fx-text-fill: rgb(220, 100, 100);";   // Rojo
            private final String colorDefecto = "-fx-text-fill: " + EstilosApp.toRgbString(EstilosApp.TEXTO_CLARO) + ";";

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle(colorDefecto); // Aplicar siempre un estilo base para limpiar
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

        // Columna de tipo con colores
        tipoColumn.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        tipoColumn.setCellFactory(column -> new TableCell<Transaccion, String>() {
            private final String colorIngreso = "-fx-text-fill: rgb(100, 220, 100);";
            private final String colorGasto = "-fx-text-fill: rgb(220, 100, 100);";
            private final String colorDefecto = "-fx-text-fill: " + EstilosApp.toRgbString(EstilosApp.TEXTO_CLARO) + ";";

            @Override
            protected void updateItem(String tipo, boolean empty) {
                super.updateItem(tipo, empty);

                if (empty || tipo == null) {
                    setText(null);
                    setStyle(colorDefecto); // Aplicar siempre un estilo base para limpiar
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
    }


    /**
     * Configura la tabla de transacciones y sus columnas
     */
    private void configurarTablaTransacciones() {
        // Configurar columnas básicas
        configurarColumnasBasicas();
        configurarColumnaAcciones();

        // Configurar columna de acciones
        EstilosApp.aplicarEstiloTabla(transaccionesTable);
        EstilosApp.aplicarEstiloCabecerasTabla(transaccionesTable);
    }

    /**
     * Configura la columna de acciones (editar/eliminar)
     */
    private void configurarColumnaAcciones() {
        accionesColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnEliminar = new Button("🗑️");
            private final HBox pane = new HBox(5, btnEditar, btnEliminar);

            {
                // Estilizar botones de acción
                btnEditar.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2;");
                btnEliminar.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2;");

                // Efectos de hover
                btnEditar.setOnMouseEntered(e -> btnEditar.setStyle(
                        "-fx-background-color: rgba(80, 80, 130, 0.3); -fx-cursor: hand; -fx-padding: 2;"));
                btnEditar.setOnMouseExited(e -> btnEditar.setStyle(
                        "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2;"));

                btnEliminar.setOnMouseEntered(e -> btnEliminar.setStyle(
                        "-fx-background-color: rgba(130, 80, 80, 0.3); -fx-cursor: hand; -fx-padding: 2;"));
                btnEliminar.setOnMouseExited(e -> btnEliminar.setStyle(
                        "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2;"));

                // Acciones de los botones
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

    /**
     * Inicializa el formulario de transacción
     */
    private void inicializarFormularioTransaccion() {
        // Inicializar combos del formulario
        tipoComboBox.setItems(FXCollections.observableArrayList("Ingreso", "Gasto"));
        tipoComboBox.getSelectionModel().selectFirst();

        // Por defecto, mostrar categorías de gastos
        categoriaComboBox.setItems(FXCollections.observableArrayList(
                transaccionServicio.obtenerCategoriasGastos()
        ));

        // Configurar validación del campo de monto
        montoField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                montoField.setText(oldValue);
            }
        });

        // Establecer fecha actual por defecto
        fechaPicker.setValue(LocalDate.now());
    }

    /**
     * Manejador para el cambio en el tipo de transacción en el formulario
     */
    @FXML
    private void handleTipoSeleccionado(ActionEvent event) {
        String tipoSeleccionado = tipoComboBox.getValue();

        // Actualizar las categorías según el tipo seleccionado
        if ("Ingreso".equals(tipoSeleccionado)) {
            categoriaComboBox.setItems(FXCollections.observableArrayList(
                    transaccionServicio.obtenerCategoriasIngresos()
            ));
        } else {
            categoriaComboBox.setItems(FXCollections.observableArrayList(
                    transaccionServicio.obtenerCategoriasGastos()
            ));
        }

        categoriaComboBox.getSelectionModel().selectFirst();
    }

    /**
     * Carga los datos según los filtros seleccionados
     */
    private void cargarDatos() {
        // Obtener parámetros de filtro
        LocalDate fechaInicio = obtenerFechaInicio();
        LocalDate fechaFin = LocalDate.now();
        String tipoSeleccionado = tipoFiltroComboBox.getValue();
        String categoriaSeleccionada = categoriaFiltroComboBox.getValue();

        // Cargar transacciones según filtros
        List<Transaccion> transacciones = filtrarTransacciones(fechaInicio, fechaFin, tipoSeleccionado, categoriaSeleccionada);

        // Actualizar tabla
        transaccionesTable.setItems(FXCollections.observableArrayList(transacciones));

        // Actualizar resumen financiero
        actualizarResumenFinanciero(fechaInicio, fechaFin);

        // Actualizar gráfico de distribución
        actualizarGraficoDistribucion(fechaInicio, fechaFin);
    }

    /**
     * Determina la fecha de inicio según el período seleccionado
     */
    private LocalDate obtenerFechaInicio() {
        String periodoSeleccionado = periodoComboBox.getValue();
        switch (periodoSeleccionado) {
            case "Este Mes":
                return LocalDate.now().withDayOfMonth(1);
            case "Mes Anterior":
                return LocalDate.now().minusMonths(1).withDayOfMonth(1);
            case "Últimos 3 Meses":
                return LocalDate.now().minusMonths(3);
            case "Este Año":
                return LocalDate.now().withDayOfYear(1);
            default: // Todos
                return LocalDate.of(2000, 1, 1); // Fecha lejana pasada
        }
    }

    /**
     * Filtra las transacciones según los criterios seleccionados
     */
    private List<Transaccion> filtrarTransacciones(LocalDate fechaInicio, LocalDate fechaFin,
                                                   String tipoSeleccionado, String categoriaSeleccionada) {
        List<Transaccion> transacciones;

        if ("Todas".equals(categoriaSeleccionada)) {
            if ("Todos".equals(tipoSeleccionado)) {
                transacciones = transaccionServicio.obtenerTransaccionesPorPeriodo(usuarioIdActual, fechaInicio, fechaFin);
            } else {
                String tipo = "Ingresos".equals(tipoSeleccionado) ? "Ingreso" : "Gasto";
                transacciones = transaccionServicio.obtenerTransaccionesPorTipo(usuarioIdActual, tipo);
                // Filtrar por fecha
                transacciones = transacciones.stream()
                        .filter(t -> !t.getFecha().isBefore(fechaInicio) && !t.getFecha().isAfter(fechaFin))
                        .toList();
            }
        } else {
            transacciones = transaccionServicio.obtenerTransaccionesPorCategoria(usuarioIdActual, categoriaSeleccionada);
            // Filtrar por fecha y tipo
            final String tipoFiltro = determinarTipoFiltro(tipoSeleccionado);

            transacciones = transacciones.stream()
                    .filter(t -> !t.getFecha().isBefore(fechaInicio) && !t.getFecha().isAfter(fechaFin))
                    .filter(t -> tipoFiltro == null || t.getTipo().equals(tipoFiltro))
                    .toList();
        }

        return transacciones;
    }

    /**
     * Determina el tipo de filtro a aplicar
     */
    private String determinarTipoFiltro(String tipoSeleccionado) {
        if ("Ingresos".equals(tipoSeleccionado)) {
            return "Ingreso";
        } else if ("Gastos".equals(tipoSeleccionado)) {
            return "Gasto";
        } else {
            return null; // Sin filtro de tipo
        }
    }

    /**
     * Actualiza el resumen financiero con los totales del período
     */
    private void actualizarResumenFinanciero(LocalDate fechaInicio, LocalDate fechaFin) {
        // Obtener totales del período
        double totalIngresos = transaccionServicio.obtenerTotalIngresos(usuarioIdActual, fechaInicio, fechaFin);
        double totalGastos = transaccionServicio.obtenerTotalGastos(usuarioIdActual, fechaInicio, fechaFin);
        double balance = totalIngresos - totalGastos;

        // Actualizar etiquetas
        ingresosTotalLabel.setText(String.format("€%.2f", totalIngresos));
        gastosTotalLabel.setText(String.format("€%.2f", totalGastos));
        balanceLabel.setText(String.format("€%.2f", balance));

        // Colorear balance según sea positivo o negativo
        balanceLabel.setTextFill(balance >= 0 ?
                Color.rgb(100, 220, 100) :   // Verde
                Color.rgb(220, 100, 100));   // Rojo
    }

    /**
     * Actualiza el gráfico de distribución de gastos
     */
    private void actualizarGraficoDistribucion(LocalDate fechaInicio, LocalDate fechaFin) {
        // Obtener gastos por categoría
        Map<String, Double> gastosPorCategoria = transaccionServicio.obtenerGastosPorCategoria(
                usuarioIdActual, fechaInicio, fechaFin);

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
        gastosPorCategoriaChart.setData(pieChartData);

        // Aplicar efecto de brillo a las secciones
        gastosPorCategoriaChart.getData().forEach(data -> {
            data.getNode().setEffect(new javafx.scene.effect.Glow(0.3));
        });
    }

    /**
     * Manejador para cambio en combo de período
     */
    @FXML
    private void handlePeriodoSeleccionado(ActionEvent event) {
        cargarDatos();
    }

    /**
     * Manejador para cambio en combo de tipo
     */
    @FXML
    private void handleTipoFiltroSeleccionado(ActionEvent event) {
        cargarDatos();
    }

    /**
     * Manejador para cambio en combo de categoría
     */
    @FXML
    private void handleCategoriaFiltroSeleccionada(ActionEvent event) {
        cargarDatos();
    }

    /**
     * Manejador para crear nueva transacción
     */
    @FXML
    private void handleNuevaTransaccion(ActionEvent event) {
        // Resetear formulario
        modoEdicion = false;
        transaccionEnEdicion = null;
        formTitleLabel.setText("Nueva Transacción");

        // Limpiar campos
        tipoComboBox.getSelectionModel().selectFirst();
        fechaPicker.setValue(LocalDate.now());
        descripcionField.clear();
        categoriaComboBox.getSelectionModel().selectFirst();
        montoField.clear();

        // Mostrar panel
        mostrarFormularioTransaccion(true);

        // Aplicar estilos al panel
        EstilosApp.aplicarEstiloTarjeta(transaccionFormPanel);
    }

    /**
     * Prepara el formulario para editar una transacción
     */
    private void editarTransaccion(Transaccion transaccion) {
        // Preparar modo edición
        modoEdicion = true;
        transaccionEnEdicion = transaccion;
        formTitleLabel.setText("Editar Transacción");

        // Cargar datos en el formulario
        tipoComboBox.setValue(transaccion.getTipo());
        fechaPicker.setValue(transaccion.getFecha());
        descripcionField.setText(transaccion.getDescripcion());

        // Cargar categorías según tipo
        if ("Ingreso".equals(transaccion.getTipo())) {
            categoriaComboBox.setItems(FXCollections.observableArrayList(
                    transaccionServicio.obtenerCategoriasIngresos()
            ));
        } else {
            categoriaComboBox.setItems(FXCollections.observableArrayList(
                    transaccionServicio.obtenerCategoriasGastos()
            ));
        }

        categoriaComboBox.setValue(transaccion.getCategoria());
        montoField.setText(String.format("%.2f", transaccion.getMonto()));

        // Mostrar panel
        mostrarFormularioTransaccion(true);

        // Aplicar estilos al panel
        EstilosApp.aplicarEstiloTarjeta(transaccionFormPanel);
    }

    /**
     * Elimina una transacción tras confirmar
     */
    private void eliminarTransaccion(Transaccion transaccion) {
        // Usar el servicio de navegación para confirmar la eliminación
        boolean confirmado = navegacionServicio.confirmarEliminarTransaccion();

        if (confirmado) {
            // Eliminar transacción
            boolean eliminada = transaccionServicio.eliminarTransaccion(transaccion.getId(), usuarioIdActual);

            if (eliminada) {
                // Recargar datos
                cargarDatos();
                // Notificar al usuario
                navegacionServicio.mostrarAlertaInformacion("Transacción eliminada",
                        "La transacción ha sido eliminada correctamente.");
            } else {
                navegacionServicio.mostrarAlertaError("Error al eliminar",
                        "No se pudo eliminar la transacción. Inténtalo de nuevo.");
            }
        }
    }

    /**
     * Guarda una transacción nueva o editada
     */
    @FXML
    private void handleGuardarTransaccion(ActionEvent event) {
        // Validar campos
        if (!validarCamposTransaccion()) {
            return;
        }

        // Obtener valores del formulario
        String tipo = tipoComboBox.getValue();
        LocalDate fecha = fechaPicker.getValue();
        String descripcion = descripcionField.getText().trim();
        String categoria = categoriaComboBox.getValue();
        double monto = Double.parseDouble(montoField.getText().trim());

        // Crear o actualizar transacción
        if (modoEdicion && transaccionEnEdicion != null) {
            // Actualizar transacción existente
            transaccionEnEdicion.setTipo(tipo);
            transaccionEnEdicion.setFecha(fecha);
            transaccionEnEdicion.setDescripcion(descripcion);
            transaccionEnEdicion.setCategoria(categoria);
            transaccionEnEdicion.setMonto(monto);

            boolean actualizada = transaccionServicio.actualizarTransaccion(transaccionEnEdicion);

            if (actualizada) {
                navegacionServicio.mostrarAlertaInformacion("Transacción actualizada",
                        "La transacción ha sido actualizada correctamente.");
            } else {
                navegacionServicio.mostrarAlertaError("Error al actualizar",
                        "No se pudo actualizar la transacción. Inténtalo de nuevo.");
            }
        } else {
            // Crear nueva transacción
            Transaccion nuevaTransaccion = new Transaccion(fecha, descripcion, categoria, monto, tipo);
            transaccionServicio.agregarTransaccion(nuevaTransaccion, usuarioIdActual);
            navegacionServicio.mostrarAlertaInformacion("Transacción registrada",
                    "La transacción ha sido registrada correctamente.");
        }

        // Ocultar formulario
        mostrarFormularioTransaccion(false);

        // Recargar datos
        cargarDatos();
    }

    /**
     * Valida los campos del formulario de transacción
     */
    private boolean validarCamposTransaccion() {
        // Validar campos
        if (tipoComboBox.getValue() == null ||
                fechaPicker.getValue() == null ||
                descripcionField.getText().trim().isEmpty() ||
                categoriaComboBox.getValue() == null ||
                montoField.getText().trim().isEmpty()) {

            navegacionServicio.mostrarAlertaError("Campos incompletos", "Por favor, completa todos los campos.");
            return false;
        }

        // Validar formato del monto
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

    /**
     * Cancela la edición de la transacción
     */
    @FXML
    private void handleCancelarTransaccion(ActionEvent event) {
        mostrarFormularioTransaccion(false);
    }

    /**
     * Muestra u oculta el formulario de transacción
     */
    private void mostrarFormularioTransaccion(boolean mostrar) {
        transaccionFormPanel.setVisible(mostrar);
        transaccionFormPanel.setManaged(mostrar);
    }

    /**
     * Sobrescribe el método de navegación a transacciones
     * Ya que estamos en la pantalla de transacciones
     */
    @Override
    public void handleTransactionsAction(ActionEvent evento) {
        // Ya estamos en la vista de transacciones, solo actualizamos los datos
        activarBoton(transactionsButton);
        cargarDatos();
    }
}