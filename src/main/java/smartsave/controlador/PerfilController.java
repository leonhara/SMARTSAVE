package smartsave.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.net.URL;

/**
 * Controlador para la vista de Perfil de Usuario
 * Extiende BaseController para heredar funcionalidad común
 */
public class PerfilController extends BaseController {

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

    // Referencias a botones de acción
    @FXML private Button configurarPerfilButton;
    @FXML private Button exportarDatosButton;
    @FXML private Button eliminarCuentaButton;

    // Servicios
    private final UsuarioServicio usuarioServicio = new UsuarioServicio();
    private final TransaccionServicio transaccionServicio = new TransaccionServicio();

    // Variables de estado
    private Long usuarioIdActual = 1L; // Simulado, en un caso real vendría de la sesión
    private Usuario usuarioActual;

    /**
     * Inicialización específica del controlador de perfil
     * Implementación del método abstracto de BaseController
     */
    @Override
    protected void inicializarControlador() {
        // Destacar el botón de perfil como seleccionado
        activarBoton(profileButton);

        // Cargar datos del usuario
        cargarDatosUsuario();

        // Cargar datos financieros
        cargarDatosFinancieros();

        // Aplicar estilos personalizados
        aplicarEstilosComponentes();
    }

    /**
     * Aplica estilos a los componentes específicos de esta pantalla
     */
    private void aplicarEstilosComponentes() {
        // Aplicar estilos a los botones principales
        EstilosApp.aplicarEstiloBotonPrimario(cambiarFotoButton);
        EstilosApp.aplicarEstiloBotonPrimario(editarDatosButton);
        EstilosApp.aplicarEstiloBotonPrimario(cambiarModalidadButton);
        EstilosApp.aplicarEstiloBotonPrimario(configurarPerfilButton);
        EstilosApp.aplicarEstiloBotonPrimario(exportarDatosButton);

        // Estilo especial para el botón de eliminar cuenta (rojo)
        eliminarCuentaButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, rgb(255,50,50), rgb(200,50,50)); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 8px 20px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-cursor: hand;"
        );

        // Efecto hover para el botón de eliminar
        eliminarCuentaButton.setOnMouseEntered(e -> {
            eliminarCuentaButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, rgb(255,70,70), rgb(220,70,70)); " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 13px; " +
                            "-fx-padding: 8px 20px; " +
                            "-fx-background-radius: 5px; " +
                            "-fx-cursor: hand;"
            );
        });

        eliminarCuentaButton.setOnMouseExited(e -> {
            eliminarCuentaButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, rgb(255,50,50), rgb(200,50,50)); " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 13px; " +
                            "-fx-padding: 8px 20px; " +
                            "-fx-background-radius: 5px; " +
                            "-fx-cursor: hand;"
            );
        });

        // Aplicar estilos a los paneles
        estilizarPanelesDeDatos();
    }

    /**
     * Aplica estilos a los paneles de datos
     */
    private void estilizarPanelesDeDatos() {
        // Encontrar todos los paneles VBox que contengan datos
        if (nombreCompletoLabel.getParent() instanceof VBox) {
            EstilosApp.aplicarEstiloTarjeta((Pane) nombreCompletoLabel.getParent());
        }

        if (modalidadActualLabel.getParent() instanceof VBox) {
            EstilosApp.aplicarEstiloTarjeta((Pane) modalidadActualLabel.getParent());
        }

        if (balanceActualLabel.getParent() instanceof VBox) {
            EstilosApp.aplicarEstiloTarjeta((Pane) balanceActualLabel.getParent());
        }

        // Estilizar etiquetas con valores importantes
        balanceActualLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        totalAhorradoLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        ingresosMesLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        gastosMesLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Colorear según valores
        if (balanceActualLabel.getText().startsWith("€") && !balanceActualLabel.getText().equals("€0.00")) {
            double valor = Double.parseDouble(balanceActualLabel.getText().substring(1).replace(",", "."));
            balanceActualLabel.setTextFill(valor >= 0 ? Color.rgb(100, 220, 100) : Color.rgb(220, 100, 100));
        }
    }

    /**
     * Carga los datos del usuario desde el servicio
     */
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

    /**
     * Carga los datos financieros del usuario
     */
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
            balanceActualLabel.setTextFill(Color.rgb(220, 100, 100)); // Rojo
        }
    }

    /**
     * Manejador para cambiar la foto de perfil
     */
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
                navegacionServicio.mostrarAlertaInformacion("Foto actualizada",
                        "Tu foto de perfil ha sido actualizada correctamente.");
            } catch (Exception e) {
                navegacionServicio.mostrarAlertaError("Error al cargar imagen",
                        "No se pudo cargar la imagen seleccionada: " + e.getMessage());
            }
        }
    }

    /**
     * Manejador para editar datos personales
     */
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

        // Aplicar estilos a los campos
        EstilosApp.aplicarEstiloCampoTexto(nombreField);
        EstilosApp.aplicarEstiloCampoTexto(apellidosField);
        EstilosApp.aplicarEstiloCampoTexto(emailField);

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
        navegacionServicio.estilizarDialog(dialog);

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
                navegacionServicio.mostrarAlertaError("Error", "Todos los campos son obligatorios.");
                return;
            }

            if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                navegacionServicio.mostrarAlertaError("Error", "El email no tiene un formato válido.");
                return;
            }

            // Actualizar usuario
            usuarioActual.setNombre(nombre);
            usuarioActual.setApellidos(apellidos);
            usuarioActual.setEmail(email);

            if (usuarioServicio.actualizarUsuario(usuarioActual)) {
                // Recargar datos en la interfaz
                cargarDatosUsuario();
                navegacionServicio.mostrarAlertaInformacion("Datos actualizados",
                        "Tus datos personales han sido actualizados correctamente.");
            } else {
                navegacionServicio.mostrarAlertaError("Error", "No se pudieron actualizar los datos.");
            }
        });
    }

    /**
     * Manejador para cambiar la modalidad de ahorro
     */
    @FXML
    private void handleCambiarModalidadAction(ActionEvent evento) {
        // Crear diálogo para cambiar modalidad de ahorro
        ChoiceDialog<String> dialog = new ChoiceDialog<>(usuarioActual.getModalidadAhorroSeleccionada(),
                "Máximo", "Equilibrado", "Estándar");
        dialog.setTitle("Cambiar Modalidad de Ahorro");
        dialog.setHeaderText("Selecciona tu modalidad de ahorro preferida");
        dialog.setContentText("Modalidad:");

        // Estilizar diálogo
        navegacionServicio.estilizarDialog(dialog);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(modalidad -> {
            usuarioActual.setModalidadAhorroSeleccionada(modalidad);
            if (usuarioServicio.actualizarUsuario(usuarioActual)) {
                cargarDatosUsuario();
                navegacionServicio.mostrarAlertaInformacion("Modalidad actualizada",
                        "Tu modalidad de ahorro ha sido cambiada a: " + modalidad);
            } else {
                navegacionServicio.mostrarAlertaError("Error", "No se pudo cambiar la modalidad.");
            }
        });
    }

    /**
     * Manejador para configurar el perfil
     */
    @FXML
    private void handleConfigurarPerfilAction(ActionEvent evento) {
        navegacionServicio.navegarAConfiguracion(obtenerEscenarioActual());
    }

    /**
     * Manejador para exportar datos del perfil
     */
    @FXML
    private void handleExportarDatosAction(ActionEvent evento) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Datos del Perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivo JSON", "*.json"),
                new FileChooser.ExtensionFilter("Archivo CSV", "*.csv")
        );

        Stage stage = obtenerEscenarioActual();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                // Simular exportación de datos
                Thread.sleep(1000);

                // En un caso real, aquí se exportarían los datos del perfil
                navegacionServicio.mostrarAlertaInformacion("Datos exportados",
                        "Los datos de tu perfil han sido exportados correctamente a:\n" + file.getAbsolutePath());
            } catch (InterruptedException e) {
                navegacionServicio.mostrarAlertaError("Error", "Error al exportar los datos: " + e.getMessage());
            }
        }
    }

    /**
     * Manejador para eliminar la cuenta de usuario
     */
    /**
     * Manejador para eliminar la cuenta de usuario
     */
    @FXML
    private void handleEliminarCuentaAction(ActionEvent evento) {
        // Mostrar diálogo de confirmación doble
        Alert confirmacion = new Alert(Alert.AlertType.WARNING);
        confirmacion.setTitle("Eliminar Cuenta - PELIGRO");
        confirmacion.setHeaderText("¿Estás ABSOLUTAMENTE seguro?");
        confirmacion.setContentText("Esta acción ELIMINARÁ PERMANENTEMENTE tu cuenta y todos tus datos.\n" +
                "Esta acción NO SE PUEDE DESHACER.\n\n" +
                "¿Deseas continuar?");

        // Usar el método correcto para estilizar el diálogo
        DialogPane dialogPane = confirmacion.getDialogPane();
        EstilosApp.aplicarEstiloDialogPane(dialogPane);

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                // Segunda confirmación
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Confirmar eliminación");
                dialog.setHeaderText("Para confirmar la eliminación, escribe: ELIMINAR");
                dialog.setContentText("Texto de confirmación:");

                // Usar el método correcto para estilizar el diálogo
                navegacionServicio.estilizarDialog(dialog);

                Optional<String> result = dialog.showAndWait();
                result.ifPresent(texto -> {
                    if ("ELIMINAR".equals(texto)) {
                        try {
                            // Simular eliminación de cuenta
                            Thread.sleep(2000);

                            navegacionServicio.mostrarAlertaInformacion("Cuenta eliminada",
                                    "Tu cuenta ha sido eliminada correctamente.\n" +
                                            "Gracias por usar SmartSave.");

                            // Redirigir al login
                            navegacionServicio.navegarALogin(obtenerEscenarioActual());

                        } catch (InterruptedException e) {
                            navegacionServicio.mostrarAlertaError("Error", "Error al eliminar la cuenta.");
                        }
                    } else {
                        navegacionServicio.mostrarAlertaError("Texto incorrecto",
                                "El texto de confirmación no es correcto. Eliminación cancelada.");
                    }
                });
            }
        });
    }

    /**
     * Sobrescribe el método de navegación a perfil
     * Ya que estamos en la vista de perfil
     */
    @Override
    public void handleProfileAction(ActionEvent evento) {
        // Ya estamos en la vista de perfil, solo recargamos los datos
        cargarDatosUsuario();
        cargarDatosFinancieros();
        activarBoton(profileButton);
    }

    /**
     * Método auxiliar para obtener el escenario actual
     */
    private Stage obtenerEscenarioActual() {
        return (Stage) mainPane.getScene().getWindow();
    }
}