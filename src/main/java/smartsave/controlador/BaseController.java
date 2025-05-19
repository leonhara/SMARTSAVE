package smartsave.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import smartsave.servicio.NavegacionServicio;
import smartsave.utilidad.EstilosApp;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class BaseController implements Initializable {

    // Elementos comunes en todas las pantallas
    @FXML protected BorderPane mainPane;
    @FXML protected HBox titleBar;
    @FXML protected VBox sideMenu;

    // Botones de navegación
    @FXML protected Button dashboardButton;
    @FXML protected Button transactionsButton;
    @FXML protected Button nutritionButton;
    @FXML protected Button shoppingButton;
    @FXML protected Button savingsButton;
    @FXML protected Button reportsButton;
    @FXML protected Button settingsButton;
    @FXML protected Button profileButton;
    @FXML protected Button logoutButton;

    // Botones de control de ventana
    @FXML protected Button minimizeButton;
    @FXML protected Button maximizeButton;
    @FXML protected Button closeButton;

    // Servicio de navegación centralizado
    protected final NavegacionServicio navegacionServicio = NavegacionServicio.getInstancia();

    // Variables para permitir el arrastre de la ventana
    private double offsetX = 0;
    private double offsetY = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Aplicar estilos comunes
        aplicarEstilos();

        // Configurar el arrastre de la ventana
        configurarVentanaArrastrable();

        // Configurar botones de navegación
        configurarBotonesNavegacion();

        // Inicialización específica del controlador (implementada por subclases)
        inicializarControlador();
    }

    /**
     * Método para inicialización específica, a implementar por cada controlador
     */
    protected abstract void inicializarControlador();

    /**
     * Aplicar estilos comunes a todos los controladores
     */
    protected void aplicarEstilos() {
        // Estilos comunes para toda la interfaz
        EstilosApp.aplicarEstiloPanelPrincipal(mainPane);
        EstilosApp.aplicarEstiloBarraTitulo(titleBar);
        EstilosApp.aplicarEstiloMenuLateral(sideMenu);

        // Estilos para botones de ventana
        EstilosApp.aplicarEstiloBotonVentana(minimizeButton);
        EstilosApp.aplicarEstiloBotonVentana(maximizeButton);
        EstilosApp.aplicarEstiloBotonVentana(closeButton);

        // Estilos para botones de navegación
        EstilosApp.aplicarEstiloBotonNavegacion(dashboardButton);
        EstilosApp.aplicarEstiloBotonNavegacion(transactionsButton);
        EstilosApp.aplicarEstiloBotonNavegacion(nutritionButton);
        EstilosApp.aplicarEstiloBotonNavegacion(shoppingButton);
        EstilosApp.aplicarEstiloBotonNavegacion(savingsButton);
        EstilosApp.aplicarEstiloBotonNavegacion(reportsButton);
        EstilosApp.aplicarEstiloBotonNavegacion(settingsButton);
        EstilosApp.aplicarEstiloBotonNavegacion(profileButton);
        EstilosApp.aplicarEstiloBotonNavegacion(logoutButton);
    }

    /**
     * Configurar arrastre de ventana (común a todos los controladores)
     */
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

    /**
     * Configurar eventos de los botones de navegación (común a todos los controladores)
     */
    private void configurarBotonesNavegacion() {
        // Configurar acción para cada botón del menú
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

    /**
     * Destaca el botón correspondiente a la vista actual
     */
    protected void activarBoton(Button botonActivo) {
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

    // Métodos de control de ventana (comunes a todos los controladores)
    @FXML
    public void handleMinimizeAction(ActionEvent evento) {
        Stage escenario = (Stage) ((Button) evento.getSource()).getScene().getWindow();
        escenario.setIconified(true);
    }

    @FXML
    public void handleMaximizeAction(ActionEvent evento) {
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
    public void handleCloseAction(ActionEvent evento) {
        Stage escenario = (Stage) ((Button) evento.getSource()).getScene().getWindow();
        escenario.close();
    }

    // Métodos de navegación (comunes a todos los controladores)
    @FXML
    public void handleDashboardAction(ActionEvent evento) {
        Stage escenarioActual = (Stage) dashboardButton.getScene().getWindow();
        navegacionServicio.navegarADashboard(escenarioActual);
    }

    @FXML
    public void handleTransactionsAction(ActionEvent evento) {
        Stage escenarioActual = (Stage) transactionsButton.getScene().getWindow();
        navegacionServicio.navegarATransacciones(escenarioActual);
    }

    @FXML
    public void handleNutritionAction(ActionEvent evento) {
        Stage escenarioActual = (Stage) nutritionButton.getScene().getWindow();
        navegacionServicio.navegarANutricion(escenarioActual);
    }

    @FXML
    public void handleShoppingAction(ActionEvent evento) {
        Stage escenarioActual = (Stage) shoppingButton.getScene().getWindow();
        navegacionServicio.navegarACompras(escenarioActual);
    }

    @FXML
    public void handleSavingsAction(ActionEvent evento) {
        Stage escenarioActual = (Stage) savingsButton.getScene().getWindow();
        navegacionServicio.navegarAAhorro(escenarioActual);
    }

    @FXML
    public void handleReportsAction(ActionEvent evento) {
        activarBoton(reportsButton);
        navegacionServicio.mostrarAlertaNoImplementado("Informes");
    }

    @FXML
    public void handleSettingsAction(ActionEvent evento) {
        Stage escenarioActual = (Stage) settingsButton.getScene().getWindow();
        navegacionServicio.navegarAConfiguracion(escenarioActual);
    }

    @FXML
    public void handleProfileAction(ActionEvent evento) {
        Stage escenarioActual = (Stage) profileButton.getScene().getWindow();
        navegacionServicio.navegarAPerfil(escenarioActual);
    }

    @FXML
    public void handleLogoutAction(ActionEvent evento) {
        Stage escenarioActual = (Stage) logoutButton.getScene().getWindow();
        navegacionServicio.confirmarCerrarSesion(escenarioActual);
    }
}