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
import smartsave.modelo.PerfilNutricional;
import smartsave.servicio.PerfilNutricionalServicio;
import smartsave.servicio.SessionManager; 
import smartsave.utilidad.EstilosApp;
import smartsave.modelo.Usuario; 

import java.util.ArrayList;
import java.util.List;

public class NutricionController extends BaseController {

    @FXML private TextField edadField;
    @FXML private TextField pesoField;
    @FXML private TextField alturaField;
    @FXML private RadioButton sexoMRadio;
    @FXML private RadioButton sexoFRadio;
    @FXML private ToggleGroup sexoGroup;
    @FXML private ComboBox<String> actividadComboBox;
    @FXML private VBox restriccionesPane;
    @FXML private Button guardarPerfilButton;
    @FXML private Label imcValueLabel;
    @FXML private Label imcCategoryLabel;
    @FXML private Label caloriasLabel;
    @FXML private Label proteinasLabel;
    @FXML private Label carbosLabel;
    @FXML private Label grasasLabel;
    @FXML private Label puntuacionLabel;
    @FXML private ProgressBar puntuacionProgress;
    @FXML private TextArea recomendacionesArea;
    @FXML private PieChart macrosPieChart;

    private final PerfilNutricionalServicio perfilServicio = new PerfilNutricionalServicio();

    private Long usuarioIdActualLocal;
    private Usuario usuarioActualLocal;
    private List<CheckBox> restriccionesCheckboxes = new ArrayList<>();
    private PerfilNutricional perfilActual = null;

