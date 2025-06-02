package smartsave.servicio;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Servicio centralizado para gestionar la navegación entre pantallas de la aplicación.
 * Reduce la duplicación de código en los controladores y centraliza la lógica de navegación.
 */
public class NavegacionServicio {

    // Constantes para las rutas de los archivos FXML
    private static final String RUTA_DASHBOARD = "/fxml/dashboard.fxml";
    private static final String RUTA_TRANSACCIONES = "/fxml/transacciones.fxml";
    private static final String RUTA_NUTRICION = "/fxml/nutricion.fxml";
    private static final String RUTA_COMPRAS = "/fxml/compras.fxml";
    private static final String RUTA_AHORRO = "/fxml/ahorro.fxml";
    private static final String RUTA_CONFIGURACION = "/fxml/configuracion.fxml";
    private static final String RUTA_PERFIL = "/fxml/perfil.fxml";
    private static final String RUTA_LOGIN = "/fxml/login.fxml";

    // Caché para los controladores (opcional, para mantener estado entre navegaciones)
    private final Map<String, Object> controladoresCache = new HashMap<>();

    // Singleton (opcional)
    private static NavegacionServicio instancia;

    public static NavegacionServicio getInstancia() {
        if (instancia == null) {
            instancia = new NavegacionServicio();
        }
        return instancia;
    }

    private NavegacionServicio() {
        // Constructor privado para singleton
    }

    /**
     * Navega a la pantalla especificada y aplica configuraciones opcionales al controlador.
     *
     * @param rutaFXML Ruta del archivo FXML
     * @param titulo Título para la ventana
     * @param escenarioActual Stage actual
     * @param configuracionControlador Función para configurar el controlador (opcional)
     */
    public <T> void navegarA(String rutaFXML, String titulo, Stage escenarioActual,
                             Consumer<T> configuracionControlador) {
        try {
            // --- PASO 1: Guardar el estado y tamaño actual de la ventana ---
            boolean eraMaximizado = escenarioActual.isMaximized();
            double anchoActual = escenarioActual.getWidth();
            double altoActual = escenarioActual.getHeight();

            FXMLLoader cargador = new FXMLLoader(getClass().getResource(rutaFXML));
            Parent raiz = cargador.load();

            if (configuracionControlador != null) {
                T controlador = cargador.getController();
                configuracionControlador.accept(controlador);
            }

            Scene escena = new Scene(raiz);
            escena.setFill(Color.TRANSPARENT); // Mantener para ventana personalizada

            // --- PASO 2: Establecer la nueva escena ---
            escenarioActual.setScene(escena);
            escenarioActual.setTitle("SmartSave - " + titulo);

            // --- PASO 3: Restaurar el estado y tamaño de la ventana ---
            if (eraMaximizado) {
                escenarioActual.setMaximized(true);
            } else {
                // Solo restaurar dimensiones si no estaba maximizada
                // Esto preserva el tamaño si el usuario lo ajustó manualmente
                escenarioActual.setWidth(anchoActual);
                escenarioActual.setHeight(altoActual);
            }
            // Es importante NO llamar a escenarioActual.sizeToScene() si quieres
            // mantener el tamaño anterior elegido por el usuario.

            // controladoresCache.put(rutaFXML, cargador.getController()); // Si usas caché

        } catch (IOException e) {
            mostrarAlertaError("Error de navegación",
                    "Error al cargar la pantalla de " + titulo + ": " + e.getMessage());
        }
    }

    // Métodos específicos de navegación

    public void navegarADashboard(Stage escenarioActual) {
        navegarA(RUTA_DASHBOARD, "Dashboard", escenarioActual, null);
    }

    public void navegarATransacciones(Stage escenarioActual) {
        navegarA(RUTA_TRANSACCIONES, "Gestión de Ingresos y Gastos", escenarioActual, null);
    }

    public void navegarANutricion(Stage escenarioActual) {
        navegarA(RUTA_NUTRICION, "Perfil Nutricional", escenarioActual, null);
    }

    public void navegarACompras(Stage escenarioActual) {
        navegarA(RUTA_COMPRAS, "Plan de Compras", escenarioActual, null);
    }

    public void navegarAAhorro(Stage escenarioActual) {
        navegarA(RUTA_AHORRO, "Modalidades de Ahorro", escenarioActual, null);
    }

    public void navegarAConfiguracion(Stage escenarioActual) {
        navegarA(RUTA_CONFIGURACION, "Configuración", escenarioActual, null);
    }

    public void navegarAPerfil(Stage escenarioActual) {
        navegarA(RUTA_PERFIL, "Mi Perfil", escenarioActual, null);
    }

    public void navegarALogin(Stage escenarioActual) {
        navegarA(RUTA_LOGIN, "Login", escenarioActual, null);
        escenarioActual.centerOnScreen();
    }

