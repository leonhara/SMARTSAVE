package smartsave.controlador;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import smartsave.modelo.*;
import smartsave.servicio.*;
import smartsave.utilidad.EstilosApp;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ComprasController implements Initializable {
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
    @FXML private ProgressIndicator busquedaProgressIndicator;

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
    private ListaCompraServicio listaCompraServicio = new ListaCompraServicio();
    private ProductoServicio productoServicio = new ProductoServicio();

    // Variables de estado
    private Long usuarioIdActual = 1L; // Simulado, en un caso real vendría de la sesión
    private ListaCompra listaSeleccionada = null;
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

        // Inicializar pantalla de listas de compra
        inicializarPantallaCompras();

        // Cargar datos
        cargarListasCompra();
        buscarProductoField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().length() >= 2) {
                // Debouncing - retrasar la búsqueda para evitar demasiadas llamadas
                Platform.runLater(() -> {
                    Timeline timeline = new Timeline(new KeyFrame(
                            Duration.millis(500),
                            e -> realizarBusqueda(newValue)
                    ));
                    timeline.play();
                });
            }
        });
    }
    private void realizarBusqueda(String termino) {
        Task<List<Producto>> task = new Task<List<Producto>>() {
            @Override
            protected List<Producto> call() throws Exception {
                return productoServicio.buscarProductos(termino);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    resultadosProductosTableView.setItems(
                            FXCollections.observableArrayList(getValue())
                    );
                });
            }
        };

        new Thread(task).start();
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

        // Destacar el botón de plan de compras como seleccionado
        shoppingButton.getStyleClass().add("selected");

        // Aplicar estilos a los campos de texto
        EstilosApp.aplicarEstiloCampoTexto(nombreListaField);
        EstilosApp.aplicarEstiloCampoTexto(presupuestoField);
        EstilosApp.aplicarEstiloCampoTexto(buscarProductoField);

        // Aplicar estilos a las tablas
        EstilosApp.aplicarEstiloTabla(productosTableView);
        EstilosApp.aplicarEstiloTabla(resultadosProductosTableView);

        // Aplicar estilos a los botones principales
        EstilosApp.aplicarEstiloBotonPrimario(crearListaButton);
        EstilosApp.aplicarEstiloBotonPrimario(guardarListaButton);
        EstilosApp.aplicarEstiloBotonPrimario(agregarProductoButton);

        // Estilo para ListView
        listasCompraListView.setStyle(
                "-fx-background-color: rgba(30, 30, 40, 0.7); " +
                        "-fx-background-radius: 5px; " +
                        "-fx-border-color: rgba(80, 80, 120, 0.5); " +
                        "-fx-border-radius: 5px; " +
                        "-fx-text-fill: white;"
        );
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
                    contenido.setPadding(new Insets(5, 10, 5, 10));

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

        // Configurar tabla de productos
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

        // Columna de acciones para productos
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

        // Configurar panel de crear lista
        modalidadComboBox.setItems(FXCollections.observableArrayList("Máximo", "Equilibrado", "Estándar"));
        modalidadComboBox.getSelectionModel().select("Equilibrado");

        // Validar campo de presupuesto (solo números y punto decimal)
        presupuestoField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                presupuestoField.setText(oldValue);
            }
        });

        // Configurar tabla de resultados de búsqueda
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
                    mostrarAlertaInformacion("Producto añadido",
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

        // Inicialmente ocultar paneles
        ocultarDetalleLista();
        ocultarPanelCrearLista();
        ocultarPanelBuscarProducto();
    }

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

    private void mostrarDetalleLista(ListaCompra lista) {
        listaSeleccionada = lista;

        // Mostrar panel de detalle
        detalleListaPane.setVisible(true);
        detalleListaPane.setManaged(true);

        // Actualizar datos básicos
        nombreListaLabel.setText(lista.getNombre());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        fechaCreacionLabel.setText(lista.getFechaCreacion().format(formatter));
        fechaProgramadaLabel.setText(lista.getFechaPlanificada() != null
                ? lista.getFechaPlanificada().format(formatter) : "No programada");

        modalidadLabel.setText(lista.getModalidadAhorro());
        presupuestoLabel.setText(String.format("€%.2f", lista.getPresupuestoMaximo()));

        // Calcular ahorro estimado según modalidad
        UsuarioServicio usuarioServicio = new UsuarioServicio();
        double factorAhorro = usuarioServicio.obtenerFactorPresupuestoUsuario(usuarioIdActual);
        double ahorroEstimado = lista.getPresupuestoMaximo() * (1 - factorAhorro);

        // Si tienes un Label para mostrar el ahorro, actualízalo
        // ahorroEstimadoLabel.setText(String.format("€%.2f", ahorroEstimado));

        // Actualizar tabla de productos
        productosTableView.setItems(FXCollections.observableArrayList(lista.getItems()));

        // Actualizar estado de completada
        completadaCheckBox.setSelected(lista.isCompletada());

        // Actualizar resumen
        actualizarResumenLista();
    }

    private void ocultarDetalleLista() {
        detalleListaPane.setVisible(false);
        detalleListaPane.setManaged(false);
        listaSeleccionada = null;
    }

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

    @FXML
    private void handleFiltroListas(ActionEvent event) {
        cargarListasCompra();
    }

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

    @FXML
    private void handleEliminarLista(ActionEvent event) {
        if (listaSeleccionada == null) {
            return;
        }

        if (mostrarConfirmacion("Eliminar Lista",
                "¿Estás seguro de que deseas eliminar esta lista de compra?")) {
            listaCompraServicio.eliminarListaCompra(listaSeleccionada.getId(), usuarioIdActual);
            ocultarDetalleLista();
            cargarListasCompra();
        }
    }

    @FXML
    private void handleGuardarLista(ActionEvent event) {
        // Validar campos
        if (nombreListaField.getText().trim().isEmpty() ||
                presupuestoField.getText().trim().isEmpty() ||
                modalidadComboBox.getValue() == null) {

            mostrarAlertaError("Campos incompletos", "Por favor, completa todos los campos obligatorios.");
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
                mostrarAlertaError("Presupuesto inválido", "El presupuesto debe ser mayor que cero.");
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

                mostrarAlertaInformacion("Lista actualizada", "La lista de compra ha sido actualizada correctamente.");
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

                mostrarAlertaInformacion("Lista creada", "La lista de compra ha sido creada correctamente.");
            }

            // Ocultar panel
            ocultarPanelCrearLista();

        } catch (NumberFormatException e) {
            mostrarAlertaError("Formato incorrecto", "Por favor, ingresa un presupuesto válido.");
        }
    }

    @FXML
    private void handleCancelarLista(ActionEvent event) {
        ocultarPanelCrearLista();
    }

    @FXML
    private void handleCompletadaChange(ActionEvent event) {
        if (listaSeleccionada != null) {
            listaSeleccionada.setCompletada(completadaCheckBox.isSelected());
            listaCompraServicio.actualizarListaCompra(listaSeleccionada);
            cargarListasCompra(); // Actualizar lista por si cambia el filtro
        }
    }

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

    @FXML
    private void handleBuscarProducto(ActionEvent event) {
        String termino = buscarProductoField.getText().trim();

        // Mostrar indicador de carga
        busquedaProgressIndicator.setVisible(true);
        busquedaProgressIndicator.setManaged(true);

        Task<List<Producto>> task = new Task<List<Producto>>() {
            @Override
            protected List<Producto> call() throws Exception {
                if (termino.isEmpty()) {
                    return productoServicio.obtenerTodosProductos();
                } else {
                    return productoServicio.buscarProductos(termino);
                }
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    resultadosProductosTableView.setItems(
                            FXCollections.observableArrayList(getValue())
                    );
                    // Ocultar indicador de carga
                    busquedaProgressIndicator.setVisible(false);
                    busquedaProgressIndicator.setManaged(false);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    busquedaProgressIndicator.setVisible(false);
                    busquedaProgressIndicator.setManaged(false);
                    mostrarAlertaError("Error de búsqueda",
                            "No se pudieron obtener los productos. Inténtalo más tarde.");
                });
            }
        };

        new Thread(task).start();
    }

    @FXML
    private void handleCerrarBusqueda(ActionEvent event) {
        ocultarPanelBuscarProducto();
    }

    private void mostrarPanelCrearLista() {
        crearListaPane.setVisible(true);
        crearListaPane.setManaged(true);
    }

    private void ocultarPanelCrearLista() {
        crearListaPane.setVisible(false);
        crearListaPane.setManaged(false);
    }

    private void mostrarPanelBuscarProducto() {
        agregarProductoPane.setVisible(true);
        agregarProductoPane.setManaged(true);
    }

    private void ocultarPanelBuscarProducto() {
        agregarProductoPane.setVisible(false);
        agregarProductoPane.setManaged(false);
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
        // Ya estamos en la vista de plan de compras, solo cargar datos
        cargarListasCompra();
        activarBoton(shoppingButton);
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
        if (mostrarConfirmacion("Cerrar Sesión", "¿Estás seguro que deseas cerrar la sesión?")) {
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
        alerta.setTitle(caracteristica + " - En desarrollo");
        alerta.setHeaderText(null);
        alerta.setContentText("Esta funcionalidad aún no está implementada.");

        // Estilizar alerta
        DialogPane dialogPane = alerta.getDialogPane();
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

        alerta.showAndWait();
    }

    private void mostrarAlertaError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        // Estilizar alerta
        DialogPane dialogPane = alerta.getDialogPane();
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

        alerta.showAndWait();
    }

    private void mostrarAlertaInformacion(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        // Estilizar alerta
        DialogPane dialogPane = alerta.getDialogPane();
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

        alerta.showAndWait();
    }

    private boolean mostrarConfirmacion(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        // Estilizar alerta
        DialogPane dialogPane = alerta.getDialogPane();
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

        return alerta.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }
}