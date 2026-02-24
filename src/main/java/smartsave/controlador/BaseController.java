package smartsave.controlador;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import smartsave.servicio.NavegacionServicio;
import smartsave.utilidad.EstilosApp;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class BaseController implements Initializable {

    @FXML protected BorderPane mainPane;
    @FXML protected HBox titleBar;
    @FXML protected VBox sideMenu;

    @FXML protected Button dashboardButton;
    @FXML protected Button transactionsButton;
    @FXML protected Button nutritionButton;
    @FXML protected Button shoppingButton;
    @FXML protected Button savingsButton;
    @FXML protected Button reportsButton;
    @FXML protected Button settingsButton;
    @FXML protected Button profileButton;
    @FXML protected Button logoutButton;

    @FXML protected Button minimizeButton;
    @FXML protected Button maximizeButton;
    @FXML protected Button closeButton;

    protected final NavegacionServicio navegacionServicio = NavegacionServicio.getInstancia();

    private double offsetX = 0;
    private double offsetY = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        aplicarEstilos();

        configurarVentanaArrastrable();

        configurarBotonesNavegacion();

        inicializarControlador();

        Platform.runLater(() -> {
            aplicarColorSoloASinColor();
        });
    }

    private void aplicarColorSoloASinColor() {
        mainPane.lookupAll(".label").forEach(nodo -> {
            if (nodo instanceof Label) {
                Label label = (Label) nodo;
                Color colorActual = (Color) label.getTextFill();

                boolean esGris = Math.abs(colorActual.getRed() - colorActual.getGreen()) < 0.1 &&
                        Math.abs(colorActual.getGreen() - colorActual.getBlue()) < 0.1 &&
                        Math.abs(colorActual.getRed() - colorActual.getBlue()) < 0.1 &&
                        colorActual.getBrightness() < 0.9;

                if (esGris) {
                    label.setTextFill(EstilosApp.TEXTO_CLARO);
                }
            }
        });
    }

    protected abstract void inicializarControlador();

    protected void aplicarEstilos() {
        EstilosApp.aplicarEstiloPanelPrincipal(mainPane);
        EstilosApp.aplicarEstiloBarraTitulo(titleBar);
        EstilosApp.aplicarEstiloMenuLateral(sideMenu);

        EstilosApp.aplicarEstiloBotonVentana(minimizeButton);
        EstilosApp.aplicarEstiloBotonVentana(maximizeButton);
        EstilosApp.aplicarEstiloBotonVentana(closeButton);

        reportsButton.setVisible(false);
        reportsButton.setManaged(false);

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

    protected void activarBoton(Button botonActivo) {
        dashboardButton.getStyleClass().remove("selected");
        transactionsButton.getStyleClass().remove("selected");
        nutritionButton.getStyleClass().remove("selected");
        shoppingButton.getStyleClass().remove("selected");
        savingsButton.getStyleClass().remove("selected");
        reportsButton.getStyleClass().remove("selected");
        settingsButton.getStyleClass().remove("selected");
        profileButton.getStyleClass().remove("selected");

        botonActivo.getStyleClass().add("selected");
    }

    @FXML
    public void handleMinimizeAction(ActionEvent evento) {
        Stage escenario = (Stage) ((Button) evento.getSource()).getScene().getWindow();
        escenario.setIconified(true);
    }

    @FXML
    public void handleMaximizeAction(ActionEvent evento) {
        Stage escenario = (Stage) ((Button) evento.getSource()).getScene().getWindow();
        escenario.setMaximized(!escenario.isMaximized());

        if (escenario.isMaximized()) {
            maximizeButton.setText("❐");
        } else {
            maximizeButton.setText("□");
        }
    }

    @FXML
    public void handleCloseAction(ActionEvent evento) {
        Stage escenario = (Stage) ((Button) evento.getSource()).getScene().getWindow();
        escenario.close();

        Platform.exit();
    }

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