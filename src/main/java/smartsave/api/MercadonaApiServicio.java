package smartsave.api;

import smartsave.modelo.Producto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.net.URLEncoder;
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
    private Process pythonServerProcess;
    private final String codigoPostal;
    private boolean apiDisponible;

    private final OkHttpClient httpClient;
    private static final String API_BASE_URL = "http://127.0.0.1:5000";
    private final MercadonaSearchCache searchCache = new MercadonaSearchCache();

    public MercadonaApiServicio(String codigoPostal) {
        this.objectMapper = new ObjectMapper();
        this.executorService = Executors.newFixedThreadPool(2);
        this.codigoPostal = codigoPostal != null ? codigoPostal : "14010";

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        try {
            this.pythonScriptPath = prepararScriptPython();
            iniciarServidorPython();
            this.apiDisponible = verificarDisponibilidadApi();
        } catch (IOException e) {
            System.err.println("Error crítico al inicializar el servidor Python: " + e.getMessage());
            e.printStackTrace();
            this.apiDisponible = false;
        }

        iniciarLimpiadorCache();

        Runtime.getRuntime().addShutdownHook(new Thread(this::cerrar));
    }

    private Path prepararScriptPython() throws IOException {
        try (InputStream scriptStream = MercadonaApiServicio.class.getResourceAsStream("/api/mercadona_bridge.py")) {
            if (scriptStream == null) {
                throw new IOException("No se pudo encontrar 'mercadona_bridge.py' en el JAR.");
            }
            Path tempScript = Files.createTempFile("mercadona_bridge_", ".py");
            tempScript.toFile().deleteOnExit();

            Files.copy(scriptStream, tempScript, StandardCopyOption.REPLACE_EXISTING);
            return tempScript;
        }
    }

    private void iniciarServidorPython() {
        try {
            System.out.println("Arrancando el microservicio Python (Flask) en segundo plano...");
            ProcessBuilder processBuilder = new ProcessBuilder("python", this.pythonScriptPath.toAbsolutePath().toString());
            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
            processBuilder.environment().put("LANG", "C.UTF-8");

            processBuilder.redirectErrorStream(true);

            this.pythonServerProcess = processBuilder.start();

            Thread.sleep(2000);
        } catch (Exception e) {
            System.err.println("Error arrancando el servidor Flask de Python: " + e.getMessage());
        }
    }

    private boolean verificarDisponibilidadApi() {
        System.out.println("Verificando conexión con el microservicio Python...");
        int intentos = 0;

        while (intentos < 3) {
            try {
                Request request = new Request.Builder()
                        .url(API_BASE_URL + "/health")
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        System.out.println("¡Conexión establecida con el microservicio Python por el puerto 5000!");
                        return true;
                    }
                }
            } catch (Exception e) {
                System.out.println("Esperando a que el servidor Python esté listo... (Intento " + (intentos + 1) + ")");
            }
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            intentos++;
        }

        System.err.println("No se pudo conectar con el microservicio Python.");
        return false;
    }

    public CompletableFuture<List<Producto>> buscarProductos(String termino) {
        return CompletableFuture.supplyAsync(() -> {
            if (!apiDisponible) return new ArrayList<>();

            String terminoNormalizado = termino.toLowerCase().trim();
            String cacheKey = "search:" + terminoNormalizado + ":" + codigoPostal;
            List<Producto> productosCache = searchCache.get(cacheKey);

            if (productosCache != null) return productosCache;

            try {
                String url = API_BASE_URL + "/search?q=" + URLEncoder.encode(terminoNormalizado, StandardCharsets.UTF_8.toString())
                        + "&postcode=" + codigoPostal + "&limit=25";

                String jsonResponse = realizarPeticionHttp(url);
                List<Producto> productos = parsearProductos(jsonResponse);
                searchCache.put(cacheKey, productos);
                return productos;
            } catch (Exception e) {
                System.err.println("Error en petición HTTP de búsqueda: " + e.getMessage());
                return new ArrayList<>();
            }
        }, executorService);
    }

    public CompletableFuture<List<Producto>> obtenerProductosNuevos() {
        return CompletableFuture.supplyAsync(() -> {
            if (!apiDisponible) return new ArrayList<>();

            String cacheKey = "new_products:" + codigoPostal;
            List<Producto> productosCache = searchCache.get(cacheKey);
            if (productosCache != null) return productosCache;

            try {
                // Petición GET web para productos nuevos
                String url = API_BASE_URL + "/new?postcode=" + codigoPostal + "&limit=30";
                String jsonResponse = realizarPeticionHttp(url);
                List<Producto> productos = parsearProductos(jsonResponse);
                searchCache.put(cacheKey, productos);
                return productos;
            } catch (Exception e) {
                System.err.println("Error en petición HTTP de productos nuevos: " + e.getMessage());
                return new ArrayList<>();
            }
        }, executorService);
    }

    private String realizarPeticionHttp(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Código inesperado del servidor HTTP: " + response);
            }
            if (response.body() != null) {
                return response.body().string();
            }
            return "";
        }
    }

    private List<Producto> parsearProductos(String json) {
        List<Producto> productos = new ArrayList<>();
        try {
            if (json == null || json.isEmpty()) return productos;
            JsonNode root = objectMapper.readTree(json);

            if (root.has("success") && root.get("success").asBoolean()) {
                JsonNode data = root.get("data");
                if (data != null && data.isArray()) {
                    for (JsonNode productNode : data) {
                        try {
                            Producto producto = MercadonaAdapter.convertirNodoAProducto(productNode);
                            if (producto != null) productos.add(producto);
                        } catch (Exception e) {
                            System.err.println("Error procesando un producto individual: " + e.getMessage());
                        }
                    }
                }
            } else {
                String errorMsg = root.has("error") ? root.get("error").asText() : "Error desconocido de API";
                System.err.println("La API Python devolvió un error: " + errorMsg);
            }
        } catch (Exception e) {
            System.err.println("Error parseando JSON: " + e.getMessage());
        }
        return productos;
    }

    public boolean isApiDisponible() {
        return apiDisponible;
    }

    public void cerrar() {
        //cargarse el servidor de python cuando se cierre la app
        if (pythonServerProcess != null && pythonServerProcess.isAlive()) {
            System.out.println("Apagando el microservicio Python...");
            pythonServerProcess.destroy();
        }

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
                System.err.println("No se pudo eliminar el script temporal: " + pythonScriptPath);
            }
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
            boolean isExpired() { return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS; }
        }
        private final java.util.Map<String, CacheEntry> cache = Collections.synchronizedMap(new java.util.LinkedHashMap<>(100, 0.75f, true));

        List<Producto> get(String cacheKey) {
            CacheEntry entry = cache.get(cacheKey);
            if (entry != null && !entry.isExpired()) return new ArrayList<>(entry.productos);
            cache.remove(cacheKey);
            return null;
        }

        void put(String cacheKey, List<Producto> productos) {
            cache.put(cacheKey, new CacheEntry(new ArrayList<>(productos)));
            if (cache.size() > 100) cache.remove(cache.keySet().iterator().next());
        }

        void clean() {
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }
    }

    private void iniciarLimpiadorCache() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                searchCache::clean, 10, 10, TimeUnit.MINUTES
        );
    }
}