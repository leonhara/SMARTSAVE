package smartsave.controlador;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import smartsave.modelo.*;
import smartsave.servicio.*;
import smartsave.utilidad.EstilosApp;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Controlador para la vista de Plan de Compras
 * Extiende BaseController para heredar funcionalidad común
 */
public class ComprasController extends BaseController {

    // Referencias a elementos de la lista de compras
    @FXML private ComboBox<String> filtroListasComboBox;
    @FXML private ListView<ListaCompra> listasCompraListView;
    @FXML private Button crearListaButton;

    // Referencias a elementos del detalle de lista
    @FXML private VBox detalleListaPane;
    @FXML private Label nombreListaLabel;
    @FXML private Button editarListaButton;
    @FXML private Button eliminarListaButton;
    @FXML private Label fechaCreacionLabel;
    @FXML private Label fechaProgramadaLabel;
    @FXML private Label modalidadLabel;
    @FXML private Label presupuestoLabel;
    @FXML private ProgressBar progresoBar;
    @FXML private Label costeTotalLabel;
    @FXML private Label progresoLabel;
    @FXML private TableView<ItemCompra> productosTableView;
    @FXML private TableColumn<ItemCompra, Boolean> compradoColumn;
    @FXML private TableColumn<ItemCompra, String> nombreColumn;
    @FXML private TableColumn<ItemCompra, Integer> cantidadColumn;
    @FXML private TableColumn<ItemCompra, String> precioColumn;
    @FXML private TableColumn<ItemCompra, String> totalColumn;
    @FXML private TableColumn<ItemCompra, Void> accionesColumn;
    @FXML private Button agregarProductoButton;
    @FXML private CheckBox completadaCheckBox;

    // Referencias a elementos del panel de creación de lista
    @FXML private VBox crearListaPane;
    @FXML private Label crearListaTituloLabel;
    @FXML private TextField nombreListaField;
    @FXML private ComboBox<String> modalidadComboBox;
    @FXML private TextField presupuestoField;
    @FXML private DatePicker fechaPlanificadaPicker;
    @FXML private CheckBox generarAutomaticoCheckBox;
    @FXML private Button guardarListaButton;
    @FXML private Button cancelarListaButton;

    // Referencias a elementos del panel de búsqueda de productos
    @FXML private VBox agregarProductoPane;
    @FXML private TextField buscarProductoField;
    @FXML private Button buscarProductoButton;
    @FXML private TableView<Producto> resultadosProductosTableView;
    @FXML private TableColumn<Producto, String> productoNombreColumn;
    @FXML private TableColumn<Producto, String> productoMarcaColumn;
    @FXML private TableColumn<Producto, String> productoCategoriaColumn;
    @FXML private TableColumn<Producto, String> productoPrecioColumn;
    @FXML private TableColumn<Producto, Void> productoAccionesColumn;
    @FXML private Button cerrarBusquedaButton;

    // Servicios
    private final ListaCompraServicio listaCompraServicio = new ListaCompraServicio();
    private final ProductoServicio productoServicio = new ProductoServicio();

    // Variables de estado
    private Long usuarioIdActual = 1L; // Simulado, en un caso real vendría de la sesión
    private ListaCompra listaSeleccionada = null;
    private boolean modoEdicion = false;

    /**
     * Inicialización específica del controlador de compras
     * Implementación del método abstracto de BaseController
     */
    @Override
    protected void inicializarControlador() {
        // Destacar el botón de plan de compras como seleccionado
        activarBoton(shoppingButton);

        // Inicializar pantalla de listas de compra
        inicializarPantallaCompras();

        // Cargar datos
        cargarListasCompra();

        // Aplicar estilos personalizados
        aplicarEstilosComponentes();
    }

