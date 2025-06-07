package smartsave.servicio;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import smartsave.api.MercadonaAdapter;
import smartsave.config.HibernateConfig;
import smartsave.modelo.Producto;
import smartsave.modelo.ModalidadAhorro;
import smartsave.api.MercadonaApiServicio;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Servicio optimizado para gestionar operaciones relacionadas con productos
 * Integra datos de BD con API de Mercadona de manera eficiente
 */
public class ProductoServicio {

    // Servicio de API de Mercadona
    private MercadonaApiServicio mercadonaApi;
    private boolean usarApiMercadona = true;

    // Cache en memoria para productos frecuentes
    private final Map<Long, Producto> cacheProductos = new HashMap<>();
    private final Map<String, List<Producto>> cacheBusquedas = new HashMap<>();
    private long ultimaLimpiezaCache = System.currentTimeMillis();

    // Constructor
    public ProductoServicio() {
        this("14010"); // Código postal por defecto
    }

    public ProductoServicio(String codigoPostal) {
        try {
            this.mercadonaApi = new MercadonaApiServicio(codigoPostal);
            this.usarApiMercadona = mercadonaApi.isApiDisponible();

            if (usarApiMercadona) {
                System.out.println("API de Mercadona disponible. Usando datos reales.");
            } else {
                System.out.println("API de Mercadona no disponible. Usando datos de BD.");
            }
        } catch (Exception e) {
            System.err.println("Error inicializando API de Mercadona: " + e.getMessage());
            this.usarApiMercadona = false;
        }
    }

    /**
     * Obtiene el servicio de API de Mercadona para uso interno
     * @return El servicio de API de Mercadona
     */
    public MercadonaApiServicio getMercadonaApiServicio() {
        return this.mercadonaApi;
    }

