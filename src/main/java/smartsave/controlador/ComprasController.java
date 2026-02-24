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
import javafx.stage.Stage;
import smartsave.modelo.*;
import smartsave.servicio.*;
import smartsave.utilidad.EstilosApp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ComprasController extends BaseController {

    @FXML private ComboBox<String> filtroListasComboBox;
    @FXML private ListView<ListaCompra> listasCompraListView;
    @FXML private Button crearListaButton;
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
    @FXML private VBox crearListaPane;
    @FXML private Label crearListaTituloLabel;
    @FXML private TextField nombreListaField;
    @FXML private ComboBox<String> modalidadComboBox;
    @FXML private TextField presupuestoField;
    @FXML private DatePicker fechaPlanificadaPicker;
    @FXML private CheckBox generarAutomaticoCheckBox;
    @FXML private Button guardarListaButton;
    @FXML private Button cancelarListaButton;
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

    private final ListaCompraServicio listaCompraServicio = new ListaCompraServicio();
    private final ProductoServicio productoServicio = new ProductoServicio();
    private final TransaccionServicio transaccionServicio = new TransaccionServicio();

    private Long usuarioIdActualLocal;
    private Usuario usuarioActualLocal; 
    private ListaCompra listaSeleccionada = null;
    private boolean modoEdicion = false;

    @Override
    protected void inicializarControlador() {
        this.usuarioActualLocal = SessionManager.getInstancia().getUsuarioActual();

        if (this.usuarioActualLocal == null) {
            System.err.println("Error crítico: No hay usuario en sesión en ComprasController.");
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

        activarBoton(shoppingButton);
        inicializarPantallaCompras();
        cargarListasCompra(); 
        aplicarEstilosComponentes();
    }

    private void disableUIComponents() {
        
        if (filtroListasComboBox != null) filtroListasComboBox.setDisable(true);
        if (listasCompraListView != null) listasCompraListView.setItems(FXCollections.observableArrayList());
        if (crearListaButton != null) crearListaButton.setDisable(true);
        ocultarDetalleLista();
        ocultarPanelCrearLista();
        ocultarPanelBuscarProducto();
    }

    private void aplicarEstilosComponentes() {
        if (filtroListasComboBox != null) EstilosApp.aplicarEstiloComboBox(filtroListasComboBox);
        if (modalidadComboBox != null) EstilosApp.aplicarEstiloComboBox(modalidadComboBox);
        if (nombreListaField != null) EstilosApp.aplicarEstiloCampoTexto(nombreListaField);
        if (presupuestoField != null) EstilosApp.aplicarEstiloCampoTexto(presupuestoField);
        if (buscarProductoField != null) EstilosApp.aplicarEstiloCampoTexto(buscarProductoField);
        if (fechaPlanificadaPicker != null) EstilosApp.aplicarEstiloDatePicker(fechaPlanificadaPicker);
        if (productosTableView != null) EstilosApp.aplicarEstiloTabla(productosTableView);
        if (resultadosProductosTableView != null) EstilosApp.aplicarEstiloTabla(resultadosProductosTableView);
        if (crearListaButton != null) EstilosApp.aplicarEstiloBotonPrimario(crearListaButton);
        if (editarListaButton != null) EstilosApp.aplicarEstiloBotonPrimario(editarListaButton);
        if (eliminarListaButton != null) EstilosApp.aplicarEstiloBotonPrimario(eliminarListaButton);
        if (agregarProductoButton != null) EstilosApp.aplicarEstiloBotonPrimario(agregarProductoButton);
        if (guardarListaButton != null) EstilosApp.aplicarEstiloBotonPrimario(guardarListaButton);
        if (cancelarListaButton != null) EstilosApp.aplicarEstiloBotonPrimario(cancelarListaButton);
        if (buscarProductoButton != null) EstilosApp.aplicarEstiloBotonPrimario(buscarProductoButton);
        if (cerrarBusquedaButton != null) EstilosApp.aplicarEstiloBotonPrimario(cerrarBusquedaButton);
        estilizarListView();
        estilizarPaneles();
    }

    private void estilizarListView() {
        if (listasCompraListView != null) {
            listasCompraListView.setStyle("-fx-background-color: rgba(30, 30, 40, 0.7); -fx-background-radius: 5px; -fx-border-color: rgba(80, 80, 120, 0.5); -fx-border-radius: 5px; -fx-text-fill: white;");
        }
    }

    private void estilizarPaneles() {
        if (detalleListaPane != null && detalleListaPane.isVisible()) EstilosApp.aplicarEstiloTarjeta(detalleListaPane);
        if (crearListaPane != null && crearListaPane.isVisible()) EstilosApp.aplicarEstiloTarjeta(crearListaPane);
        if (agregarProductoPane != null && agregarProductoPane.isVisible()) EstilosApp.aplicarEstiloTarjeta(agregarProductoPane);
    }

    private void inicializarPantallaCompras() {
        if (filtroListasComboBox != null) {
            filtroListasComboBox.setItems(FXCollections.observableArrayList("Todas", "Activas", "Completadas"));
            filtroListasComboBox.getSelectionModel().selectFirst();
        }
        if (listasCompraListView != null) {
            listasCompraListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) mostrarDetalleLista(newVal); else ocultarDetalleLista();
            });
            configurarCeldaListaCompra();
        }
        configurarTablaProductos();
        configurarPanelCrearLista();
        ocultarDetalleLista();
        ocultarPanelCrearLista();
        ocultarPanelBuscarProducto();
    }

    private void configurarCeldaListaCompra() {
        if (listasCompraListView == null) return;
        listasCompraListView.setCellFactory(param -> new ListCell<ListaCompra>() {
            @Override
            protected void updateItem(ListaCompra item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setGraphic(null); setStyle("-fx-background-color: transparent;");
                } else {
                    VBox contenido = new VBox(5);
                    contenido.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));
                    Label nombreLabel = new Label(item.getNombre());
                    nombreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
                    HBox detalles = new HBox(15);
                    Label costeLabel = new Label(String.format("€%.2f", item.getCosteTotal()));
                    costeLabel.setStyle("-fx-text-fill: rgb(230, 230, 250);");
                    Label progresoLabelVista = new Label(item.getPorcentajeProgreso() + "% completado");
                    progresoLabelVista.setStyle("-fx-text-fill: " + (item.isCompletada() ? "rgb(100, 220, 100)" : "rgb(230, 230, 250)") + ";");
                    detalles.getChildren().addAll(costeLabel, progresoLabelVista);
                    Label estadoLabel = new Label(item.isCompletada() ? "Completada" : "En progreso");
                    estadoLabel.setStyle("-fx-text-fill: " + (item.isCompletada() ? "rgb(100, 220, 100)" : "rgb(255, 200, 0)") + "; -fx-font-size: 11px;");
                    contenido.getChildren().addAll(nombreLabel, detalles, estadoLabel);
                    setGraphic(contenido);
                    selectedProperty().addListener((obs, wasSelected, isNowSelected) -> setStyle(isNowSelected ? "-fx-background-color: rgba(80, 80, 140, 0.7); -fx-background-radius: 5px;" : "-fx-background-color: transparent;"));
                    setStyle(isSelected() ? "-fx-background-color: rgba(80, 80, 140, 0.7); -fx-background-radius: 5px;" : "-fx-background-color: transparent;");
                }
            }
        });
    }

    private void configurarTablaProductos() {
        if (productosTableView == null || compradoColumn == null || nombreColumn == null || cantidadColumn == null || precioColumn == null || totalColumn == null) return;
        compradoColumn.setCellValueFactory(new PropertyValueFactory<>("comprado"));
        compradoColumn.setCellFactory(col -> new TableCell<ItemCompra, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setOnAction(event -> {
                    if (getTableView().getItems().size() > getIndex() && getIndex() >= 0) {
                        ItemCompra item = getTableView().getItems().get(getIndex());
                        item.setComprado(checkBox.isSelected());
                        listaCompraServicio.actualizarEstadoItem(listaSeleccionada, item.getId(), checkBox.isSelected());
                        actualizarResumenLista();
                    }
                });
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null); else { checkBox.setSelected(item); setGraphic(checkBox); }
            }
        });
        nombreColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProducto().getNombre()));
        cantidadColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        precioColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("€%.2f", cellData.getValue().getProducto().getPrecio())));
        totalColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("€%.2f", cellData.getValue().getPrecioTotal())));
        configurarColumnaAccionesProductos();
        configurarTablaResultadosBusqueda();
        EstilosApp.aplicarEstiloTabla(productosTableView);
        EstilosApp.aplicarEstiloCabecerasTabla(productosTableView);
    }

    private void configurarColumnaAccionesProductos() {
        if (accionesColumn == null) return;
        accionesColumn.setCellFactory(param -> new TableCell<ItemCompra, Void>() {
            private final Button btnAumentar = new Button("+");
            private final Button btnDisminuir = new Button("-");
            private final Button btnEliminar = new Button("🗑️");
            private final HBox pane = new HBox(5, btnAumentar, btnDisminuir, btnEliminar);
            {
                String btnStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand;";
                btnAumentar.setStyle(btnStyle); btnDisminuir.setStyle(btnStyle); btnEliminar.setStyle(btnStyle);
                btnAumentar.setOnAction(event -> {
                    if (getTableView().getItems().size() > getIndex() && getIndex() >= 0) {
                        ItemCompra item = getTableView().getItems().get(getIndex());
                        item.incrementarCantidad();
                        listaCompraServicio.actualizarCantidadItem(listaSeleccionada, item.getId(), item.getCantidad());
                        actualizarResumenLista(); getTableView().refresh();
                    }
                });
                btnDisminuir.setOnAction(event -> {
                    if (getTableView().getItems().size() > getIndex() && getIndex() >= 0) {
                        ItemCompra item = getTableView().getItems().get(getIndex());
                        item.decrementarCantidad();
                        listaCompraServicio.actualizarCantidadItem(listaSeleccionada, item.getId(), item.getCantidad());
                        actualizarResumenLista(); getTableView().refresh();
                    }
                });
                btnEliminar.setOnAction(event -> {
                    if (getTableView().getItems().size() > getIndex() && getIndex() >= 0) {
                        ItemCompra item = getTableView().getItems().get(getIndex());
                        if (mostrarConfirmacion("Eliminar Producto", "¿Estás seguro de que deseas eliminar este producto de la lista?")) {
                            Long listaId = listaSeleccionada.getId();
                            Long usuarioId = listaSeleccionada.getUsuarioId();
                            boolean eliminado = listaCompraServicio.eliminarItemDeLista(listaSeleccionada, item.getId());
                            if (eliminado) {
                                listaSeleccionada = listaCompraServicio.obtenerListaCompra(listaId, usuarioId);
                                mostrarDetalleLista(listaSeleccionada);
                            }
                        }
                    }
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

    private void configurarPanelCrearLista() {
        if (modalidadComboBox != null) {
            modalidadComboBox.setItems(FXCollections.observableArrayList("Máximo", "Equilibrado", "Estándar"));
            modalidadComboBox.getSelectionModel().select("Equilibrado");
        }
        if (presupuestoField != null) {
            presupuestoField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*\\.?\\d*")) presupuestoField.setText(oldValue);
            });
        }
    }

    private void configurarTablaResultadosBusqueda() {
        if (resultadosProductosTableView == null || productoNombreColumn == null || productoMarcaColumn == null || productoCategoriaColumn == null || productoPrecioColumn == null || productoAccionesColumn == null) return;
        productoNombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        productoMarcaColumn.setCellValueFactory(new PropertyValueFactory<>("marca"));
        productoCategoriaColumn.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        productoPrecioColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("€%.2f", cellData.getValue().getPrecio())));
        productoAccionesColumn.setCellFactory(param -> new TableCell<Producto, Void>() {
            private final Button btnAnadir = new Button("Añadir");
            {
                btnAnadir.setStyle("-fx-background-color: rgba(100, 180, 100, 0.7); -fx-text-fill: white; -fx-cursor: hand;");
                btnAnadir.setOnAction(event -> {
                    if (getTableView().getItems().size() > getIndex() && getIndex() >= 0) {
                        Producto producto = getTableView().getItems().get(getIndex());
                        listaCompraServicio.agregarProductoALista(listaSeleccionada, producto.getId(), producto, 1);
                        mostrarDetalleLista(listaSeleccionada);
                        navegacionServicio.mostrarAlertaInformacion("Producto añadido", "El producto ha sido añadido a la lista de compra.");
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : btnAnadir); }
        });
        EstilosApp.aplicarEstiloCabecerasTabla(resultadosProductosTableView);
    }

    private void cargarListasCompra() {
        if (usuarioIdActualLocal == null) {
            disableUIComponents();
            return;
        }
        if (filtroListasComboBox == null || listasCompraListView == null) return;
        String filtro = filtroListasComboBox.getValue();
        ObservableList<ListaCompra> listas;
        if ("Activas".equals(filtro)) listas = FXCollections.observableArrayList(listaCompraServicio.obtenerListasActivas(usuarioIdActualLocal));
        else if ("Completadas".equals(filtro)) listas = FXCollections.observableArrayList(listaCompraServicio.obtenerListasCompletadas(usuarioIdActualLocal));
        else listas = FXCollections.observableArrayList(listaCompraServicio.obtenerListasCompraUsuario(usuarioIdActualLocal));
        listasCompraListView.setItems(listas);
        if (listas.isEmpty()) ocultarDetalleLista();
    }

    private void mostrarDetalleLista(ListaCompra lista) {
        if (detalleListaPane == null || nombreListaLabel == null || fechaCreacionLabel == null || fechaProgramadaLabel == null || modalidadLabel == null || presupuestoLabel == null || productosTableView == null || completadaCheckBox == null || progresoBar == null || progresoLabel == null || costeTotalLabel == null) {
            ocultarDetalleLista(); return;
        }
        if (usuarioIdActualLocal == null) {
            ocultarDetalleLista(); return;
        }

        ListaCompra listaActualizada = listaCompraServicio.obtenerListaCompra(lista.getId(), usuarioIdActualLocal);
        if (listaActualizada == null) {
            ocultarDetalleLista();
            navegacionServicio.mostrarAlertaError("Error", "No se pudo cargar la lista seleccionada.");
            return;
        }
        listaSeleccionada = listaActualizada;
        detalleListaPane.setVisible(true);
        detalleListaPane.setManaged(true);
        EstilosApp.aplicarEstiloTarjeta(detalleListaPane);
        nombreListaLabel.setText(listaActualizada.getNombre());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        fechaCreacionLabel.setText(listaActualizada.getFechaCreacion().format(formatter));
        fechaProgramadaLabel.setText(listaActualizada.getFechaPlanificada() != null ? listaActualizada.getFechaPlanificada().format(formatter) : "No programada");
        modalidadLabel.setText(listaActualizada.getModalidadAhorro());
        presupuestoLabel.setText(String.format("€%.2f", listaActualizada.getPresupuestoMaximoAsDouble()));
        ObservableList<ItemCompra> items = FXCollections.observableArrayList(listaActualizada.getItems());
        productosTableView.setItems(items);
        productosTableView.refresh();
        completadaCheckBox.setSelected(listaActualizada.isCompletada());
        actualizarResumenLista();
        Platform.runLater(() -> {
            productosTableView.refresh();
        });
    }

    private void ocultarDetalleLista() {
        if (detalleListaPane != null) {
            detalleListaPane.setVisible(false);
            detalleListaPane.setManaged(false);
        }
        listaSeleccionada = null;
    }

    private void actualizarResumenLista() {
        if (listaSeleccionada != null && costeTotalLabel != null && progresoBar != null && progresoLabel != null) {
            double costeTotal = listaSeleccionada.getCosteTotal();
            costeTotalLabel.setText(String.format("€%.2f", costeTotal));
            int porcentaje = listaSeleccionada.getPorcentajeProgreso();
            progresoBar.setProgress(porcentaje / 100.0);
            progresoLabel.setText(porcentaje + "%");
            String color;
            if (porcentaje > 80) color = "rgb(100, 220, 100)";
            else if (porcentaje > 50) color = "rgb(200, 200, 0)";
            else color = "rgb(100, 100, 200)";
            progresoBar.setStyle("-fx-accent: " + color + ";");
        }
    }

    @FXML private void handleFiltroListas(ActionEvent event) { cargarListasCompra(); }

    @FXML
    private void handleCrearLista(ActionEvent event) {
        if (crearListaTituloLabel == null || nombreListaField == null || modalidadComboBox == null || presupuestoField == null || fechaPlanificadaPicker == null || generarAutomaticoCheckBox == null) return;

        modoEdicion = false;
        crearListaTituloLabel.setText("Crear Lista de Compra");
        nombreListaField.clear();
        presupuestoField.clear();
        fechaPlanificadaPicker.setValue(LocalDate.now().plusDays(7));
        generarAutomaticoCheckBox.setSelected(false);

        String modalidadPorDefecto = "Equilibrado";

        if (usuarioActualLocal != null && usuarioActualLocal.getModalidadAhorroSeleccionada() != null && !usuarioActualLocal.getModalidadAhorroSeleccionada().isEmpty()) {
            modalidadPorDefecto = usuarioActualLocal.getModalidadAhorroSeleccionada();
        }

        modalidadComboBox.setValue(modalidadPorDefecto);

        mostrarPanelCrearLista();
    }

    @FXML
    private void handleEditarLista(ActionEvent event) {
        if (listaSeleccionada == null || crearListaTituloLabel == null || nombreListaField == null || modalidadComboBox == null || presupuestoField == null || fechaPlanificadaPicker == null || generarAutomaticoCheckBox == null) return;
        modoEdicion = true;
        crearListaTituloLabel.setText("Editar Lista de Compra");
        nombreListaField.setText(listaSeleccionada.getNombre());
        modalidadComboBox.setValue(listaSeleccionada.getModalidadAhorro());
        presupuestoField.setText(String.format("%.2f", listaSeleccionada.getPresupuestoMaximoAsDouble()));
        fechaPlanificadaPicker.setValue(listaSeleccionada.getFechaPlanificada());
        generarAutomaticoCheckBox.setSelected(false);
        generarAutomaticoCheckBox.setDisable(true);
        mostrarPanelCrearLista();
    }

    @FXML
    private void handleEliminarLista(ActionEvent event) {
        if (listaSeleccionada == null || usuarioIdActualLocal == null) return;
        if (navegacionServicio.confirmarEliminarLista()) {
            listaCompraServicio.eliminarListaCompra(listaSeleccionada.getId(), usuarioIdActualLocal);
            ocultarDetalleLista();
            cargarListasCompra();
        }
    }

    @FXML
    private void handleGuardarLista(ActionEvent event) {
        if (usuarioIdActualLocal == null) {
            navegacionServicio.mostrarAlertaError("Error de Sesión", "No hay un usuario activo para guardar la lista.");
            return;
        }
        if (nombreListaField.getText().trim().isEmpty() || presupuestoField.getText().trim().isEmpty() || modalidadComboBox.getValue() == null) {
            navegacionServicio.mostrarAlertaError("Campos incompletos", "Por favor, completa todos los campos obligatorios.");
            return;
        }
        try {
            String nombre = nombreListaField.getText().trim();
            String modalidad = modalidadComboBox.getValue();
            double presupuesto = Double.parseDouble(presupuestoField.getText().trim());
            LocalDate fechaPlanificada = fechaPlanificadaPicker.getValue();
            boolean generarAutomatico = generarAutomaticoCheckBox.isSelected();
            if (presupuesto <= 0) {
                navegacionServicio.mostrarAlertaError("Presupuesto inválido", "El presupuesto debe ser mayor que cero.");
                return;
            }
            if (modoEdicion && listaSeleccionada != null) {
                listaSeleccionada.setNombre(nombre);
                listaSeleccionada.setModalidadAhorro(modalidad);
                listaSeleccionada.setPresupuestoMaximo(presupuesto);
                listaSeleccionada.setFechaPlanificada(fechaPlanificada);
                listaCompraServicio.actualizarListaCompra(listaSeleccionada);
                mostrarDetalleLista(listaSeleccionada);
                navegacionServicio.mostrarAlertaInformacion("Lista actualizada", "La lista de compra ha sido actualizada correctamente.");
            } else {
                ListaCompra nuevaLista;
                if (generarAutomatico) nuevaLista = listaCompraServicio.generarListaOptimizada(usuarioIdActualLocal, nombre, modalidad, presupuesto);
                else nuevaLista = listaCompraServicio.crearListaCompra(usuarioIdActualLocal, nombre, modalidad, presupuesto);
                nuevaLista.setFechaPlanificada(fechaPlanificada);
                listaCompraServicio.actualizarListaCompra(nuevaLista);
                cargarListasCompra();
                if (listasCompraListView != null) listasCompraListView.getSelectionModel().select(nuevaLista);
                navegacionServicio.mostrarAlertaInformacion("Lista creada", "La lista de compra ha sido creada correctamente.");
            }
            ocultarPanelCrearLista();
        } catch (NumberFormatException e) {
            navegacionServicio.mostrarAlertaError("Formato incorrecto", "Por favor, ingresa un presupuesto válido.");
        }
    }

    @FXML private void handleCancelarLista(ActionEvent event) { ocultarPanelCrearLista(); }

    @FXML
    private void handleCompletadaChange(ActionEvent event) {
        if (listaSeleccionada == null || completadaCheckBox == null || usuarioIdActualLocal == null) {
            return;
        }

        boolean completada = completadaCheckBox.isSelected();
        listaSeleccionada.setCompletada(completada);

        if (completada) {
            
            
            if (listaSeleccionada.getTransaccionIdAsociada() == null) {
                
                Transaccion nuevaTransaccion = new Transaccion();
                nuevaTransaccion.setFecha(LocalDate.now());
                nuevaTransaccion.setDescripcion("Compra: " + listaSeleccionada.getNombre());
                nuevaTransaccion.setCategoria("Alimentación");
                nuevaTransaccion.setMonto(listaSeleccionada.getCosteTotal());
                nuevaTransaccion.setTipo("Gasto");

                
                Transaccion transaccionGuardada = transaccionServicio.agregarTransaccion(nuevaTransaccion, usuarioIdActualLocal);

                
                listaSeleccionada.setTransaccionIdAsociada(transaccionGuardada.getId());

                navegacionServicio.mostrarAlertaInformacion("Gasto Registrado", "La compra se ha añadido como un gasto en tus transacciones.");
            }
        } else {
            
            
            Long transaccionId = listaSeleccionada.getTransaccionIdAsociada();
            if (transaccionId != null) {
                
                boolean eliminada = transaccionServicio.eliminarTransaccion(transaccionId, usuarioIdActualLocal);
                if (eliminada) {
                    
                    listaSeleccionada.setTransaccionIdAsociada(null);
                    navegacionServicio.mostrarAlertaInformacion("Gasto Anulado", "El gasto asociado a esta compra ha sido eliminado de tus transacciones.");
                }
            }
        }

        
        listaCompraServicio.actualizarListaCompra(listaSeleccionada);

        
        cargarListasCompra();
    }

    @FXML
    private void handleAgregarProducto(ActionEvent event) {
        if (listaSeleccionada == null) return;
        if (buscarProductoField != null) buscarProductoField.clear();
        if (resultadosProductosTableView != null) resultadosProductosTableView.setItems(FXCollections.observableArrayList());
        mostrarPanelBuscarProducto();
    }

    @FXML
    private void handleBuscarProducto(ActionEvent event) {
        if (buscarProductoField == null || buscarProductoButton == null || resultadosProductosTableView == null) return;
        String termino = buscarProductoField.getText().trim();

        
        if (listaSeleccionada == null) {
            navegacionServicio.mostrarAlertaError("Seleccione una Lista", "Por favor, selecciona primero una lista de compra para poder buscar productos.");
            return;
        }

        ModalidadAhorroServicio modalidadServicio = new ModalidadAhorroServicio();
        ModalidadAhorro modalidadActual = modalidadServicio.obtenerModalidadPorNombre(listaSeleccionada.getModalidadAhorro());

        if (modalidadActual == null) {
            navegacionServicio.mostrarAlertaError("Error de Modalidad", "No se pudo cargar la modalidad de ahorro de la lista. Seleccionando 'Equilibrado' por defecto.");
            modalidadActual = modalidadServicio.obtenerModalidadPorNombre("Equilibrado");
            if (modalidadActual == null) { 
                navegacionServicio.mostrarAlertaError("Error Crítico", "No se encontraron las modalidades de ahorro.");
                return;
            }
        }
        

        buscarProductoButton.setDisable(true);
        buscarProductoButton.setText("Buscando...");

        
        final ModalidadAhorro modalidadParaBusqueda = modalidadActual;

        CompletableFuture.supplyAsync(() -> {
            
            return productoServicio.buscarProductos(termino, modalidadParaBusqueda);
        }).thenAccept(resultados -> Platform.runLater(() -> {
            resultadosProductosTableView.setItems(FXCollections.observableArrayList(resultados));
            buscarProductoButton.setDisable(false);
            buscarProductoButton.setText("Buscar");
            if (resultados.isEmpty()) {
                navegacionServicio.mostrarAlertaInformacion("Búsqueda sin resultados", "No se encontraron productos para: " + termino);
            }
        })).exceptionally(e -> {
            Platform.runLater(() -> {
                navegacionServicio.mostrarAlertaError("Error en búsqueda", "Se produjo un error al buscar productos: " + e.getMessage());
                buscarProductoButton.setDisable(false);
                buscarProductoButton.setText("Buscar");
            });
            return null;
        });
    }

    @FXML private void handleCerrarBusqueda(ActionEvent event) { ocultarPanelBuscarProducto(); }

    private void mostrarPanelCrearLista() {
        if (crearListaPane != null) {
            crearListaPane.setVisible(true);
            crearListaPane.setManaged(true);
            EstilosApp.aplicarEstiloTarjeta(crearListaPane);
        }
    }

    private void ocultarPanelCrearLista() {
        if (crearListaPane != null) {
            crearListaPane.setVisible(false);
            crearListaPane.setManaged(false);
        }
    }

    private void mostrarPanelBuscarProducto() {
        if (buscarProductoField != null) buscarProductoField.clear();
        if (resultadosProductosTableView != null) resultadosProductosTableView.setItems(FXCollections.observableArrayList());
        try {
            if (productoServicio != null) {
                List<Producto> productosPopulares = productoServicio.obtenerTodosProductos().stream().limit(15).collect(Collectors.toList()); 
                if (resultadosProductosTableView != null && !productosPopulares.isEmpty()) {
                    resultadosProductosTableView.setItems(FXCollections.observableArrayList(productosPopulares));
                    System.out.println("Cargados " + productosPopulares.size() + " productos populares");
                } else if (resultadosProductosTableView != null) {
                    System.out.println("No se encontraron productos populares");
                }
            }
        } catch (Exception e) {
            System.err.println("Error cargando productos populares: " + e.getMessage());
        }
        if (agregarProductoPane != null) {
            agregarProductoPane.setVisible(true);
            agregarProductoPane.setManaged(true);
            EstilosApp.aplicarEstiloTarjeta(agregarProductoPane); 
        }
    }

    private void ocultarPanelBuscarProducto() {
        if (agregarProductoPane != null) {
            agregarProductoPane.setVisible(false);
            agregarProductoPane.setManaged(false);
        }
    }

    private boolean mostrarConfirmacion(String titulo, String mensaje) {
        return navegacionServicio.confirmarAccion(titulo, mensaje);
    }

    @Override
    public void handleShoppingAction(ActionEvent evento) {
        this.usuarioActualLocal = SessionManager.getInstancia().getUsuarioActual();
        if (this.usuarioActualLocal == null) {
            System.err.println("Error crítico: No hay usuario en sesión al re-navegar a Compras.");
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
        cargarListasCompra();
        activarBoton(shoppingButton);
    }
}