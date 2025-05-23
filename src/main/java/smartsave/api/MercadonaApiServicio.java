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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Servicio optimizado para integrar con la API de Mercadona usando mercapy (Python)
 */
public class MercadonaApiServicio {

    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    private final String pythonScriptPath;
    private final String codigoPostal;
    private boolean apiDisponible;

    // Control de tasa de peticiones para no sobrecargar la API
    private static final long MIN_TIEMPO_ENTRE_PETICIONES_MS = 500; // 500ms mínimo entre peticiones
    private long ultimaPeticionTimestamp = 0;

    // Cache de búsquedas recientes para mejorar rendimiento
    private final MercadonaSearchCache searchCache = new MercadonaSearchCache();

    public MercadonaApiServicio(String codigoPostal) {
        this.objectMapper = new ObjectMapper();
        this.executorService = Executors.newFixedThreadPool(2); // Limitamos a 2 hilos para no sobrecargar
        this.codigoPostal = codigoPostal != null ? codigoPostal : "28001"; // Madrid por defecto

        // Buscar el script Python
        this.pythonScriptPath = encontrarScriptPython();
        this.apiDisponible = verificarDisponibilidadApi();

        // Limpiar caché periódicamente
        iniciarLimpiadorCache();
    }

    /**
     * Clase interna para manejo de caché de búsquedas
     */
    private static class MercadonaSearchCache {
        private static final long CACHE_EXPIRY_MS = TimeUnit.MINUTES.toMillis(15); // 15 minutos de validez
        private static class CacheEntry {
            final List<Producto> productos;
            final long timestamp;

            CacheEntry(List<Producto> productos) {
                this.productos = productos;
                this.timestamp = System.currentTimeMillis();
            }

            boolean isExpired() {
                return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS;
            }
        }

        private final java.util.Map<String, CacheEntry> cache = Collections.synchronizedMap(new java.util.LinkedHashMap<>(100, 0.75f, true));

        List<Producto> get(String cacheKey) {
            CacheEntry entry = cache.get(cacheKey);
            if (entry != null && !entry.isExpired()) {
                return new ArrayList<>(entry.productos); // Devolver copia para evitar modificaciones
            }
            return null;
        }

        void put(String cacheKey, List<Producto> productos) {
            cache.put(cacheKey, new CacheEntry(new ArrayList<>(productos)));
            // Limitar tamaño de caché
            if (cache.size() > 100) {
                String oldestKey = cache.keySet().iterator().next();
                cache.remove(oldestKey);
            }
        }

        void clean() {
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }
    }

