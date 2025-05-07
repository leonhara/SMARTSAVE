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
    private double xOffset;
    private double yOffset;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar la vista de login desde el archivo FXML
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));

        // Configurar la escena con fondo transparente para el efecto de ventana personalizada
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        // Configurar el estilo de la ventana sin decoración del sistema operativo
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setTitle("SmartSave");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        // Permitir arrastrar la ventana desde cualquier parte
        setupDraggableWindow(scene, primaryStage);

        primaryStage.show();
    }

    /**
     * Configura los manejadores de eventos para permitir arrastrar la ventana
     */
    private void setupDraggableWindow(Scene scene, Stage stage) {
        scene.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        scene.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}