    /**
     * Busca y guarda un producto de Mercadona en la base de datos si no existe
     * @param productoId ID del producto generado
     * @return El producto guardado o null si no se encuentra
     */
    public Producto buscarYGuardarProductoMercadona(Long productoId) {
        // Verificar caché primero
        if (cacheProductos.containsKey(productoId)) {
            return cacheProductos.get(productoId);
        }

        // Verificar BD
        Producto productoExistente = obtenerProductoPorId(productoId);
        if (productoExistente != null) {
            // Guardar en caché
            cacheProductos.put(productoId, productoExistente);
            return productoExistente;
        }

        // Si no existe y la API no está disponible, retornar null
        if (!usarApiMercadona) {
            return null;
        }

        try {
            // Buscar en diferentes fuentes de Mercadona
            List<Producto> productosMercadona = new ArrayList<>();

            // Productos nuevos (más probable que se intenten añadir)
            CompletableFuture<List<Producto>> futureProductos = mercadonaApi.obtenerProductosNuevos();

            // Buscar con términos comunes para aumentar posibilidades
            CompletableFuture<List<Producto>> futureLeche = mercadonaApi.buscarProductos("leche");
            CompletableFuture<List<Producto>> futurePan = mercadonaApi.buscarProductos("pan");

            // Esperar a que completen con timeout para evitar bloqueos largos
            try {
                productosMercadona.addAll(futureProductos.get(5, TimeUnit.SECONDS));
                productosMercadona.addAll(futureLeche.get(5, TimeUnit.SECONDS));
                productosMercadona.addAll(futurePan.get(5, TimeUnit.SECONDS));
            } catch (TimeoutException e) {
                System.err.println("Timeout esperando respuesta de Mercadona API");
            }

            // Buscar el producto con el ID específico
            Producto productoEncontrado = productosMercadona.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(productoId))
                    .findFirst()
                    .orElse(null);

            if (productoEncontrado != null) {
                // Guardar en la base de datos
                Producto guardado = guardarProducto(productoEncontrado);
                // Actualizar caché
                cacheProductos.put(productoId, guardado);
                return guardado;
            }

            return null;
        } catch (Exception e) {
            System.err.println("Error buscando producto de Mercadona con ID " + productoId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene todos los productos disponibles (BD + Mercadona) con caché
     * @return Lista de productos
     */
    public List<Producto> obtenerTodosProductos() {
        // Verificar caché
        String cacheKey = "todos_productos";
        if (cacheBusquedas.containsKey(cacheKey)) {
            // Limpiar caché si es necesario
            limpiarCacheSiNecesario();
            return new ArrayList<>(cacheBusquedas.get(cacheKey));
        }

        List<Producto> todosProductos = new ArrayList<>();

        // Obtener productos de la base de datos
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Producto> query = session.createQuery(
                    "FROM Producto p WHERE p.disponible = true",
                    Producto.class);
            todosProductos.addAll(query.getResultList());
        } catch (Exception e) {
            System.err.println("Error obteniendo productos de BD: " + e.getMessage());
        }

        // Añadir productos de Mercadona si la API está disponible
        if (usarApiMercadona) {
            try {
                // Usar timeout para evitar bloqueos
                CompletableFuture<List<Producto>> futureProductos = mercadonaApi.obtenerProductosNuevos();
                List<Producto> productosMercadona = futureProductos.get(10, TimeUnit.SECONDS);

                // Eliminar posibles duplicados por ID antes de añadir
                Set<Long> idsExistentes = todosProductos.stream()
                        .map(Producto::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                for (Producto producto : productosMercadona) {
                    if (producto.getId() != null && !idsExistentes.contains(producto.getId())) {
                        todosProductos.add(producto);
                        idsExistentes.add(producto.getId());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error obteniendo productos de Mercadona: " + e.getMessage());
            }
        }

        // Guardar en caché
        cacheBusquedas.put(cacheKey, new ArrayList<>(todosProductos));

        return todosProductos;
    }

    /**
     * Busca productos por nombre, marca o categoría con caché
     * @param termino Término de búsqueda
     * @return Lista de productos que coinciden con la búsqueda
     */
    // Reemplaza el método completo en: smartsave/servicio/ProductoServicio.java

    public List<Producto> buscarProductos(String termino, ModalidadAhorro modalidad) {
        // 1. OBTENER TODOS LOS RESULTADOS (esta parte no cambia)
        if (termino == null || termino.trim().isEmpty()) {
            return obtenerTodosProductos();
        }
        String terminoNormalizado = termino.toLowerCase().trim();
        List<Producto> resultados = new ArrayList<>();
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Producto> query = session.createQuery(
                    "FROM Producto p WHERE p.disponible = true AND " +
                            "(LOWER(p.nombre) LIKE :termino OR LOWER(p.marca) LIKE :termino OR LOWER(p.categoria) LIKE :termino)",
                    Producto.class);
            query.setParameter("termino", "%" + terminoNormalizado + "%");
            resultados.addAll(query.getResultList());
        } catch (Exception e) {
            System.err.println("Error buscando productos en BD: " + e.getMessage());
        }
        if (usarApiMercadona) {
            try {
                CompletableFuture<List<Producto>> futureProductos = mercadonaApi.buscarProductos(terminoNormalizado);
                List<Producto> productosMercadona = futureProductos.get(15, TimeUnit.SECONDS);
                Set<Long> idsExistentes = resultados.stream().map(Producto::getId).filter(Objects::nonNull).collect(Collectors.toSet());
                for (Producto producto : productosMercadona) {
                    if (producto.getId() != null && !idsExistentes.contains(producto.getId())) {
                        resultados.add(producto);
                        idsExistentes.add(producto.getId());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error buscando en Mercadona: " + e.getMessage());
            }
        }

        // 2. APLICAR NUEVA LÓGICA DE FILTRADO POR BANDAS DE PRECIOS
        if (resultados.isEmpty() || modalidad == null) {
            return resultados;
        }

        // Ordenar siempre por precio para poder trabajar con los rangos
        resultados.sort(Comparator.comparing(Producto::getPrecio));

        String nombreModalidad = modalidad.getNombre();
        int totalProductos = resultados.size();

        switch (nombreModalidad.toLowerCase()) {
            case "máximo":
                // Muestra el 50% de los productos más baratos.
                // Si hay pocos productos, muestra hasta 5.
                int limiteMaximo = Math.max(5, (int) (totalProductos * 0.5));
                return resultados.stream().limit(limiteMaximo).collect(Collectors.toList());

            case "equilibrado":
                // Busca un 'punto dulce' en el medio, mostrando productos que están
                // entre el 20% y el 80% del rango. Elimina los extremos.
                int inicioEquilibrado = (int) (totalProductos * 0.20);
                int finEquilibrado = (int) (totalProductos * 0.80);
                if (finEquilibrado <= inicioEquilibrado) { // Asegurar que haya rango
                    return resultados;
                }
                return resultados.subList(inicioEquilibrado, finEquilibrado);

            case "estándar":
                // Se enfoca en la calidad/precio superior, mostrando la mitad más cara de los productos.
                // Ignora el 50% más barato.
                int inicioEstandar = (int) (totalProductos * 0.50);
                if (totalProductos <= 5) { // Si hay muy pocos, mostrarlos todos
                    return resultados;
                }
                return resultados.subList(inicioEstandar, totalProductos);

            default:
                // Si la modalidad no es reconocida, devolver todo ordenado por precio.
                return resultados;
        }
    }

    /**
     * Limpia la caché si ha pasado el tiempo límite
     */
    private void limpiarCacheSiNecesario() {
        long tiempoActual = System.currentTimeMillis();
        if (tiempoActual - ultimaLimpiezaCache > TimeUnit.MINUTES.toMillis(10)) {
            cacheProductos.clear();
            cacheBusquedas.clear();
            ultimaLimpiezaCache = tiempoActual;
        }
    }

    /**
     * Obtiene un producto por su ID con caché
     * @param id ID del producto
     * @return El producto si existe, null en caso contrario
     */
    public Producto obtenerProductoPorId(Long id) {
        // Verificar caché primero
        if (cacheProductos.containsKey(id)) {
            return cacheProductos.get(id);
        }

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Producto producto = session.get(Producto.class, id);
            // Actualizar caché si se encontró
            if (producto != null) {
                cacheProductos.put(id, producto);
            }
            return producto;
        } catch (Exception e) {
            System.err.println("Error obteniendo producto por ID: " + e.getMessage());
            return null;
        }
    }

    /**
     * Busca productos por categoría
     * @param categoria Categoría a buscar
     * @return Lista de productos de la categoría especificada
     */
    public List<Producto> buscarPorCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            return obtenerTodosProductos();
        }

        List<Producto> resultados = new ArrayList<>();

        // Buscar en base de datos
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Producto> query = session.createQuery(
                    "FROM Producto p WHERE p.disponible = true AND LOWER(p.categoria) = :categoria",
                    Producto.class);
            query.setParameter("categoria", categoria.toLowerCase());
            resultados.addAll(query.getResultList());
        } catch (Exception e) {
            System.err.println("Error buscando por categoría en BD: " + e.getMessage());
        }

        // Para Mercadona, usamos búsqueda por término ya que no tenemos categorías específicas
        if (isApiMercadonaDisponible()) {
            try {
                // Usar el adaptador para transformar los resultados de Mercadona
                CompletableFuture<List<Producto>> futureProductos = mercadonaApi.buscarProductos(categoria);
                List<Producto> productosMercadona = futureProductos.get(5, TimeUnit.SECONDS);

                // Filtrar solo productos de la categoría solicitada o similares
                Set<Long> idsExistentes = resultados.stream()
                        .map(Producto::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                for (Producto producto : productosMercadona) {
                    if (producto.getCategoria().toLowerCase().contains(categoria.toLowerCase()) &&
                            !idsExistentes.contains(producto.getId())) {
                        resultados.add(producto);
                        idsExistentes.add(producto.getId());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error buscando categoría en Mercadona: " + e.getMessage());
            }
        }

        return resultados;
    }

    /**
     * Busca productos que cumplan con restricciones alimentarias
     * @param restricciones Lista de restricciones alimentarias
     * @return Lista de productos que cumplen con las restricciones
     */
    public List<Producto> buscarProductosCompatibles(List<String> restricciones) {
        if (restricciones == null || restricciones.isEmpty()) {
            return obtenerTodosProductos();
        }

        // Obtener todos los productos y filtrar
        List<Producto> todosProductos = obtenerTodosProductos();

        return todosProductos.stream()
                .filter(p -> cumpleRestricciones(p, restricciones))
                .collect(Collectors.toList());
    }

    /**
     * Verifica si un producto cumple con las restricciones alimentarias
     * @param producto Producto a verificar
     * @param restricciones Lista de restricciones
     * @return true si cumple con todas las restricciones
     */
    private boolean cumpleRestricciones(Producto producto, List<String> restricciones) {
        if (restricciones == null || restricciones.isEmpty()) {
            return true;
        }

        String categoriaLower = producto.getCategoria().toLowerCase();

        for (String restriccion : restricciones) {
            switch (restriccion.toLowerCase()) {
                case "sin gluten":
                    if (categoriaLower.contains("pan") || categoriaLower.contains("pasta") ||
                            categoriaLower.contains("galleta") || categoriaLower.contains("cereal")) {
                        return false;
                    }
                    break;
                case "sin lactosa":
                    if (categoriaLower.contains("lácteo") || categoriaLower.contains("lacteo") ||
                            categoriaLower.contains("leche") || categoriaLower.contains("queso") ||
                            categoriaLower.contains("yogur")) {
                        return false;
                    }
                    break;
                case "vegano":
                    if (categoriaLower.contains("carne") || categoriaLower.contains("pescado") ||
                            categoriaLower.contains("lácteo") || categoriaLower.contains("lacteo") ||
                            categoriaLower.contains("huevo")) {
                        return false;
                    }
                    break;
                case "vegetariano":
                    if (categoriaLower.contains("carne") || categoriaLower.contains("pescado")) {
                        return false;
                    }
                    break;
                case "bajo en sodio":
                    if (producto.getInfoNutricional() != null &&
                            producto.getInfoNutricional().getSodio() > 500) {
                        return false;
                    }
                    break;
                case "bajo en azúcar":
                case "bajo en azucar":
                    if (producto.getInfoNutricional() != null &&
                            producto.getInfoNutricional().getAzucares() > 10) {
                        return false;
                    }
                    break;
            }
        }

        return true;
    }

    /**
     * Guarda un producto en la base de datos con manejo mejorado de Mercadona
     * @param producto Producto a guardar
     * @return Producto guardado con ID asignado
     */
    public Producto guardarProducto(Producto producto) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Verificar si ya existe (por ID)
            if (producto.getId() != null) {
                Producto existente = session.get(Producto.class, producto.getId());
                if (existente != null) {
                    // Actualizar campos del producto existente
                    existente.setNombre(producto.getNombre());
                    existente.setMarca(producto.getMarca());
                    existente.setCategoria(producto.getCategoria());
                    existente.setPrecioBD(producto.getPrecioBD());
                    existente.setSupermercado(producto.getSupermercado());
                    existente.setDisponible(producto.isDisponible());

                    // Actualizar información nutricional si existe
                    if (producto.getInfoNutricional() != null) {
                        if (existente.getInfoNutricional() == null) {
                            existente.setInfoNutricional(new Producto.NutricionProducto());
                        }

                        existente.getInfoNutricional().setCaloriasBD(producto.getInfoNutricional().getCaloriasBD());
                        existente.getInfoNutricional().setProteinasBD(producto.getInfoNutricional().getProteinasBD());
                        existente.getInfoNutricional().setCarbohidratosBD(producto.getInfoNutricional().getCarbohidratosBD());
                        existente.getInfoNutricional().setGrasasBD(producto.getInfoNutricional().getGrasasBD());
                        existente.getInfoNutricional().setFibraBD(producto.getInfoNutricional().getFibraBD());
                        existente.getInfoNutricional().setSodioBD(producto.getInfoNutricional().getSodioBD());
                        existente.getInfoNutricional().setAzucaresBD(producto.getInfoNutricional().getAzucaresBD());
                    }

                    session.update(existente);
                    transaction.commit();

                    // Actualizar caché
                    cacheProductos.put(existente.getId(), existente);
                    return existente;
                }
            }

            // Si no existe, guardar nuevo
            session.saveOrUpdate(producto);
            transaction.commit();

            // Actualizar caché
            if (producto.getId() != null) {
                cacheProductos.put(producto.getId(), producto);
            }

            return producto;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error guardando producto: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si la API de Mercadona está disponible
     * @return true si está disponible y habilitada
     */
    public boolean isApiMercadonaDisponible() {
        return usarApiMercadona && mercadonaApi != null && mercadonaApi.isApiDisponible();
    }

    /**
     * Cierra recursos del servicio y limpia caché
     */
    public void cerrar() {
        if (mercadonaApi != null) {
            mercadonaApi.cerrar();
        }
        cacheProductos.clear();
        cacheBusquedas.clear();
    }
}