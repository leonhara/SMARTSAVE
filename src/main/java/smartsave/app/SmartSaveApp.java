package smartsave.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SmartSaveApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar la vista de login desde el archivo FXML
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));

        // Configurar la escena (sin referencia a CSS)
        Scene scene = new Scene(root, 800, 600);

        // Configurar el escenario (ventana)
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