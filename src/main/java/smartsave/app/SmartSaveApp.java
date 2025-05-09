package smartsave.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SmartSaveApp extends Application {

    // Coordenadas para permitir el arrastre de la ventana
    private double offsetX;
    private double offsetY;

    @Override
    public void start(Stage escenarioPrincipal) throws Exception {
        // Cargar la vista de login desde el archivo FXML
        Parent raiz = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));

        // Configurar la escena con fondo transparente para el efecto de ventana personalizada
        Scene escena = new Scene(raiz);
        escena.setFill(Color.TRANSPARENT);

        // Configurar el estilo de la ventana sin decoración del sistema operativo
        escenarioPrincipal.initStyle(StageStyle.TRANSPARENT);
        escenarioPrincipal.setTitle("SmartSave");
        escenarioPrincipal.setScene(escena);
        escenarioPrincipal.setMinWidth(800);
        escenarioPrincipal.setMinHeight(600);

        // Permitir arrastrar la ventana desde cualquier parte
        configurarVentanaArrastrable(escena, escenarioPrincipal);

        escenarioPrincipal.show();
    }

    /**
     * Configura los manejadores de eventos para permitir arrastrar la ventana
     */
    private void configurarVentanaArrastrable(Scene escena, Stage escenario) {
        escena.setOnMousePressed(evento -> {
            offsetX = evento.getSceneX();
            offsetY = evento.getSceneY();
        });

        escena.setOnMouseDragged(evento -> {
            escenario.setX(evento.getScreenX() - offsetX);
            escenario.setY(evento.getScreenY() - offsetY);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}