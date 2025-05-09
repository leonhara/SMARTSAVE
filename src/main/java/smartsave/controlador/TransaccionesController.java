package smartsave.controlador;

import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import smartsave.modelo.Transaccion;
import smartsave.servicio.TransaccionServicio;
import smartsave.utilidad.EstilosApp;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class TransaccionesController implements Initializable {

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

    // Servicios
    private TransaccionServicio transaccionServicio = new TransaccionServicio();

    // Variables de estado
    private Long usuarioIdActual = 1L; // Simulado, en un caso real vendría de la sesión
    private Transaccion transaccionEnEdicion = null;
    private boolean modoEdicion = false;

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

        // Inicializar combos de filtros
        inicializarFiltros();

        // Configurar tabla de transacciones
        configurarTablaTransacciones();

        // Inicializar formulario de transacción
        inicializarFormularioTransaccion();

        // Cargar datos iniciales
        cargarDatos();
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

        // Destacar el botón de transacciones como seleccionado
        transactionsButton.getStyleClass().add("selected");

        // Aplicar estilos a los gráficos
        EstilosApp.aplicarEstiloGrafico(gastosPorCategoriaChart);

        // Aplicar estilos a la tabla
        EstilosApp.aplicarEstiloTabla(transaccionesTable);

        // Estilizar panel de formulario
        if (transaccionFormPanel != null) {
            transaccionFormPanel.setBackground(new Background(new BackgroundFill(
                    Color.rgb(30, 30, 40, 0.95),
                    new CornerRadii(10),
                    null
            )));
            transaccionFormPanel.setBorder(new Border(new BorderStroke(
                    Color.rgb(120, 80, 200, 0.7),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(10),
                    new BorderWidths(1)
            )));
            transaccionFormPanel.setEffect(new javafx.scene.effect.DropShadow(10, Color.rgb(0, 0, 0, 0.5)));
        }
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

        // Inicializar combo de categoría (se llenará con todas las categorías)
        categoriaFiltroComboBox.setItems(FXCollections.observableArrayList("Todas"));
        categoriaFiltroComboBox.getItems().addAll(transaccionServicio.obtenerCategoriasGastos());
        categoriaFiltroComboBox.getItems().addAll(transaccionServicio.obtenerCategoriasIngresos());
        categoriaFiltroComboBox.getSelectionModel().selectFirst();
    }

    private void configurarTablaTransacciones() {
        // Configurar columnas de la tabla
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        fechaColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

        descripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        categoriaColumn.setCellValueFactory(new PropertyValueFactory<>("categoria"));

        montoColumn.setCellValueFactory(new PropertyValueFactory<>("monto"));
        montoColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("€%.2f", item));

                    // Colorear según sea ingreso o gasto
                    Transaccion transaccion = getTableView().getItems().get(getIndex());
                    if ("Ingreso".equals(transaccion.getTipo())) {
                        setTextFill(Color.rgb(100, 220, 100)); // Verde para ingresos
                    } else {
                        setTextFill(Color.rgb(220, 100, 100)); // Rojo para gastos
                    }
                }
            }
        });

        tipoColumn.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        tipoColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);

                    // Colorear según tipo
                    if ("Ingreso".equals(item)) {
                        setTextFill(Color.rgb(100, 220, 100)); // Verde para ingresos
                    } else {
                        setTextFill(Color.rgb(220, 100, 100)); // Rojo para gastos
                    }
                }
            }
        });

        // Configurar columna de acciones (editar/eliminar)
        accionesColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnEliminar = new Button("🗑️");
            private final HBox pane = new HBox(5, btnEditar, btnEliminar);

            {
                // Estilizar botones de acción
                btnEditar.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-cursor: hand; " +
                                "-fx-padding: 2;"
                );
                btnEliminar.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-cursor: hand; " +
                                "-fx-padding: 2;"
                );

                // Efectos de hover
                btnEditar.setOnMouseEntered(e -> btnEditar.setStyle(
                        "-fx-background-color: rgba(80, 80, 130, 0.3); " +
                                "-fx-cursor: hand; " +
                                "-fx-padding: 2;"
                ));
                btnEditar.setOnMouseExited(e -> btnEditar.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-cursor: hand; " +
                                "-fx-padding: 2;"
                ));

                btnEliminar.setOnMouseEntered(e -> btnEliminar.setStyle(
                        "-fx-background-color: rgba(130, 80, 80, 0.3); " +
                                "-fx-cursor: hand; " +
                                "-fx-padding: 2;"
                ));
                btnEliminar.setOnMouseExited(e -> btnEliminar.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-cursor: hand; " +
                                "-fx-padding: 2;"
                ));

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
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void inicializarFormularioTransaccion() {
        // Inicializar combos del formulario
        tipoComboBox.setItems(FXCollections.observableArrayList("Ingreso", "Gasto"));

        // Por defecto, mostrar categorías de gastos
        categoriaComboBox.setItems(FXCollections.observableArrayList(
                transaccionServicio.obtenerCategoriasGastos()
        ));

        // Configurar acción al cambiar el tipo
        tipoComboBox.setOnAction(event -> {
            String tipoSeleccionado = tipoComboBox.getValue();
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
        });

        // Configurar validación del campo de monto
        montoField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                montoField.setText(oldValue);
            }
        });

        // Establecer fecha actual por defecto
        fechaPicker.setValue(LocalDate.now());
    }

    private void cargarDatos() {
        // Obtener período seleccionado
        String periodoSeleccionado = periodoComboBox.getValue();
        LocalDate fechaInicio = null;
        LocalDate fechaFin = LocalDate.now();

        // Determinar fechas según período
        switch (periodoSeleccionado) {
            case "Este Mes":
                fechaInicio = LocalDate.now().withDayOfMonth(1);
                break;
            case "Mes Anterior":
                fechaInicio = LocalDate.now().minusMonths(1).withDayOfMonth(1);
                fechaFin = LocalDate.now().withDayOfMonth(1).minusDays(1);
                break;
            case "Últimos 3 Meses":
                fechaInicio = LocalDate.now().minusMonths(3);
                break;
            case "Este Año":
                fechaInicio = LocalDate.now().withDayOfYear(1);
                break;
            default: // Todos
                fechaInicio = LocalDate.of(2000, 1, 1); // Fecha lejana pasada
                break;
        }

        // Crear variables finales para usar en lambdas
        final LocalDate inicio = fechaInicio;
        final LocalDate fin = fechaFin;

        // Obtener los tipo seleccionado
        String tipoSeleccionado = tipoFiltroComboBox.getValue();

        // Obtener las categoría seleccionada
        String categoriaSeleccionada = categoriaFiltroComboBox.getValue();

        // Cargar transacciones según filtros
        List<Transaccion> transacciones;

        if ("Todas".equals(categoriaSeleccionada)) {
            if ("Todos".equals(tipoSeleccionado)) {
                transacciones = transaccionServicio.obtenerTransaccionesPorPeriodo(usuarioIdActual, inicio, fin);
            } else {
                String tipo = "Ingresos".equals(tipoSeleccionado) ? "Ingreso" : "Gasto";
                transacciones = transaccionServicio.obtenerTransaccionesPorTipo(usuarioIdActual, tipo);
                // Filtrar por fecha ya que el método por tipo no filtra por fecha
                transacciones = transacciones.stream()
                        .filter(t -> !t.getFecha().isBefore(inicio) && !t.getFecha().isAfter(fin))
                        .toList();
            }
        } else {
            transacciones = transaccionServicio.obtenerTransaccionesPorCategoria(usuarioIdActual, categoriaSeleccionada);
            // Filtrar por fecha y tipo
            final String tipoFiltro = "Ingresos".equals(tipoSeleccionado) ? "Ingreso" :
                    "Gastos".equals(tipoSeleccionado) ? "Gasto" : null;

            transacciones = transacciones.stream()
                    .filter(t -> !t.getFecha().isBefore(inicio) && !t.getFecha().isAfter(fin))
                    .filter(t -> tipoFiltro == null || t.getTipo().equals(tipoFiltro))
                    .toList();
        }

        // Actualizar tabla
        transaccionesTable.setItems(FXCollections.observableArrayList(transacciones));

        // Actualizar resumen financiero
        actualizarResumenFinanciero(inicio, fin);

        // Actualizar gráfico de distribución
        actualizarGraficoDistribucion(inicio, fin);
    }

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
        if (balance >= 0) {
            balanceLabel.setTextFill(Color.rgb(100, 220, 100)); // Verde para balance positivo
        } else {
            balanceLabel.setTextFill(Color.rgb(220, 100, 100)); // Rojo para balance negativo
        }
    }

    private void actualizarGraficoDistribucion(LocalDate fechaInicio, LocalDate fechaFin) {
        // Obtener gastos por categoría
        Map<String, Double> gastosPorCategoria = transaccionServicio.obtenerGastosPorCategoria(usuarioIdActual, fechaInicio, fechaFin);

        // Crear datos para el gráfico
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey() + " - €" + String.format("%.2f", entry.getValue()), entry.getValue()));
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

    @FXML
    private void handlePeriodoSeleccionado(ActionEvent event) {
        cargarDatos();
    }

    @FXML
    private void handleTipoFiltroSeleccionado(ActionEvent event) {
        cargarDatos();
    }

    @FXML
    private void handleCategoriaFiltroSeleccionada(ActionEvent event) {
        cargarDatos();
    }

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
    }

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
    }

    private void eliminarTransaccion(Transaccion transaccion) {
        // Confirmar eliminación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText(null);
        alert.setContentText("¿Estás seguro de que deseas eliminar esta transacción?");

        // Estilizar alerta
        DialogPane dialogPane = alert.getDialogPane();
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

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // Eliminar transacción
                boolean eliminada = transaccionServicio.eliminarTransaccion(transaccion.getId(), usuarioIdActual);

                if (eliminada) {
                    // Recargar datos
                    cargarDatos();

                    // Notificar al usuario
                    mostrarAlertaInformacion("Transacción eliminada", "La transacción ha sido eliminada correctamente.");
                } else {
                    mostrarAlertaError("Error al eliminar", "No se pudo eliminar la transacción. Inténtalo de nuevo.");
                }
            }
        });
    }

    @FXML
    private void handleGuardarTransaccion(ActionEvent event) {
        // Validar campos
        if (tipoComboBox.getValue() == null ||
                fechaPicker.getValue() == null ||
                descripcionField.getText().trim().isEmpty() ||
                categoriaComboBox.getValue() == null ||
                montoField.getText().trim().isEmpty()) {

            mostrarAlertaError("Campos incompletos", "Por favor, completa todos los campos.");
            return;
        }

        // Validar formato del monto
        double monto;
        try {
            monto = Double.parseDouble(montoField.getText().trim());
            if (monto <= 0) {
                mostrarAlertaError("Monto inválido", "El monto debe ser mayor que cero.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlertaError("Monto inválido", "Por favor, ingresa un valor numérico válido.");
            return;
        }

        // Crear o actualizar transacción
        if (modoEdicion && transaccionEnEdicion != null) {
            // Actualizar transacción existente
            transaccionEnEdicion.setTipo(tipoComboBox.getValue());
            transaccionEnEdicion.setFecha(fechaPicker.getValue());
            transaccionEnEdicion.setDescripcion(descripcionField.getText().trim());
            transaccionEnEdicion.setCategoria(categoriaComboBox.getValue());
            transaccionEnEdicion.setMonto(monto);

            boolean actualizada = transaccionServicio.actualizarTransaccion(transaccionEnEdicion);

            if (actualizada) {
                mostrarAlertaInformacion("Transacción actualizada", "La transacción ha sido actualizada correctamente.");
            } else {
                mostrarAlertaError("Error al actualizar", "No se pudo actualizar la transacción. Inténtalo de nuevo.");
            }
        } else {
            // Crear nueva transacción
            Transaccion nuevaTransaccion = new Transaccion(
                    fechaPicker.getValue(),
                    descripcionField.getText().trim(),
                    categoriaComboBox.getValue(),
                    monto,
                    tipoComboBox.getValue()
            );

            transaccionServicio.agregarTransaccion(nuevaTransaccion, usuarioIdActual);
            mostrarAlertaInformacion("Transacción registrada", "La transacción ha sido registrada correctamente.");
        }

        // Ocultar formulario
        mostrarFormularioTransaccion(false);

        // Recargar datos
        cargarDatos();
    }

    @FXML
    private void handleCancelarTransaccion(ActionEvent event) {
        mostrarFormularioTransaccion(false);
    }

    @FXML
    private void handleTipoSeleccionado(ActionEvent event) {
        // Ya está configurado en inicializarFormularioTransaccion()
    }

    private void mostrarFormularioTransaccion(boolean mostrar) {
        transaccionFormPanel.setVisible(mostrar);
        transaccionFormPanel.setManaged(mostrar);
    }

    private void mostrarAlertaInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        configurarEstiloAlerta(alert, titulo, mensaje);
        alert.showAndWait();
    }

    private void mostrarAlertaError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        configurarEstiloAlerta(alert, titulo, mensaje);
        alert.showAndWait();
    }

    private void configurarEstiloAlerta(Alert alert, String titulo, String mensaje) {
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        DialogPane dialogPane = alert.getDialogPane();
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
    }

// Métodos de navegación

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
        // Ya estamos en la vista de transacciones, solo actualizamos los datos
        cargarDatos();
        activarBoton(transactionsButton);
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
        // Cambiar a la vista de plan de compras
        activarBoton(shoppingButton);
        mostrarAlertaNoImplementado("Plan de Compras");
    }

    @FXML
    private void handleSavingsAction(ActionEvent evento) {
        // Cambiar a la vista de modalidades de ahorro
        activarBoton(savingsButton);
        mostrarAlertaNoImplementado("Modalidades de Ahorro");
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

        configurarEstiloAlerta(alerta, "Cerrar Sesión", "¿Estás seguro que deseas cerrar la sesión?");

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
        configurarEstiloAlerta(alerta, caracteristica + " - En desarrollo",
                "Esta funcionalidad aún no está implementada.");
        alerta.showAndWait();
    }
}