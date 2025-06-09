package smartsave.controlador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import smartsave.modelo.ListaCompra;
import smartsave.modelo.PerfilNutricional;
import smartsave.modelo.Usuario;
import smartsave.servicio.*;
import smartsave.utilidad.EstilosApp;
import smartsave.utilidad.ValidacionUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import java.util.stream.Collectors;


public class PerfilController extends BaseController {


    @FXML private Circle profilePhotoCircle;
    @FXML private ImageView profilePhoto;
    @FXML private Button cambiarFotoButton;

    @FXML private Label nombreCompletoLabel;
    @FXML private Label emailLabel;
    @FXML private Label fechaRegistroLabel;
    @FXML private Label ultimoAccesoLabel;
    @FXML private Button editarDatosButton;


    @FXML private Label modalidadActualLabel;
    @FXML private Button cambiarModalidadButton;
    @FXML private Label descripcionModalidadLabel;


    @FXML private Label balanceActualLabel;
    @FXML private Label totalAhorradoLabel;
    @FXML private Label gastosMesLabel;
    @FXML private Label ingresosMesLabel;
    @FXML private Label diasUsandoAppLabel;


    @FXML private Button configurarPerfilButton;
    @FXML private Button exportarDatosButton;
    @FXML private Button eliminarCuentaButton;


    private final UsuarioServicio usuarioServicio = new UsuarioServicio();
    private final TransaccionServicio transaccionServicio = new TransaccionServicio();
    private final PerfilNutricionalServicio perfilNutricionalServicio = new PerfilNutricionalServicio();
    private final ListaCompraServicio listaCompraServicio = new ListaCompraServicio();


    private Long usuarioIdActualLocal;
    private Usuario usuarioActualLocal;