    @Override
    protected void inicializarControlador() {
        this.usuarioActualLocal = SessionManager.getInstancia().getUsuarioActual();

        if (this.usuarioActualLocal == null) {
            System.err.println("Error crítico: No hay usuario en sesión en NutricionController.");
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

        activarBoton(nutritionButton);
        inicializarFormulario();
        cargarPerfilExistente(); 
        aplicarEstilosComponentes();
    }

    private void disableUIComponents() {
        
        if (edadField != null) edadField.setDisable(true);
        if (pesoField != null) pesoField.setDisable(true);
        if (alturaField != null) alturaField.setDisable(true);
        if (sexoMRadio != null) sexoMRadio.setDisable(true);
        if (sexoFRadio != null) sexoFRadio.setDisable(true);
        if (actividadComboBox != null) actividadComboBox.setDisable(true);
        if (restriccionesPane != null) restriccionesPane.setDisable(true);
        if (guardarPerfilButton != null) guardarPerfilButton.setDisable(true);
        
        if (imcValueLabel != null) imcValueLabel.setText("N/A");
        if (imcCategoryLabel != null) imcCategoryLabel.setText("N/A");
        if (caloriasLabel != null) caloriasLabel.setText("N/A kcal");
        if (proteinasLabel != null) proteinasLabel.setText("N/A g");
        if (carbosLabel != null) carbosLabel.setText("N/A g");
        if (grasasLabel != null) grasasLabel.setText("N/A g");
        if (puntuacionLabel != null) puntuacionLabel.setText("--/100");
        if (puntuacionProgress != null) puntuacionProgress.setProgress(0);
        if (recomendacionesArea != null) recomendacionesArea.setText("Cree o cargue un perfil para ver recomendaciones.");
        if (macrosPieChart != null) macrosPieChart.setData(FXCollections.observableArrayList());
    }


    private void aplicarEstilosComponentes() {
        if (edadField != null) EstilosApp.aplicarEstiloCampoTexto(edadField); 
        if (pesoField != null) EstilosApp.aplicarEstiloCampoTexto(pesoField); 
        if (alturaField != null) EstilosApp.aplicarEstiloCampoTexto(alturaField); 
        if (recomendacionesArea != null) EstilosApp.aplicarEstiloTextArea(recomendacionesArea); 
        if (guardarPerfilButton != null) EstilosApp.aplicarEstiloBotonPrimario(guardarPerfilButton); 
        if (actividadComboBox != null) EstilosApp.aplicarEstiloComboBox(actividadComboBox); 
        if (macrosPieChart != null) EstilosApp.aplicarEstiloGrafico(macrosPieChart); 
        estilizarRadioButtons();
    }

    private void estilizarRadioButtons() {
        if (sexoMRadio == null || sexoFRadio == null) return;
        String radioButtonStyle = "-fx-text-fill: white; -fx-background-color: rgba(40, 40, 50, 0.7); -fx-background-radius: 5px; -fx-padding: 5px 10px;";
        sexoMRadio.setStyle(radioButtonStyle);
        sexoFRadio.setStyle(radioButtonStyle);
        sexoMRadio.setOnMouseEntered(e -> sexoMRadio.setStyle(radioButtonStyle + "-fx-background-color: rgba(60, 60, 80, 0.7);"));
        sexoMRadio.setOnMouseExited(e -> sexoMRadio.setStyle(radioButtonStyle));
        sexoFRadio.setOnMouseEntered(e -> sexoFRadio.setStyle(radioButtonStyle + "-fx-background-color: rgba(60, 60, 80, 0.7);"));
        sexoFRadio.setOnMouseExited(e -> sexoFRadio.setStyle(radioButtonStyle));
    }

    private void inicializarFormulario() {
        configurarValidacionCampos();
        if (actividadComboBox != null) actividadComboBox.setItems(FXCollections.observableArrayList(perfilServicio.obtenerNivelesActividad())); 

        if (restriccionesPane != null) {
            List<String> restricciones = perfilServicio.obtenerRestriccionesAlimentarias(); 
            restriccionesPane.getChildren().clear();
            restriccionesCheckboxes.clear();
            HBox filaActual = null;
            int contador = 0;
            for (String restriccion : restricciones) {
                if (contador % 2 == 0) {
                    filaActual = new HBox(20);
                    restriccionesPane.getChildren().add(filaActual);
                }
                CheckBox checkbox = new CheckBox(restriccion);
                checkbox.setTextFill(Color.rgb(230, 230, 250));
                restriccionesCheckboxes.add(checkbox);
                if (filaActual != null) filaActual.getChildren().add(checkbox);
                contador++;
            }
        }
    }

    private void configurarValidacionCampos() {
        if (edadField != null) edadField.textProperty().addListener((observable, oldValue, newValue) -> { if (!newValue.matches("\\d*")) edadField.setText(oldValue); });
        if (pesoField != null) pesoField.textProperty().addListener((observable, oldValue, newValue) -> { if (!newValue.matches("\\d*\\.?\\d*")) pesoField.setText(oldValue); });
        if (alturaField != null) alturaField.textProperty().addListener((observable, oldValue, newValue) -> { if (!newValue.matches("\\d*\\.?\\d*")) alturaField.setText(oldValue); });
    }

    private void cargarPerfilExistente() {
        if (usuarioIdActualLocal == null) return; 
        if (perfilServicio.tienePerfil(usuarioIdActualLocal)) { 
            perfilActual = perfilServicio.obtenerPerfilPorUsuario(usuarioIdActualLocal); 
            if (perfilActual != null) {
                if (edadField != null) edadField.setText(String.valueOf(perfilActual.getEdad())); 
                if (pesoField != null) pesoField.setText(String.format("%.1f", perfilActual.getPeso())); 
                if (alturaField != null) alturaField.setText(String.format("%.1f", perfilActual.getAltura())); 
                if (sexoMRadio != null && sexoFRadio != null) {
                    if ("M".equals(perfilActual.getSexo())) sexoMRadio.setSelected(true); else sexoFRadio.setSelected(true); 
                }
                if (actividadComboBox != null) actividadComboBox.setValue(perfilActual.getNivelActividad()); 
                for (CheckBox checkbox : restriccionesCheckboxes) {
                    checkbox.setSelected(perfilActual.getRestricciones().contains(checkbox.getText())); 
                }
                actualizarResumen(perfilActual);
            }
        }
    }

    @FXML
    private void handleGuardarPerfil(ActionEvent event) {
        if (usuarioIdActualLocal == null) {
            navegacionServicio.mostrarAlertaError("Error de Sesión", "No hay un usuario activo para guardar el perfil."); 
            return;
        }
        if (edadField.getText().trim().isEmpty() || pesoField.getText().trim().isEmpty() || alturaField.getText().trim().isEmpty() || actividadComboBox.getValue() == null) {
            navegacionServicio.mostrarAlertaError("Campos incompletos", "Por favor, completa todos los campos obligatorios."); 
            return;
        }
        try {
            int edad = Integer.parseInt(edadField.getText().trim());
            double peso = Double.parseDouble(pesoField.getText().trim());
            double altura = Double.parseDouble(alturaField.getText().trim());
            String sexo = sexoMRadio.isSelected() ? "M" : "F";
            String nivelActividad = actividadComboBox.getValue();

            if (edad < 12 || edad > 120) { navegacionServicio.mostrarAlertaError("Valor incorrecto", "La edad debe estar entre 12 y 120 años."); return; } 
            if (peso < 30 || peso > 300) { navegacionServicio.mostrarAlertaError("Valor incorrecto", "El peso debe estar entre 30 y 300 kg."); return; } 
            if (altura < 100 || altura > 250) { navegacionServicio.mostrarAlertaError("Valor incorrecto", "La altura debe estar entre 100 y 250 cm."); return; } 

            PerfilNutricional perfil;
            if (perfilActual != null) {
                perfil = perfilActual;
                perfil.setEdad(edad); perfil.setPeso(peso); perfil.setAltura(altura); perfil.setSexo(sexo); perfil.setNivelActividad(nivelActividad); 
                perfil.getRestricciones().clear(); 
            } else {
                perfil = new PerfilNutricional(usuarioIdActualLocal, edad, peso, altura, sexo, nivelActividad); 
            }
            for (CheckBox checkbox : restriccionesCheckboxes) {
                if (checkbox.isSelected()) perfil.agregarRestriccion(checkbox.getText()); 
            }
            perfilActual = perfilServicio.guardarPerfil(perfil); 
            actualizarResumen(perfilActual);
            navegacionServicio.mostrarAlertaInformacion("Perfil guardado", "Tu perfil nutricional ha sido guardado correctamente."); 
        } catch (NumberFormatException e) {
            navegacionServicio.mostrarAlertaError("Formato incorrecto", "Por favor, ingresa valores numéricos válidos."); 
        }
    }

    private void actualizarResumen(PerfilNutricional perfil) {
        if (imcValueLabel == null || imcCategoryLabel == null || caloriasLabel == null || proteinasLabel == null || carbosLabel == null || grasasLabel == null || puntuacionLabel == null || puntuacionProgress == null || recomendacionesArea == null || macrosPieChart == null) return;
        double imc = perfil.getImc(); 
        imcValueLabel.setText(String.format("%.1f", imc));
        String categoriaIMC = perfil.getCategoriaIMC(); 
        imcCategoryLabel.setText(categoriaIMC);
        switch (categoriaIMC) {
            case "Bajo peso": imcCategoryLabel.setTextFill(Color.rgb(255, 200, 0)); break;
            case "Normal": imcCategoryLabel.setTextFill(Color.rgb(100, 220, 100)); break;
            case "Sobrepeso": imcCategoryLabel.setTextFill(Color.rgb(255, 150, 0)); break;
            case "Obesidad": imcCategoryLabel.setTextFill(Color.rgb(255, 80, 80)); break;
        }
        caloriasLabel.setText(perfil.getCaloriasDiarias() + " kcal"); 
        PerfilNutricional.MacronutrientesDiarios macros = perfil.getMacronutrientesDiarios(); 
        proteinasLabel.setText(macros.getProteinas() + " g");
        carbosLabel.setText(macros.getCarbohidratos() + " g");
        grasasLabel.setText(macros.getGrasas() + " g");
        int puntuacion = perfilServicio.calcularPuntuacionNutricional(perfil); 
        puntuacionLabel.setText(puntuacion + "/100");
        puntuacionProgress.setProgress(puntuacion / 100.0);
        if (puntuacion >= 80) puntuacionLabel.setTextFill(Color.rgb(100, 220, 100));
        else if (puntuacion >= 60) puntuacionLabel.setTextFill(Color.rgb(200, 220, 100));
        else if (puntuacion >= 40) puntuacionLabel.setTextFill(Color.rgb(255, 200, 0));
        else puntuacionLabel.setTextFill(Color.rgb(255, 100, 100));
        recomendacionesArea.setText(perfilServicio.generarRecomendacionAlimentaria(perfil)); 
        actualizarGraficoMacros(macros);
    }

    private void actualizarGraficoMacros(PerfilNutricional.MacronutrientesDiarios macros) {
        if (macrosPieChart == null) return;
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Proteínas (30%)", macros.getProteinas()),
                new PieChart.Data("Carbohidratos (40%)", macros.getCarbohidratos()),
                new PieChart.Data("Grasas (30%)", macros.getGrasas())
        );
        macrosPieChart.setData(pieChartData);
        if (!pieChartData.isEmpty()) { 
            if(pieChartData.get(0) != null && pieChartData.get(0).getNode() != null) pieChartData.get(0).getNode().setStyle("-fx-pie-color: rgb(100, 220, 100);");
            if(pieChartData.size() > 1 && pieChartData.get(1) != null && pieChartData.get(1).getNode() != null) pieChartData.get(1).getNode().setStyle("-fx-pie-color: rgb(100, 170, 255);");
            if(pieChartData.size() > 2 && pieChartData.get(2) != null && pieChartData.get(2).getNode() != null) pieChartData.get(2).getNode().setStyle("-fx-pie-color: rgb(255, 170, 100);");
        }
    }

    @Override
    public void handleNutritionAction(ActionEvent evento) {
        this.usuarioActualLocal = SessionManager.getInstancia().getUsuarioActual(); 
        if (this.usuarioActualLocal == null) {
            System.err.println("Error crítico: No hay usuario en sesión al re-navegar a Nutrición.");
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
        activarBoton(nutritionButton);
        
        cargarPerfilExistente();
    }
}