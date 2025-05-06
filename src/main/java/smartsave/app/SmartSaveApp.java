package smartsave.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SmartSaveApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar la vista de login desde el archivo FXML
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));

        // Configurar la escena con transparencia
        Scene scene = new Scene(root, 800, 600);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

        // Configurar el escenario (ventana) con transparencia
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setTitle("SmartSave - Gestión Financiera y Nutricional");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}