package smartsave.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SmartSaveApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar la vista de login desde el archivo FXML
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));

        // Configurar la escena sin transparencia para Windows 11
        Scene scene = new Scene(root);

        // Configurar sin transparencia para mejor compatibilidad con Windows 11
        scene.setFill(Color.TRANSPARENT);

        // Usar UNDECORATED en lugar de TRANSPARENT para mejor compatibilidad
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setTitle("SmartSave");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}