    /**
     * Aplica estilos a los componentes específicos de esta pantalla
     */
    private void aplicarEstilosComponentes() {
        // Aplicar estilos a los ComboBox
        EstilosApp.aplicarEstiloComboBox(filtroListasComboBox);
        EstilosApp.aplicarEstiloComboBox(modalidadComboBox);

        // Aplicar estilos a los campos de texto
        EstilosApp.aplicarEstiloCampoTexto(nombreListaField);
        EstilosApp.aplicarEstiloCampoTexto(presupuestoField);
        EstilosApp.aplicarEstiloCampoTexto(buscarProductoField);

        // Aplicar estilos al DatePicker
        EstilosApp.aplicarEstiloDatePicker(fechaPlanificadaPicker);

        // Aplicar estilos a las tablas
        EstilosApp.aplicarEstiloTabla(productosTableView);
        EstilosApp.aplicarEstiloTabla(resultadosProductosTableView);

        // Aplicar estilos a los botones principales
        EstilosApp.aplicarEstiloBotonPrimario(crearListaButton);
        EstilosApp.aplicarEstiloBotonPrimario(editarListaButton);
        EstilosApp.aplicarEstiloBotonPrimario(eliminarListaButton);
        EstilosApp.aplicarEstiloBotonPrimario(agregarProductoButton);
        EstilosApp.aplicarEstiloBotonPrimario(guardarListaButton);
        EstilosApp.aplicarEstiloBotonPrimario(cancelarListaButton);
        EstilosApp.aplicarEstiloBotonPrimario(buscarProductoButton);
        EstilosApp.aplicarEstiloBotonPrimario(cerrarBusquedaButton);

        // Aplicar estilos a los ListView
        estilizarListView();

        // Aplicar estilos a los paneles
        estilizarPaneles();
    }

    /**
     * Aplica estilos a los ListView
     */
    private void estilizarListView() {
        // Estilo para ListView
        listasCompraListView.setStyle(
                "-fx-background-color: rgba(30, 30, 40, 0.7); " +
                        "-fx-background-radius: 5px; " +
                        "-fx-border-color: rgba(80, 80, 120, 0.5); " +
                        "-fx-border-radius: 5px; " +
                        "-fx-text-fill: white;"
        );
    }

    /**
     * Aplica estilos a los paneles
     */
    private void estilizarPaneles() {
        // Aplicar estilos a los paneles principales
        if (detalleListaPane.isVisible()) {
            EstilosApp.aplicarEstiloTarjeta(detalleListaPane);
        }

        if (crearListaPane.isVisible()) {
            EstilosApp.aplicarEstiloTarjeta(crearListaPane);
        }

        if (agregarProductoPane.isVisible()) {
            EstilosApp.aplicarEstiloTarjeta(agregarProductoPane);
        }
    }

