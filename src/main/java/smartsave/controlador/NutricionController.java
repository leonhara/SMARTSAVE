package smartsave.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import smartsave.modelo.PerfilNutricional;
import smartsave.servicio.PerfilNutricionalServicio;
import smartsave.utilidad.EstilosApp;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.net.URL;

/**
 * Controlador para la vista de Perfil Nutricional
 * Extiende BaseController para heredar funcionalidad común
 */
public class NutricionController extends BaseController {

    // Referencias a elementos del formulario
    @FXML private TextField edadField;
    @FXML private TextField pesoField;
    @FXML private TextField alturaField;
    @FXML private RadioButton sexoMRadio;
    @FXML private RadioButton sexoFRadio;
    @FXML private ToggleGroup sexoGroup;
    @FXML private ComboBox<String> actividadComboBox;
    @FXML private VBox restriccionesPane;
    @FXML private Button guardarPerfilButton;

    // Referencias a elementos de resumen
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

    // Servicios
    private final PerfilNutricionalServicio perfilServicio = new PerfilNutricionalServicio();

    // Variables de estado
    private Long usuarioIdActual = 1L; // Simulado, en un caso real vendría de la sesión
    private List<CheckBox> restriccionesCheckboxes = new ArrayList<>();
    private PerfilNutricional perfilActual = null;

    /**
     * Inicialización específica del controlador de nutrición
     * Implementación del método abstracto de BaseController
     */
    @Override
    protected void inicializarControlador() {
        // Destacar el botón de nutrición como seleccionado
        activarBoton(nutritionButton);

        // Inicializar elementos del formulario
        inicializarFormulario();

        // Cargar datos del perfil si existe
        cargarPerfilExistente();

        // Aplicar estilos personalizados
        aplicarEstilosComponentes();
    }

    /**
     * Aplica estilos a los componentes específicos de esta pantalla
     */
    private void aplicarEstilosComponentes() {
        // Aplicar estilos a los campos de texto
        EstilosApp.aplicarEstiloCampoTexto(edadField);
        EstilosApp.aplicarEstiloCampoTexto(pesoField);
        EstilosApp.aplicarEstiloCampoTexto(alturaField);

        // Aplicar estilos a TextArea
        EstilosApp.aplicarEstiloTextArea(recomendacionesArea);

        // Aplicar estilos a los botones
        EstilosApp.aplicarEstiloBotonPrimario(guardarPerfilButton);

        // Aplicar estilos a ComboBox
        EstilosApp.aplicarEstiloComboBox(actividadComboBox);

        // Aplicar estilos al gráfico
        EstilosApp.aplicarEstiloGrafico(macrosPieChart);

        // Aplicar estilos a los paneles
        EstilosApp.aplicarEstiloTarjeta(restriccionesPane);

        // Estilizar radio buttons
        estilizarRadioButtons();
    }

    /**
     * Aplica estilos a los radio buttons
     */
    private void estilizarRadioButtons() {
        // Estilizar radio buttons para el sexo
        String radioButtonStyle =
                "-fx-text-fill: white; " +
                        "-fx-background-color: rgba(40, 40, 50, 0.7); " +
                        "-fx-background-radius: 5px; " +
                        "-fx-padding: 5px 10px;";

        sexoMRadio.setStyle(radioButtonStyle);
        sexoFRadio.setStyle(radioButtonStyle);

        // Efectos de hover
        sexoMRadio.setOnMouseEntered(e ->
                sexoMRadio.setStyle(radioButtonStyle + "-fx-background-color: rgba(60, 60, 80, 0.7);"));
        sexoMRadio.setOnMouseExited(e ->
                sexoMRadio.setStyle(radioButtonStyle));

        sexoFRadio.setOnMouseEntered(e ->
                sexoFRadio.setStyle(radioButtonStyle + "-fx-background-color: rgba(60, 60, 80, 0.7);"));
        sexoFRadio.setOnMouseExited(e ->
                sexoFRadio.setStyle(radioButtonStyle));
    }