    @Override
    protected void inicializarControlador() {

        this.usuarioActualLocal = SessionManager.getInstancia().getUsuarioActual();

        if (this.usuarioActualLocal == null) {
            System.err.println("Error crítico: No hay usuario en sesión en PerfilController.");
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

        if (profileButton != null) {
            activarBoton(profileButton);
        }

        cargarDatosUsuario();
        cargarDatosFinancieros();
        aplicarEstilosComponentes();
    }

    private void disableUIComponents() {

        if(cambiarFotoButton != null) cambiarFotoButton.setDisable(true);
        if(editarDatosButton != null) editarDatosButton.setDisable(true);
        if(cambiarModalidadButton != null) cambiarModalidadButton.setDisable(true);
        if(configurarPerfilButton != null) configurarPerfilButton.setDisable(true);
        if(exportarDatosButton != null) exportarDatosButton.setDisable(true);
        if(eliminarCuentaButton != null) eliminarCuentaButton.setDisable(true);

        if(nombreCompletoLabel != null) nombreCompletoLabel.setText("N/A");
        if(emailLabel != null) emailLabel.setText("N/A");
        if(fechaRegistroLabel != null) fechaRegistroLabel.setText("N/A");
        if(ultimoAccesoLabel != null) ultimoAccesoLabel.setText("N/A");
        if(modalidadActualLabel != null) modalidadActualLabel.setText("N/A");
        if(descripcionModalidadLabel != null) descripcionModalidadLabel.setText("N/A");
        if(balanceActualLabel != null) balanceActualLabel.setText("€0.00");
        if(totalAhorradoLabel != null) totalAhorradoLabel.setText("€0.00");
        if(gastosMesLabel != null) gastosMesLabel.setText("€0.00");
        if(ingresosMesLabel != null) ingresosMesLabel.setText("€0.00");
        if(diasUsandoAppLabel != null) diasUsandoAppLabel.setText("N/A días");
    }

    private void aplicarEstilosComponentes() {
        if(cambiarFotoButton != null) EstilosApp.aplicarEstiloBotonPrimario(cambiarFotoButton);
        if(editarDatosButton != null) EstilosApp.aplicarEstiloBotonPrimario(editarDatosButton);
        if(cambiarModalidadButton != null) EstilosApp.aplicarEstiloBotonPrimario(cambiarModalidadButton);
        if(configurarPerfilButton != null) EstilosApp.aplicarEstiloBotonPrimario(configurarPerfilButton);
        if(exportarDatosButton != null) EstilosApp.aplicarEstiloBotonPrimario(exportarDatosButton);

        if (eliminarCuentaButton != null) {
            eliminarCuentaButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, rgb(255,50,50), rgb(200,50,50)); " +
                            "-fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8px 20px; " +
                            "-fx-background-radius: 5px; -fx-cursor: hand;"
            );
            eliminarCuentaButton.setOnMouseEntered(e -> eliminarCuentaButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, rgb(255,70,70), rgb(220,70,70)); " +
                            "-fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8px 20px; " +
                            "-fx-background-radius: 5px; -fx-cursor: hand;"
            ));
            eliminarCuentaButton.setOnMouseExited(e -> eliminarCuentaButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, rgb(255,50,50), rgb(200,50,50)); " +
                            "-fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8px 20px; " +
                            "-fx-background-radius: 5px; -fx-cursor: hand;"
            ));
        }
        estilizarPanelesDeDatos();
    }

    private void estilizarPanelesDeDatos() {
        if (nombreCompletoLabel != null && nombreCompletoLabel.getParent() instanceof VBox) {
            EstilosApp.aplicarEstiloTarjeta((Pane) nombreCompletoLabel.getParent());
        }
        if (modalidadActualLabel != null && modalidadActualLabel.getParent() instanceof VBox) {
            EstilosApp.aplicarEstiloTarjeta((Pane) modalidadActualLabel.getParent());
        }
        if (balanceActualLabel != null && balanceActualLabel.getParent() instanceof VBox) {
            EstilosApp.aplicarEstiloTarjeta((Pane) balanceActualLabel.getParent());
        }

        String estiloTextoClaro = EstilosApp.toRgbString(EstilosApp.TEXTO_CLARO);
        if (balanceActualLabel != null) balanceActualLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + estiloTextoClaro + ";");
        if (totalAhorradoLabel != null) totalAhorradoLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + estiloTextoClaro + ";");
        if (ingresosMesLabel != null) ingresosMesLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + estiloTextoClaro + ";");
        if (gastosMesLabel != null) gastosMesLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + estiloTextoClaro + ";");

        if (balanceActualLabel != null && balanceActualLabel.getText() != null && balanceActualLabel.getText().startsWith("€") && !balanceActualLabel.getText().equals("€0.00")) {
            try {
                double valor = Double.parseDouble(balanceActualLabel.getText().replace("€", "").replace(",", "."));
                balanceActualLabel.setTextFill(valor >= 0 ? Color.rgb(100, 220, 100) : Color.rgb(220, 100, 100));
            } catch (NumberFormatException e) {
                System.err.println("Error al parsear el texto de balanceActualLabel: " + balanceActualLabel.getText() + " - " + e.getMessage());
            }
        }
    }

    private void cargarDatosUsuario() {
        if (usuarioActualLocal != null) {
            if (nombreCompletoLabel != null) nombreCompletoLabel.setText(usuarioActualLocal.getNombreCompleto());
            if (emailLabel != null) emailLabel.setText(usuarioActualLocal.getEmail());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            if (fechaRegistroLabel != null) fechaRegistroLabel.setText(usuarioActualLocal.getFechaRegistro() != null ? usuarioActualLocal.getFechaRegistro().format(formatter) : "N/A");

            if (ultimoAccesoLabel != null) {
                ultimoAccesoLabel.setText(usuarioActualLocal.getUltimoLogin() != null ? usuarioActualLocal.getUltimoLogin().format(formatter) : "Nunca");
            }

            if (modalidadActualLabel != null) modalidadActualLabel.setText(usuarioActualLocal.getModalidadAhorroSeleccionada() != null ? usuarioActualLocal.getModalidadAhorroSeleccionada() : "No definida");

            String descModalidad = "Modalidad no definida.";
            if (usuarioActualLocal.getModalidadAhorroSeleccionada() != null) {
                switch (usuarioActualLocal.getModalidadAhorroSeleccionada()) {
                    case "Máximo": descModalidad = "Maximiza tu ahorro priorizando los precios más bajos."; break;
                    case "Equilibrado": descModalidad = "Balance perfecto entre ahorro y calidad nutricional."; break;
                    case "Estándar": descModalidad = "Prioriza la calidad nutricional manteniendo un presupuesto razonable."; break;
                }
            }
            if (descripcionModalidadLabel != null) descripcionModalidadLabel.setText(descModalidad);
        } else {

            disableUIComponents();
        }
    }

    private void cargarDatosFinancieros() {
        if (usuarioActualLocal == null || usuarioActualLocal.getFechaRegistro() == null || this.usuarioIdActualLocal == null) {
            if (diasUsandoAppLabel != null) diasUsandoAppLabel.setText("N/A días");
            if (balanceActualLabel != null) balanceActualLabel.setText("€0.00");
            if (totalAhorradoLabel != null) totalAhorradoLabel.setText("€0.00");
            if (gastosMesLabel != null) gastosMesLabel.setText("€0.00");
            if (ingresosMesLabel != null) ingresosMesLabel.setText("€0.00");
            return;
        }

        long diasUsando = LocalDate.now().toEpochDay() - usuarioActualLocal.getFechaRegistro().toEpochDay();
        if (diasUsandoAppLabel != null) diasUsandoAppLabel.setText(diasUsando + " días");

        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        double ingresosMes = transaccionServicio.obtenerTotalIngresos(this.usuarioIdActualLocal, inicioMes, LocalDate.now());
        double gastosMes = transaccionServicio.obtenerTotalGastos(this.usuarioIdActualLocal, inicioMes, LocalDate.now());
        double balance = transaccionServicio.obtenerBalance(this.usuarioIdActualLocal, null, LocalDate.now());
        double totalAhorrado = balance * 0.3;

        if (balanceActualLabel != null) balanceActualLabel.setText(String.format("€%.2f", balance));
        if (totalAhorradoLabel != null) totalAhorradoLabel.setText(String.format("€%.2f", totalAhorrado));
        if (gastosMesLabel != null) gastosMesLabel.setText(String.format("€%.2f", gastosMes));
        if (ingresosMesLabel != null) ingresosMesLabel.setText(String.format("€%.2f", ingresosMes));

        if (balanceActualLabel != null) {
            balanceActualLabel.setTextFill(balance >= 0 ? Color.rgb(100, 220, 100) : Color.rgb(220, 100, 100));
        }
    }

    @FXML
    private void handleCambiarFotoAction(ActionEvent evento) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar foto de perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos de imagen", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp")
        );

        Stage stage = obtenerEscenarioActual();
        if (stage == null) {
            navegacionServicio.mostrarAlertaError("Error de Interfaz", "No se pudo obtener la ventana principal para el selector de archivos.");
            return;
        }
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                Image imagen = new Image(file.toURI().toString());
                if (profilePhoto != null) profilePhoto.setImage(imagen);
                navegacionServicio.mostrarAlertaInformacion("Foto actualizada",
                        "Tu foto de perfil ha sido actualizada correctamente (simulado).");
            } catch (Exception e) {
                navegacionServicio.mostrarAlertaError("Error al cargar imagen",
                        "No se pudo cargar la imagen seleccionada: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleEditarDatosAction(ActionEvent evento) {
        if (usuarioActualLocal == null) {
            navegacionServicio.mostrarAlertaError("Error de Usuario", "No hay datos de usuario para editar.");
            return;
        }
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Editar Datos Personales");
        dialog.setHeaderText("Modifica tus datos personales");

        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nombreField = new TextField(usuarioActualLocal.getNombre());
        nombreField.setPromptText("Nombre");
        TextField apellidosField = new TextField(usuarioActualLocal.getApellidos());
        apellidosField.setPromptText("Apellidos");
        TextField emailFieldDialog = new TextField(usuarioActualLocal.getEmail());
        emailFieldDialog.setPromptText("Email");

        EstilosApp.aplicarEstiloCampoTexto(nombreField);
        EstilosApp.aplicarEstiloCampoTexto(apellidosField);
        EstilosApp.aplicarEstiloCampoTexto(emailFieldDialog);

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Apellidos:"), 0, 1);
        grid.add(apellidosField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailFieldDialog, 1, 2);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(nombreField::requestFocus);

        navegacionServicio.estilizarDialog(dialog);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                return new String[]{nombreField.getText(), apellidosField.getText(), emailFieldDialog.getText()};
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String nombre = result[0];
            String apellidos = result[1];
            String email = result[2];

            if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty()) {
                navegacionServicio.mostrarAlertaError("Campos Vacíos", "Todos los campos son obligatorios.");
                return;
            }
            if (!ValidacionUtil.esEmailValido(email)) {
                navegacionServicio.mostrarAlertaError("Email Inválido", "El email no tiene un formato válido.");
                return;
            }

            usuarioActualLocal.setNombre(nombre);
            usuarioActualLocal.setApellidos(apellidos);
            usuarioActualLocal.setEmail(email);

            if (usuarioServicio.actualizarUsuario(usuarioActualLocal)) {
                cargarDatosUsuario();
                navegacionServicio.mostrarAlertaInformacion("Datos Actualizados",
                        "Tus datos personales han sido actualizados correctamente.");
            } else {
                navegacionServicio.mostrarAlertaError("Error al Actualizar", "No se pudieron actualizar los datos del usuario.");
            }
        });
    }

    @FXML
    private void handleCambiarModalidadAction(ActionEvent evento) {
        if (usuarioActualLocal == null) {
            navegacionServicio.mostrarAlertaError("Error de Usuario", "No hay datos de usuario para modificar la modalidad.");
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                usuarioActualLocal.getModalidadAhorroSeleccionada() != null ? usuarioActualLocal.getModalidadAhorroSeleccionada() : "Equilibrado",
                "Máximo", "Equilibrado", "Estándar");
        dialog.setTitle("Cambiar Modalidad de Ahorro");
        dialog.setHeaderText("Selecciona tu modalidad de ahorro preferida");
        dialog.setContentText("Modalidad:");

        navegacionServicio.estilizarDialog(dialog);
        dialog.showAndWait().ifPresent(modalidad -> {
            usuarioActualLocal.setModalidadAhorroSeleccionada(modalidad);
            if (usuarioServicio.actualizarUsuario(usuarioActualLocal)) {
                cargarDatosUsuario();
                navegacionServicio.mostrarAlertaInformacion("Modalidad Actualizada",
                        "Tu modalidad de ahorro ha sido cambiada a: " + modalidad);
            } else {
                navegacionServicio.mostrarAlertaError("Error al Actualizar", "No se pudo cambiar la modalidad de ahorro.");
            }
        });
    }

    @FXML
    private void handleConfigurarPerfilAction(ActionEvent evento) {
        Stage stage = obtenerEscenarioActual();
        if (stage != null) {
            navegacionServicio.navegarAConfiguracion(stage);
        } else {
            navegacionServicio.mostrarAlertaError("Error de Navegación", "No se pudo obtener la ventana actual para navegar a configuración.");
        }
    }

    @FXML
    private void handleExportarDatosAction(ActionEvent evento) {
        if (usuarioActualLocal == null || usuarioIdActualLocal == null) {
            navegacionServicio.mostrarAlertaError("Exportación Fallida", "No hay datos de usuario cargados para exportar.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Datos del Perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivo JSON", "*.json")
        );
        String nombreBase = (usuarioActualLocal.getNombre() != null && !usuarioActualLocal.getNombre().isEmpty()) ?
                usuarioActualLocal.getNombre().toLowerCase() : "usuario";
        String nombreArchivoSugerido = "datos_perfil_" + nombreBase.replace(" ", "_") + ".json";
        fileChooser.setInitialFileName(nombreArchivoSugerido);

        Stage stage = obtenerEscenarioActual();
        if (stage == null) {
            navegacionServicio.mostrarAlertaError("Error de Interfaz", "No se pudo obtener la ventana principal para el selector de archivos.");
            return;
        }
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                Map<String, Object> datosExportar = new HashMap<>();
                Map<String, Object> infoUsuario = new HashMap<>();
                infoUsuario.put("idUsuario", usuarioActualLocal.getId());
                infoUsuario.put("nombreCompleto", usuarioActualLocal.getNombreCompleto());
                infoUsuario.put("email", usuarioActualLocal.getEmail());
                infoUsuario.put("fechaRegistro", usuarioActualLocal.getFechaRegistro() != null ? usuarioActualLocal.getFechaRegistro().toString() : "N/A");
                infoUsuario.put("ultimoLogin", usuarioActualLocal.getUltimoLogin() != null ? usuarioActualLocal.getUltimoLogin().toString() : "N/A");
                infoUsuario.put("modalidadAhorroSeleccionada", usuarioActualLocal.getModalidadAhorroSeleccionada() != null ? usuarioActualLocal.getModalidadAhorroSeleccionada() : "No definida");
                datosExportar.put("informacionUsuario", infoUsuario);

                Map<String, Object> resumenFinanciero = new HashMap<>();
                LocalDate hoy = LocalDate.now();
                resumenFinanciero.put("totalTransacciones", transaccionServicio.obtenerTransaccionesPorUsuario(this.usuarioIdActualLocal).size());
                resumenFinanciero.put("totalIngresosGenerales", String.format("%.2f", transaccionServicio.obtenerTotalIngresos(this.usuarioIdActualLocal, null, hoy)));
                resumenFinanciero.put("totalGastosGenerales", String.format("%.2f", transaccionServicio.obtenerTotalGastos(this.usuarioIdActualLocal, null, hoy)));
                resumenFinanciero.put("balanceActualGeneral", String.format("%.2f", transaccionServicio.obtenerBalance(this.usuarioIdActualLocal, null, hoy)));
                datosExportar.put("resumenFinanciero", resumenFinanciero);

                if (perfilNutricionalServicio.tienePerfil(this.usuarioIdActualLocal)) {
                    PerfilNutricional perfil = perfilNutricionalServicio.obtenerPerfilPorUsuario(this.usuarioIdActualLocal);
                    if (perfil != null) {
                        Map<String, Object> resumenNutricional = new HashMap<>();
                        resumenNutricional.put("imc", String.format("%.1f", perfil.getImc()));
                        resumenNutricional.put("categoriaImc", perfil.getCategoriaIMC());
                        resumenNutricional.put("caloriasDiariasRecomendadas", perfil.getCaloriasDiarias());
                        PerfilNutricional.MacronutrientesDiarios macros = perfil.getMacronutrientesDiarios();
                        resumenNutricional.put("proteinasRecomendadasGramos", macros.getProteinas());
                        resumenNutricional.put("carbohidratosRecomendadosGramos", macros.getCarbohidratos());
                        resumenNutricional.put("grasasRecomendadasGramos", macros.getGrasas());
                        resumenNutricional.put("restricciones", perfil.getRestricciones().isEmpty() ? "Ninguna" : String.join(", ", perfil.getRestricciones()));
                        datosExportar.put("resumenNutricional", resumenNutricional);
                    } else {
                        datosExportar.put("resumenNutricional", "El perfil nutricional existe pero no se pudo cargar para la exportación.");
                    }
                } else {
                    datosExportar.put("resumenNutricional", "No existe perfil nutricional creado para este usuario.");
                }

                List<ListaCompra> listasActivas = listaCompraServicio.obtenerListasActivas(this.usuarioIdActualLocal);
                datosExportar.put("numeroListasCompraActivas", listasActivas.size());
                if (!listasActivas.isEmpty()) {
                    datosExportar.put("nombresListasActivas", listasActivas.stream().map(ListaCompra::getNombre).collect(Collectors.toList()));
                }

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                try (FileWriter fileWriter = new FileWriter(file)) {
                    objectMapper.writeValue(fileWriter, datosExportar);
                }
                navegacionServicio.mostrarAlertaInformacion("Datos Exportados",
                        "Los datos de tu perfil han sido exportados correctamente a:\n" + file.getAbsolutePath());

            } catch (IOException e) {
                navegacionServicio.mostrarAlertaError("Error al Exportar",
                        "Ocurrió un error al escribir el archivo: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                navegacionServicio.mostrarAlertaError("Error Inesperado",
                        "Ocurrió un error inesperado durante la exportación: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleEliminarCuentaAction(ActionEvent evento) {
        if (usuarioActualLocal == null) {
            navegacionServicio.mostrarAlertaError("Error de Usuario", "No hay un usuario activo para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.WARNING);
        confirmacion.setTitle("Eliminar Cuenta - ¡PELIGRO!");
        confirmacion.setHeaderText("¿Estás ABSOLUTAMENTE seguro de que deseas eliminar tu cuenta?");
        confirmacion.setContentText("Esta acción ELIMINARÁ PERMANENTEMENTE tu cuenta y todos tus datos asociados (transacciones, perfiles, listas de compra, etc.).\n" +
                "Esta acción NO SE PUEDE DESHACER.\n\n" +
                "¿Deseas continuar con la eliminación?");

        DialogPane dialogPane = confirmacion.getDialogPane();
        EstilosApp.aplicarEstiloDialogPane(dialogPane);

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Confirmar Eliminación Definitiva");
                dialog.setHeaderText("Para confirmar la eliminación PERMANENTE de tu cuenta, escribe la palabra 'ELIMINAR' en el campo de abajo.");
                dialog.setContentText("Texto de confirmación:");

                navegacionServicio.estilizarDialog(dialog);

                dialog.showAndWait().ifPresent(texto -> {
                    if ("ELIMINAR".equals(texto.trim())) {
                        boolean eliminado = usuarioServicio.eliminarUsuario(usuarioActualLocal.getEmail());
                        if (eliminado) {
                            navegacionServicio.mostrarAlertaInformacion("Cuenta Eliminada",
                                    "Tu cuenta ha sido eliminada permanentemente.\n" +
                                            "Gracias por usar SmartSave. Esperamos verte de nuevo.");
                            Stage stageActual = obtenerEscenarioActual();
                            if (stageActual != null) {
                                SessionManager.getInstancia().cerrarSesion();
                                navegacionServicio.navegarALogin(stageActual);
                            }
                        } else {
                            navegacionServicio.mostrarAlertaError("Error al Eliminar", "No se pudo eliminar la cuenta. Es posible que el usuario ya no exista o haya ocurrido un problema con la base de datos. Por favor, contacta a soporte si el problema persiste.");
                        }
                    } else {
                        navegacionServicio.mostrarAlertaError("Confirmación Fallida",
                                "El texto de confirmación no es correcto. La eliminación de la cuenta ha sido cancelada.");
                    }
                });
            } else {
                navegacionServicio.mostrarAlertaInformacion("Eliminación Cancelada", "La eliminación de la cuenta ha sido cancelada.");
            }
        });
    }

    @Override
    public void handleProfileAction(ActionEvent evento) {

        this.usuarioActualLocal = SessionManager.getInstancia().getUsuarioActual();
        if (this.usuarioActualLocal != null) {
            this.usuarioIdActualLocal = this.usuarioActualLocal.getId();

            cargarDatosUsuario();
            cargarDatosFinancieros();
        } else {
            System.err.println("PerfilController: Se intentó recargar el perfil, pero el usuario en sesión era null. Reintentando inicialización completa.");
            inicializarControlador();
        }
        if (profileButton != null) {
            activarBoton(profileButton);
        }
    }

    private Stage obtenerEscenarioActual() {
        if (mainPane != null && mainPane.getScene() != null && mainPane.getScene().getWindow() instanceof Stage) {
            return (Stage) mainPane.getScene().getWindow();
        }
        Node[] nodosPotenciales = {exportarDatosButton, cambiarFotoButton, editarDatosButton, cambiarModalidadButton, configurarPerfilButton, eliminarCuentaButton};
        for (Node nodo : nodosPotenciales) {
            if (nodo != null && nodo.getScene() != null && nodo.getScene().getWindow() instanceof Stage) {
                return (Stage) nodo.getScene().getWindow();
            }
        }
        System.err.println("Advertencia: No se pudo obtener el Stage actual en PerfilController. La funcionalidad que depende del Stage (ej. FileChooser) podría fallar.");
        return null;
    }
}