    /**
     * Inicia un limpiador de caché periódico
     */
    private void iniciarLimpiadorCache() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                searchCache::clean,
                10,
                10,
                TimeUnit.MINUTES
        );
    }

    /**
     * Busca productos en Mercadona con caché y control de tasa
     */
    public CompletableFuture<List<Producto>> buscarProductos(String termino) {
        return CompletableFuture.supplyAsync(() -> {
            if (!apiDisponible) {
                System.out.println("API de Mercadona no disponible para búsqueda");
                return new ArrayList<>();
            }

            // Normalizar el término de búsqueda (minúsculas, sin espacios extra)
            String terminoNormalizado = termino.toLowerCase().trim();

            // Verificar caché
            String cacheKey = "search:" + terminoNormalizado;
            List<Producto> productosCache = searchCache.get(cacheKey);
            if (productosCache != null) {
                System.out.println("Productos obtenidos de caché para: " + terminoNormalizado);
                return productosCache;
            }

            // Control de tasa
            esperarControlTasa();

            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "python", pythonScriptPath, "search", terminoNormalizado,
                        "--postcode", codigoPostal,
                        "--limit", "25"
                );

                String resultado = ejecutarProcesoConBuilder(processBuilder);
                List<Producto> productos = parsearProductos(resultado);

                // Guardar en caché
                searchCache.put(cacheKey, productos);

                return productos;
            } catch (Exception e) {
                System.err.println("Error al buscar productos de Mercadona: " + e.getMessage());
                return new ArrayList<>();
            }
        }, executorService);
    }

    /**
     * Obtiene productos nuevos de Mercadona con caché
     */
    public CompletableFuture<List<Producto>> obtenerProductosNuevos() {
        return CompletableFuture.supplyAsync(() -> {
            if (!apiDisponible) {
                return new ArrayList<>();
            }

            // Verificar caché
            String cacheKey = "new_products";
            List<Producto> productosCache = searchCache.get(cacheKey);
            if (productosCache != null) {
                return productosCache;
            }

            // Control de tasa
            esperarControlTasa();

            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "python", pythonScriptPath, "new", "--postcode", codigoPostal
                );

                String resultado = ejecutarProcesoConBuilder(processBuilder);
                List<Producto> productos = parsearProductos(resultado);

                // Guardar en caché
                searchCache.put(cacheKey, productos);

                return productos;
            } catch (Exception e) {
                System.err.println("Error al obtener productos nuevos de Mercadona: " + e.getMessage());
                return new ArrayList<>();
            }
        }, executorService);
    }

    /**
     * Espera si es necesario para controlar la tasa de peticiones
     */
    private synchronized void esperarControlTasa() {
        long tiempoActual = System.currentTimeMillis();
        long tiempoDesdeUltimaPeticion = tiempoActual - ultimaPeticionTimestamp;

        if (tiempoDesdeUltimaPeticion < MIN_TIEMPO_ENTRE_PETICIONES_MS) {
            try {
                Thread.sleep(MIN_TIEMPO_ENTRE_PETICIONES_MS - tiempoDesdeUltimaPeticion);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ultimaPeticionTimestamp = System.currentTimeMillis();
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

        return "src/main/java/smartsave/api/mercadona_bridge.py";
    }

    private boolean verificarDisponibilidadApi() {
        try {
            // Verificar existencia del script Python primero
            if (!Files.exists(Paths.get(pythonScriptPath))) {
                System.err.println("El script Python no existe en la ruta: " + pythonScriptPath);
                return false;
            }

            // Verificar python y mercapy
            ProcessBuilder pb = new ProcessBuilder("python", "-c", "import mercapy; print('OK')");
            System.out.println("Verificando disponibilidad de mercapy...");
            Process process = pb.start();

            // Capturar la salida para diagnóstico
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            StringBuilder error = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line).append("\n");
                }
            }

            // Esperar con timeout para no bloquear
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroy();
                System.err.println("Timeout verificando mercapy");
                return false;
            }

            boolean disponible = process.exitValue() == 0;

            if (disponible) {
                System.out.println("API de Mercadona disponible. Usando datos reales.");
                // Probar el script con una búsqueda simple para verificar que funciona
                testScriptPython();
                return true;
            } else {
                System.err.println("Mercapy no disponible. Salida: " + output.toString());
                System.err.println("Error: " + error.toString());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error verificando mercapy: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void testScriptPython() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "python", pythonScriptPath, "search", "pan",
                    "--postcode", codigoPostal, "--limit", "1"
            );
            // pb.redirectErrorStream(true); // ELIMINADO
            pb.redirectError(ProcessBuilder.Redirect.INHERIT); // Opcional: ver logs en consola

            Process process = pb.start();
            StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroy();
                System.err.println("Timeout en test del script Python");
                return;
            }

            if (process.exitValue() == 0) {
                System.out.println("Test de script Python exitoso");
            } else {
                System.err.println("Test de script Python falló, código: " + process.exitValue());
                System.err.println("Salida: " + output.toString());
            }
        } catch (Exception e) {
            System.err.println("Error en test de script Python: " + e.getMessage());
        }
    }

    private String ejecutarProcesoConBuilder(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        // processBuilder.redirectErrorStream(true); // ELIMINADO
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT); // Opcional: ver logs en consola

        // Establecer variables de entorno para UTF-8
        processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
        processBuilder.environment().put("LANG", "C.UTF-8");

        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();

        // Leer stdout mientras se ejecuta el proceso para evitar deadlocks
        try (BufferedReader outputReader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "UTF-8"))) {

            String line;
            while ((line = outputReader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        // Esperar finalización con timeout
        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroy();
            throw new IOException("Timeout ejecutando el script Python");
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new IOException("Error ejecutando script Python. Exit code: " + exitCode);
        }

        return output.toString().trim();
    }

    private List<Producto> parsearProductos(String json) {
        List<Producto> productos = new ArrayList<>();

        try {
            if (json == null || json.isEmpty()) {
                return productos;
            }

            JsonNode root = objectMapper.readTree(json);

            if (root.has("success") && root.get("success").asBoolean()) {
                JsonNode data = root.get("data");
                if (data != null && data.isArray()) {
                    for (JsonNode productNode : data) {
                        try {
                            // Usar el adaptador para convertir nodo a producto
                            Producto producto = MercadonaAdapter.convertirNodoAProducto(productNode);
                            if (producto != null) {
                                productos.add(producto);
                            }
                        } catch (Exception e) {
                            System.err.println("Error procesando producto de Mercadona: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parseando productos de Mercadona: " + e.getMessage());
        }

        return productos;
    }

    public boolean isApiDisponible() {
        return apiDisponible;
    }

    public void cerrar() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}