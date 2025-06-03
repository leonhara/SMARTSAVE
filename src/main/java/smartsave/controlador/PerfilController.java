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
import smartsave.servicio.ListaCompraServicio;
import smartsave.servicio.UsuarioServicio;
import smartsave.servicio.TransaccionServicio;
import smartsave.servicio.PerfilNutricionalServicio;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador para la vista de Perfil de Usuario.
 * Gestiona la presentación y modificación de los datos del perfil del usuario,
 * incluyendo información personal, modalidad de ahorro, resumen financiero y acciones como
 * exportar datos o eliminar la cuenta.
 * Extiende BaseController para heredar funcionalidad común de navegación y estilos.
 */
public class PerfilController extends BaseController {

    //Estos son los componentes que se sacan del fxml asociado
    @FXML private Circle profilePhotoCircle;
    @FXML private ImageView profilePhoto;
    @FXML private Button cambiarFotoButton;

    @FXML private Label nombreCompletoLabel;
    @FXML private Label emailLabel;
    @FXML private Label fechaRegistroLabel;
    @FXML private Label ultimoAccesoLabel;
    @FXML private Button editarDatosButton;

    // Componentes FXML para la modalidad de ahorro
    @FXML private Label modalidadActualLabel;    // Etiqueta para mostrar la modalidad de ahorro actual
    @FXML private Button cambiarModalidadButton; // Botón para cambiar la modalidad de ahorro
    @FXML private Label descripcionModalidadLabel; // Etiqueta para la descripción de la modalidad

    // Componentes FXML para el resumen financiero
    @FXML private Label balanceActualLabel;  // Etiqueta para el balance financiero actual
    @FXML private Label totalAhorradoLabel;  // Etiqueta para el total ahorrado (simulado)
    @FXML private Label gastosMesLabel;      // Etiqueta para los gastos del mes actual
    @FXML private Label ingresosMesLabel;    // Etiqueta para los ingresos del mes actual
    @FXML private Label diasUsandoAppLabel;  // Etiqueta para los días usando la aplicación

    // Componentes FXML para acciones del perfil
    @FXML private Button configurarPerfilButton; // Botón para ir a la configuración general
    @FXML private Button exportarDatosButton;    // Botón para exportar los datos del perfil
    @FXML private Button eliminarCuentaButton;   // Botón para eliminar la cuenta del usuario

    // Servicios necesarios para la lógica del controlador
    private final UsuarioServicio usuarioServicio = new UsuarioServicio();
    private final TransaccionServicio transaccionServicio = new TransaccionServicio();
    private final PerfilNutricionalServicio perfilNutricionalServicio = new PerfilNutricionalServicio();
    private final ListaCompraServicio listaCompraServicio = new ListaCompraServicio();

    // Variables de estado para el usuario actual
    private Long usuarioIdActual; // ID del usuario actualmente logueado
    private Usuario usuarioActual;  // Objeto Usuario del usuario actualmente logueado

    /**
     * Método de inicialización del controlador.
     * Se ejecuta cuando se carga la vista FXML asociada.
     * Establece el ID del usuario actual (simulado para este ejemplo), carga los datos
     * del usuario y financieros, activa el botón de navegación correspondiente y aplica estilos.
     */
    @Override
    protected void inicializarControlador() {
        // IMPORTANTE: En una aplicación real, el ID del usuario vendría de un sistema de sesión.
        // Para este ejemplo, se usa un ID fijo (1L).
        // Asegúrate de que un usuario con este ID exista en tu base de datos para que las pruebas funcionen.
        this.usuarioIdActual = 1L; // ID de usuario de ejemplo. ¡CAMBIAR EN PRODUCCIÓN!

        if (this.usuarioIdActual == null) {
            System.err.println("Error crítico: usuarioIdActual no pudo ser establecido en PerfilController.");
            if (navegacionServicio != null) {
                navegacionServicio.mostrarAlertaError("Error de Sesión", "No se pudo identificar al usuario. Por favor, inicie sesión de nuevo.");
            }
            // Considerar deshabilitar la interfaz o redirigir al login si no hay usuario.
            return;
        }

        // Cargar el objeto Usuario completo usando el ID.
        this.usuarioActual = usuarioServicio.obtenerUsuarioPorId(this.usuarioIdActual);

        if (this.usuarioActual == null) {
            System.err.println("Error crítico: No se pudo cargar el usuario con ID: " + this.usuarioIdActual + " en PerfilController.");
            if (navegacionServicio != null) {
                navegacionServicio.mostrarAlertaError("Error de Usuario", "No se pudieron cargar los datos del usuario. ID: " + this.usuarioIdActual);
            }
            // Considerar deshabilitar la interfaz o redirigir al login.
            return;
        }

        // Activar el botón de perfil en el menú de navegación.
        if (profileButton != null) { // Verificar que el botón FXML esté inyectado.
            activarBoton(profileButton);
        }

        // Cargar y mostrar los datos del usuario y financieros en la interfaz.
        cargarDatosUsuario();
        cargarDatosFinancieros();

        // Aplicar estilos visuales a los componentes de esta pantalla.
        aplicarEstilosComponentes();
    }

