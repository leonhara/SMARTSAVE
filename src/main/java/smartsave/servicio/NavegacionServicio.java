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

public class NavegacionServicio {

    private static final String RUTA_DASHBOARD = "/fxml/dashboard.fxml";
    private static final String RUTA_TRANSACCIONES = "/fxml/transacciones.fxml";
    private static final String RUTA_NUTRICION = "/fxml/nutricion.fxml";
    private static final String RUTA_COMPRAS = "/fxml/compras.fxml";
    private static final String RUTA_AHORRO = "/fxml/ahorro.fxml";
    private static final String RUTA_CONFIGURACION = "/fxml/configuracion.fxml";
    private static final String RUTA_PERFIL = "/fxml/perfil.fxml";
    private static final String RUTA_LOGIN = "/fxml/login.fxml";

    private final Map<String, Object> controladoresCache = new HashMap<>();

    private static NavegacionServicio instancia;

    public static NavegacionServicio getInstancia() {
        if (instancia == null) {
            instancia = new NavegacionServicio();
        }
        return instancia;
    }

    private NavegacionServicio() {
    }

    public <T> void navegarA(String rutaFXML, String titulo, Stage escenarioActual,
                             Consumer<T> configuracionControlador) {
        try {

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
            escena.setFill(Color.TRANSPARENT);

            escenarioActual.setScene(escena);
            escenarioActual.setTitle("SmartSave - " + titulo);

            if (eraMaximizado) {
                escenarioActual.setMaximized(true);
            } else {
                escenarioActual.setWidth(anchoActual);
                escenarioActual.setHeight(altoActual);
            }

        } catch (IOException e) {
            mostrarAlertaError("Error de navegación",
                    "Error al cargar la pantalla de " + titulo + ": " + e.getMessage());
        }
    }

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

    public void mostrarAlertaNoImplementado(String caracteristica) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(caracteristica + " - En desarrollo");
        alerta.setHeaderText(null);
        alerta.setContentText("Esta funcionalidad aún no está implementada.");

        estilizarAlerta(alerta);
        alerta.showAndWait();
    }

    public void mostrarAlertaError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        estilizarAlerta(alerta);
        alerta.showAndWait();
    }

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

    public void mostrarAlertaInformacion(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        estilizarAlerta(alerta);
        alerta.showAndWait();
    }

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

    public boolean confirmarEliminarLista() {
        return confirmarAccion("Eliminar Lista",
                "¿Estás seguro de que deseas eliminar esta lista de compra?");
    }

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