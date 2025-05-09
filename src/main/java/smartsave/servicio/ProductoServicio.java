package smartsave.servicio;

import smartsave.modelo.Producto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar operaciones relacionadas con productos
 * (Por ahora utiliza una simulación en memoria, luego se conectará a APIs de supermercados)
 */
public class ProductoServicio {

    // Simulación de base de datos (solo para demostración)
    private static final List<Producto> PRODUCTOS = new ArrayList<>();
    private static Long ultimoId = 0L;

    // Inicializa la lista con productos de muestra
    static {
        // Carnes
        agregarProductoInicial("Pechuga de pollo", "Carrefour", "Carnes", 4.99, "Carrefour", 120, 22, 0, 2.5);
        agregarProductoInicial("Filete de ternera", "El Pozo", "Carnes", 8.50, "Mercadona", 180, 26, 0, 10);
        agregarProductoInicial("Hamburguesa de vacuno", "Hacendado", "Carnes", 3.75, "Mercadona", 240, 18, 5, 18);

        // Lácteos
        agregarProductoInicial("Leche semidesnatada", "Hacendado", "Lácteos", 0.85, "Mercadona", 46, 3.2, 4.8, 1.6);
        agregarProductoInicial("Yogur natural", "Danone", "Lácteos", 1.75, "Carrefour", 55, 3.9, 5.0, 1.5);
        agregarProductoInicial("Queso fresco", "García Baquero", "Lácteos", 2.99, "Dia", 220, 18, 3, 16);

        // Frutas y verduras
        agregarProductoInicial("Plátano de Canarias", "Plátano", "Frutas", 1.89, "Mercadona", 89, 1.1, 22.8, 0.3);
        agregarProductoInicial("Manzana Golden", "Manzanas", "Frutas", 1.99, "Carrefour", 52, 0.3, 14, 0.2);
        agregarProductoInicial("Brócoli", "Verduras", "Verduras", 1.50, "Dia", 34, 2.8, 6.6, 0.4);
        agregarProductoInicial("Tomate", "Tomates", "Verduras", 1.29, "Mercadona", 18, 0.9, 3.9, 0.2);

        // Pescados
        agregarProductoInicial("Salmón fresco", "Pescados", "Pescados", 13.95, "Carrefour", 208, 20, 0, 13);
        agregarProductoInicial("Merluza fileteada", "Pescados", "Pescados", 10.50, "Mercadona", 83, 18, 0, 1.3);

        // Cereales y legumbres
        agregarProductoInicial("Arroz", "La Fallera", "Cereales", 1.25, "Mercadona", 130, 2.7, 28.2, 0.3);
        agregarProductoInicial("Lentejas", "Luengo", "Legumbres", 1.95, "Carrefour", 116, 9, 20, 0.4);
        agregarProductoInicial("Garbanzos", "Hacendado", "Legumbres", 0.99, "Mercadona", 120, 7.2, 20.5, 2.1);

        // Panadería
        agregarProductoInicial("Pan de molde integral", "Bimbo", "Panadería", 2.15, "Dia", 220, 9, 41, 3);
        agregarProductoInicial("Baguette", "Panadería", "Panadería", 0.65, "Mercadona", 250, 8, 50, 1);

        // Snacks y dulces
        agregarProductoInicial("Chocolate negro 85%", "Lindt", "Dulces", 2.75, "Carrefour", 600, 7.5, 19, 46);
        agregarProductoInicial("Patatas fritas", "Lays", "Snacks", 1.89, "Mercadona", 500, 6, 50, 31);
    }

    private static void agregarProductoInicial(String nombre, String marca, String categoria, double precio,
                                               String supermercado, double calorias, double proteinas,
                                               double carbohidratos, double grasas) {
        Producto producto = new Producto(nombre, marca, categoria, precio, supermercado,
                calorias, proteinas, carbohidratos, grasas);
        producto.setId(++ultimoId);
        PRODUCTOS.add(producto);
    }

    /**
     * Obtiene todos los productos disponibles
     * @return Lista de productos
     */
    public List<Producto> obtenerTodosProductos() {
        return new ArrayList<>(PRODUCTOS);
    }

    /**
     * Busca productos por nombre, marca o categoría
     * @param termino Término de búsqueda
     * @return Lista de productos que coinciden con la búsqueda
     */
    public List<Producto> buscarProductos(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return new ArrayList<>(PRODUCTOS);
        }

