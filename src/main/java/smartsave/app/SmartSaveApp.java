package smartsave.app;

import javafx.application.Application;
import javafx.application.Platform; // Necesario para Platform.exit()
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import smartsave.config.HibernateConfig; // Asegúrate de importar HibernateConfig
import smartsave.servicio.ModalidadAhorroServicio; // Importa el servicio

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class SmartSaveApp extends Application {

    // Coordenadas para permitir el arrastre de la ventana
    private double offsetX;
    private double offsetY;

    @Override
    public void start(Stage escenarioPrincipal) throws Exception {
        // Verificar e instalar dependencias de Python al inicio
        boolean pythonDepsOk = checkAndInstallPythonDependencies();
        if (!pythonDepsOk) {
            // Mostrar una alerta si las dependencias de Python no se pudieron instalar
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

            // Mostrar una alerta al usuario sobre el error crítico
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Inicialización Crítico");
            alert.setHeaderText("No se pudieron inicializar los datos base de la aplicación (modalidades de ahorro).");
            alert.setContentText("La aplicación no puede continuar y se cerrará.\nError: " + e.getMessage() +
                    "\n\nPor favor, revisa la conexión a la base de datos y la configuración.");
            alert.showAndWait();
            Platform.exit(); // Cerrar la aplicación JavaFX
            System.exit(1); // Terminar el proceso de la JVM
            return; // Salir del método start para evitar que continúe
        }
        // --- FIN DE LA SECCIÓN MODIFICADA/AÑADIDA ---

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

    /**
     * Verifica si pip está disponible en el sistema.
     * @return true si pip está disponible, false en caso contrario.
     */
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

    /**
     * Intenta instalar las dependencias de Python listadas en requirements.txt.
     * Asume que Python y pip están instalados y en el PATH.
     * El archivo requirements.txt debe estar en los recursos del JAR (ej. /api/requirements.txt).
     * @return true si las dependencias se instalaron/verificaron correctamente o ya estaban presentes, false si hubo un error.
     */
    private static boolean checkAndInstallPythonDependencies() {
        System.out.println("Verificando e instalando dependencias de Python...");

        if (!isPipAvailable()) {
            // Ya se mostró un mensaje de error en isPipAvailable()
            return false;
        }

        Path tempRequirementsPath = null;
        try {
            try (InputStream reqStream = SmartSaveApp.class.getResourceAsStream("/api/requirements.txt")) {
                if (reqStream == null) {
                    System.err.println("No se pudo encontrar 'requirements.txt' en el JAR. Verifica la ruta: /api/requirements.txt");
                    return false; // No se puede continuar sin el archivo de requisitos
                }
                // Crear un archivo temporal que se borrará al salir de la JVM
                tempRequirementsPath = Files.createTempFile("smartsave_req_", ".txt");
                tempRequirementsPath.toFile().deleteOnExit(); // Asegura que se borre

                Files.copy(reqStream, tempRequirementsPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("requirements.txt extraído a: " + tempRequirementsPath.toAbsolutePath().toString());
            }

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python", "-m", "pip", "install", "-r", tempRequirementsPath.toAbsolutePath().toString()
            );
            processBuilder.redirectErrorStream(true); // Combina la salida de error con la estándar

            System.out.println("Ejecutando comando: " + String.join(" ", processBuilder.command()));
            Process process = processBuilder.start();

            // Leer la salida del proceso para logging y depuración
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                    System.out.println("Salida de pip: " + line); // Loguea cada línea
                }
            }

            int exitCode = process.waitFor(); // Espera a que el proceso termine

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
                Thread.currentThread().interrupt(); // Buena práctica restablecer la interrupción
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