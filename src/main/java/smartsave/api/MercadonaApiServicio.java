package smartsave.api;

import smartsave.modelo.Producto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servicio para integrar con la API de Mercadona usando mercapy (Python)
 */
public class MercadonaApiServicio {

    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    private final String pythonScriptPath;
    private final String codigoPostal;
    private boolean apiDisponible;

    public MercadonaApiServicio(String codigoPostal) {
        this.objectMapper = new ObjectMapper();
        this.executorService = Executors.newFixedThreadPool(4);
        this.codigoPostal = codigoPostal != null ? codigoPostal : "28001"; // Madrid por defecto

        // Buscar el script Python
        this.pythonScriptPath = encontrarScriptPython();
        this.apiDisponible = verificarDisponibilidadApi();
    }

    /**
     * Busca productos en Mercadona
     */
    public CompletableFuture<List<Producto>> buscarProductos(String termino) {
        return CompletableFuture.supplyAsync(() -> {
            if (!apiDisponible) {
                System.out.println("API de Mercadona no disponible, devolviendo lista vacía");
                return new ArrayList<>();
            }

            try {
                // Aumentar límite a 50 productos
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "python", pythonScriptPath, "search", termino,
                        "--postcode", codigoPostal,
                        "--limit", "50"  // ← AGREGAR ESTA LÍNEA
                );

                String resultado = ejecutarProcesoConBuilder(processBuilder);
                return parsearProductos(resultado);
            } catch (Exception e) {
                System.err.println("Error al buscar productos: " + e.getMessage());
                return new ArrayList<>();
            }
        }, executorService);
    }

    /**
     * Obtiene detalle completo de un producto
     */
    public CompletableFuture<Producto> obtenerDetalleProducto(String productId) {
        return CompletableFuture.supplyAsync(() -> {
            if (!apiDisponible) {
                return null;
            }

            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "python", pythonScriptPath, "detail", productId, "--postcode", codigoPostal
                );

                String resultado = ejecutarProcesoConBuilder(processBuilder);
                return parsearProducto(resultado);
            } catch (Exception e) {
                System.err.println("Error al obtener detalle: " + e.getMessage());
                return null;
            }
        }, executorService);
    }

    /**
     * Obtiene productos nuevos de Mercadona
     */
    public CompletableFuture<List<Producto>> obtenerProductosNuevos() {
        return CompletableFuture.supplyAsync(() -> {
            if (!apiDisponible) {
                return new ArrayList<>();
            }

            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "python", pythonScriptPath, "new", "--postcode", codigoPostal
                );

                String resultado = ejecutarProcesoConBuilder(processBuilder);
                return parsearProductos(resultado);
            } catch (Exception e) {
                System.err.println("Error al obtener productos nuevos: " + e.getMessage());
                return new ArrayList<>();
            }
        }, executorService);
    }

    private String encontrarScriptPython() {
        // Buscar el script en diferentes ubicaciones posibles
        String[] posiblesRutas = {
                "src/main/java/smartsave/api/mercadona_bridge.py",
                "mercadona_bridge.py",
                "api/mercadona_bridge.py"
        };

        for (String ruta : posiblesRutas) {
            if (Files.exists(Paths.get(ruta))) {
                return ruta;
            }
        }

        // Si no se encuentra, crear en la ubicación predeterminada
        String rutaPorDefecto = "src/main/java/smartsave/api/mercadona_bridge.py";
        System.out.println("Script de Python encontrado en: " + rutaPorDefecto);
        return rutaPorDefecto;
    }

    private boolean verificarDisponibilidadApi() {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "-c", "import mercapy; print('OK')");
            Process process = pb.start();
            process.waitFor();
            boolean disponible = process.exitValue() == 0;

            if (disponible && Files.exists(Paths.get(pythonScriptPath))) {
                System.out.println("API de Mercadona disponible. Usando datos reales.");
                return true;
            } else {
                System.out.println("Mercapy no disponible o script no encontrado.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Mercapy no disponible: " + e.getMessage());
            return false;
        }
    }

    private String ejecutarProcesoConBuilder(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        processBuilder.redirectErrorStream(true);

        // Establecer variables de entorno para UTF-8
        processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
        processBuilder.environment().put("LANG", "C.UTF-8");

        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();

        // Leer stdout y stderr por separado para mejor control
        try (BufferedReader outputReader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "UTF-8"));
             BufferedReader errorReader = new BufferedReader(
                     new InputStreamReader(process.getErrorStream(), "UTF-8"))) {

            String line;
            while ((line = outputReader.readLine()) != null) {
                output.append(line).append("\n");
            }

            while ((line = errorReader.readLine()) != null) {
                error.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.err.println("Error ejecutando script Python. Exit code: " + exitCode);
            System.err.println("Stdout: " + output.toString());
            System.err.println("Stderr: " + error.toString());
            throw new RuntimeException("Error ejecutando script Python");
        }

        String resultado = output.toString().trim();

        // Log del resultado para debug
        System.out.println("JSON recibido (primeros 500 chars): " +
                resultado.substring(0, Math.min(500, resultado.length())));

        return resultado;
    }

    private List<Producto> parsearProductos(String json) {
        List<Producto> productos = new ArrayList<>();

        try {
            // Verificar que el JSON esté completo
            if (!json.trim().endsWith("}")) {
                System.err.println("JSON truncado detectado. Longitud: " + json.length());
                return productos; // Retornar lista vacía en lugar de fallar
            }

            JsonNode root = objectMapper.readTree(json);

            if (root.has("success") && root.get("success").asBoolean()) {
                JsonNode data = root.get("data");
                if (data != null && data.isArray()) {
                    for (JsonNode productNode : data) {
                        try {
                            Producto producto = convertirNodoAProducto(productNode);
                            if (producto != null) {
                                productos.add(producto);
                            }
                        } catch (Exception e) {
                            System.err.println("Error procesando producto individual: " + e.getMessage());
                            // Continuar con el siguiente producto en lugar de fallar todo
                        }
                    }
                }
            } else {
                String errorMsg = root.has("error") ? root.get("error").asText() : "Error desconocido";
                System.err.println("Error en respuesta API: " + errorMsg);
            }
        } catch (Exception e) {
            System.err.println("Error parseando productos: " + e.getMessage());
            // Mostrar más context del JSON para debug
            System.err.println("JSON completo: " + json);
        }

        return productos;
    }

    private Producto parsearProducto(String json) {
        try {
            // Limpiar el JSON de posibles caracteres de escape problemáticos
            String jsonLimpio = json.replaceAll("\\\\n", "\n").replaceAll("\\\\\"", "\"");

            JsonNode root = objectMapper.readTree(jsonLimpio);
            if (root.has("success") && root.get("success").asBoolean()) {
                JsonNode data = root.get("data");
                return convertirNodoAProducto(data);
            }
        } catch (Exception e) {
            System.err.println("Error parseando producto: " + e.getMessage());
        }
        return null;
    }

    private Producto convertirNodoAProducto(JsonNode productNode) {
        try {
            String idMercadona = productNode.has("id") ? productNode.get("id").asText() : null;
            String nombre = productNode.has("name") ? productNode.get("name").asText() : "";
            String marca = productNode.has("brand") ? productNode.get("brand").asText() : "Mercadona";
            String categoria = productNode.has("category") ? productNode.get("category").asText() : "";
            double precio = productNode.has("unit_price") ? productNode.get("unit_price").asDouble() : 0.0;

            // Crear el producto con tu estructura existente
            Producto producto = new Producto(nombre, marca, categoria, precio, "Mercadona");

            // Generar un ID único para el producto de Mercadona
            // Usamos el hash del ID de Mercadona para generar un Long único
            if (idMercadona != null) {
                Long idUnico = Math.abs((long) idMercadona.hashCode()) + 100000L; // +100000 para evitar conflictos con IDs locales
                producto.setId(idUnico);
            }

            // Añadir información nutricional estimada basada en la categoría
            producto.getInfoNutricional().setCalorias(estimarCalorias(categoria));
            producto.getInfoNutricional().setProteinas(estimarProteinas(categoria));
            producto.getInfoNutricional().setCarbohidratos(estimarCarbohidratos(categoria));
            producto.getInfoNutricional().setGrasas(estimarGrasas(categoria));

            return producto;
        } catch (Exception e) {
            System.err.println("Error convirtiendo producto: " + e.getMessage());
            return null;
        }
    }

    // Métodos para estimar valores nutricionales basados en la categoría
    private double estimarCalorias(String categoria) {
        categoria = categoria.toLowerCase();
        if (categoria.contains("fruta")) return 50;
        if (categoria.contains("verdura")) return 25;
        if (categoria.contains("carne")) return 200;
        if (categoria.contains("pescado")) return 150;
        if (categoria.contains("lácteo")) return 60;
        if (categoria.contains("cereal")) return 350;
        if (categoria.contains("pan")) return 250;
        return 100; // Valor por defecto
    }

    private double estimarProteinas(String categoria) {
        categoria = categoria.toLowerCase();
        if (categoria.contains("carne")) return 20;
        if (categoria.contains("pescado")) return 18;
        if (categoria.contains("lácteo")) return 3;
        if (categoria.contains("legumbre")) return 8;
        if (categoria.contains("cereal")) return 10;
        return 2; // Valor por defecto
    }

    private double estimarCarbohidratos(String categoria) {
        categoria = categoria.toLowerCase();
        if (categoria.contains("fruta")) return 12;
        if (categoria.contains("verdura")) return 5;
        if (categoria.contains("cereal")) return 70;
        if (categoria.contains("pan")) return 50;
        if (categoria.contains("lácteo")) return 5;
        return 10; // Valor por defecto
    }

    private double estimarGrasas(String categoria) {
        categoria = categoria.toLowerCase();
        if (categoria.contains("carne")) return 10;
        if (categoria.contains("pescado")) return 8;
        if (categoria.contains("lácteo")) return 3;
        if (categoria.contains("aceite")) return 100;
        return 1; // Valor por defecto
    }

    public boolean isApiDisponible() {
        return apiDisponible;
    }

    public void cerrar() {
        executorService.shutdown();
    }
}