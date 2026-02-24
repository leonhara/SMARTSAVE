package smartsave.api;

import com.fasterxml.jackson.databind.JsonNode;
import smartsave.modelo.Producto;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MercadonaAdapter {

    
    private static final Map<String, Producto> CACHE_PRODUCTOS = new ConcurrentHashMap<>();
    
    private static final long CACHE_TTL_MS = TimeUnit.HOURS.toMillis(1);
    
    private static volatile long ultimaLimpiezaCacheMs = System.currentTimeMillis();

    
    private static final Map<String, String> MAPEO_CATEGORIAS = new HashMap<>();
    static {
        
        MAPEO_CATEGORIAS.put("Frutas y verduras", "Frutas");
        MAPEO_CATEGORIAS.put("Fruta", "Frutas");
        MAPEO_CATEGORIAS.put("Verdura", "Verduras");
        MAPEO_CATEGORIAS.put("Carnicería", "Carnes");
        MAPEO_CATEGORIAS.put("Pescadería", "Pescados");
        MAPEO_CATEGORIAS.put("Charcutería", "Carnes");
        MAPEO_CATEGORIAS.put("Quesos", "Lácteos");
        MAPEO_CATEGORIAS.put("Lácteos", "Lácteos");
        MAPEO_CATEGORIAS.put("Huevos", "Lácteos");
        MAPEO_CATEGORIAS.put("Panadería", "Panadería");
        MAPEO_CATEGORIAS.put("Pastelería", "Panadería");
        MAPEO_CATEGORIAS.put("Cereales", "Cereales");
        MAPEO_CATEGORIAS.put("Legumbres", "Legumbres");
        MAPEO_CATEGORIAS.put("Pasta", "Cereales");
        MAPEO_CATEGORIAS.put("Aceites", "Aceites");
        
    }

    
    private static class ProductoCacheado {
        final Producto producto;
        final long timestamp;

        ProductoCacheado(Producto producto) {
            this.producto = producto;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public static Producto convertirNodoAProducto(JsonNode productNode) {
        try {
            
            String idMercadona = productNode.has("id") ? productNode.get("id").asText() : null;
            if (idMercadona != null && CACHE_PRODUCTOS.containsKey(idMercadona)) {
                return CACHE_PRODUCTOS.get(idMercadona);
            }

            limpiarCacheAntigua();

            String nombre = productNode.has("name") ? productNode.get("name").asText() : "";
            String marca = productNode.has("brand") ? productNode.get("brand").asText() : "Mercadona";
            String categoriaOriginal = productNode.has("category") ? productNode.get("category").asText() : "";
            double precio = productNode.has("unit_price") ? productNode.get("unit_price").asDouble() : 0.0;

            String categoriaAdaptada = mapearCategoria(categoriaOriginal);

            Producto producto = new Producto();
            producto.setNombre(limpiarTexto(nombre));
            producto.setMarca(limpiarTexto(marca));
            producto.setCategoria(categoriaAdaptada);
            producto.setPrecioBD(BigDecimal.valueOf(precio));
            producto.setSupermercado("Mercadona");
            producto.setDisponible(true);

            
            if (idMercadona != null) {
                try {
                    Long idReal;
                    if (idMercadona.contains(".")) {
                        idReal = (long) Double.parseDouble(idMercadona);
                    } else {
                        idReal = Long.parseLong(idMercadona);
                    }
                    producto.setId(idReal);
                } catch (NumberFormatException e) {
                    System.err.println("ID Mercadona no convertible a Long: " + idMercadona);
                    return null;
                }
            }

            configurarInfoNutricional(producto, categoriaAdaptada);

            if (idMercadona != null) {
                CACHE_PRODUCTOS.put(idMercadona, producto);
            }

            
            if (producto.getNombre().isEmpty() || producto.getMarca().isEmpty() ||
                    producto.getCategoria().isEmpty() || producto.getPrecioBD() == null ||
                    producto.getSupermercado().isEmpty()) {
                System.err.println("Producto Mercadona con campos obligatorios vacíos: " + producto);
                return null;
            }

            return producto;
        } catch (Exception e) {
            System.err.println("Error convirtiendo producto de Mercadona: " + e.getMessage());
            return null;
        }
    }

    private static String mapearCategoria(String categoriaOriginal) {
        if (categoriaOriginal == null || categoriaOriginal.isEmpty()) {
            return "Otros";
        }

        
        String categoria = MAPEO_CATEGORIAS.get(categoriaOriginal);
        if (categoria != null) {
            return categoria;
        }

        
        String categoriaLower = categoriaOriginal.toLowerCase();
        if (categoriaLower.contains("fruta")) return "Frutas";
        if (categoriaLower.contains("verdura")) return "Verduras";
        if (categoriaLower.contains("carne")) return "Carnes";
        if (categoriaLower.contains("pescado")) return "Pescados";
        if (categoriaLower.contains("lácteo") || categoriaLower.contains("lacteo")) return "Lácteos";
        if (categoriaLower.contains("leche")) return "Lácteos";
        if (categoriaLower.contains("pan")) return "Panadería";
        if (categoriaLower.contains("cereal")) return "Cereales";
        if (categoriaLower.contains("legumbre")) return "Legumbres";
        if (categoriaLower.contains("aceite")) return "Aceites";

        return "Otros";
    }

    private static void configurarInfoNutricional(Producto producto, String categoria) {
        Producto.NutricionProducto infoNutricional = producto.getInfoNutricional();
        if (infoNutricional == null) {
            infoNutricional = new Producto.NutricionProducto();
            producto.setInfoNutricional(infoNutricional);
        }

        switch (categoria) {
            case "Frutas":
                infoNutricional.setCalorias(50);
                infoNutricional.setProteinas(0.5);
                infoNutricional.setCarbohidratos(12);
                infoNutricional.setGrasas(0.2);
                infoNutricional.setFibra(2);
                infoNutricional.setAzucares(10);
                break;
            case "Verduras":
                infoNutricional.setCalorias(25);
                infoNutricional.setProteinas(1.5);
                infoNutricional.setCarbohidratos(5);
                infoNutricional.setGrasas(0.2);
                infoNutricional.setFibra(3);
                infoNutricional.setAzucares(2);
                break;
            case "Carnes":
                infoNutricional.setCalorias(200);
                infoNutricional.setProteinas(25);
                infoNutricional.setCarbohidratos(0);
                infoNutricional.setGrasas(12);
                infoNutricional.setSodio(60);
                break;
            case "Pescados":
                infoNutricional.setCalorias(150);
                infoNutricional.setProteinas(22);
                infoNutricional.setCarbohidratos(0);
                infoNutricional.setGrasas(6);
                infoNutricional.setSodio(50);
                break;
            case "Lácteos":
                infoNutricional.setCalorias(120);
                infoNutricional.setProteinas(5);
                infoNutricional.setCarbohidratos(9);
                infoNutricional.setGrasas(7);
                infoNutricional.setSodio(40);
                infoNutricional.setAzucares(5);
                break;
            case "Panadería":
                infoNutricional.setCalorias(250);
                infoNutricional.setProteinas(8);
                infoNutricional.setCarbohidratos(48);
                infoNutricional.setGrasas(2);
                infoNutricional.setFibra(2);
                infoNutricional.setSodio(500);
                break;
            case "Cereales":
                infoNutricional.setCalorias(350);
                infoNutricional.setProteinas(10);
                infoNutricional.setCarbohidratos(70);
                infoNutricional.setGrasas(2);
                infoNutricional.setFibra(7);
                infoNutricional.setSodio(5);
                break;
            case "Legumbres":
                infoNutricional.setCalorias(300);
                infoNutricional.setProteinas(20);
                infoNutricional.setCarbohidratos(50);
                infoNutricional.setGrasas(1.5);
                infoNutricional.setFibra(15);
                infoNutricional.setSodio(15);
                break;
            case "Aceites":
                infoNutricional.setCalorias(900);
                infoNutricional.setProteinas(0);
                infoNutricional.setCarbohidratos(0);
                infoNutricional.setGrasas(100);
                break;
            default:
                infoNutricional.setCalorias(100);
                infoNutricional.setProteinas(2);
                infoNutricional.setCarbohidratos(10);
                infoNutricional.setGrasas(1);
                break;
        }
    }

    private static void limpiarCacheAntigua() {
        long tiempoActual = System.currentTimeMillis();

        
        if (tiempoActual - ultimaLimpiezaCacheMs > TimeUnit.MINUTES.toMillis(30)) {
            CACHE_PRODUCTOS.entrySet().removeIf(entry ->
                    tiempoActual - ultimaLimpiezaCacheMs > CACHE_TTL_MS);

            ultimaLimpiezaCacheMs = tiempoActual;
        }
    }

    private static String limpiarTexto(String texto) {
        if (texto == null) return "";
        return texto.trim();
    }

    public static void limpiarCache() {
        CACHE_PRODUCTOS.clear();
        ultimaLimpiezaCacheMs = System.currentTimeMillis();
    }
}