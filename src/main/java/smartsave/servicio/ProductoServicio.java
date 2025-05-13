package smartsave.servicio;

import smartsave.api.MercadonaAPI;
import smartsave.configuracion.ConfiguracionMercadona;
import smartsave.modelo.Producto;
import smartsave.modelo.Producto.NutricionProducto;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Random;


public class ProductoServicio {
    private MercadonaAPI mercadonaAPI;
    private CacheProductos cache;
    private List<Producto> productosLocales;

    public ProductoServicio() {
        // Inicializar con un código postal por defecto
        this.mercadonaAPI = new MercadonaAPI(ConfiguracionMercadona.getCodigoPostal(), "es");
        this.cache = new CacheProductos();
        this.productosLocales = new ArrayList<>();

        // Inicializar algunos productos de ejemplo
        inicializarProductosEjemplo();
    }

    private void inicializarProductosEjemplo() {
        // Lácteos
        Producto leche = new Producto("Leche Entera", "Central Lechera Asturiana", "Lácteos", 1.25, "Hipercor");
        leche.setInfoNutricional(new NutricionProducto(64, 3.2, 4.8, 3.6, 0, 50, 4.8));
        productosLocales.add(leche);

        Producto yogur = new Producto("Yogur Natural", "Danone", "Lácteos", 2.10, "El Corte Inglés");
        yogur.setInfoNutricional(new NutricionProducto(79, 4.5, 6.0, 3.3, 0, 50, 6.0));
        productosLocales.add(yogur);

        Producto queso = new Producto("Queso Manchego", "García Baquero", "Lácteos", 8.50, "DIA");
        queso.setInfoNutricional(new NutricionProducto(406, 29.5, 0.9, 32.4, 0, 580, 0.9));
        productosLocales.add(queso);

        // Carnes
        Producto pollo = new Producto("Pechuga de Pollo", "Sada", "Carnes", 6.99, "Mercadona");
        pollo.setInfoNutricional(new NutricionProducto(165, 31.0, 0, 3.6, 0, 74, 0));
        productosLocales.add(pollo);

        Producto ternera = new Producto("Filete de Ternera", "Fribin", "Carnes", 12.50, "Carrefour");
        ternera.setInfoNutricional(new NutricionProducto(174, 28.8, 0, 5.3, 0, 62, 0));
        productosLocales.add(ternera);

        // Pescados
        Producto salmon = new Producto("Salmón Fresco", "Mariscos del Norte", "Pescados", 15.99, "El Corte Inglés");
        salmon.setInfoNutricional(new NutricionProducto(206, 22.1, 0, 12.4, 0, 50, 0));
        productosLocales.add(salmon);

        Producto merluza = new Producto("Merluza", "Pescanova", "Pescados", 9.90, "Hipercor");
        merluza.setInfoNutricional(new NutricionProducto(92, 17.9, 0, 1.8, 0, 80, 0));
        productosLocales.add(merluza);

        // Frutas
        Producto manzanas = new Producto("Manzanas Golden", "Frutas Montaña", "Frutas", 2.50, "Mercadona");
        manzanas.setInfoNutricional(new NutricionProducto(52, 0.3, 13.8, 0.4, 2.4, 1, 10.4));
        productosLocales.add(manzanas);

        Producto platanos = new Producto("Plátanos de Canarias", "Platanología", "Frutas", 1.99, "LIDL");
        platanos.setInfoNutricional(new NutricionProducto(89, 1.1, 23.0, 0.3, 2.6, 1, 12.2));
        productosLocales.add(platanos);

        // Verduras
        Producto tomates = new Producto("Tomates Cherry", "Huerta Verde", "Verduras", 3.20, "Carrefour");
        tomates.setInfoNutricional(new NutricionProducto(18, 0.9, 3.9, 0.2, 1.2, 5, 2.6));
        productosLocales.add(tomates);

        Producto lechuga = new Producto("Lechuga Iceberg", "Verde Natura", "Verduras", 1.50, "DIA");
        lechuga.setInfoNutricional(new NutricionProducto(14, 0.9, 2.0, 0.1, 1.2, 10, 1.4));
        productosLocales.add(lechuga);

        // Cereales y Panadería
        Producto pan = new Producto("Pan Integral", "Bimbo", "Panadería", 1.80, "Mercadona");
        pan.setInfoNutricional(new NutricionProducto(247, 12.9, 39.4, 4.6, 8.5, 520, 3.4));
        productosLocales.add(pan);

        Producto arroz = new Producto("Arroz Bomba", "Calasparra", "Cereales", 3.50, "El Corte Inglés");
        arroz.setInfoNutricional(new NutricionProducto(381, 7.0, 85.2, 0.4, 0.3, 5, 0.2));
        productosLocales.add(arroz);

        // Legumbres
        Producto lentejas = new Producto("Lentejas Castellanas", "Cidacos", "Legumbres", 2.30, "LIDL");
        lentejas.setInfoNutricional(new NutricionProducto(337, 26.0, 51.1, 1.4, 11.5, 20, 1.9));
        productosLocales.add(lentejas);

        Producto garbanzos = new Producto("Garbanzos de Fuentes", "Conservas Dantza", "Legumbres", 2.80, "Hipercor");
        garbanzos.setInfoNutricional(new NutricionProducto(364, 19.3, 55.8, 5.0, 17.4, 16, 2.3));
        productosLocales.add(garbanzos);

        // Aceites y Condimentos
        Producto aceiteOliva = new Producto("Aceite de Oliva Virgen Extra", "Carbonell", "Aceites", 4.50, "Carrefour");
        aceiteOliva.setInfoNutricional(new NutricionProducto(900, 0, 0, 100, 0, 0, 0));
        productosLocales.add(aceiteOliva);

        // Bebidas
        Producto agua = new Producto("Agua Mineral", "Font Vella", "Bebidas", 0.89, "DIA");
        agua.setInfoNutricional(new NutricionProducto(0, 0, 0, 0, 0, 5, 0));
        productosLocales.add(agua);

        Producto zumo = new Producto("Zumo de Naranja", "Don Simon", "Bebidas", 1.95, "Mercadona");
        zumo.setInfoNutricional(new NutricionProducto(45, 0.8, 10.4, 0.2, 0.2, 3, 10.4));
        productosLocales.add(zumo);

        // Snacks y Dulces
        Producto galletas = new Producto("Galletas Digestive", "McVitie's", "Snacks", 2.50, "El Corte Inglés");
        galletas.setInfoNutricional(new NutricionProducto(477, 7.3, 68.0, 18.1, 4.0, 650, 15.0));
        productosLocales.add(galletas);

        // Productos de higiene
        Producto champu = new Producto("Champú Anticaspa", "Pantene", "Higiene", 3.99, "LIDL");
        productosLocales.add(champu);

        Producto jabon = new Producto("Jabón de Manos", "Palmolive", "Higiene", 1.50, "DIA");
        productosLocales.add(jabon);

        // Asignar IDs aleatorios
        Random random = new Random();
        for (Producto p : productosLocales) {
            p.setId((long) (random.nextInt(900000) + 100000));
        }
    }