    /**
     * Inicializa la pantalla de listas de compra
     */
    private void inicializarPantallaCompras() {
        // Configurar combo de filtro de listas
        filtroListasComboBox.setItems(FXCollections.observableArrayList("Todas", "Activas", "Completadas"));
        filtroListasComboBox.getSelectionModel().selectFirst();

        // Configurar selección de lista
        listasCompraListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                mostrarDetalleLista(newVal);
            } else {
                ocultarDetalleLista();
            }
        });

        // Configurar celda personalizada para ListView
        configurarCeldaListaCompra();

        // Configurar tabla de productos
        configurarTablaProductos();

        // Configurar panel de crear lista
        configurarPanelCrearLista();

        // Inicialmente ocultar paneles
        ocultarDetalleLista();
        ocultarPanelCrearLista();
        ocultarPanelBuscarProducto();
    }

    /**
     * Configura la celda personalizada para el ListView de listas de compra
     */
    private void configurarCeldaListaCompra() {
        listasCompraListView.setCellFactory(param -> new ListCell<ListaCompra>() {
            @Override
            protected void updateItem(ListaCompra item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    // Crear contenido de la celda
                    VBox contenido = new VBox(5);
                    contenido.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));

                    // Nombre de la lista
                    Label nombreLabel = new Label(item.getNombre());
                    nombreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

                    // Coste total y porcentaje de progreso
                    HBox detalles = new HBox(15);
                    Label costeLabel = new Label(String.format("€%.2f", item.getCosteTotal()));
                    costeLabel.setStyle("-fx-text-fill: rgb(230, 230, 250);");

                    Label progresoLabel = new Label(item.getPorcentajeProgreso() + "% completado");
                    progresoLabel.setStyle("-fx-text-fill: " +
                            (item.isCompletada() ? "rgb(100, 220, 100)" : "rgb(230, 230, 250)") + ";");

                    detalles.getChildren().addAll(costeLabel, progresoLabel);

                    // Estado
                    Label estadoLabel = new Label(item.isCompletada() ? "Completada" : "En progreso");
                    estadoLabel.setStyle("-fx-text-fill: " +
                            (item.isCompletada() ? "rgb(100, 220, 100)" : "rgb(255, 200, 0)") + "; " +
                            "-fx-font-size: 11px;");

                    contenido.getChildren().addAll(nombreLabel, detalles, estadoLabel);

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
    }

    /**
     * Configura la tabla de productos
     */
    private void configurarTablaProductos() {
        compradoColumn.setCellValueFactory(new PropertyValueFactory<>("comprado"));
        compradoColumn.setCellFactory(col -> new TableCell<ItemCompra, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(event -> {
                    ItemCompra item = getTableView().getItems().get(getIndex());
                    item.setComprado(checkBox.isSelected());
                    listaCompraServicio.actualizarEstadoItem(listaSeleccionada, item.getId(), checkBox.isSelected());
                    actualizarResumenLista();
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item);
                    setGraphic(checkBox);
                }
            }
        });

        nombreColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProducto().getNombre()));

        cantidadColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        precioColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("€%.2f", cellData.getValue().getProducto().getPrecio())));

        totalColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("€%.2f", cellData.getValue().getPrecioTotal())));

        configurarColumnaAccionesProductos();

        // Configurar tabla de resultados de búsqueda
        configurarTablaResultadosBusqueda();
    }

    /**
     * Configura la columna de acciones para la tabla de productos
     */
    private void configurarColumnaAccionesProductos() {
        accionesColumn.setCellFactory(param -> new TableCell<ItemCompra, Void>() {
            private final Button btnAumentar = new Button("+");
            private final Button btnDisminuir = new Button("-");
            private final Button btnEliminar = new Button("🗑️");
            private final HBox pane = new HBox(5, btnAumentar, btnDisminuir, btnEliminar);

            {
                // Estilizar botones
                btnAumentar.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand;");
                btnDisminuir.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand;");
                btnEliminar.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand;");

                // Acciones
                btnAumentar.setOnAction(event -> {
                    ItemCompra item = getTableView().getItems().get(getIndex());
                    item.incrementarCantidad();
                    listaCompraServicio.actualizarCantidadItem(listaSeleccionada, item.getId(), item.getCantidad());
                    actualizarResumenLista();
                    getTableView().refresh();
                });

                btnDisminuir.setOnAction(event -> {
                    ItemCompra item = getTableView().getItems().get(getIndex());
                    item.decrementarCantidad();
                    listaCompraServicio.actualizarCantidadItem(listaSeleccionada, item.getId(), item.getCantidad());
                    actualizarResumenLista();
                    getTableView().refresh();
                });

                btnEliminar.setOnAction(event -> {
                    ItemCompra item = getTableView().getItems().get(getIndex());
                    if (mostrarConfirmacion("Eliminar Producto",
                            "¿Estás seguro de que deseas eliminar este producto de la lista?")) {
                        listaCompraServicio.eliminarItemDeLista(listaSeleccionada, item.getId());
                        mostrarDetalleLista(listaSeleccionada);
                    }
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

    /**
     * Configura el panel de creación de lista
     */
    private void configurarPanelCrearLista() {
        modalidadComboBox.setItems(FXCollections.observableArrayList("Máximo", "Equilibrado", "Estándar"));
        modalidadComboBox.getSelectionModel().select("Equilibrado");

        // Validar campo de presupuesto (solo números y punto decimal)
        presupuestoField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                presupuestoField.setText(oldValue);
            }
        });
    }

    /**
     * Configura la tabla de resultados de búsqueda
     */
    private void configurarTablaResultadosBusqueda() {
        productoNombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        productoMarcaColumn.setCellValueFactory(new PropertyValueFactory<>("marca"));
        productoCategoriaColumn.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        productoPrecioColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("€%.2f", cellData.getValue().getPrecio())));

        productoAccionesColumn.setCellFactory(param -> new TableCell<Producto, Void>() {
            private final Button btnAñadir = new Button("Añadir");

            {
                btnAñadir.setStyle(
                        "-fx-background-color: rgba(100, 180, 100, 0.7); " +
                                "-fx-text-fill: white; " +
                                "-fx-cursor: hand;"
                );

                btnAñadir.setOnAction(event -> {
                    Producto producto = getTableView().getItems().get(getIndex());
                    listaCompraServicio.agregarProductoALista(listaSeleccionada, producto.getId(), 1);
                    mostrarDetalleLista(listaSeleccionada);
                    navegacionServicio.mostrarAlertaInformacion("Producto añadido",
                            "El producto ha sido añadido a la lista de compra.");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnAñadir);
                }
            }
        });
    }

    /**
     * Carga las listas de compra según el filtro seleccionado
     */
    private void cargarListasCompra() {
        // Obtener listas según filtro seleccionado
        String filtro = filtroListasComboBox.getValue();

        ObservableList<ListaCompra> listas;
        if ("Activas".equals(filtro)) {
            listas = FXCollections.observableArrayList(
                    listaCompraServicio.obtenerListasActivas(usuarioIdActual));
        } else if ("Completadas".equals(filtro)) {
            listas = FXCollections.observableArrayList(
                    listaCompraServicio.obtenerListasCompletadas(usuarioIdActual));
        } else {
            // Todas
            listas = FXCollections.observableArrayList(
                    listaCompraServicio.obtenerListasCompraUsuario(usuarioIdActual));
        }

        listasCompraListView.setItems(listas);

        // Si no hay listas, ocultar detalle
        if (listas.isEmpty()) {
            ocultarDetalleLista();
        }
    }

    /**
     * Muestra el detalle de la lista seleccionada
     */
    private void mostrarDetalleLista(ListaCompra lista) {
        // Recargar la lista con items actualizados SIEMPRE
        ListaCompra listaActualizada = listaCompraServicio.obtenerListaCompra(lista.getId(), usuarioIdActual);
        if (listaActualizada == null) {
            ocultarDetalleLista();
            return;
        }

        // USAR SOLO LA LISTA ACTUALIZADA
        listaSeleccionada = listaActualizada;

        // Mostrar panel de detalle
        detalleListaPane.setVisible(true);
        detalleListaPane.setManaged(true);
        EstilosApp.aplicarEstiloTarjeta(detalleListaPane);

        // Actualizar datos básicos
        nombreListaLabel.setText(listaActualizada.getNombre());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        fechaCreacionLabel.setText(listaActualizada.getFechaCreacion().format(formatter));
        fechaProgramadaLabel.setText(listaActualizada.getFechaPlanificada() != null
                ? listaActualizada.getFechaPlanificada().format(formatter) : "No programada");

        modalidadLabel.setText(listaActualizada.getModalidadAhorro());
        presupuestoLabel.setText(String.format("€%.2f", listaActualizada.getPresupuestoMaximo()));

        // Actualizar tabla de productos CON LOGGING
        System.out.println("=== ACTUALIZANDO TABLA ===");
        System.out.println("Número de items en la lista: " + listaActualizada.getItems().size());

        ObservableList<ItemCompra> items = FXCollections.observableArrayList(listaActualizada.getItems());
        productosTableView.setItems(items);
        productosTableView.refresh(); // Forzar el refresh de la tabla

        System.out.println("Items en ObservableList: " + items.size());
        System.out.println("Items en TableView: " + productosTableView.getItems().size());

        // Actualizar estado de completada
        completadaCheckBox.setSelected(listaActualizada.isCompletada());

        // Actualizar resumen
        actualizarResumenLista();

        // FORZAR ACTUALIZACIÓN DE LA VISTA
        Platform.runLater(() -> {
            productosTableView.refresh();
            productosTableView.getScene().getWindow().sizeToScene();
        });
    }

    /**
     * Oculta el panel de detalle de lista
     */
    private void ocultarDetalleLista() {
        detalleListaPane.setVisible(false);
        detalleListaPane.setManaged(false);
        listaSeleccionada = null;
    }

    /**
     * Actualiza el resumen de la lista seleccionada
     */
    private void actualizarResumenLista() {
        if (listaSeleccionada != null) {
            // Calcular coste total
            double costeTotal = listaSeleccionada.getCosteTotal();
            costeTotalLabel.setText(String.format("€%.2f", costeTotal));

            // Calcular progreso
            int porcentaje = listaSeleccionada.getPorcentajeProgreso();
            progresoBar.setProgress(porcentaje / 100.0);
            progresoLabel.setText(porcentaje + "%");

            // Colorear barra de progreso según porcentaje
            String color;
            if (porcentaje > 80) {
                color = "rgb(100, 220, 100)"; // Verde
            } else if (porcentaje > 50) {
                color = "rgb(200, 200, 0)"; // Amarillo
            } else {
                color = "rgb(100, 100, 200)"; // Azul
            }

            progresoBar.setStyle("-fx-accent: " + color + ";");
        }
    }

    /**
     * Manejador para el cambio en el filtro de listas
     */
    @FXML
    private void handleFiltroListas(ActionEvent event) {
        cargarListasCompra();
    }

    /**
     * Manejador para crear una nueva lista
     */
    @FXML
    private void handleCrearLista(ActionEvent event) {
        // Mostrar panel de creación de lista
        modoEdicion = false;
        crearListaTituloLabel.setText("Crear Lista de Compra");

        // Limpiar campos
        nombreListaField.clear();
        modalidadComboBox.getSelectionModel().select("Equilibrado");
        presupuestoField.clear();
        fechaPlanificadaPicker.setValue(LocalDate.now().plusDays(7));
        generarAutomaticoCheckBox.setSelected(true);

        mostrarPanelCrearLista();
    }

    /**
     * Manejador para editar una lista
     */
    @FXML
    private void handleEditarLista(ActionEvent event) {
        if (listaSeleccionada == null) {
            return;
        }

        // Mostrar panel de edición de lista
        modoEdicion = true;
        crearListaTituloLabel.setText("Editar Lista de Compra");

        // Cargar datos actuales
        nombreListaField.setText(listaSeleccionada.getNombre());
        modalidadComboBox.setValue(listaSeleccionada.getModalidadAhorro());
        presupuestoField.setText(String.format("%.2f", listaSeleccionada.getPresupuestoMaximo()));
        fechaPlanificadaPicker.setValue(listaSeleccionada.getFechaPlanificada());
        generarAutomaticoCheckBox.setSelected(false);
        generarAutomaticoCheckBox.setDisable(true); // No permitir generación automática en edición

        mostrarPanelCrearLista();
    }

    /**
     * Manejador para eliminar una lista
     */
    @FXML
    private void handleEliminarLista(ActionEvent event) {
        if (listaSeleccionada == null) {
            return;
        }

        if (navegacionServicio.confirmarEliminarLista()) {
            listaCompraServicio.eliminarListaCompra(listaSeleccionada.getId(), usuarioIdActual);
            ocultarDetalleLista();
            cargarListasCompra();
        }
    }

    /**
     * Manejador para guardar la lista
     */
    @FXML
    private void handleGuardarLista(ActionEvent event) {
        // Validar campos
        if (nombreListaField.getText().trim().isEmpty() ||
                presupuestoField.getText().trim().isEmpty() ||
                modalidadComboBox.getValue() == null) {

            navegacionServicio.mostrarAlertaError("Campos incompletos", "Por favor, completa todos los campos obligatorios.");
            return;
        }

        try {
            // Obtener valores
            String nombre = nombreListaField.getText().trim();
            String modalidad = modalidadComboBox.getValue();
            double presupuesto = Double.parseDouble(presupuestoField.getText().trim());
            LocalDate fechaPlanificada = fechaPlanificadaPicker.getValue();
            boolean generarAutomatico = generarAutomaticoCheckBox.isSelected();

            // Validar presupuesto
            if (presupuesto <= 0) {
                navegacionServicio.mostrarAlertaError("Presupuesto inválido", "El presupuesto debe ser mayor que cero.");
                return;
            }

            if (modoEdicion) {
                // Actualizar lista existente
                listaSeleccionada.setNombre(nombre);
                listaSeleccionada.setModalidadAhorro(modalidad);
                listaSeleccionada.setPresupuestoMaximo(presupuesto);
                listaSeleccionada.setFechaPlanificada(fechaPlanificada);

                listaCompraServicio.actualizarListaCompra(listaSeleccionada);
                mostrarDetalleLista(listaSeleccionada);

                navegacionServicio.mostrarAlertaInformacion("Lista actualizada", "La lista de compra ha sido actualizada correctamente.");
            } else {
                // Crear nueva lista
                ListaCompra nuevaLista;

                if (generarAutomatico) {
                    // Generar lista automáticamente según perfil nutricional
                    nuevaLista = listaCompraServicio.generarListaOptimizada(
                            usuarioIdActual, nombre, modalidad, presupuesto);
                } else {
                    // Crear lista vacía
                    nuevaLista = listaCompraServicio.crearListaCompra(
                            usuarioIdActual, nombre, modalidad, presupuesto);
                }

                // Establecer fecha planificada
                nuevaLista.setFechaPlanificada(fechaPlanificada);
                listaCompraServicio.actualizarListaCompra(nuevaLista);

                // Actualizar lista y seleccionar la nueva
                cargarListasCompra();
                listasCompraListView.getSelectionModel().select(nuevaLista);

                navegacionServicio.mostrarAlertaInformacion("Lista creada", "La lista de compra ha sido creada correctamente.");
            }

            // Ocultar panel
            ocultarPanelCrearLista();

        } catch (NumberFormatException e) {
            navegacionServicio.mostrarAlertaError("Formato incorrecto", "Por favor, ingresa un presupuesto válido.");
        }
    }

    /**
     * Manejador para cancelar la creación/edición de lista
     */
    @FXML
    private void handleCancelarLista(ActionEvent event) {
        ocultarPanelCrearLista();
    }

    /**
     * Manejador para cambiar estado de completada
     */
    @FXML
    private void handleCompletadaChange(ActionEvent event) {
        if (listaSeleccionada != null) {
            listaSeleccionada.setCompletada(completadaCheckBox.isSelected());
            listaCompraServicio.actualizarListaCompra(listaSeleccionada);
            cargarListasCompra(); // Actualizar lista por si cambia el filtro
        }
    }

    /**
     * Manejador para agregar producto a la lista
     */
    @FXML
    private void handleAgregarProducto(ActionEvent event) {
        if (listaSeleccionada == null) {
            return;
        }

        // Limpiar búsqueda anterior
        buscarProductoField.clear();
        resultadosProductosTableView.setItems(FXCollections.observableArrayList());

        // Mostrar panel de búsqueda
        mostrarPanelBuscarProducto();
    }

    /**
     * Manejador para buscar productos
     */
    @FXML
    private void handleBuscarProducto(ActionEvent event) {
        String termino = buscarProductoField.getText().trim();

        // Mostrar indicador de carga
        buscarProductoButton.setDisable(true);
        buscarProductoButton.setText("Buscando...");

        // Usar CompletableFuture para no bloquear la UI
        CompletableFuture.supplyAsync(() -> {
            List<Producto> resultados;
            if (termino.isEmpty()) {
                resultados = productoServicio.obtenerTodosProductos();
            } else {
                resultados = productoServicio.buscarProductos(termino);
            }
            return resultados;
        }).thenAccept(resultados -> {
            // Actualizar UI en el hilo de JavaFX
            Platform.runLater(() -> {
                resultadosProductosTableView.setItems(FXCollections.observableArrayList(resultados));
                buscarProductoButton.setDisable(false);
                buscarProductoButton.setText("Buscar");

                // Mostrar mensaje informativo
                if (resultados.isEmpty()) {
                    navegacionServicio.mostrarAlertaInformacion(
                            "Búsqueda sin resultados",
                            "No se encontraron productos para: " + termino);
                } else {
                    System.out.println("Encontrados " + resultados.size() + " productos para: " + termino);
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                navegacionServicio.mostrarAlertaError(
                        "Error en búsqueda",
                        "Se produjo un error al buscar productos: " + e.getMessage());
                buscarProductoButton.setDisable(false);
                buscarProductoButton.setText("Buscar");
            });
            return null;
        });
    }

    /**
     * Manejador para cerrar la búsqueda de productos
     */
    @FXML
    private void handleCerrarBusqueda(ActionEvent event) {
        ocultarPanelBuscarProducto();
    }

    /**
     * Muestra el panel de creación/edición de lista
     */
    private void mostrarPanelCrearLista() {
        crearListaPane.setVisible(true);
        crearListaPane.setManaged(true);
        EstilosApp.aplicarEstiloTarjeta(crearListaPane);
    }

    /**
     * Oculta el panel de creación/edición de lista
     */
    private void ocultarPanelCrearLista() {
        crearListaPane.setVisible(false);
        crearListaPane.setManaged(false);
    }

    /**
     * Muestra el panel de búsqueda de productos
     */
    private void mostrarPanelBuscarProducto() {
        // Limpiar búsqueda anterior
        buscarProductoField.clear();
        resultadosProductosTableView.setItems(FXCollections.observableArrayList());

        // Cargar algunos productos populares por defecto
        try {
            List<Producto> productosPopulares = productoServicio.obtenerTodosProductos().stream()
                    .limit(15)  // Mostrar los primeros 15 productos
                    .collect(Collectors.toList());

            if (!productosPopulares.isEmpty()) {
                resultadosProductosTableView.setItems(FXCollections.observableArrayList(productosPopulares));
                System.out.println("Cargados " + productosPopulares.size() + " productos populares");
            } else {
                System.out.println("No se encontraron productos populares");
            }
        } catch (Exception e) {
            System.err.println("Error cargando productos populares: " + e.getMessage());
        }

        // Mostrar panel de búsqueda
        agregarProductoPane.setVisible(true);
        agregarProductoPane.setManaged(true);
        EstilosApp.aplicarEstiloTarjeta(agregarProductoPane);
    }

    /**
     * Oculta el panel de búsqueda de productos
     */
    private void ocultarPanelBuscarProducto() {
        agregarProductoPane.setVisible(false);
        agregarProductoPane.setManaged(false);
    }

    /**
     * Muestra un diálogo de confirmación
     */
    private boolean mostrarConfirmacion(String titulo, String mensaje) {
        return navegacionServicio.confirmarAccion(titulo, mensaje);
    }

    /**
     * Sobrescribe el método de navegación a compras
     * Ya que estamos en la vista de plan de compras
     */
    @Override
    public void handleShoppingAction(ActionEvent evento) {
        // Ya estamos en la vista de plan de compras, solo cargar datos
        cargarListasCompra();
        activarBoton(shoppingButton);
    }
}