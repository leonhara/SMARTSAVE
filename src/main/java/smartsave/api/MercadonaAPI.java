package smartsave.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import smartsave.modelo.Producto;

public class MercadonaAPI {
    private static final String API_URL = "https://tienda.mercadona.es/";
    private static final String ALGOLIA_APP_ID = "7UZJKL1DJ0";
    private static final String ALGOLIA_API_KEY = "9d8f2e9e90df472b4f2e559a116fe17";

    private String warehouse;
    private String language;
    private OkHttpClient client;
    private ObjectMapper objectMapper;

    public MercadonaAPI(String postalCode, String language) {
        this.language = language;
        this.objectMapper = new ObjectMapper();

        // Configurar cliente HTTP con timeouts
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // Obtener el warehouse code basado en el código postal
        this.warehouse = getWarehouseCode(postalCode);
    }

    public String getWarehouseCode(String postalCode) {
        String url = API_URL + "api/postal-codes/actions/change-pc/";
        RequestBody body = RequestBody.create(
                MediaType.get("application/json"),
                "{\"new_postal_code\":\"" + postalCode + "\"}"
        );

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String warehouseCode = response.header("X-Customer-Wh");
                return warehouseCode != null ? warehouseCode : "mad1";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "mad1"; // Default warehouse
    }

    public List<Producto> buscarProductos(String query) {
        List<Producto> productos = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return productos;
        }

        String url = String.format(
                "https://7uzjkl1dj0-dsn.algolia.net/1/indexes/products_prod_%s_%s/query",
                warehouse, language
        );

        // Crear el payload JSON usando ObjectMapper
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("params", "query=" + query.trim());

