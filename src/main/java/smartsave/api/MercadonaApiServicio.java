package smartsave.api;

import smartsave.modelo.Producto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MercadonaApiServicio {

    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    private Path pythonScriptPath; 
    private final String codigoPostal;
    private boolean apiDisponible;

    private static final long MIN_TIEMPO_ENTRE_PETICIONES_MS = 500;
    private long ultimaPeticionTimestamp = 0;

    private final MercadonaSearchCache searchCache = new MercadonaSearchCache();

    public MercadonaApiServicio(String codigoPostal) {
        this.objectMapper = new ObjectMapper();
        this.executorService = Executors.newFixedThreadPool(2);
        this.codigoPostal = codigoPostal != null ? codigoPostal : "14010";

        try {
            
            this.pythonScriptPath = prepararScriptPython();
            this.apiDisponible = verificarDisponibilidadApi();
        } catch (IOException e) {
            System.err.println("Error crítico al preparar el script de Python: " + e.getMessage());
            e.printStackTrace(); 
            this.apiDisponible = false;
        }

        iniciarLimpiadorCache();
    }

    private Path prepararScriptPython() throws IOException {
        
        try (InputStream scriptStream = MercadonaApiServicio.class.getResourceAsStream("/api/mercadona_bridge.py")) {
            if (scriptStream == null) {
                throw new IOException("No se pudo encontrar el script 'mercadona_bridge.py' en el JAR. Verifica la ruta: /api/mercadona_bridge.py");
            }
            
            Path tempScript = Files.createTempFile("mercadona_bridge_", ".py");
            tempScript.toFile().deleteOnExit(); 

            Files.copy(scriptStream, tempScript, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Script de Python extraído a: " + tempScript.toAbsolutePath().toString());
            return tempScript;
        }
    }

    private static class MercadonaSearchCache {
        private static final long CACHE_EXPIRY_MS = TimeUnit.MINUTES.toMillis(15); 
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
                return new ArrayList<>(entry.productos);
            }
            cache.remove(cacheKey);
            return null;
        }

        void put(String cacheKey, List<Producto> productos) {
            cache.put(cacheKey, new CacheEntry(new ArrayList<>(productos)));
            
            if (cache.size() > 100) {
                String oldestKey = cache.keySet().iterator().next();
                cache.remove(oldestKey);
            }
        }

        void clean() {
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }
    }

    private void iniciarLimpiadorCache() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                searchCache::clean,
                10,
                10,
                TimeUnit.MINUTES
        );
    }

    public CompletableFuture<List<Producto>> buscarProductos(String termino) {
        return CompletableFuture.supplyAsync(() -> {
            if (!apiDisponible || this.pythonScriptPath == null) {
                System.out.println("API de Mercadona no disponible o script no preparado para búsqueda.");
                return new ArrayList<>();
            }

            String terminoNormalizado = termino.toLowerCase().trim();
            String cacheKey = "search:" + terminoNormalizado + ":" + codigoPostal;
            List<Producto> productosCache = searchCache.get(cacheKey);
            if (productosCache != null) {
                System.out.println("Productos obtenidos de caché para: " + terminoNormalizado);
                return productosCache;
            }

            esperarControlTasa();

            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "python", this.pythonScriptPath.toAbsolutePath().toString(),
                        "search", terminoNormalizado,
                        "--postcode", codigoPostal,
                        "--limit", "25"
                );

                String resultado = ejecutarProcesoConBuilder(processBuilder);
                List<Producto> productos = parsearProductos(resultado);
                searchCache.put(cacheKey, productos);
                return productos;
            } catch (Exception e) {
                System.err.println("Error al buscar productos de Mercadona ("+termino+"): " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        }, executorService);
    }

    public CompletableFuture<List<Producto>> obtenerProductosNuevos() {
        return CompletableFuture.supplyAsync(() -> {
            if (!apiDisponible || this.pythonScriptPath == null) {
                System.out.println("API de Mercadona no disponible o script no preparado para obtener nuevos productos.");
                return new ArrayList<>();
            }

            String cacheKey = "new_products:" + codigoPostal;
            List<Producto> productosCache = searchCache.get(cacheKey);
            if (productosCache != null) {
                System.out.println("Productos nuevos obtenidos de caché.");
                return productosCache;
            }

            esperarControlTasa();

            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "python", this.pythonScriptPath.toAbsolutePath().toString(),
                        "new", "--postcode", codigoPostal,
                        "--limit", "30" 
                );

                String resultado = ejecutarProcesoConBuilder(processBuilder);
                List<Producto> productos = parsearProductos(resultado);
                searchCache.put(cacheKey, productos);
                return productos;
            } catch (Exception e) {
                System.err.println("Error al obtener productos nuevos de Mercadona: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        }, executorService);
    }

    private synchronized void esperarControlTasa() {
        long tiempoActual = System.currentTimeMillis();
        long tiempoDesdeUltimaPeticion = tiempoActual - ultimaPeticionTimestamp;

        if (tiempoDesdeUltimaPeticion < MIN_TIEMPO_ENTRE_PETICIONES_MS) {
            try {
                Thread.sleep(MIN_TIEMPO_ENTRE_PETICIONES_MS - tiempoDesdeUltimaPeticion);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Interrupción durante espera de control de tasa.");
            }
        }
        ultimaPeticionTimestamp = System.currentTimeMillis();
    }

    private boolean verificarDisponibilidadApi() {
        try {
            if (this.pythonScriptPath == null || !Files.exists(this.pythonScriptPath)) {
                System.err.println("El script Python no existe o no está inicializado en la ruta: " + (this.pythonScriptPath != null ? this.pythonScriptPath.toString() : "null"));
                return false;
            }

            ProcessBuilder pb = new ProcessBuilder("python", "-c", "import mercapy; print('OK')");
            System.out.println("Verificando disponibilidad de mercapy...");
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            StringBuilder error = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line).append("\n");
                }
            }

            if (!process.waitFor(10, TimeUnit.SECONDS)) { 
                process.destroyForcibly(); 
                System.err.println("Timeout verificando mercapy. Proceso destruido.");
                return false;
            }

            boolean disponible = process.exitValue() == 0 && output.toString().trim().equals("OK");

            if (disponible) {
                System.out.println("Mercapy parece estar disponible.");
                
                return true;
            } else {
                System.err.println("Mercapy no disponible o no responde como se esperaba.");
                if (!output.toString().trim().equals("OK")) System.err.println("Salida inesperada: " + output.toString());
                if (error.length() > 0) System.err.println("Error en el proceso: " + error.toString());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error verificando mercapy: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void testScriptPython() {
        if (this.pythonScriptPath == null) {
            System.err.println("No se puede testear el script de Python, ruta no definida.");
            return;
        }
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "python", this.pythonScriptPath.toAbsolutePath().toString(),
                    "search", "leche", 
                    "--postcode", codigoPostal, "--limit", "1"
            );
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process process = pb.start();
            StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            if (!process.waitFor(15, TimeUnit.SECONDS)) { 
                process.destroyForcibly();
                System.err.println("Timeout en test del script Python. Proceso destruido.");
                this.apiDisponible = false; 
                return;
            }

            if (process.exitValue() == 0) {
                System.out.println("Test de script Python con mercadona_bridge.py exitoso.");
                
            } else {
                System.err.println("Test de script Python con mercadona_bridge.py falló, código: " + process.exitValue());
                System.err.println("Salida del test: " + output.toString());
                this.apiDisponible = false; 
            }
        } catch (Exception e) {
            System.err.println("Error en test de script Python con mercadona_bridge.py: " + e.getMessage());
            e.printStackTrace();
            this.apiDisponible = false; 
        }
    }


    private String ejecutarProcesoConBuilder(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
        processBuilder.environment().put("LANG", "C.UTF-8"); 

        Process process = processBuilder.start();
        StringBuilder output = new StringBuilder();

        try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = outputReader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IOException("Timeout ejecutando el script Python (30 segundos). Proceso destruido.");
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            System.err.println("Salida del script Python (stdout): " + output.toString());
            
            throw new IOException("Error ejecutando script Python. Exit code: " + exitCode + ". Revisa la consola para los errores de Python.");
        }

        return output.toString().trim();
    }

    private List<Producto> parsearProductos(String json) {
        List<Producto> productos = new ArrayList<>();
        try {
            if (json == null || json.isEmpty()) {
                System.out.println("Respuesta JSON vacía o nula de Mercadona API.");
                return productos;
            }

            JsonNode root = objectMapper.readTree(json);

            if (root.has("success") && root.get("success").asBoolean()) {
                JsonNode data = root.get("data");
                if (data != null && data.isArray()) {
                    for (JsonNode productNode : data) {
                        try {
                            Producto producto = MercadonaAdapter.convertirNodoAProducto(productNode); 
                            if (producto != null) {
                                productos.add(producto);
                            }
                        } catch (Exception e) {
                            System.err.println("Error procesando un producto individual de Mercadona: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    System.out.println("Parseados " + productos.size() + " productos de Mercadona.");
                } else {
                    System.out.println("La respuesta JSON de Mercadona no contiene un array de datos o 'data' es null.");
                }
            } else {
                String errorMsg = root.has("error") ? root.get("error").asText("Error desconocido") : "La API indicó un fallo sin mensaje de error.";
                System.err.println("La API de Mercadona devolvio un error: " + errorMsg);
                if (root.has("details")) {
                    System.err.println("Detalles del error: " + root.get("details").asText());
                }
            }
        } catch (Exception e) {
            System.err.println("Error crítico parseando la respuesta JSON de Mercadona: " + e.getMessage());
            e.printStackTrace();
            System.err.println("JSON problemático: " + json.substring(0, Math.min(json.length(), 500)) + "..."); 
        }
        return productos;
    }

    public boolean isApiDisponible() {
        return apiDisponible;
    }

    public void cerrar() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        if (pythonScriptPath != null) {
            try {
                Files.deleteIfExists(pythonScriptPath);
            } catch (IOException e) {
                System.err.println("No se pudo eliminar el script temporal: " + pythonScriptPath.toString() + " - " + e.getMessage());
            }
        }
    }
}