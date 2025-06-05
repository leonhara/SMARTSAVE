package smartsave.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import smartsave.modelo.ModalidadAhorro;
import smartsave.servicio.ModalidadAhorroServicio;
import smartsave.servicio.SessionManager; // Importar SessionManager
import smartsave.servicio.UsuarioServicio;
import smartsave.utilidad.EstilosApp;
import smartsave.modelo.Usuario; // Importar Usuario

import java.util.List;

public class AhorroController extends BaseController {

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

    private final ModalidadAhorroServicio modalidadServicio = new ModalidadAhorroServicio();

    // private Long usuarioIdActual = 1L; // Eliminado
    private Usuario usuarioActualLocal; // Para el objeto Usuario en sesión
    private ModalidadAhorro modalidadSeleccionada = null;
    // private boolean modalidadAplicada = false; // Se manejará con la modalidad del usuario

    @Override
    protected void inicializarControlador() {
        this.usuarioActualLocal = SessionManager.getInstancia().getUsuarioActual();

        if (this.usuarioActualLocal == null) {
            System.err.println("Error crítico: No hay usuario en sesión en AhorroController.");
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
        // this.usuarioIdActualLocal = this.usuarioActualLocal.getId(); // No se usa directamente el ID aquí pero es bueno tenerlo si se necesitara

        activarBoton(savingsButton);
        inicializarPantallaAhorro();
        cargarModalidades();
        aplicarEstilosComponentes();
    }

    private void disableUIComponents() {
        // Deshabilitar o limpiar componentes si no hay usuario
        if (modalidadesListView != null) modalidadesListView.setDisable(true);
        if (aplicarModalidadButton != null) aplicarModalidadButton.setDisable(true);
        if (presupuestoEjemploField != null) presupuestoEjemploField.setDisable(true);
        if (calcularButton != null) calcularButton.setDisable(true);
        if (tituloModalidadLabel != null) tituloModalidadLabel.setText("Modalidad: N/A");
        if (descripcionModalidadTextArea != null) descripcionModalidadTextArea.setText("Seleccione una modalidad.");
        // ... limpiar otras etiquetas y progresos ...
        if (resultadoCalculoPane != null) {
            resultadoCalculoPane.setVisible(false);
            resultadoCalculoPane.setManaged(false);
        }
    }

    private void aplicarEstilosComponentes() {
        if (modalidadesListView != null) {
            modalidadesListView.setStyle("-fx-background-color: rgba(30, 30, 40, 0.7); -fx-background-radius: 5px; -fx-border-color: rgba(80, 80, 120, 0.5); -fx-border-radius: 5px; -fx-text-fill: white;");
            EstilosApp.aplicarEstiloLista(modalidadesListView); //
        }
        if (consejosListView != null) consejosListView.setStyle("-fx-background-color: rgba(30, 30, 40, 0.7); -fx-background-radius: 5px; -fx-border-color: rgba(80, 80, 120, 0.5); -fx-border-radius: 5px; -fx-text-fill: white;");
        if (descripcionModalidadTextArea != null) EstilosApp.aplicarEstiloTextArea(descripcionModalidadTextArea); //
        if (presupuestoEjemploField != null) EstilosApp.aplicarEstiloCampoTexto(presupuestoEjemploField); //
        if (aplicarModalidadButton != null) EstilosApp.aplicarEstiloBotonPrimario(aplicarModalidadButton); //
        if (calcularButton != null) EstilosApp.aplicarEstiloBotonPrimario(calcularButton); //
        if (distribucionGastosChart != null) EstilosApp.aplicarEstiloGrafico(distribucionGastosChart); //
        if (resultadoCalculoPane != null && resultadoCalculoPane.isVisible()) EstilosApp.aplicarEstiloTarjeta(resultadoCalculoPane); //
        if (ejemploCalculoPane != null) EstilosApp.aplicarEstiloTarjeta(ejemploCalculoPane); //
    }

    private void inicializarPantallaAhorro() {
        if (modalidadesListView != null) {
            modalidadesListView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> { if (newValue != null) mostrarDetalleModalidad(newValue); }
            );
            modalidadesListView.setCellFactory(param -> new ListCell<ModalidadAhorro>() { /* ... (código de celda como antes) ... */
                @Override
                protected void updateItem(ModalidadAhorro item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null); setGraphic(null); setStyle("-fx-background-color: transparent;");
                    } else {
                        VBox contenido = new VBox(5);
                        Label nombreLabel = new Label(item.getNombre());
                        nombreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
                        Label factorLabel = new Label(String.format("Factor: %.0f%%", item.getFactorPresupuestoAsDouble() * 100)); //
                        factorLabel.setStyle("-fx-text-fill: rgb(200, 200, 220);");
                        HBox prioridades = new HBox(10);
                        Label precioLabel = new Label("Precio: " + item.getPrioridadPrecio() + "/10"); //
                        precioLabel.setStyle("-fx-text-fill: rgb(100, 200, 255);");
                        Label nutricionLabel = new Label("Nutrición: " + item.getPrioridadNutricion() + "/10"); //
                        nutricionLabel.setStyle("-fx-text-fill: rgb(100, 220, 100);");
                        prioridades.getChildren().addAll(precioLabel, nutricionLabel);
                        contenido.getChildren().addAll(nombreLabel, factorLabel, prioridades);
                        setGraphic(contenido);
                        selectedProperty().addListener((obs, wasSelected, isNowSelected) -> setStyle(isNowSelected ? "-fx-background-color: rgba(80, 80, 140, 0.7); -fx-background-radius: 5px;" : "-fx-background-color: transparent;"));
                        setStyle(isSelected() ? "-fx-background-color: rgba(80, 80, 140, 0.7); -fx-background-radius: 5px;" : "-fx-background-color: transparent;");
                    }
                }
            });
        }
        if (consejosListView != null) {
            consejosListView.setCellFactory(param -> new ListCell<String>() { /* ... (código de celda como antes) ... */
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setGraphic(null); }
                    else { setText("• " + item); setTextFill(Color.rgb(230, 230, 250)); }
                }
            });
        }
        if (presupuestoEjemploField != null) presupuestoEjemploField.textProperty().addListener((observable, oldValue, newValue) -> { if (!newValue.matches("\\d*\\.?\\d*")) presupuestoEjemploField.setText(oldValue); });
        if (calcularButton != null) calcularButton.setOnAction(this::handleCalcularEjemplo);
        if (aplicarModalidadButton != null) aplicarModalidadButton.setOnAction(this::handleAplicarModalidad);
        if (resultadoCalculoPane != null) {
            resultadoCalculoPane.setVisible(false);
            resultadoCalculoPane.setManaged(false);
        }
    }

    private void cargarModalidades() {
        if (modalidadesListView == null) return;
        List<ModalidadAhorro> modalidades = modalidadServicio.obtenerTodasModalidades(); //
        modalidadesListView.setItems(FXCollections.observableArrayList(modalidades));
        if (!modalidades.isEmpty()) {
            // Intentar seleccionar la modalidad actual del usuario si existe
            if (usuarioActualLocal != null && usuarioActualLocal.getModalidadAhorroSeleccionada() != null) { //
                ModalidadAhorro modalidadUsuario = modalidadServicio.obtenerModalidadPorNombre(usuarioActualLocal.getModalidadAhorroSeleccionada()); //
                if (modalidadUsuario != null) {
                    modalidadesListView.getSelectionModel().select(modalidadUsuario);
                } else {
                    modalidadesListView.getSelectionModel().select(0);
                }
            } else {
                modalidadesListView.getSelectionModel().select(0);
            }
        }
    }

    private void mostrarDetalleModalidad(ModalidadAhorro modalidad) {
        if (modalidad == null || tituloModalidadLabel == null || descripcionModalidadTextArea == null || factorPresupuestoLabel == null || prioridadPrecioLabel == null || prioridadPrecioProgress == null || prioridadNutricionLabel == null || prioridadNutricionProgress == null || consejosListView == null || presupuestoOriginalLabel == null || presupuestoAjustadoLabel == null || ahorroEstimadoLabel == null || aplicarModalidadButton == null) return;
        modalidadSeleccionada = modalidad;
        tituloModalidadLabel.setText("Modalidad: " + modalidad.getNombre()); //
        descripcionModalidadTextArea.setText(modalidad.getDescripcion()); //
        factorPresupuestoLabel.setText(String.format("%.0f%%", modalidad.getFactorPresupuestoAsDouble() * 100)); //
        prioridadPrecioLabel.setText(modalidad.getPrioridadPrecio() + "/10"); //
        prioridadPrecioProgress.setProgress(modalidad.getPrioridadPrecio() / 10.0); //
        prioridadNutricionLabel.setText(modalidad.getPrioridadNutricion() + "/10"); //
        prioridadNutricionProgress.setProgress(modalidad.getPrioridadNutricion() / 10.0); //
        // ... (lógica de colores para progress bars como antes) ...
        String colorPrecio; if (modalidad.getPrioridadPrecio() >= 7) colorPrecio = "rgb(100, 200, 255)"; else if (modalidad.getPrioridadPrecio() >= 4) colorPrecio = "rgb(200, 200, 0)"; else colorPrecio = "rgb(200, 100, 100)"; prioridadPrecioProgress.setStyle("-fx-accent: " + colorPrecio + ";"); //
        String colorNutricion; if (modalidad.getPrioridadNutricion() >= 7) colorNutricion = "rgb(100, 220, 100)"; else if (modalidad.getPrioridadNutricion() >= 4) colorNutricion = "rgb(200, 200, 0)"; else colorNutricion = "rgb(200, 100, 100)"; prioridadNutricionProgress.setStyle("-fx-accent: " + colorNutricion + ";"); //

        List<String> consejos = modalidadServicio.obtenerConsejosAhorro(modalidad); //
        consejosListView.setItems(FXCollections.observableArrayList(consejos));
        double presupuestoEjemplo = 200.0;
        double presupuestoAjustadoCalc = modalidadServicio.calcularPresupuestoAjustado(presupuestoEjemplo, modalidad); //
        double ahorroEstimadoCalc = presupuestoEjemplo - presupuestoAjustadoCalc;
        presupuestoOriginalLabel.setText(String.format("€%.2f", presupuestoEjemplo));
        presupuestoAjustadoLabel.setText(String.format("€%.2f", presupuestoAjustadoCalc));
        ahorroEstimadoLabel.setText(String.format("€%.2f", ahorroEstimadoCalc));

        boolean esModalidadActualUsuario = usuarioActualLocal != null && modalidad.getNombre().equals(usuarioActualLocal.getModalidadAhorroSeleccionada()); //
        aplicarModalidadButton.setText(esModalidadActualUsuario ? "Modalidad Aplicada" : "Aplicar Modalidad");
        aplicarModalidadButton.setDisable(esModalidadActualUsuario);
    }

    @FXML
    private void handleCalcularEjemplo(ActionEvent event) {
        if (modalidadSeleccionada == null || presupuestoEjemploField == null || presupuestoOriginalResultadoLabel == null || presupuestoAjustadoResultadoLabel == null || ahorroEstimadoResultadoLabel == null || distribucionGastosChart == null || resultadoCalculoPane == null) return;
        try {
            String presupuestoTexto = presupuestoEjemploField.getText().trim();
            if (presupuestoTexto.isEmpty()) { navegacionServicio.mostrarAlertaError("Error", "Por favor, ingresa un presupuesto válido."); return; } //
            double presupuesto = Double.parseDouble(presupuestoTexto);
            if (presupuesto <= 0) { navegacionServicio.mostrarAlertaError("Error", "El presupuesto debe ser mayor que cero."); return; } //
            double presupuestoAjustadoCalc = modalidadServicio.calcularPresupuestoAjustado(presupuesto, modalidadSeleccionada); //
            double ahorroEstimadoCalc = presupuesto - presupuestoAjustadoCalc;
            presupuestoOriginalResultadoLabel.setText(String.format("€%.2f", presupuesto));
            presupuestoAjustadoResultadoLabel.setText(String.format("€%.2f", presupuestoAjustadoCalc));
            ahorroEstimadoResultadoLabel.setText(String.format("€%.2f", ahorroEstimadoCalc));
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Gasto", presupuestoAjustadoCalc), new PieChart.Data("Ahorro", ahorroEstimadoCalc)
            );
            distribucionGastosChart.setData(pieChartData);
            if (!pieChartData.isEmpty()) { // Añadida comprobación
                if(pieChartData.get(0) != null && pieChartData.get(0).getNode() != null) pieChartData.get(0).getNode().setStyle("-fx-pie-color: rgb(100, 170, 255);");
                if(pieChartData.size() > 1 && pieChartData.get(1) != null && pieChartData.get(1).getNode() != null) pieChartData.get(1).getNode().setStyle("-fx-pie-color: rgb(100, 220, 100);");
            }
            resultadoCalculoPane.setVisible(true);
            resultadoCalculoPane.setManaged(true);
            EstilosApp.aplicarEstiloTarjeta(resultadoCalculoPane); //
        } catch (NumberFormatException e) {
            navegacionServicio.mostrarAlertaError("Formato incorrecto", "Por favor, ingresa un valor numérico válido."); //
        }
    }

    @FXML
    private void handleAplicarModalidad(ActionEvent event) {
        if (modalidadSeleccionada == null || usuarioActualLocal == null || aplicarModalidadButton == null) return;

        // Actualizar la modalidad en el objeto Usuario
        usuarioActualLocal.setModalidadAhorroSeleccionada(modalidadSeleccionada.getNombre()); //

        // Guardar el cambio en la base de datos a través del UsuarioServicio
        UsuarioServicio usuarioServicio = new UsuarioServicio(); // Considera inyectarlo o hacerlo singleton si no lo es
        boolean actualizado = usuarioServicio.actualizarUsuario(usuarioActualLocal); //

        if (actualizado) {
            aplicarModalidadButton.setText("Modalidad Aplicada");
            aplicarModalidadButton.setDisable(true);
            navegacionServicio.mostrarAlertaInformacion("Modalidad Aplicada", "La modalidad de ahorro '" + modalidadSeleccionada.getNombre() + "' ha sido aplicada correctamente."); //
            // Actualizar la sesión en SessionManager si es necesario (si el objeto Usuario es una copia)
            SessionManager.getInstancia().setUsuarioActual(usuarioActualLocal);
        } else {
            navegacionServicio.mostrarAlertaError("Error", "No se pudo aplicar la modalidad de ahorro."); //
        }
    }

    @Override
    public void handleSavingsAction(ActionEvent evento) {
        this.usuarioActualLocal = SessionManager.getInstancia().getUsuarioActual(); // Refrescar usuario
        if (this.usuarioActualLocal == null) {
            System.err.println("Error crítico: No hay usuario en sesión al re-navegar a Ahorro.");
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
        activarBoton(savingsButton);
        cargarModalidades(); // Recargar modalidades y actualizar selección
    }
}