            RequestBody body = RequestBody.create(
                    MediaType.get("application/json"),
                    objectMapper.writeValueAsString(payload)
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("x-algolia-application-id", ALGOLIA_APP_ID)
                    .addHeader("x-algolia-api-key", ALGOLIA_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String jsonString = response.body().string();
                    JsonNode jsonNode = objectMapper.readTree(jsonString);
                    JsonNode hits = jsonNode.get("hits");

                    for (JsonNode hit : hits) {
                        Producto producto = parseProductoFromJson(hit);
                        if (producto != null) {
                            productos.add(producto);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return productos;
    }

    public List<Producto> obtenerTodosLosProductos() {
        // Obtener productos por categorías principales
        List<String> categoriasPopulares = List.of(
                "leche", "pan", "huevos", "aceite", "arroz", "pasta",
                "pollo", "ternera", "pescado", "frutas", "verduras"
        );

        List<Producto> todosProductos = new ArrayList<>();

        for (String categoria : categoriasPopulares) {
            List<Producto> productosCategoria = buscarProductos(categoria);

            // Limitar a 5 productos por categoría para no sobrecargar
            productosCategoria.stream()
                    .limit(5)
                    .forEach(todosProductos::add);
        }

        return todosProductos;
    }

    private Producto parseProductoFromJson(JsonNode json) {
        String id = json.get("id").asText();

        // Crear producto básico desde la respuesta de Algolia
        Producto producto = new Producto();
        producto.setId(Long.parseLong(id));
        producto.setNombre(json.has("display_name") ? json.get("display_name").asText() : "");

        // Obtener precio de Algolia
        if (json.has("price_instructions")) {
            JsonNode priceInstructions = json.get("price_instructions");
            if (priceInstructions.has("unit_price")) {
                producto.setPrecio(priceInstructions.get("unit_price").asDouble());
            }
        }

        // Obtener marca de Algolia o usar default
        if (json.has("brand")) {
            producto.setMarca(json.get("brand").asText());
        } else {
            producto.setMarca("Mercadona");
        }

        // Obtener categoría de Algolia
        if (json.has("categories") && json.get("categories").isArray() && json.get("categories").size() > 0) {
            JsonNode categoriesNode = json.get("categories");
            JsonNode firstCategory = categoriesNode.get(0);
            if (firstCategory.has("categories") && firstCategory.get("categories").isArray() &&
                    firstCategory.get("categories").size() > 0) {
                JsonNode subCategory = firstCategory.get("categories").get(0);
                if (subCategory.has("name")) {
                    producto.setCategoria(subCategory.get("name").asText());
                }
            }
        }

        if (producto.getCategoria() == null) {
            producto.setCategoria("General");
        }

        producto.setSupermercado("Mercadona");
        producto.setDisponible(true);

        // Crear información nutricional básica
        Producto.NutricionProducto nutricion = new Producto.NutricionProducto();
        producto.setInfoNutricional(nutricion);

        return producto;
    }

    public Producto obtenerDetallesProducto(String productId) {
        String url = API_URL + "api/products/" + productId + "/";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("lang", language)
                .addHeader("wh", warehouse)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String jsonString = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(jsonString);

                return crearProductoDetalladoFromJson(jsonNode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Producto crearProductoDetalladoFromJson(JsonNode json) {
        Producto producto = new Producto();

        try {
            producto.setId(Long.parseLong(json.get("id").asText()));
            producto.setNombre(json.get("display_name").asText());

            JsonNode brandNode = json.get("brand");
            if (brandNode != null && !brandNode.isNull()) {
                producto.setMarca(brandNode.asText());
            } else {
                producto.setMarca("Mercadona");
            }

            // Precio
            JsonNode priceInstructions = json.get("price_instructions");
            if (priceInstructions != null) {
                JsonNode unitPriceNode = priceInstructions.get("unit_price");
                if (unitPriceNode != null && !unitPriceNode.isNull()) {
                    producto.setPrecio(unitPriceNode.asDouble());
                }
            }

            // Información nutricional detallada
            JsonNode details = json.get("details");
            if (details != null) {
                Producto.NutricionProducto nutricion = new Producto.NutricionProducto();

                // Aquí puedes agregar más campos nutricionales si están disponibles
                // Por ejemplo, si el JSON contiene información nutricional:
                if (details.has("nutrition")) {
                    JsonNode nutrition = details.get("nutrition");
                    if (nutrition.has("energy")) {
                        nutricion.setCalorias(nutrition.get("energy").asDouble());
                    }
                    if (nutrition.has("proteins")) {
                        nutricion.setProteinas(nutrition.get("proteins").asDouble());
                    }
                    if (nutrition.has("carbohydrates")) {
                        nutricion.setCarbohidratos(nutrition.get("carbohydrates").asDouble());
                    }
                    if (nutrition.has("fats")) {
                        nutricion.setGrasas(nutrition.get("fats").asDouble());
                    }
                }

                producto.setInfoNutricional(nutricion);
            }

            // Categoría
            JsonNode categoriesNode = json.get("categories");
            if (categoriesNode != null && categoriesNode.isArray() && categoriesNode.size() > 0) {
                JsonNode firstCategory = categoriesNode.get(0);
                if (firstCategory.has("categories") && firstCategory.get("categories").isArray() &&
                        firstCategory.get("categories").size() > 0) {
                    JsonNode subCategory = firstCategory.get("categories").get(0);
                    if (subCategory.has("name")) {
                        producto.setCategoria(subCategory.get("name").asText());
                    }
                }
            }

            if (producto.getCategoria() == null) {
                producto.setCategoria("General");
            }

            producto.setSupermercado("Mercadona");
            producto.setDisponible(true);

        } catch (Exception e) {
            System.err.println("Error parsing product details: " + e.getMessage());
            e.printStackTrace();
        }

        return producto;
    }

    // Método para cambiar el código postal
    public void cambiarCodigoPostal(String nuevoCodigoPostal) {
        this.warehouse = getWarehouseCode(nuevoCodigoPostal);
    }

    // Getters
    public String getWarehouse() {
        return warehouse;
    }

    public String getLanguage() {
        return language;
    }
}