    /**
     * Muestra una alerta con una característica "no implementada".
     *
     * @param caracteristica Nombre de la característica
     */
    public void mostrarAlertaNoImplementado(String caracteristica) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(caracteristica + " - En desarrollo");
        alerta.setHeaderText(null);
        alerta.setContentText("Esta funcionalidad aún no está implementada.");

        estilizarAlerta(alerta);
        alerta.showAndWait();
    }

    /**
     * Muestra una alerta de error.
     *
     * @param titulo Título de la alerta
     * @param mensaje Mensaje de la alerta
     */
    public void mostrarAlertaError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        estilizarAlerta(alerta);
        alerta.showAndWait();
    }

    /**
     * Muestra un diálogo de confirmación para cerrar sesión.
     *
     * @param escenarioActual Stage actual
     * @return true si el usuario confirmó, false en caso contrario
     */
    public boolean confirmarCerrarSesion(Stage escenarioActual) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Cerrar Sesión");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Estás seguro que deseas cerrar la sesión?");

        estilizarAlerta(alerta);

        boolean confirmado = alerta.showAndWait()
                .filter(respuesta -> respuesta == ButtonType.OK)
                .isPresent();

        if (confirmado) {
            navegarALogin(escenarioActual);
        }

        return confirmado;
    }

    /**
     * Muestra un diálogo de confirmación para eliminar una transacción.
     *
     * @return true si el usuario confirmó, false en caso contrario
     */
    public boolean confirmarEliminarTransaccion() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar eliminación");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Estás seguro de que deseas eliminar esta transacción?");

        estilizarAlerta(alerta);

        return alerta.showAndWait()
                .filter(respuesta -> respuesta == ButtonType.OK)
                .isPresent();
    }

    /**
     * Muestra una alerta de información.
     *
     * @param titulo Título de la alerta
     * @param mensaje Mensaje de la alerta
     */
    public void mostrarAlertaInformacion(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        estilizarAlerta(alerta);
        alerta.showAndWait();
    }

    /**
     * Aplica estilos de la aplicación a una alerta.
     *
     * @param alerta Alerta a estilizar
     */
    private void estilizarAlerta(Alert alerta) {
        DialogPane dialogPane = alerta.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #1A1A25; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-color: #FF00FF; " +
                        "-fx-border-width: 1px;"
        );

        dialogPane.lookupAll(".label").forEach(node ->
                node.setStyle("-fx-text-fill: white;")
        );

        dialogPane.lookupAll(".button").forEach(node -> {
            node.setStyle(
                    "-fx-background-color: #25253A; " +
                            "-fx-text-fill: white; " +
                            "-fx-border-color: #4050FF; " +
                            "-fx-border-width: 1px;"
            );

            // Efectos de hover
            node.setOnMouseEntered(e ->
                    node.setStyle(
                            "-fx-background-color: #35354A; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-border-color: #FF00FF; " +
                                    "-fx-border-width: 1px;"
                    )
            );

            node.setOnMouseExited(e ->
                    node.setStyle(
                            "-fx-background-color: #25253A; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-border-color: #4050FF; " +
                                    "-fx-border-width: 1px;"
                    )
            );
        });
    }

    /**
     * Muestra un diálogo de confirmación genérico
     * @param titulo Título del diálogo
     * @param mensaje Mensaje a mostrar
     * @return true si el usuario confirmó, false en caso contrario
     */
    public boolean confirmarAccion(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        estilizarDialog(alerta);

        return alerta.showAndWait()
                .filter(respuesta -> respuesta == ButtonType.OK)
                .isPresent();
    }

    /**
     * Muestra un diálogo de confirmación para eliminar una lista
     * @return true si el usuario confirmó, false en caso contrario
     */
    public boolean confirmarEliminarLista() {
        return confirmarAccion("Eliminar Lista",
                "¿Estás seguro de que deseas eliminar esta lista de compra?");
    }

    // Agregar al NavegacionServicio

    /**
     * Estiliza un diálogo general (para usarse con cualquier tipo de diálogo)
     */
    public void estilizarDialog(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #1A1A25; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-color: #FF00FF; " +
                        "-fx-border-width: 1px;"
        );

        dialogPane.lookupAll(".label").forEach(node ->
                node.setStyle("-fx-text-fill: white;")
        );

        dialogPane.lookupAll(".button").forEach(node -> {
            node.setStyle(
                    "-fx-background-color: #25253A; " +
                            "-fx-text-fill: white; " +
                            "-fx-border-color: #4050FF; " +
                            "-fx-border-width: 1px;"
            );

            // Efectos de hover
            node.setOnMouseEntered(e ->
                    node.setStyle(
                            "-fx-background-color: #35354A; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-border-color: #FF00FF; " +
                                    "-fx-border-width: 1px;"
                    )
            );

            node.setOnMouseExited(e ->
                    node.setStyle(
                            "-fx-background-color: #25253A; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-border-color: #4050FF; " +
                                    "-fx-border-width: 1px;"
                    )
            );
        });
    }
}