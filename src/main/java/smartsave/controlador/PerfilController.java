package smartsave.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import smartsave.modelo.Usuario;
import smartsave.servicio.UsuarioServicio;
import smartsave.servicio.TransaccionServicio;
import smartsave.utilidad.EstilosApp;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PerfilController implements Initializable {

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

    // Referencias a elementos del perfil
    @FXML private Circle profilePhotoCircle;
    @FXML private ImageView profilePhoto;
    @FXML private Button cambiarFotoButton;
    @FXML private Label nombreCompletoLabel;
    @FXML private Label emailLabel;
    @FXML private Label fechaRegistroLabel;
    @FXML private Label ultimoAccesoLabel;
    @FXML private Button editarDatosButton;

    // Referencias a modalidad de ahorro
    @FXML private Label modalidadActualLabel;
    @FXML private Button cambiarModalidadButton;
    @FXML private Label descripcionModalidadLabel;

    // Referencias a resumen financiero
    @FXML private Label balanceActualLabel;
    @FXML private Label totalAhorradoLabel;
    @FXML private Label gastosMesLabel;
    @FXML private Label ingresosMesLabel;
    @FXML private Label diasUsandoAppLabel;

    // Referencias a gráficos
    @FXML private LineChart<String, Number> evolucionFinancieraChart;
    @FXML private PieChart distribucionGastosChart;

    // Referencias a botones de acción
    @FXML private Button configurarPerfilButton;
    @FXML private Button exportarDatosButton;
    @FXML private Button eliminarCuentaButton;

    // Servicios
    private UsuarioServicio usuarioServicio = new UsuarioServicio();
    private TransaccionServicio transaccionServicio = new TransaccionServicio();

    // Variables de estado
    private Long usuarioIdActual = 1L; // Simulado, en un caso real vendría de la sesión
    private Usuario usuarioActual;

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

        // Cargar datos del usuario
        cargarDatosUsuario();

        // Cargar datos financieros
        cargarDatosFinancieros();

        // Cargar gráficos
        cargarGraficos();
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

        // Destacar el botón de perfil como seleccionado
        profileButton.getStyleClass().add("selected");

        // Aplicar estilos a los botones principales
        EstilosApp.aplicarEstiloBotonPrimario(cambiarFotoButton);
        EstilosApp.aplicarEstiloBotonPrimario(editarDatosButton);
        EstilosApp.aplicarEstiloBotonPrimario(cambiarModalidadButton);
        EstilosApp.aplicarEstiloBotonPrimario(configurarPerfilButton);
        EstilosApp.aplicarEstiloBotonPrimario(exportarDatosButton);
        EstilosApp.aplicarEstiloBotonPrimario(eliminarCuentaButton);

        // Aplicar estilos a los gráficos
        EstilosApp.aplicarEstiloGrafico(evolucionFinancieraChart);
        EstilosApp.aplicarEstiloGrafico(distribucionGastosChart);
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

    private void cargarDatosUsuario() {
        // Cargar datos del usuario actual
        usuarioActual = usuarioServicio.obtenerUsuarioPorId(usuarioIdActual);

        if (usuarioActual != null) {
            // Datos personales
            nombreCompletoLabel.setText(usuarioActual.getNombreCompleto());
            emailLabel.setText(usuarioActual.getEmail());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            fechaRegistroLabel.setText(usuarioActual.getFechaRegistro().format(formatter));

            if (usuarioActual.getUltimoLogin() != null) {
                ultimoAccesoLabel.setText(usuarioActual.getUltimoLogin().format(formatter));
            } else {
                ultimoAccesoLabel.setText("Nunca");
            }

            // Modalidad de ahorro
            modalidadActualLabel.setText(usuarioActual.getModalidadAhorroSeleccionada());

            switch (usuarioActual.getModalidadAhorroSeleccionada()) {
                case "Máximo":
                    descripcionModalidadLabel.setText("Maximiza tu ahorro priorizando los precios más bajos.");
                    break;
                case "Equilibrado":
                    descripcionModalidadLabel.setText("Balance perfecto entre ahorro y calidad nutricional.");
                    break;
                case "Estándar":
                    descripcionModalidadLabel.setText("Prioriza la calidad nutricional manteniendo un presupuesto razonable.");
                    break;
                default:
                    descripcionModalidadLabel.setText("Modalidad no definida.");
                    break;
            }
        }
    }

    private void cargarDatosFinancieros() {
        // Calcular días usando la app
        if (usuarioActual != null && usuarioActual.getFechaRegistro() != null) {
            long diasUsando = LocalDate.now().toEpochDay() - usuarioActual.getFechaRegistro().toEpochDay();
            diasUsandoAppLabel.setText(diasUsando + " días");
        }

        // Calcular balance actual
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        double ingresosMes = transaccionServicio.obtenerTotalIngresos(usuarioIdActual, inicioMes, LocalDate.now());
        double gastosMes = transaccionServicio.obtenerTotalGastos(usuarioIdActual, inicioMes, LocalDate.now());
        double balance = transaccionServicio.obtenerBalance(usuarioIdActual, null, LocalDate.now());

        // Simular total ahorrado (en un caso real, esto vendría de una tabla de ahorros)
        double totalAhorrado = balance * 0.3; // Simulación: 30% del balance como ahorros

        // Actualizar labels
        balanceActualLabel.setText(String.format("€%.2f", balance));
        totalAhorradoLabel.setText(String.format("€%.2f", totalAhorrado));
        gastosMesLabel.setText(String.format("€%.2f", gastosMes));
        ingresosMesLabel.setText(String.format("€%.2f", ingresosMes));

        // Colorear según valores
        if (balance >= 0) {
            balanceActualLabel.setTextFill(Color.rgb(100, 220, 100)); // Verde
        } else {
            balanceActualLabel.setTextFill(Color.rgb(255, 100, 100)); // Rojo
        }
    }

    private void cargarGraficos() {
        // Cargar gráfico de evolución financiera
        cargarGraficoEvolucion();

        // Cargar gráfico de distribución de gastos
        cargarGraficoDistribucion();
    }

    private void cargarGraficoEvolucion() {
        // Datos simulados para los últimos 6 meses
        XYChart.Series<String, Number> serieBalance = new XYChart.Series<>();
        serieBalance.setName("Balance");

        // Generar datos para los últimos 6 meses
        LocalDate fechaActual = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate fecha = fechaActual.minusMonths(i);
            String mes = fecha.format(DateTimeFormatter.ofPattern("MMM"));

            // Simular balance creciente con algunas fluctuaciones
            double balance = 1000 + (i * 200) + (Math.random() * 300 - 150);
            serieBalance.getData().add(new XYChart.Data<>(mes, balance));
        }

        XYChart.Series<String, Number> serieAhorros = new XYChart.Series<>();
        serieAhorros.setName("Ahorros");

        // Generar datos de ahorros acumulativos
        for (int i = 5; i >= 0; i--) {
            LocalDate fecha = fechaActual.minusMonths(i);
            String mes = fecha.format(DateTimeFormatter.ofPattern("MMM"));

            // Simular ahorros crecientes
            double ahorros = 500 + ((5 - i) * 400) + (Math.random() * 200 - 100);
            serieAhorros.getData().add(new XYChart.Data<>(mes, ahorros));
        }

        evolucionFinancieraChart.getData().clear();
        evolucionFinancieraChart.getData().addAll(serieBalance, serieAhorros);
    }

    private void cargarGraficoDistribucion() {
        // Obtener distribución de gastos del mes actual
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        var gastosPorCategoria = transaccionServicio.obtenerGastosPorCategoria(usuarioIdActual, inicioMes, LocalDate.now());

        // Crear datos para el gráfico
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        if (gastosPorCategoria.isEmpty()) {
            // Datos simulados si no hay datos reales
            pieChartData.addAll(
                    new PieChart.Data("Alimentación", 450),
                    new PieChart.Data("Vivienda", 800),
                    new PieChart.Data("Transporte", 200),
                    new PieChart.Data("Entretenimiento", 150),
                    new PieChart.Data("Otros", 100)
            );
        } else {
            for (var entry : gastosPorCategoria.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
        }

        distribucionGastosChart.setData(pieChartData);

        // Aplicar colores personalizados a las secciones
        if (!pieChartData.isEmpty()) {
            Color[] colores = {
                    Color.rgb(255, 100, 255),  // Rosa neón
                    Color.rgb(100, 170, 255),  // Azul neón
                    Color.rgb(100, 220, 100),  // Verde neón
                    Color.rgb(255, 200, 100),  // Naranja neón
                    Color.rgb(200, 100, 255)   // Purpura neón
            };

            for (int i = 0; i < pieChartData.size() && i < colores.length; i++) {
                int finalI = i;
                pieChartData.get(i).nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle(String.format("-fx-pie-color: rgb(%d, %d, %d);",
                                (int) (colores[finalI].getRed() * 255),
                                (int) (colores[finalI].getGreen() * 255),
                                (int) (colores[finalI].getBlue() * 255)));
                    }
                });
            }
        }
    }

    // Manejadores de eventos

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
            mostrarAlerta(Alert.AlertType.ERROR, "Error de navegación", "Error al cargar el dashboard: " + e.getMessage());
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
            mostrarAlerta(Alert.AlertType.ERROR, "Error de navegación", "Error al cargar la pantalla de transacciones: " + e.getMessage());
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
            mostrarAlerta(Alert.AlertType.ERROR, "Error de navegación", "Error al cargar la pantalla de perfil nutricional: " + e.getMessage());
        }
    }

    @FXML
    private void handleShoppingAction(ActionEvent evento) {
        try {
            // Cargar la vista de plan de compras
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/fxml/compras.fxml"));
            Parent raizCompras = cargador.load();

            // Configurar la nueva escena
            Scene escenaCompras = new Scene(raizCompras);
            escenaCompras.setFill(Color.TRANSPARENT);

            // Obtener el escenario actual
            Stage escenarioActual = (Stage) shoppingButton.getScene().getWindow();

            // Establecer la nueva escena
            escenarioActual.setScene(escenaCompras);
            escenarioActual.setTitle("SmartSave - Plan de Compras");

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de navegación", "Error al cargar la pantalla de plan de compras: " + e.getMessage());
        }
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
            mostrarAlerta(Alert.AlertType.ERROR, "Error de navegación", "Error al cargar la pantalla de modalidades de ahorro: " + e.getMessage());
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
        try {
            // Cargar la vista de configuración
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/fxml/configuracion.fxml"));
            Parent raizConfiguracion = cargador.load();

            // Configurar la nueva escena
            Scene escenaConfiguracion = new Scene(raizConfiguracion);
            escenaConfiguracion.setFill(Color.TRANSPARENT);

            // Obtener el escenario actual
            Stage escenarioActual = (Stage) settingsButton.getScene().getWindow();

            // Establecer la nueva escena
            escenarioActual.setScene(escenaConfiguracion);
            escenarioActual.setTitle("SmartSave - Configuración");

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de navegación", "Error al cargar la pantalla de configuración: " + e.getMessage());
        }
    }

    @FXML
    private void handleProfileAction(ActionEvent evento) {
        // Ya estamos en la vista de perfil, solo actualizamos los datos
        cargarDatosUsuario();
        cargarDatosFinancieros();
        cargarGraficos();
        activarBoton(profileButton);
    }

    @FXML
    private void handleLogoutAction(ActionEvent evento) {
        // Mostrar confirmación antes de cerrar sesión
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Cerrar Sesión");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Estás seguro que deseas cerrar la sesión?");

        estilizarAlerta(alerta);

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
                    mostrarAlerta(Alert.AlertType.ERROR, "Error al volver a la pantalla de login", e.getMessage());
                }
            }
        });
    }

    // Manejadores específicos del perfil

    @FXML
    private void handleCambiarFotoAction(ActionEvent evento) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar foto de perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos de imagen", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp")
        );

        Stage stage = (Stage) cambiarFotoButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                // Cargar la nueva imagen
                Image imagen = new Image(file.toURI().toString());
                profilePhoto.setImage(imagen);

                // En un caso real, aquí se guardaría la imagen en el servidor o base de datos
                mostrarAlerta(Alert.AlertType.INFORMATION, "Foto actualizada",
                        "Tu foto de perfil ha sido actualizada correctamente.");
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error al cargar imagen",
                        "No se pudo cargar la imagen seleccionada: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleEditarDatosAction(ActionEvent evento) {
        // Crear diálogo para editar datos personales
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Editar Datos Personales");
        dialog.setHeaderText("Modifica tus datos personales");

        // Configurar botones
        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);

        // Crear el formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre");
        TextField apellidosField = new TextField();
        apellidosField.setPromptText("Apellidos");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        // Cargar datos actuales
        if (usuarioActual != null) {
            nombreField.setText(usuarioActual.getNombre());
            apellidosField.setText(usuarioActual.getApellidos());
            emailField.setText(usuarioActual.getEmail());
        }

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Apellidos:"), 0, 1);
        grid.add(apellidosField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        nombreField.requestFocus();

        // Estilizar el diálogo
        estilizarAlerta((Alert) dialog);

        // Convertir el resultado cuando se presiona el botón guardar
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                return new String[]{nombreField.getText(), apellidosField.getText(), emailField.getText()};
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String nombre = result[0];
            String apellidos = result[1];
            String email = result[2];

            // Validar datos
            if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty()) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Todos los campos son obligatorios.");
                return;
            }

            if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "El email no tiene un formato válido.");
                return;
            }

            // Actualizar usuario
            usuarioActual.setNombre(nombre);
            usuarioActual.setApellidos(apellidos);
            usuarioActual.setEmail(email);

            if (usuarioServicio.actualizarUsuario(usuarioActual)) {
                // Recargar datos en la interfaz
                cargarDatosUsuario();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Datos actualizados",
                        "Tus datos personales han sido actualizados correctamente.");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron actualizar los datos.");
            }
        });
    }

    @FXML
    private void handleCambiarModalidadAction(ActionEvent evento) {
        // Crear diálogo para cambiar modalidad de ahorro
        ChoiceDialog<String> dialog = new ChoiceDialog<>(usuarioActual.getModalidadAhorroSeleccionada(),
                "Máximo", "Equilibrado", "Estándar");
        dialog.setTitle("Cambiar Modalidad de Ahorro");
        dialog.setHeaderText("Selecciona tu modalidad de ahorro preferida");
        dialog.setContentText("Modalidad:");

        // Estilizar diálogo