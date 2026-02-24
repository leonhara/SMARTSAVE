package smartsave.app;

import javafx.application.Application;
import javafx.application.Platform; 
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import smartsave.config.HibernateConfig; 
import smartsave.servicio.ModalidadAhorroServicio; 

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class SmartSaveApp extends Application {
    private double offsetX;
    private double offsetY;

    @Override
    public void start(Stage escenarioPrincipal) throws Exception {
        boolean pythonDepsOk = checkAndInstallPythonDependencies();
        if (!pythonDepsOk) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertencia de Dependencias");
            alert.setHeaderText("Problema con las dependencias de Python");
            alert.setContentText("No se pudieron instalar/verificar las dependencias de Python (mercapy, requests).\n" +
                    "La funcionalidad de búsqueda de productos de Mercadona podría no estar disponible.\n" +
                    "Por favor, asegúrate de tener Python y pip instalados y configurados en el PATH, y una conexión a internet.");
            alert.showAndWait();
        }
        try {

            HibernateConfig.getSessionFactory();
            ModalidadAhorroServicio modalidadServicio = new ModalidadAhorroServicio();
            modalidadServicio.inicializarModalidadesPredefinidas();
            System.out.println("Modalidades de ahorro predefinidas verificadas/inicializadas exitosamente.");

        } catch (Exception e) {
            System.err.println("Error crítico durante la inicialización de datos base (modalidades de ahorro): " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Inicialización Crítico");
            alert.setHeaderText("No se pudieron inicializar los datos base de la aplicación (modalidades de ahorro).");
            alert.setContentText("La aplicación no puede continuar y se cerrará.\nError: " + e.getMessage() +
                    "\n\nPor favor, revisa la conexión a la base de datos y la configuración.");
            alert.showAndWait();
            Platform.exit();
            System.exit(1);
            return;
        }

        Parent raiz = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));

        Scene escena = new Scene(raiz);
        escena.setFill(Color.TRANSPARENT);

        escenarioPrincipal.initStyle(StageStyle.TRANSPARENT);
        escenarioPrincipal.setTitle("SmartSave");
        escenarioPrincipal.setScene(escena);
        escenarioPrincipal.setMinWidth(800);
        escenarioPrincipal.setMinHeight(600);

        configurarVentanaArrastrable(escena, escenarioPrincipal);

        escenarioPrincipal.show();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Cerrando SmartSave, limpiando recursos...");

        HibernateConfig.shutdown();
        System.out.println("Recursos de base de datos liberados.");

        System.out.println("Apagando la Máquina Virtual. Adiós.");
        System.exit(0);
    }

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

    private static boolean isPipAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("pip", "--version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("'pip' está disponible.");
                return true;
            } else {
                System.err.println("'pip' no está disponible o no se encuentra en el PATH (código de salida: " + exitCode + ").");
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Excepción al verificar 'pip': " + e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    private static boolean checkAndInstallPythonDependencies() {
        System.out.println("Verificando e instalando dependencias de Python...");

        if (!isPipAvailable()) {
            
            return false;
        }

        Path tempRequirementsPath = null;
        try {
            try (InputStream reqStream = SmartSaveApp.class.getResourceAsStream("/api/requirements.txt")) {
                if (reqStream == null) {
                    System.err.println("No se pudo encontrar 'requirements.txt' en el JAR. Verifica la ruta: /api/requirements.txt");
                    return false;
                }
                tempRequirementsPath = Files.createTempFile("smartsave_req_", ".txt");
                tempRequirementsPath.toFile().deleteOnExit(); 

                Files.copy(reqStream, tempRequirementsPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("requirements.txt extraído a: " + tempRequirementsPath.toAbsolutePath().toString());
            }

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python", "-m", "pip", "install", "-r", tempRequirementsPath.toAbsolutePath().toString()
            );
            processBuilder.redirectErrorStream(true);

            System.out.println("Ejecutando comando: " + String.join(" ", processBuilder.command()));
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                    System.out.println("Salida de pip: " + line); 
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Dependencias de Python ('mercapy', 'requests') instaladas/verificadas correctamente.");
                return true;
            } else {
                System.err.println("Error al instalar dependencias de Python. Código de salida: " + exitCode);
                System.err.println("Salida completa de pip:\n" + output.toString());
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Excepción al intentar instalar dependencias de Python: " + e.getMessage());
            e.printStackTrace();
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        } finally {
            if (tempRequirementsPath != null) {
                try {

                    Files.deleteIfExists(tempRequirementsPath);
                } catch (IOException ex) {
                    System.err.println("Advertencia: No se pudo eliminar el archivo temporal de requisitos: " + tempRequirementsPath);
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}