    /**
     * Aplica estilos visuales personalizados a los componentes de la pantalla de perfil.
     * Utiliza la clase EstilosApp para mantener la consistencia visual.
     */
    private void aplicarEstilosComponentes() {
        // Aplicar estilo primario a los botones de acción principales.
        // Se verifica la nulidad por si algún botón no estuviera presente en el FXML.
        if(cambiarFotoButton != null) EstilosApp.aplicarEstiloBotonPrimario(cambiarFotoButton);
        if(editarDatosButton != null) EstilosApp.aplicarEstiloBotonPrimario(editarDatosButton);
        if(cambiarModalidadButton != null) EstilosApp.aplicarEstiloBotonPrimario(cambiarModalidadButton);
        if(configurarPerfilButton != null) EstilosApp.aplicarEstiloBotonPrimario(configurarPerfilButton);
        if(exportarDatosButton != null) EstilosApp.aplicarEstiloBotonPrimario(exportarDatosButton);

        // Aplicar estilo especial (rojo) al botón de eliminar cuenta.
        if (eliminarCuentaButton != null) {
            eliminarCuentaButton.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, rgb(255,50,50), rgb(200,50,50)); " +
                            "-fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8px 20px; " +
                            "-fx-background-radius: 5px; -fx-cursor: hand;"
            );
            // Efecto hover para el botón de eliminar.
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
        // Aplicar estilos de tarjeta a los paneles de datos.
        estilizarPanelesDeDatos();
    }

    /**
     * Aplica un estilo de "tarjeta" a los paneles que contienen grupos de información.
     */
    private void estilizarPanelesDeDatos() {
        // Aplicar estilo de tarjeta a los VBox que son padres de las etiquetas principales.
        if (nombreCompletoLabel != null && nombreCompletoLabel.getParent() instanceof VBox) {
            EstilosApp.aplicarEstiloTarjeta((Pane) nombreCompletoLabel.getParent());
        }
        if (modalidadActualLabel != null && modalidadActualLabel.getParent() instanceof VBox) {
            EstilosApp.aplicarEstiloTarjeta((Pane) modalidadActualLabel.getParent());
        }
        if (balanceActualLabel != null && balanceActualLabel.getParent() instanceof VBox) {
            EstilosApp.aplicarEstiloTarjeta((Pane) balanceActualLabel.getParent());
        }

        // Estilo para etiquetas de valores financieros importantes.
        String estiloTextoClaro = EstilosApp.toRgbString(EstilosApp.TEXTO_CLARO); // Usar color de texto definido.
        if (balanceActualLabel != null) balanceActualLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + estiloTextoClaro + ";");
        if (totalAhorradoLabel != null) totalAhorradoLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + estiloTextoClaro + ";");
        if (ingresosMesLabel != null) ingresosMesLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + estiloTextoClaro + ";");
        if (gastosMesLabel != null) gastosMesLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + estiloTextoClaro + ";");

        // Colorear el balance actual (verde para positivo, rojo para negativo).
        if (balanceActualLabel != null && balanceActualLabel.getText() != null && balanceActualLabel.getText().startsWith("€") && !balanceActualLabel.getText().equals("€0.00")) {
            try {
                // Parsear el valor numérico del texto de la etiqueta.
                double valor = Double.parseDouble(balanceActualLabel.getText().replace("€", "").replace(",", "."));
                balanceActualLabel.setTextFill(valor >= 0 ? Color.rgb(100, 220, 100) : Color.rgb(220, 100, 100));
            } catch (NumberFormatException e) {
                // Registrar error si el texto no es un número parseable.
                System.err.println("Error al parsear el texto de balanceActualLabel: " + balanceActualLabel.getText() + " - " + e.getMessage());
            }
        }
    }

    /**
     * Carga los datos del usuario actual (nombre, email, fechas, modalidad) y los muestra en la interfaz.
     * Asume que `this.usuarioActual` ya ha sido cargado.
     */
    private void cargarDatosUsuario() {
        if (usuarioActual != null) {
            // Mostrar datos personales.
            if (nombreCompletoLabel != null) nombreCompletoLabel.setText(usuarioActual.getNombreCompleto());
            if (emailLabel != null) emailLabel.setText(usuarioActual.getEmail());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            if (fechaRegistroLabel != null) fechaRegistroLabel.setText(usuarioActual.getFechaRegistro() != null ? usuarioActual.getFechaRegistro().format(formatter) : "N/A");

            if (ultimoAccesoLabel != null) {
                ultimoAccesoLabel.setText(usuarioActual.getUltimoLogin() != null ? usuarioActual.getUltimoLogin().format(formatter) : "Nunca");
            }

            // Mostrar modalidad de ahorro.
            if (modalidadActualLabel != null) modalidadActualLabel.setText(usuarioActual.getModalidadAhorroSeleccionada() != null ? usuarioActual.getModalidadAhorroSeleccionada() : "No definida");

            String descModalidad = "Modalidad no definida."; // Descripción por defecto.
            if (usuarioActual.getModalidadAhorroSeleccionada() != null) {
                switch (usuarioActual.getModalidadAhorroSeleccionada()) {
                    case "Máximo": descModalidad = "Maximiza tu ahorro priorizando los precios más bajos."; break;
                    case "Equilibrado": descModalidad = "Balance perfecto entre ahorro y calidad nutricional."; break;
                    case "Estándar": descModalidad = "Prioriza la calidad nutricional manteniendo un presupuesto razonable."; break;
                }
            }
            if (descripcionModalidadLabel != null) descripcionModalidadLabel.setText(descModalidad);

        } else {
            // Si usuarioActual es null, mostrar valores por defecto o mensajes de error.
            if (nombreCompletoLabel != null) nombreCompletoLabel.setText("Usuario no disponible");
            if (emailLabel != null) emailLabel.setText("N/A");
            if (fechaRegistroLabel != null) fechaRegistroLabel.setText("N/A");
            if (ultimoAccesoLabel != null) ultimoAccesoLabel.setText("N/A");
            if (modalidadActualLabel != null) modalidadActualLabel.setText("N/A");
            if (descripcionModalidadLabel != null) descripcionModalidadLabel.setText("N/A");
        }
    }

    /**
     * Carga y muestra un resumen de los datos financieros del usuario (días usando la app, balance, etc.).
     * Asume que `this.usuarioActual` y `this.usuarioIdActual` están disponibles.
     */
    private void cargarDatosFinancieros() {
        // Si no hay usuario o ID, o falta la fecha de registro, no se pueden calcular los datos.
        if (usuarioActual == null || usuarioActual.getFechaRegistro() == null || this.usuarioIdActual == null) {
            if (diasUsandoAppLabel != null) diasUsandoAppLabel.setText("N/A días");
            if (balanceActualLabel != null) balanceActualLabel.setText("€0.00");
            if (totalAhorradoLabel != null) totalAhorradoLabel.setText("€0.00");
            if (gastosMesLabel != null) gastosMesLabel.setText("€0.00");
            if (ingresosMesLabel != null) ingresosMesLabel.setText("€0.00");
            return;
        }

        // Calcular días desde el registro.
        long diasUsando = LocalDate.now().toEpochDay() - usuarioActual.getFechaRegistro().toEpochDay();
        if (diasUsandoAppLabel != null) diasUsandoAppLabel.setText(diasUsando + " días");

        // Obtener datos financieros del mes actual.
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        double ingresosMes = transaccionServicio.obtenerTotalIngresos(this.usuarioIdActual, inicioMes, LocalDate.now());
        double gastosMes = transaccionServicio.obtenerTotalGastos(this.usuarioIdActual, inicioMes, LocalDate.now());
        double balance = transaccionServicio.obtenerBalance(this.usuarioIdActual, null, LocalDate.now()); // Balance total.
        double totalAhorrado = balance * 0.3; // Simulación: 30% del balance como ahorro.

        // Mostrar datos financieros.
        if (balanceActualLabel != null) balanceActualLabel.setText(String.format("€%.2f", balance));
        if (totalAhorradoLabel != null) totalAhorradoLabel.setText(String.format("€%.2f", totalAhorrado));
        if (gastosMesLabel != null) gastosMesLabel.setText(String.format("€%.2f", gastosMes));
        if (ingresosMesLabel != null) ingresosMesLabel.setText(String.format("€%.2f", ingresosMes));

        // Colorear el balance.
        if (balanceActualLabel != null) {
            balanceActualLabel.setTextFill(balance >= 0 ? Color.rgb(100, 220, 100) : Color.rgb(220, 100, 100));
        }
    }

    /**
     * Maneja la acción de cambiar la foto de perfil.
     * Abre un FileChooser para que el usuario seleccione una imagen.
     * (Actualmente simula la actualización).
     */
    @FXML
    private void handleCambiarFotoAction(ActionEvent evento) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar foto de perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos de imagen", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp")
        );

        Stage stage = obtenerEscenarioActual(); // Obtener el Stage actual.
        if (stage == null) {
            navegacionServicio.mostrarAlertaError("Error de Interfaz", "No se pudo obtener la ventana principal para el selector de archivos.");
            return;
        }
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                Image imagen = new Image(file.toURI().toString());
                if (profilePhoto != null) profilePhoto.setImage(imagen); // Mostrar la nueva imagen.
                // En una aplicación real, aquí se guardaría la ruta de la imagen o la imagen en sí.
                navegacionServicio.mostrarAlertaInformacion("Foto actualizada",
                        "Tu foto de perfil ha sido actualizada correctamente (simulado).");
            } catch (Exception e) {
                navegacionServicio.mostrarAlertaError("Error al cargar imagen",
                        "No se pudo cargar la imagen seleccionada: " + e.getMessage());
            }
        }
    }

    /**
     * Maneja la acción de editar los datos personales del usuario.
     * Abre un diálogo para modificar nombre, apellidos y email.
     */
    @FXML
    private void handleEditarDatosAction(ActionEvent evento) {
        if (usuarioActual == null) {
            navegacionServicio.mostrarAlertaError("Error de Usuario", "No hay datos de usuario para editar.");
            return;
        }
        // Crear un diálogo para la edición.
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Editar Datos Personales");
        dialog.setHeaderText("Modifica tus datos personales");

        // Configurar botones del diálogo (Guardar, Cancelar).
        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);

        // Crear el formulario dentro del diálogo.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nombreField = new TextField(usuarioActual.getNombre());
        nombreField.setPromptText("Nombre");
        TextField apellidosField = new TextField(usuarioActual.getApellidos());
        apellidosField.setPromptText("Apellidos");
        TextField emailFieldDialog = new TextField(usuarioActual.getEmail()); // Renombrado para evitar colisión con FXML.
        emailFieldDialog.setPromptText("Email");

        // Aplicar estilos a los campos del diálogo.
        EstilosApp.aplicarEstiloCampoTexto(nombreField);
        EstilosApp.aplicarEstiloCampoTexto(apellidosField);
        EstilosApp.aplicarEstiloCampoTexto(emailFieldDialog);

        // Añadir campos al GridPane.
        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Apellidos:"), 0, 1);
        grid.add(apellidosField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailFieldDialog, 1, 2);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(nombreField::requestFocus); // Solicitar foco al primer campo.

        navegacionServicio.estilizarDialog(dialog); // Aplicar estilo al diálogo.

        // Convertir el resultado del diálogo cuando se presiona "Guardar".
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                return new String[]{nombreField.getText(), apellidosField.getText(), emailFieldDialog.getText()};
            }
            return null; // Si se cancela.
        });

        // Procesar el resultado del diálogo.
        dialog.showAndWait().ifPresent(result -> {
            String nombre = result[0];
            String apellidos = result[1];
            String email = result[2];

            // Validar los datos ingresados.
            if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty()) {
                navegacionServicio.mostrarAlertaError("Campos Vacíos", "Todos los campos son obligatorios.");
                return;
            }
            if (!ValidacionUtil.esEmailValido(email)) { // Usar la clase de utilidad para validar email.
                navegacionServicio.mostrarAlertaError("Email Inválido", "El email no tiene un formato válido.");
                return;
            }

            // Actualizar el objeto Usuario y persistir los cambios.
            usuarioActual.setNombre(nombre);
            usuarioActual.setApellidos(apellidos);
            usuarioActual.setEmail(email);

            if (usuarioServicio.actualizarUsuario(usuarioActual)) {
                cargarDatosUsuario(); // Recargar datos en la UI.
                navegacionServicio.mostrarAlertaInformacion("Datos Actualizados",
                        "Tus datos personales han sido actualizados correctamente.");
            } else {
                navegacionServicio.mostrarAlertaError("Error al Actualizar", "No se pudieron actualizar los datos del usuario.");
            }
        });
    }

    /**
     * Maneja la acción de cambiar la modalidad de ahorro del usuario.
     * Muestra un ChoiceDialog para seleccionar la nueva modalidad.
     */
    @FXML
    private void handleCambiarModalidadAction(ActionEvent evento) {
        if (usuarioActual == null) {
            navegacionServicio.mostrarAlertaError("Error de Usuario", "No hay datos de usuario para modificar la modalidad.");
            return;
        }
        // Crear un ChoiceDialog con las opciones de modalidad.
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                usuarioActual.getModalidadAhorroSeleccionada() != null ? usuarioActual.getModalidadAhorroSeleccionada() : "Equilibrado", // Valor por defecto si es null
                "Máximo", "Equilibrado", "Estándar");
        dialog.setTitle("Cambiar Modalidad de Ahorro");
        dialog.setHeaderText("Selecciona tu modalidad de ahorro preferida");
        dialog.setContentText("Modalidad:");

        navegacionServicio.estilizarDialog(dialog); // Aplicar estilo al diálogo.

        // Procesar la selección del usuario.
        dialog.showAndWait().ifPresent(modalidad -> {
            usuarioActual.setModalidadAhorroSeleccionada(modalidad);
            if (usuarioServicio.actualizarUsuario(usuarioActual)) {
                cargarDatosUsuario(); // Recargar datos en la UI.
                navegacionServicio.mostrarAlertaInformacion("Modalidad Actualizada",
                        "Tu modalidad de ahorro ha sido cambiada a: " + modalidad);
            } else {
                navegacionServicio.mostrarAlertaError("Error al Actualizar", "No se pudo cambiar la modalidad de ahorro.");
            }
        });
    }

    /**
     * Maneja la acción de navegar a la pantalla de configuración general de la aplicación.
     */
    @FXML
    private void handleConfigurarPerfilAction(ActionEvent evento) {
        Stage stage = obtenerEscenarioActual();
        if (stage != null) {
            navegacionServicio.navegarAConfiguracion(stage);
        } else {
            navegacionServicio.mostrarAlertaError("Error de Navegación", "No se pudo obtener la ventana actual para navegar a configuración.");
        }
    }

    /**
     * Maneja la acción de exportar los datos del perfil del usuario a un archivo JSON.
     * Recopila información del usuario, resumen financiero, nutricional y de listas de compra.
     */
    @FXML
    private void handleExportarDatosAction(ActionEvent evento) {
        // Verificar que haya un usuario cargado.
        if (usuarioActual == null || usuarioIdActual == null) {
            navegacionServicio.mostrarAlertaError("Exportación Fallida", "No hay datos de usuario cargados para exportar.");
            return;
        }

        // Configurar el FileChooser para guardar el archivo JSON.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Datos del Perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivo JSON", "*.json")
        );
        String nombreBase = (usuarioActual.getNombre() != null && !usuarioActual.getNombre().isEmpty()) ?
                usuarioActual.getNombre().toLowerCase() : "usuario";
        String nombreArchivoSugerido = "datos_perfil_" + nombreBase.replace(" ", "_") + ".json";
        fileChooser.setInitialFileName(nombreArchivoSugerido);

        Stage stage = obtenerEscenarioActual();
        if (stage == null) {
            navegacionServicio.mostrarAlertaError("Error de Interfaz", "No se pudo obtener la ventana principal para el selector de archivos.");
            return;
        }
        File file = fileChooser.showSaveDialog(stage); // Mostrar diálogo para guardar archivo.

        if (file != null) { // Si el usuario seleccionó una ubicación.
            try {
                // Crear un mapa para almacenar todos los datos a exportar.
                Map<String, Object> datosExportar = new HashMap<>();

                // 1. Información del Usuario.
                Map<String, Object> infoUsuario = new HashMap<>();
                infoUsuario.put("idUsuario", usuarioActual.getId());
                infoUsuario.put("nombreCompleto", usuarioActual.getNombreCompleto());
                infoUsuario.put("email", usuarioActual.getEmail());
                infoUsuario.put("fechaRegistro", usuarioActual.getFechaRegistro() != null ? usuarioActual.getFechaRegistro().toString() : "N/A");
                infoUsuario.put("ultimoLogin", usuarioActual.getUltimoLogin() != null ? usuarioActual.getUltimoLogin().toString() : "N/A");
                infoUsuario.put("modalidadAhorroSeleccionada", usuarioActual.getModalidadAhorroSeleccionada() != null ? usuarioActual.getModalidadAhorroSeleccionada() : "No definida");
                datosExportar.put("informacionUsuario", infoUsuario);

                // 2. Resumen Financiero.
                Map<String, Object> resumenFinanciero = new HashMap<>();
                LocalDate hoy = LocalDate.now();
                resumenFinanciero.put("totalTransacciones", transaccionServicio.obtenerTransaccionesPorUsuario(this.usuarioIdActual).size());
                resumenFinanciero.put("totalIngresosGenerales", String.format("%.2f", transaccionServicio.obtenerTotalIngresos(this.usuarioIdActual, null, hoy)));
                resumenFinanciero.put("totalGastosGenerales", String.format("%.2f", transaccionServicio.obtenerTotalGastos(this.usuarioIdActual, null, hoy)));
                resumenFinanciero.put("balanceActualGeneral", String.format("%.2f", transaccionServicio.obtenerBalance(this.usuarioIdActual, null, hoy)));
                datosExportar.put("resumenFinanciero", resumenFinanciero);

                // 3. Resumen Nutricional (si existe).
                if (perfilNutricionalServicio.tienePerfil(this.usuarioIdActual)) {
                    PerfilNutricional perfil = perfilNutricionalServicio.obtenerPerfilPorUsuario(this.usuarioIdActual);
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

                // 4. Resumen de Listas de Compra.
                List<ListaCompra> listasActivas = listaCompraServicio.obtenerListasActivas(this.usuarioIdActual);
                datosExportar.put("numeroListasCompraActivas", listasActivas.size());
                if (!listasActivas.isEmpty()) {
                    datosExportar.put("nombresListasActivas", listasActivas.stream().map(ListaCompra::getNombre).collect(Collectors.toList()));
                }

                // Configurar ObjectMapper para formatear el JSON de manera legible.
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // JSON "bonito".

                // Escribir los datos al archivo seleccionado.
                try (FileWriter fileWriter = new FileWriter(file)) {
                    objectMapper.writeValue(fileWriter, datosExportar);
                }

                navegacionServicio.mostrarAlertaInformacion("Datos Exportados",
                        "Los datos de tu perfil han sido exportados correctamente a:\n" + file.getAbsolutePath());

            } catch (IOException e) {
                navegacionServicio.mostrarAlertaError("Error al Exportar",
                        "Ocurrió un error al escribir el archivo: " + e.getMessage());
                e.printStackTrace(); // Imprimir stack trace para depuración.
            } catch (Exception e) { // Capturar cualquier otra excepción inesperada.
                navegacionServicio.mostrarAlertaError("Error Inesperado",
                        "Ocurrió un error inesperado durante la exportación: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Maneja la acción de eliminar la cuenta del usuario.
     * Requiere una doble confirmación por seguridad.
     */
    @FXML
    private void handleEliminarCuentaAction(ActionEvent evento) {
        if (usuarioActual == null) {
            navegacionServicio.mostrarAlertaError("Error de Usuario", "No hay un usuario activo para eliminar.");
            return;
        }

        // Primera confirmación.
        Alert confirmacion = new Alert(Alert.AlertType.WARNING);
        confirmacion.setTitle("Eliminar Cuenta - ¡PELIGRO!");
        confirmacion.setHeaderText("¿Estás ABSOLUTAMENTE seguro de que deseas eliminar tu cuenta?");
        confirmacion.setContentText("Esta acción ELIMINARÁ PERMANENTEMENTE tu cuenta y todos tus datos asociados (transacciones, perfiles, listas de compra, etc.).\n" +
                "Esta acción NO SE PUEDE DESHACER.\n\n" +
                "¿Deseas continuar con la eliminación?");

        DialogPane dialogPane = confirmacion.getDialogPane();
        EstilosApp.aplicarEstiloDialogPane(dialogPane); // Aplicar estilo al diálogo.

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) { // Si el usuario confirma la primera vez.
                // Segunda confirmación: pedir que escriba "ELIMINAR".
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Confirmar Eliminación Definitiva");
                dialog.setHeaderText("Para confirmar la eliminación PERMANENTE de tu cuenta, escribe la palabra 'ELIMINAR' en el campo de abajo.");
                dialog.setContentText("Texto de confirmación:");

                navegacionServicio.estilizarDialog(dialog); // Aplicar estilo al diálogo de texto.

                dialog.showAndWait().ifPresent(texto -> {
                    if ("ELIMINAR".equals(texto.trim())) { // Verificar el texto de confirmación.
                        // Proceder con la eliminación real de la cuenta.
                        boolean eliminado = usuarioServicio.eliminarUsuario(usuarioActual.getEmail()); // Asume que este método existe y funciona.
                        if (eliminado) {
                            navegacionServicio.mostrarAlertaInformacion("Cuenta Eliminada",
                                    "Tu cuenta ha sido eliminada permanentemente.\n" +
                                            "Gracias por usar SmartSave. Esperamos verte de nuevo.");
                            Stage stageActual = obtenerEscenarioActual();
                            if (stageActual != null) {
                                navegacionServicio.navegarALogin(stageActual); // Redirigir a la pantalla de login.
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

    /**
     * Maneja la acción de navegar a la pantalla de perfil (generalmente para refrescar).
     * Este método se llama si el usuario ya está en la pantalla de perfil y vuelve a hacer clic en el botón de perfil.
     */
    @Override
    public void handleProfileAction(ActionEvent evento) {
        // Si el usuario actual está cargado, refrescar sus datos en la UI.
        if (this.usuarioActual != null) {
            cargarDatosUsuario();
            cargarDatosFinancieros();
        } else {
            // Si por alguna razón el usuario no está cargado, intentar inicializar de nuevo.
            // Esto es una medida de seguridad, pero idealmente no debería ocurrir.
            System.err.println("PerfilController: Se intentó recargar el perfil, pero usuarioActual era null. Reintentando inicialización.");
            inicializarControlador(); // Cuidado con bucles si la inicialización falla repetidamente.
        }
        // Mantener el botón de perfil activado en el menú.
        if (profileButton != null) {
            activarBoton(profileButton);
        }
    }

    /**
     * Método auxiliar para obtener el escenario (Stage) actual de la aplicación.
     * Intenta obtener el Stage desde el mainPane o, como fallback, desde uno de los botones.
     * @return El Stage actual, o null si no se puede determinar.
     */
    private Stage obtenerEscenarioActual() {
        // Intentar obtener el Stage desde el panel principal.
        if (mainPane != null && mainPane.getScene() != null && mainPane.getScene().getWindow() instanceof Stage) {
            return (Stage) mainPane.getScene().getWindow();
        }
        // Fallback: intentar obtener el Stage desde el botón de exportar (o cualquier otro botón FXML disponible).
        Node[] nodosPotenciales = {exportarDatosButton, cambiarFotoButton, editarDatosButton, cambiarModalidadButton, configurarPerfilButton, eliminarCuentaButton};
        for (Node nodo : nodosPotenciales) {
            if (nodo != null && nodo.getScene() != null && nodo.getScene().getWindow() instanceof Stage) {
                return (Stage) nodo.getScene().getWindow();
            }
        }
        // Si no se puede obtener el Stage, registrar una advertencia.
        System.err.println("Advertencia: No se pudo obtener el Stage actual en PerfilController. La funcionalidad que depende del Stage (ej. FileChooser) podría fallar.");
        return null;
    }
}