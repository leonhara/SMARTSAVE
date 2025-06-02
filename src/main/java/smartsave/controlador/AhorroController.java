package smartsave.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import smartsave.modelo.ModalidadAhorro;
import smartsave.servicio.ModalidadAhorroServicio;
import smartsave.utilidad.EstilosApp;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de Modalidades de Ahorro
 * Extiende BaseController para heredar funcionalidad común
 */
public class AhorroController extends BaseController {

    // Referencias a elementos específicos de la pantalla de ahorro
    @FXML private ListView<ModalidadAhorro> modalidadesListView;
    @FXML private Label tituloModalidadLabel;
    @FXML private TextArea descripcionModalidadTextArea;
    @FXML private Label factorPresupuestoLabel;
    @FXML private Label prioridadPrecioLabel;
    @FXML private Label prioridadNutricionLabel;
    @FXML private ProgressBar prioridadPrecioProgress;
    @FXML private ProgressBar prioridadNutricionProgress;
    @FXML private Label ahorroEstimadoLabel;
    @FXML private Label presupuestoOriginalLabel;
    @FXML private Label presupuestoAjustadoLabel;
    @FXML private Button aplicarModalidadButton;
    @FXML private ListView<String> consejosListView;
    @FXML private VBox ejemploCalculoPane;
    @FXML private TextField presupuestoEjemploField;
    @FXML private Button calcularButton;
    @FXML private VBox resultadoCalculoPane;
    @FXML private Label presupuestoOriginalResultadoLabel;
    @FXML private Label presupuestoAjustadoResultadoLabel;
    @FXML private Label ahorroEstimadoResultadoLabel;
    @FXML private PieChart distribucionGastosChart;

    // Servicios
    private final ModalidadAhorroServicio modalidadServicio = new ModalidadAhorroServicio();

    // Variables de estado
    private Long usuarioIdActual = 1L; // Simulado, en un caso real vendría de la sesión
    private ModalidadAhorro modalidadSeleccionada = null;
    private boolean modalidadAplicada = false;

    /**
     * Inicialización específica del controlador de ahorro
     * Implementación del método abstracto de BaseController
     */
    @Override
    protected void inicializarControlador() {
        // Destacar el botón de ahorros como seleccionado
        activarBoton(savingsButton);

        // Inicializar pantalla de modalidades de ahorro
        inicializarPantallaAhorro();

        // Cargar datos
        cargarModalidades();

        // Aplicar estilos personalizados
        aplicarEstilosComponentes();
    }

    /**
     * Aplica estilos a los componentes específicos de esta pantalla
     */
    private void aplicarEstilosComponentes() {
        // Aplicar estilos al ListView y TextArea
        modalidadesListView.setStyle(
                "-fx-background-color: rgba(30, 30, 40, 0.7); " +
                        "-fx-background-radius: 5px; " +
                        "-fx-border-color: rgba(80, 80, 120, 0.5); " +
                        "-fx-border-radius: 5px; " +
                        "-fx-text-fill: white;"
        );

        consejosListView.setStyle(
                "-fx-background-color: rgba(30, 30, 40, 0.7); " +
                        "-fx-background-radius: 5px; " +
                        "-fx-border-color: rgba(80, 80, 120, 0.5); " +
                        "-fx-border-radius: 5px; " +
                        "-fx-text-fill: white;"
        );

        // Aplicar estilos a TextArea
        EstilosApp.aplicarEstiloTextArea(descripcionModalidadTextArea);

        // Aplicar estilos a los campos de texto
        EstilosApp.aplicarEstiloCampoTexto(presupuestoEjemploField);

        // Aplicar estilos a los botones principales
        EstilosApp.aplicarEstiloBotonPrimario(aplicarModalidadButton);
        EstilosApp.aplicarEstiloBotonPrimario(calcularButton);

        // Aplicar estilos al gráfico
        EstilosApp.aplicarEstiloGrafico(distribucionGastosChart);

        // Aplicar estilos a los paneles
        if (resultadoCalculoPane.isVisible()) {
            EstilosApp.aplicarEstiloTarjeta(resultadoCalculoPane);
        }
        EstilosApp.aplicarEstiloTarjeta(ejemploCalculoPane);
        EstilosApp.aplicarEstiloLista(modalidadesListView);
    }