        String terminoLower = termino.toLowerCase().trim();

        return PRODUCTOS.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(terminoLower) ||
                        p.getMarca().toLowerCase().contains(terminoLower) ||
                        p.getCategoria().toLowerCase().contains(terminoLower))
                .collect(Collectors.toList());
    }

    /**
     * Busca productos por categoría
     * @param categoria Categoría a buscar
     * @return Lista de productos de la categoría especificada
     */
    public List<Producto> buscarPorCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            return new ArrayList<>(PRODUCTOS);
        }

        String categoriaLower = categoria.toLowerCase().trim();

        return PRODUCTOS.stream()
                .filter(p -> p.getCategoria().toLowerCase().equals(categoriaLower))
                .collect(Collectors.toList());
    }

    /**
     * Busca productos por supermercado
     * @param supermercado Supermercado a buscar
     * @return Lista de productos del supermercado especificado
     */
    public List<Producto> buscarPorSupermercado(String supermercado) {
        if (supermercado == null || supermercado.trim().isEmpty()) {
            return new ArrayList<>(PRODUCTOS);
        }

        String supermercadoLower = supermercado.toLowerCase().trim();

        return PRODUCTOS.stream()
                .filter(p -> p.getSupermercado().toLowerCase().equals(supermercadoLower))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un producto por su ID
     * @param id ID del producto
     * @return El producto si existe, null en caso contrario
     */
    public Producto obtenerProductoPorId(Long id) {
        return PRODUCTOS.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Busca productos que cumplan con restricciones alimentarias
     * @param restricciones Lista de restricciones alimentarias
     * @return Lista de productos que cumplen con las restricciones
     */
    public List<Producto> buscarProductosCompatibles(List<String> restricciones) {
        if (restricciones == null || restricciones.isEmpty()) {
            return new ArrayList<>(PRODUCTOS);
        }

        return PRODUCTOS.stream()
                .filter(p -> p.cumpleRestricciones(restricciones))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las categorías disponibles de productos
     * @return Lista de categorías únicas
     */
    public List<String> obtenerCategorias() {
        return PRODUCTOS.stream()
                .map(Producto::getCategoria)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los supermercados disponibles
     * @return Lista de supermercados únicos
     */
    public List<String> obtenerSupermercados() {
        return PRODUCTOS.stream()
                .map(Producto::getSupermercado)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Compara precios de un producto en diferentes supermercados
     * @param nombreProducto Nombre del producto a comparar
     * @return Mapa con supermercado como clave y precio como valor
     */
    public Map<String, Double> compararPrecios(String nombreProducto) {
        if (nombreProducto == null || nombreProducto.trim().isEmpty()) {
            return new HashMap<>();
        }

        String nombreLower = nombreProducto.toLowerCase().trim();

        Map<String, Double> preciosPorSupermercado = new HashMap<>();

        PRODUCTOS.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(nombreLower))
                .forEach(p -> {
                    // Solo guarda el precio si es menor que el que ya existe
                    // o si no hay precio registrado para ese supermercado
                    if (!preciosPorSupermercado.containsKey(p.getSupermercado()) ||
                            preciosPorSupermercado.get(p.getSupermercado()) > p.getPrecio()) {
                        preciosPorSupermercado.put(p.getSupermercado(), p.getPrecio());
                    }
                });

        return preciosPorSupermercado;
    }

    /**
     * Obtiene productos con mejor relación proteína/precio
     * @param limite Número máximo de productos a devolver
     * @return Lista de productos ordenados por relación proteína/precio
     */
    public List<Producto> obtenerProductosMejorRelacionProteinaPrecio(int limite) {
        return PRODUCTOS.stream()
                .sorted((p1, p2) -> Double.compare(p2.getRelacionProteinaPrecio(), p1.getRelacionProteinaPrecio()))
                .limit(limite)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene productos con mejor relación calorías/precio
     * @param limite Número máximo de productos a devolver
     * @return Lista de productos ordenados por relación calorías/precio
     */
    public List<Producto> obtenerProductosMejorRelacionCaloriasPrecio(int limite) {
        return PRODUCTOS.stream()
                .sorted((p1, p2) -> Double.compare(p2.getRelacionCaloriasPrecio(), p1.getRelacionCaloriasPrecio()))
                .limit(limite)
                .collect(Collectors.toList());
    }
}