    public List<Producto> buscarProductos(String termino) {
        List<Producto> productos = new ArrayList<>();

        // Verificar caché primero
        String cacheKey = "busqueda_" + termino.toLowerCase().trim();
        List<Producto> productosCache = cache.get(cacheKey);
        if (productosCache != null) {
            return productosCache;
        }

        // Agregar productos locales
        productos.addAll(buscarProductosLocales(termino));

        // Agregar productos de Mercadona
        try {
            List<Producto> productosMercadona = mercadonaAPI.buscarProductos(termino);
            productos.addAll(productosMercadona);
        } catch (Exception e) {
            System.err.println("Error al buscar en Mercadona: " + e.getMessage());
            // Continuar sin productos de Mercadona en caso de error
        }

        // Guardar en caché
        cache.put(cacheKey, productos);

        return productos;
    }

    private List<Producto> buscarProductosLocales(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return new ArrayList<>(productosLocales);
        }

        String terminoLower = termino.toLowerCase().trim();
        return productosLocales.stream()
                .filter(producto ->
                        producto.getNombre().toLowerCase().contains(terminoLower) ||
                                producto.getMarca().toLowerCase().contains(terminoLower) ||
                                producto.getCategoria().toLowerCase().contains(terminoLower)
                )
                .collect(Collectors.toList());
    }

    public List<Producto> obtenerTodosProductos() {
        String cacheKey = "todos_productos";
        List<Producto> productosCache = cache.get(cacheKey);
        if (productosCache != null) {
            return productosCache;
        }

        List<Producto> productos = new ArrayList<>();
        productos.addAll(productosLocales);

        // Opcional: Cargar algunos productos populares de Mercadona
        try {
            // Buscar categorías populares
            List<String> categoriasPopulares = List.of("leche", "pan", "huevos", "aceite");
            for (String categoria : categoriasPopulares) {
                List<Producto> productosCategoria = mercadonaAPI.buscarProductos(categoria);
                productos.addAll(productosCategoria.stream().limit(5).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            System.err.println("Error al cargar productos de Mercadona: " + e.getMessage());
        }

        cache.put(cacheKey, productos);
        return productos;
    }

    public List<Producto> obtenerProductosPorCategoria(String categoria) {
        return productosLocales.stream()
                .filter(producto -> producto.getCategoria().equalsIgnoreCase(categoria))
                .collect(Collectors.toList());
    }

    public Producto obtenerProductoPorId(Long id) {
        // Buscar en productos locales
        for (Producto producto : productosLocales) {
            if (producto.getId().equals(id)) {
                return producto;
            }
        }

        // Si no se encuentra localmente, buscar en Mercadona
        try {
            return mercadonaAPI.obtenerDetallesProducto(id.toString());
        } catch (Exception e) {
            System.err.println("Error al obtener producto de Mercadona: " + e.getMessage());
        }

        return null;
    }

    public void agregarProducto(Producto producto) {
        productosLocales.add(producto);
        // Invalidar caché
        cache.invalidar();
    }

    public void actualizarProducto(Producto producto) {
        for (int i = 0; i < productosLocales.size(); i++) {
            if (productosLocales.get(i).getId().equals(producto.getId())) {
                productosLocales.set(i, producto);
                // Invalidar caché
                cache.invalidar();
                break;
            }
        }
    }

    public boolean eliminarProducto(Long id) {
        boolean eliminado = productosLocales.removeIf(producto -> producto.getId().equals(id));
        if (eliminado) {
            // Invalidar caché
            cache.invalidar();
        }
        return eliminado;
    }

    public List<Producto> obtenerProductosRecomendados(String modalidadAhorro) {
        List<Producto> todosProductos = obtenerTodosProductos();

        // Filtrar según modalidad de ahorro
        switch (modalidadAhorro.toLowerCase()) {
            case "máximo":
                // Productos más baratos
                return todosProductos.stream()
                        .sorted((p1, p2) -> Double.compare(p1.getPrecio(), p2.getPrecio()))
                        .limit(20)
                        .collect(Collectors.toList());

            case "equilibrado":
                // Balance entre precio y calidad
                return todosProductos.stream()
                        .filter(p -> p.getPrecio() < 10.0)
                        .limit(20)
                        .collect(Collectors.toList());

            case "estándar":
                // Productos en rango medio de precios
                return todosProductos.stream()
                        .filter(p -> p.getPrecio() >= 2.0 && p.getPrecio() <= 15.0)
                        .limit(20)
                        .collect(Collectors.toList());

            default:
                return todosProductos.stream().limit(20).collect(Collectors.toList());
        }
    }

    public List<Producto> obtenerProductosEnOferta() {
        // En la implementación real, esto vendría de la API de Mercadona
        List<Producto> productosOferta = new ArrayList<>();

        try {
            // Buscar productos en ofertas específicas
            productosOferta.addAll(mercadonaAPI.buscarProductos("oferta"));
        } catch (Exception e) {
            System.err.println("Error al obtener ofertas de Mercadona: " + e.getMessage());
        }

        // Simular algunas ofertas locales
        Random random = new Random();
        productosLocales.stream()
                .filter(p -> random.nextBoolean())
                .limit(5)
                .forEach(productosOferta::add);

        return productosOferta;
    }

    public void cambiarCodigoPostal(String nuevoCodigoPostal) {
        ConfiguracionMercadona.setCodigoPostal(nuevoCodigoPostal);
        // Reinicializar API con nuevo código postal
        this.mercadonaAPI = new MercadonaAPI(nuevoCodigoPostal, "es");
        // Invalidar caché
        cache.invalidar();
    }

    public List<String> obtenerCategoriasDisponibles() {
        List<String> categorias = productosLocales.stream()
                .map(Producto::getCategoria)
                .distinct()
                .collect(Collectors.toList());

        // Agregar categorías estándar de Mercadona
        categorias.addAll(List.of("Bebidas", "Congelados", "Conservas", "Limpieza", "Perfumería"));

        return categorias.stream().distinct().collect(Collectors.toList());
    }

    // AGREGAR ESTOS DOS MÉTODOS AQUÍ
    public List<Producto> buscarProductosCompatibles(List<String> restricciones) {
        if (restricciones == null || restricciones.isEmpty()) {
            return new ArrayList<>(productosLocales);
        }

        return productosLocales.stream()
                .filter(producto -> producto.cumpleRestricciones(restricciones))
                .collect(Collectors.toList());
    }

    public List<Producto> buscarPorCategoria(String categoria) {
        return obtenerProductosPorCategoria(categoria);
    }
}
