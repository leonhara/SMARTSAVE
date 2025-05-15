package smartsave.servicio;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.util.List;
import smartsave.modelo.Producto;

public class CacheProductos {
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final int DURACION_CACHE_MINUTOS = 30;
    private static final int MAX_CACHE_SIZE = 100; // Añadir esta constante

    static class CacheEntry {
        List<Producto> productos;
        LocalDateTime timestamp;

        CacheEntry(List<Producto> productos) {
            this.productos = productos;
            this.timestamp = LocalDateTime.now();
        }

        boolean isValid() {
            return timestamp.isAfter(LocalDateTime.now().minusMinutes(DURACION_CACHE_MINUTOS));
        }
    }

    public List<Producto> get(String clave) {
        CacheEntry entry = cache.get(clave);
        if (entry != null && entry.isValid()) {
            return entry.productos;
        }
        return null;
    }

    public void put(String clave, List<Producto> productos) {
        // Limitar el tamaño del caché
        if (cache.size() >= MAX_CACHE_SIZE) {
            // Eliminar entradas más antiguas
            String oldestKey = cache.entrySet().stream()
                    .min(Map.Entry.comparingByValue(
                            (e1, e2) -> e1.timestamp.compareTo(e2.timestamp)))
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (oldestKey != null) {
                cache.remove(oldestKey);
            }
        }

        cache.put(clave, new CacheEntry(productos));
    }

    public void invalidar() {
        cache.clear();
    }

    public void invalidar(String clave) {
        cache.remove(clave);
    }
}