    /**
     * Inicializa los elementos del formulario
     */
    private void inicializarFormulario() {
        // Configurar validación de campos numéricos
        configurarValidacionCampos();

        // Cargar niveles de actividad
        actividadComboBox.setItems(FXCollections.observableArrayList(
                perfilServicio.obtenerNivelesActividad()
        ));

        // Crear checkboxes para restricciones alimentarias
        List<String> restricciones = perfilServicio.obtenerRestriccionesAlimentarias();

        restriccionesPane.getChildren().clear();
        restriccionesCheckboxes.clear();

        // Crear checkboxes por fila (2 por fila)
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
            filaActual.getChildren().add(checkbox);

            contador++;
        }
    }

    /**
     * Configura la validación de campos numéricos
     */
    private void configurarValidacionCampos() {
        // Permitir solo números en el campo de edad
        edadField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                edadField.setText(oldValue);
            }
        });

        // Permitir números y punto decimal en el campo de peso
        pesoField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                pesoField.setText(oldValue);
            }
        });

        // Permitir números y punto decimal en el campo de altura
        alturaField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                alturaField.setText(oldValue);
            }
        });
    }

    /**
     * Carga los datos del perfil existente si hay uno
     */
    private void cargarPerfilExistente() {
        if (perfilServicio.tienePerfil(usuarioIdActual)) {
            perfilActual = perfilServicio.obtenerPerfilPorUsuario(usuarioIdActual);

            // Cargar datos en el formulario
            edadField.setText(String.valueOf(perfilActual.getEdad()));
            pesoField.setText(String.format("%.1f", perfilActual.getPeso()));
            alturaField.setText(String.format("%.1f", perfilActual.getAltura()));

            if ("M".equals(perfilActual.getSexo())) {
                sexoMRadio.setSelected(true);
            } else {
                sexoFRadio.setSelected(true);
            }

            actividadComboBox.setValue(perfilActual.getNivelActividad());

            // Marcar restricciones
            for (CheckBox checkbox : restriccionesCheckboxes) {
                checkbox.setSelected(perfilActual.getRestricciones().contains(checkbox.getText()));
            }

            // Actualizar resumen
            actualizarResumen(perfilActual);
        }
    }

    /**
     * Guarda el perfil nutricional del usuario
     */
    @FXML
    private void handleGuardarPerfil(ActionEvent event) {
        // Validar campos
        if (edadField.getText().trim().isEmpty() ||
                pesoField.getText().trim().isEmpty() ||
                alturaField.getText().trim().isEmpty() ||
                actividadComboBox.getValue() == null) {

            navegacionServicio.mostrarAlertaError("Campos incompletos", "Por favor, completa todos los campos obligatorios.");
            return;
        }

        try {
            // Obtener valores del formulario
            int edad = Integer.parseInt(edadField.getText().trim());
            double peso = Double.parseDouble(pesoField.getText().trim());
            double altura = Double.parseDouble(alturaField.getText().trim());
            String sexo = sexoMRadio.isSelected() ? "M" : "F";
            String nivelActividad = actividadComboBox.getValue();

            // Validar valores
            if (edad < 12 || edad > 120) {
                navegacionServicio.mostrarAlertaError("Valor incorrecto", "La edad debe estar entre 12 y 120 años.");
                return;
            }

            if (peso < 30 || peso > 300) {
                navegacionServicio.mostrarAlertaError("Valor incorrecto", "El peso debe estar entre 30 y 300 kg.");
                return;
            }

            if (altura < 100 || altura > 250) {
                navegacionServicio.mostrarAlertaError("Valor incorrecto", "La altura debe estar entre 100 y 250 cm.");
                return;
            }

            // Crear o actualizar perfil
            PerfilNutricional perfil;
            if (perfilActual != null) {
                perfil = perfilActual;
                perfil.setEdad(edad);
                perfil.setPeso(peso);
                perfil.setAltura(altura);
                perfil.setSexo(sexo);
                perfil.setNivelActividad(nivelActividad);
                perfil.getRestricciones().clear(); // Limpiar restricciones existentes
            } else {
                perfil = new PerfilNutricional(usuarioIdActual, edad, peso, altura, sexo, nivelActividad);
            }

            // Agregar restricciones seleccionadas
            for (CheckBox checkbox : restriccionesCheckboxes) {
                if (checkbox.isSelected()) {
                    perfil.agregarRestriccion(checkbox.getText());
                }
            }

            // Guardar perfil
            perfilActual = perfilServicio.guardarPerfil(perfil);

            // Actualizar resumen
            actualizarResumen(perfilActual);

            // Mostrar mensaje de éxito
            navegacionServicio.mostrarAlertaInformacion("Perfil guardado", "Tu perfil nutricional ha sido guardado correctamente.");

        } catch (NumberFormatException e) {
            navegacionServicio.mostrarAlertaError("Formato incorrecto", "Por favor, ingresa valores numéricos válidos.");
        }
    }

    /**
     * Actualiza el resumen con los datos del perfil
     */
    private void actualizarResumen(PerfilNutricional perfil) {
        // Actualizar valores de IMC
        double imc = perfil.getImc();
        imcValueLabel.setText(String.format("%.1f", imc));
        String categoriaIMC = perfil.getCategoriaIMC();
        imcCategoryLabel.setText(categoriaIMC);

        // Colorear según categoría IMC
        switch (categoriaIMC) {
            case "Bajo peso":
                imcCategoryLabel.setTextFill(Color.rgb(255, 200, 0));
                break;
            case "Normal":
                imcCategoryLabel.setTextFill(Color.rgb(100, 220, 100));
                break;
            case "Sobrepeso":
                imcCategoryLabel.setTextFill(Color.rgb(255, 150, 0));
                break;
            case "Obesidad":
                imcCategoryLabel.setTextFill(Color.rgb(255, 80, 80));
                break;
        }

        // Actualizar calorías
        caloriasLabel.setText(perfil.getCaloriasDiarias() + " kcal");

        // Actualizar macronutrientes
        PerfilNutricional.MacronutrientesDiarios macros = perfil.getMacronutrientesDiarios();
        proteinasLabel.setText(macros.getProteinas() + " g");
        carbosLabel.setText(macros.getCarbohidratos() + " g");
        grasasLabel.setText(macros.getGrasas() + " g");

        // Actualizar puntuación
        int puntuacion = perfilServicio.calcularPuntuacionNutricional(perfil);
        puntuacionLabel.setText(puntuacion + "/100");
        puntuacionProgress.setProgress(puntuacion / 100.0);

        // Colorear puntuación según valor
        if (puntuacion >= 80) {
            puntuacionLabel.setTextFill(Color.rgb(100, 220, 100)); // Verde
        } else if (puntuacion >= 60) {
            puntuacionLabel.setTextFill(Color.rgb(200, 220, 100)); // Verde-amarillo
        } else if (puntuacion >= 40) {
            puntuacionLabel.setTextFill(Color.rgb(255, 200, 0)); // Amarillo
        } else {
            puntuacionLabel.setTextFill(Color.rgb(255, 100, 100)); // Rojo
        }

        // Actualizar recomendaciones
        recomendacionesArea.setText(perfilServicio.generarRecomendacionAlimentaria(perfil));

        // Actualizar gráfico de macronutrientes
        actualizarGraficoMacros(macros);
    }

    /**
     * Actualiza el gráfico de macronutrientes
     */
    private void actualizarGraficoMacros(PerfilNutricional.MacronutrientesDiarios macros) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Proteínas (30%)", macros.getProteinas()),
                new PieChart.Data("Carbohidratos (40%)", macros.getCarbohidratos()),
                new PieChart.Data("Grasas (30%)", macros.getGrasas())
        );

        macrosPieChart.setData(pieChartData);

        // Aplicar colores a las secciones
        pieChartData.get(0).getNode().setStyle("-fx-pie-color: rgb(100, 220, 100);"); // Verde para proteínas
        pieChartData.get(1).getNode().setStyle("-fx-pie-color: rgb(100, 170, 255);"); // Azul para carbohidratos
        pieChartData.get(2).getNode().setStyle("-fx-pie-color: rgb(255, 170, 100);"); // Naranja para grasas
    }

    /**
     * Sobrescribe el método de navegación a nutrición
     * Ya que estamos en la vista de nutrición
     */
    @Override
    public void handleNutritionAction(ActionEvent evento) {
        // Ya estamos en la vista de nutrición, solo activamos el botón
        activarBoton(nutritionButton);
    }
}