    /**
     * Inicializa la pantalla de ahorro
     */
    private void inicializarPantallaAhorro() {
        // Configurar la selección de modalidad
        modalidadesListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        mostrarDetalleModalidad(newValue);
                    }
                });

        // Configurar celda personalizada para ListView de modalidades
        modalidadesListView.setCellFactory(param -> new ListCell<ModalidadAhorro>() {
            @Override
            protected void updateItem(ModalidadAhorro item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    VBox contenido = new VBox(5);

                    // Nombre de la modalidad
                    Label nombreLabel = new Label(item.getNombre());
                    nombreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

                    // Factor de presupuesto
                    Label factorLabel = new Label(String.format("Factor: %.0f%%", item.getFactorPresupuestoAsDouble() * 100));
                    factorLabel.setStyle("-fx-text-fill: rgb(200, 200, 220);");

                    // Prioridades
                    HBox prioridades = new HBox(10);
                    Label precioLabel = new Label("Precio: " + item.getPrioridadPrecio() + "/10");
                    precioLabel.setStyle("-fx-text-fill: rgb(100, 200, 255);");

                    Label nutricionLabel = new Label("Nutrición: " + item.getPrioridadNutricion() + "/10");
                    nutricionLabel.setStyle("-fx-text-fill: rgb(100, 220, 100);");

                    prioridades.getChildren().addAll(precioLabel, nutricionLabel);

                    contenido.getChildren().addAll(nombreLabel, factorLabel, prioridades);

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

        // Configurar celda personalizada para ListView de consejos
        consejosListView.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText("• " + item);
                    setTextFill(Color.rgb(230, 230, 250));
                }
            }
        });

        // Configurar validación para el campo de presupuesto ejemplo
        presupuestoEjemploField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                presupuestoEjemploField.setText(oldValue);
            }
        });

        // Configurar acción del botón calcular
        calcularButton.setOnAction(this::handleCalcularEjemplo);

        // Configurar acción del botón aplicar modalidad
        aplicarModalidadButton.setOnAction(this::handleAplicarModalidad);

        // Ocultar el panel de resultado de cálculo inicialmente
        resultadoCalculoPane.setVisible(false);
        resultadoCalculoPane.setManaged(false);
    }

    /**
     * Carga las modalidades de ahorro desde el servicio
     */
    private void cargarModalidades() {
        // Obtener todas las modalidades
        List<ModalidadAhorro> modalidades = modalidadServicio.obtenerTodasModalidades();

        // Cargar en el ListView
        modalidadesListView.setItems(FXCollections.observableArrayList(modalidades));

        // Seleccionar la primera modalidad
        if (!modalidades.isEmpty()) {
            modalidadesListView.getSelectionModel().select(0);
        }
    }

    /**
     * Muestra los detalles de la modalidad seleccionada
     */
    private void mostrarDetalleModalidad(ModalidadAhorro modalidad) {
        modalidadSeleccionada = modalidad;

        // Actualizar la interfaz con los detalles de la modalidad
        tituloModalidadLabel.setText("Modalidad: " + modalidad.getNombre());
        descripcionModalidadTextArea.setText(modalidad.getDescripcion());

        // Actualizar etiquetas y barras de progreso
        factorPresupuestoLabel.setText(String.format("%.0f%%", modalidad.getFactorPresupuestoAsDouble() * 100));

        prioridadPrecioLabel.setText(modalidad.getPrioridadPrecio() + "/10");
        prioridadPrecioProgress.setProgress(modalidad.getPrioridadPrecio() / 10.0);

        prioridadNutricionLabel.setText(modalidad.getPrioridadNutricion() + "/10");
        prioridadNutricionProgress.setProgress(modalidad.getPrioridadNutricion() / 10.0);

        // Colorear barras de progreso
        String colorPrecio;
        if (modalidad.getPrioridadPrecio() >= 7) {
            colorPrecio = "rgb(100, 200, 255)"; // Azul
        } else if (modalidad.getPrioridadPrecio() >= 4) {
            colorPrecio = "rgb(200, 200, 0)"; // Amarillo
        } else {
            colorPrecio = "rgb(200, 100, 100)"; // Rojo
        }
        prioridadPrecioProgress.setStyle("-fx-accent: " + colorPrecio + ";");

        String colorNutricion;
        if (modalidad.getPrioridadNutricion() >= 7) {
            colorNutricion = "rgb(100, 220, 100)"; // Verde
        } else if (modalidad.getPrioridadNutricion() >= 4) {
            colorNutricion = "rgb(200, 200, 0)"; // Amarillo
        } else {
            colorNutricion = "rgb(200, 100, 100)"; // Rojo
        }
        prioridadNutricionProgress.setStyle("-fx-accent: " + colorNutricion + ";");

        // Cargar consejos
        List<String> consejos = modalidadServicio.obtenerConsejosAhorro(modalidad);
        consejosListView.setItems(FXCollections.observableArrayList(consejos));

        // Calcular ejemplo con un presupuesto predeterminado
        double presupuestoEjemplo = 200.0; // € 200 como ejemplo
        double presupuestoAjustado = modalidadServicio.calcularPresupuestoAjustado(presupuestoEjemplo, modalidad);
        double ahorroEstimado = presupuestoEjemplo - presupuestoAjustado;

        // Actualizar etiquetas de ejemplo
        presupuestoOriginalLabel.setText(String.format("€%.2f", presupuestoEjemplo));
        presupuestoAjustadoLabel.setText(String.format("€%.2f", presupuestoAjustado));
        ahorroEstimadoLabel.setText(String.format("€%.2f", ahorroEstimado));

        // Actualizar estado del botón
        aplicarModalidadButton.setText(modalidadAplicada ? "Modalidad Aplicada" : "Aplicar Modalidad");
        aplicarModalidadButton.setDisable(modalidadAplicada);
    }

    /**
     * Manejador para el cálculo de ejemplo con presupuesto personalizado
     */
    @FXML
    private void handleCalcularEjemplo(ActionEvent event) {
        if (modalidadSeleccionada == null) {
            return;
        }

        try {
            // Obtener el presupuesto ingresado
            String presupuestoTexto = presupuestoEjemploField.getText().trim();
            if (presupuestoTexto.isEmpty()) {
                navegacionServicio.mostrarAlertaError("Error", "Por favor, ingresa un presupuesto válido.");
                return;
            }

            double presupuesto = Double.parseDouble(presupuestoTexto);
            if (presupuesto <= 0) {
                navegacionServicio.mostrarAlertaError("Error", "El presupuesto debe ser mayor que cero.");
                return;
            }

            // Calcular presupuesto ajustado
            double presupuestoAjustado = modalidadServicio.calcularPresupuestoAjustado(presupuesto, modalidadSeleccionada);
            double ahorroEstimado = presupuesto - presupuestoAjustado;

            // Mostrar resultados
            presupuestoOriginalResultadoLabel.setText(String.format("€%.2f", presupuesto));
            presupuestoAjustadoResultadoLabel.setText(String.format("€%.2f", presupuestoAjustado));
            ahorroEstimadoResultadoLabel.setText(String.format("€%.2f", ahorroEstimado));

            // Actualizar gráfico
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Gasto", presupuestoAjustado),
                    new PieChart.Data("Ahorro", ahorroEstimado)
            );
            distribucionGastosChart.setData(pieChartData);

            // Colorear secciones del gráfico
            pieChartData.get(0).getNode().setStyle("-fx-pie-color: rgb(100, 170, 255);"); // Azul para gastos
            pieChartData.get(1).getNode().setStyle("-fx-pie-color: rgb(100, 220, 100);"); // Verde para ahorro

            // Mostrar panel de resultados
            resultadoCalculoPane.setVisible(true);
            resultadoCalculoPane.setManaged(true);

            // Aplicar estilo a la tarjeta de resultados
            EstilosApp.aplicarEstiloTarjeta(resultadoCalculoPane);

        } catch (NumberFormatException e) {
            navegacionServicio.mostrarAlertaError("Formato incorrecto", "Por favor, ingresa un valor numérico válido.");
        }
    }

    /**
     * Manejador para aplicar la modalidad seleccionada
     */
    @FXML
    private void handleAplicarModalidad(ActionEvent event) {
        if (modalidadSeleccionada == null) {
            return;
        }

        // Aquí se implementaría la lógica para aplicar la modalidad al usuario
        // Por ejemplo, guardar la preferencia en la base de datos

        // Simulación de aplicación exitosa
        modalidadAplicada = true;
        aplicarModalidadButton.setText("Modalidad Aplicada");
        aplicarModalidadButton.setDisable(true);

        navegacionServicio.mostrarAlertaInformacion("Modalidad Aplicada",
                "La modalidad de ahorro '" + modalidadSeleccionada.getNombre() +
                        "' ha sido aplicada correctamente. Se utilizará en tus recomendaciones de compra y presupuesto.");
    }

    /**
     * Sobrescribe el método de navegación a ahorros
     * Ya que estamos en la vista de modalidades de ahorro
     */
    @Override
    public void handleSavingsAction(ActionEvent evento) {
        // Ya estamos en la vista de modalidades de ahorro, solo activamos el botón
        activarBoton(savingsButton);
    }
}