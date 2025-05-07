package smartsave.controller;

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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import smartsave.model.Transaction;
import smartsave.util.AppStyles;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    // Referencias a elementos principales del layout
    @FXML private BorderPane mainPane;
    @FXML private HBox titleBar;
    @FXML private VBox sideMenu;

    // Referencias a elementos del menú y controles
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

    // Referencias a los gráficos
    @FXML private PieChart expensesPieChart;
    @FXML private LineChart<String, Number> evolutionLineChart;

    // Referencias a tabla de transacciones
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, LocalDate> dateColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;

    // Referencias a etiquetas de datos financieros
    @FXML private Label balanceAmount;
    @FXML private Label balanceChange;
    @FXML private Label expensesAmount;
    @FXML private Label expensesChange;
    @FXML private Label savingsAmount;
    @FXML private Label savingsChange;
    @FXML private Label nutritionScore;
    @FXML private Label nutritionStatus;

    // Referencias a las barras de progreso
    @FXML private ProgressBar goal1Progress;
    @FXML private ProgressBar goal2Progress;
    @FXML private ProgressBar goal3Progress;

    // Variables para permitir el arrastre de la ventana
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Aplicar estilos neón oscuros a los componentes
        applyStyles();

        // Configurar el arrastre de la ventana
        setupDraggableWindow();

        // Configurar botones de navegación
        setupNavigationButtons();

        // Cargar datos de ejemplo
        loadSampleData();
    }

    private void applyStyles() {
        // Aplicar estilos al tema oscuro con neón
        AppStyles.applyMainPaneStyle(mainPane);
        AppStyles.applyTitleBarStyle(titleBar);
        AppStyles.applySideMenuStyle(sideMenu);

        // Aplicar estilos a los botones de la ventana
        AppStyles.applyWindowButtonStyle(minimizeButton);
        AppStyles.applyWindowButtonStyle(maximizeButton);
        AppStyles.applyWindowButtonStyle(closeButton);

        // Aplicar estilos a los botones de navegación
        AppStyles.applyNavigationButtonStyle(dashboardButton);
        AppStyles.applyNavigationButtonStyle(transactionsButton);
        AppStyles.applyNavigationButtonStyle(nutritionButton);
        AppStyles.applyNavigationButtonStyle(shoppingButton);
        AppStyles.applyNavigationButtonStyle(savingsButton);
        AppStyles.applyNavigationButtonStyle(reportsButton);
        AppStyles.applyNavigationButtonStyle(settingsButton);
        AppStyles.applyNavigationButtonStyle(profileButton);
        AppStyles.applyNavigationButtonStyle(logoutButton);

        // Destacar el botón de dashboard como seleccionado
        dashboardButton.getStyleClass().add("selected");

        // Aplicar estilos a los gráficos
        AppStyles.applyChartStyle(expensesPieChart);
        AppStyles.applyChartStyle(evolutionLineChart);

        // Aplicar estilos a la tabla
        AppStyles.applyTableStyle(transactionsTable);
    }

    private void setupDraggableWindow() {
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    private void setupNavigationButtons() {
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

    private void loadSampleData() {
        // Cargar datos para el gráfico de distribución de gastos
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Alimentos", 350),
                new PieChart.Data("Vivienda", 800),
                new PieChart.Data("Transporte", 150),
                new PieChart.Data("Entretenimiento", 200),
                new PieChart.Data("Otros", 100)
        );
        expensesPieChart.setData(pieChartData);

        // Cargar datos para el gráfico de evolución
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Ingresos");
        incomeSeries.getData().add(new XYChart.Data<>("Ene", 2000));
        incomeSeries.getData().add(new XYChart.Data<>("Feb", 2000));
        incomeSeries.getData().add(new XYChart.Data<>("Mar", 2200));
        incomeSeries.getData().add(new XYChart.Data<>("Abr", 2200));
        incomeSeries.getData().add(new XYChart.Data<>("May", 2200));
        incomeSeries.getData().add(new XYChart.Data<>("Jun", 2500));

        XYChart.Series<String, Number> expensesSeries = new XYChart.Series<>();
        expensesSeries.setName("Gastos");
        expensesSeries.getData().add(new XYChart.Data<>("Ene", 1800));
        expensesSeries.getData().add(new XYChart.Data<>("Feb", 1700));
        expensesSeries.getData().add(new XYChart.Data<>("Mar", 1900));
        expensesSeries.getData().add(new XYChart.Data<>("Abr", 1600));
        expensesSeries.getData().add(new XYChart.Data<>("May", 1550));
        expensesSeries.getData().add(new XYChart.Data<>("Jun", 1500));

        XYChart.Series<String, Number> savingsSeries = new XYChart.Series<>();
        savingsSeries.setName("Ahorros");
        savingsSeries.getData().add(new XYChart.Data<>("Ene", 200));
        savingsSeries.getData().add(new XYChart.Data<>("Feb", 300));
        savingsSeries.getData().add(new XYChart.Data<>("Mar", 300));
        savingsSeries.getData().add(new XYChart.Data<>("Abr", 600));
        savingsSeries.getData().add(new XYChart.Data<>("May", 650));
        savingsSeries.getData().add(new XYChart.Data<>("Jun", 1000));

        evolutionLineChart.getData().addAll(incomeSeries, expensesSeries, savingsSeries);

        // Configurar tabla de transacciones
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        // Datos de ejemplo para la tabla
        ObservableList<Transaction> transactions = FXCollections.observableArrayList(
                new Transaction(LocalDate.now().minusDays(1), "Supermercado El Corte Inglés", "Alimentos", 75.32, "Gasto"),
                new Transaction(LocalDate.now().minusDays(3), "Transferencia Nómina", "Salario", 2200.00, "Ingreso"),
                new Transaction(LocalDate.now().minusDays(5), "Netflix", "Entretenimiento", 12.99, "Gasto"),
                new Transaction(LocalDate.now().minusDays(8), "Gasolinera Repsol", "Transporte", 50.00, "Gasto"),
                new Transaction(LocalDate.now().minusDays(12), "Dividendos Acciones", "Inversiones", 125.75, "Ingreso")
        );

        transactionsTable.setItems(transactions);
    }

    @FXML
    private void handleMinimizeAction(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void handleMaximizeAction(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());

        // Cambiar el símbolo del botón según el estado
        if (stage.isMaximized()) {
            maximizeButton.setText("❐");  // Símbolo para restaurar
        } else {
            maximizeButton.setText("□");  // Símbolo para maximizar
        }
    }

    @FXML
    private void handleCloseAction(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleDashboardAction(ActionEvent event) {
        // Ya estamos en el dashboard, solo actualizamos el estilo
        setActiveButton(dashboardButton);
    }

    @FXML
    private void handleTransactionsAction(ActionEvent event) {
        // Cambiar a la vista de transacciones
        setActiveButton(transactionsButton);
        showNotImplementedAlert("Transacciones");
    }

    @FXML
    private void handleNutritionAction(ActionEvent event) {
        // Cambiar a la vista de perfil nutricional
        setActiveButton(nutritionButton);
        showNotImplementedAlert("Perfil Nutricional");
    }

    @FXML
    private void handleShoppingAction(ActionEvent event) {
        // Cambiar a la vista de plan de compras
        setActiveButton(shoppingButton);
        showNotImplementedAlert("Plan de Compras");
    }

    @FXML
    private void handleSavingsAction(ActionEvent event) {
        // Cambiar a la vista de modalidades de ahorro
        setActiveButton(savingsButton);
        showNotImplementedAlert("Modalidades de Ahorro");
    }

    @FXML
    private void handleReportsAction(ActionEvent event) {
        // Cambiar a la vista de informes
        setActiveButton(reportsButton);
        showNotImplementedAlert("Informes");
    }

    @FXML
    private void handleSettingsAction(ActionEvent event) {
        // Cambiar a la vista de configuración
        setActiveButton(settingsButton);
        showNotImplementedAlert("Configuración");
    }

    @FXML
    private void handleProfileAction(ActionEvent event) {
        // Cambiar a la vista de perfil
        setActiveButton(profileButton);
        showNotImplementedAlert("Mi Perfil");
    }

    @FXML
    private void handleLogoutAction(ActionEvent event) {
        // Mostrar confirmación antes de cerrar sesión
        Alert alert = createStyledAlert(
                Alert.AlertType.CONFIRMATION,
                "Cerrar Sesión",
                "¿Estás seguro que deseas cerrar la sesión?"
        );

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Volver a la pantalla de login
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                    Parent loginRoot = loader.load();

                    Scene loginScene = new Scene(loginRoot);
                    loginScene.setFill(Color.TRANSPARENT);

                    Stage currentStage = (Stage) logoutButton.getScene().getWindow();
                    currentStage.setScene(loginScene);
                    currentStage.setTitle("SmartSave - Login");
                    currentStage.centerOnScreen();

                } catch (IOException e) {
                    showErrorAlert("Error al volver a la pantalla de login", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleViewAllTransactionsAction(ActionEvent event) {
        showNotImplementedAlert("Ver Todas las Transacciones");
    }

    @FXML
    private void handleAddGoalAction(ActionEvent event) {
        showNotImplementedAlert("Añadir Objetivo");
    }

    private void setActiveButton(Button activeButton) {
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
        activeButton.getStyleClass().add("selected");
    }

    private void showNotImplementedAlert(String feature) {
        Alert alert = createStyledAlert(
                Alert.AlertType.INFORMATION,
                feature + " - En desarrollo",
                "Esta funcionalidad aún no está implementada."
        );
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = createStyledAlert(
                Alert.AlertType.ERROR,
                title,
                message
        );
        alert.showAndWait();
    }

    private Alert createStyledAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Aplicar estilo al diálogo
        DialogPane dialogPane = alert.getDialogPane();

        // Fondo oscuro
        dialogPane.setStyle(
                "-fx-background-color: #1A1A25; " +
                        "-fx-border-color: #FF00FF; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 5px;"
        );

        // Color de texto claro
        dialogPane.lookupAll(".label").forEach(label ->
                label.setStyle("-fx-text-fill: white; -fx-font-size: 14px;")
        );

        // Estilo para los botones
        dialogPane.lookupAll(".button").forEach(button -> {
            button.setStyle(
                    "-fx-background-color: #25253A; " +
                            "-fx-text-fill: white; " +
                            "-fx-border-color: #4050FF; " +
                            "-fx-border-width: 1px; " +
                            "-fx-border-radius: 3px;"
            );

            // Efecto hover
            button.setOnMouseEntered(e ->
                    button.setStyle(
                            "-fx-background-color: #35354A; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-border-color: #FF00FF; " +
                                    "-fx-border-width: 1px; " +
                                    "-fx-border-radius: 3px;"
                    )
            );

            button.setOnMouseExited(e ->
                    button.setStyle(
                            "-fx-background-color: #25253A; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-border-color: #4050FF; " +
                                    "-fx-border-width: 1px; " +
                                    "-fx-border-radius: 3px;"
                    )
            );
        });

        return alert;
    }
}