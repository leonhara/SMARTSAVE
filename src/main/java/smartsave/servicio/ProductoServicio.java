package smartsave.servicio;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import smartsave.config.HibernateConfig;
import smartsave.modelo.Producto;
import smartsave.modelo.ModalidadAhorro;
import smartsave.api.MercadonaApiServicio;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar operaciones relacionadas con productos
 * MIGRADO A HIBERNATE - Integra datos de BD con API de Mercadona
 */
public class ProductoServicio {

    // Servicio de API de Mercadona (mantener funcionalidad existente)
    private MercadonaApiServicio mercadonaApi;
    private boolean usarApiMercadona = true;

    // Constructor
    public ProductoServicio() {
        this("14010"); //del salesianos
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
     * Obtiene todos los productos disponibles (BD + Mercadona)
     * @return Lista de productos
     */
    public List<Producto> obtenerTodosProductos() {
        List<Producto> todosProductos = new ArrayList<>();

        // Obtener productos de la base de datos
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Producto> query = session.createQuery("FROM Producto p WHERE p.disponible = true", Producto.class);
            todosProductos.addAll(query.getResultList());
        } catch (Exception e) {
            System.err.println("Error obteniendo productos de BD: " + e.getMessage());
        }

        // Añadir productos de Mercadona si la API está disponible
        if (usarApiMercadona) {
            try {
                CompletableFuture<List<Producto>> futureProductos = mercadonaApi.obtenerProductosNuevos();
                List<Producto> productosMercadona = futureProductos.get();

                for (Producto producto : productosMercadona) {
                    todosProductos.add(producto);
                }
            } catch (Exception e) {
                System.err.println("Error obteniendo productos de Mercadona: " + e.getMessage());
            }
        }

        return todosProductos;
    }

    /**
     * Busca productos por nombre, marca o categoría
     * @param termino Término de búsqueda
     * @return Lista de productos que coinciden con la búsqueda
     */
    public List<Producto> buscarProductos(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return obtenerTodosProductos();
        }

        List<Producto> resultados = new ArrayList<>();
        String terminoLower = termino.toLowerCase().trim();

        // Buscar en base de datos
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Producto> query = session.createQuery(
                    "FROM Producto p WHERE p.disponible = true AND " +
                            "(LOWER(p.nombre) LIKE :termino OR LOWER(p.marca) LIKE :termino OR LOWER(p.categoria) LIKE :termino)",
                    Producto.class);
            query.setParameter("termino", "%" + terminoLower + "%");
            resultados.addAll(query.getResultList());
        } catch (Exception e) {
            System.err.println("Error buscando productos en BD: " + e.getMessage());
        }

        // Buscar en Mercadona si la API está disponible
        if (usarApiMercadona) {
            try {
                CompletableFuture<List<Producto>> futureProductos = mercadonaApi.buscarProductos(termino);
                List<Producto> productosMercadona = futureProductos.get();
                resultados.addAll(productosMercadona);
            } catch (Exception e) {
                System.err.println("Error buscando en Mercadona: " + e.getMessage());
            }
        }

        return resultados;
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
        if (usarApiMercadona) {
            try {
                CompletableFuture<List<Producto>> futureProductos = mercadonaApi.buscarProductos(categoria);
                List<Producto> productosMercadona = futureProductos.get();

                for (Producto producto : productosMercadona) {
                    if (producto.getCategoria().toLowerCase().contains(categoria.toLowerCase())) {
                        resultados.add(producto);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error buscando categoría en Mercadona: " + e.getMessage());
            }
        }

        return resultados;
    }

    /**
     * Busca productos por supermercado
     * @param supermercado Supermercado a buscar
     * @return Lista de productos del supermercado especificado
     */
    public List<Producto> buscarPorSupermercado(String supermercado) {
        if (supermercado == null || supermercado.trim().isEmpty()) {
            return obtenerTodosProductos();
        }

        List<Producto> resultados = new ArrayList<>();

        // Buscar en base de datos
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Producto> query = session.createQuery(
                    "FROM Producto p WHERE p.disponible = true AND LOWER(p.supermercado) = :supermercado",
                    Producto.class);
            query.setParameter("supermercado", supermercado.toLowerCase());
            resultados.addAll(query.getResultList());
        } catch (Exception e) {
            System.err.println("Error buscando por supermercado en BD: " + e.getMessage());
        }

        // Si buscan específicamente Mercadona, incluir productos de la API
        if (supermercado.toLowerCase().equals("mercadona") && usarApiMercadona) {
            try {
                CompletableFuture<List<Producto>> futureProductos = mercadonaApi.obtenerProductosNuevos();
                List<Producto> productosMercadona = futureProductos.get();
                resultados.addAll(productosMercadona);
            } catch (Exception e) {
                System.err.println("Error obteniendo productos de Mercadona: " + e.getMessage());
            }
        }

        return resultados;
    }

    /**
     * Obtiene un producto por su ID
     * @param id ID del producto
     * @return El producto si existe, null en caso contrario
     */
    public Producto obtenerProductoPorId(Long id) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.get(Producto.class, id);
        } catch (Exception e) {
            System.err.println("Error obteniendo producto por ID: " + e.getMessage());
            return null;
        }
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

        return obtenerTodosProductos().stream()
                .filter(p -> p.cumpleRestricciones(restricciones))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las categorías disponibles de productos
     * @return Lista de categorías únicas
     */
    public List<String> obtenerCategorias() {
        Set<String> categorias = new HashSet<>();

        // Obtener categorías de la base de datos
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<String> query = session.createQuery(
                    "SELECT DISTINCT p.categoria FROM Producto p WHERE p.disponible = true",
                    String.class);
            categorias.addAll(query.getResultList());
        } catch (Exception e) {
            System.err.println("Error obteniendo categorías de BD: " + e.getMessage());
        }

        // Categorías comunes de Mercadona
        if (usarApiMercadona) {
            categorias.addAll(Arrays.asList(
                    "Frescos", "Charcutería y quesos", "Carnicería y pescadería",
                    "Frutas y verduras", "Panadería y pastelería", "Lácteos y huevos",
                    "Convenience", "Alimentación", "Bebidas", "Perfumería e higiene",
                    "Limpieza", "Hogar", "Textil", "Mascotas"
            ));
        }

        return categorias.stream().sorted().collect(Collectors.toList());
    }

    /**
     * Obtiene los supermercados disponibles
     * @return Lista de supermercados únicos
     */
    public List<String> obtenerSupermercados() {
        Set<String> supermercados = new HashSet<>();

        // Obtener supermercados de la base de datos
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<String> query = session.createQuery(
                    "SELECT DISTINCT p.supermercado FROM Producto p WHERE p.disponible = true",
                    String.class);
            supermercados.addAll(query.getResultList());
        } catch (Exception e) {
            System.err.println("Error obteniendo supermercados de BD: " + e.getMessage());
        }

        if (usarApiMercadona) {
            supermercados.add("Mercadona");
        }

        return supermercados.stream().sorted().collect(Collectors.toList());
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

        // Buscar en base de datos
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Object[]> query = session.createQuery(
                    "SELECT p.supermercado, MIN(p.precio) FROM Producto p " +
                            "WHERE p.disponible = true AND LOWER(p.nombre) LIKE :nombre " +
                            "GROUP BY p.supermercado",
                    Object[].class);
            query.setParameter("nombre", "%" + nombreLower + "%");

            List<Object[]> resultados = query.getResultList();
            for (Object[] resultado : resultados) {
                String supermercado = (String) resultado[0];
                Double precio = (Double) resultado[1];
                preciosPorSupermercado.put(supermercado, precio);
            }
        } catch (Exception e) {
            System.err.println("Error comparando precios en BD: " + e.getMessage());
        }

        // Buscar en Mercadona
        if (usarApiMercadona) {
            try {
                CompletableFuture<List<Producto>> futureProductos = mercadonaApi.buscarProductos(nombreProducto);
                List<Producto> productosMercadona = futureProductos.get();

                for (Producto producto : productosMercadona) {
                    if (producto.getNombre().toLowerCase().contains(nombreLower)) {
                        if (!preciosPorSupermercado.containsKey("Mercadona") ||
                                preciosPorSupermercado.get("Mercadona") > producto.getPrecio()) {
                            preciosPorSupermercado.put("Mercadona", producto.getPrecio());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error comparando precios en Mercadona: " + e.getMessage());
            }
        }

        return preciosPorSupermercado;
    }

    /**
     * Obtiene productos con mejor relación proteína/precio
     * @param limite Número máximo de productos a devolver
     * @return Lista de productos ordenados por relación proteína/precio
     */
    public List<Producto> obtenerProductosMejorRelacionProteinaPrecio(int limite) {
        return obtenerTodosProductos().stream()
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
        return obtenerTodosProductos().stream()
                .sorted((p1, p2) -> Double.compare(p2.getRelacionCaloriasPrecio(), p1.getRelacionCaloriasPrecio()))
                .limit(limite)
                .collect(Collectors.toList());
    }

    /**
     * Filtra productos según una modalidad de ahorro
     * @param productos Lista de productos a filtrar
     * @param modalidad Modalidad de ahorro a aplicar
     * @return Lista filtrada y ordenada según la modalidad
     */
    public List<Producto> filtrarSegunModalidad(List<Producto> productos, ModalidadAhorro modalidad) {
        if (modalidad == null) {
            return new ArrayList<>(productos);
        }

        List<Producto> productosFiltrados = new ArrayList<>(productos);

        // Ordenar según prioridades de la modalidad
        if ("Máximo".equals(modalidad.getNombre())) {
            // Priorizar precio
            productosFiltrados.sort(Comparator.comparing(Producto::getPrecio));
        } else if ("Estándar".equals(modalidad.getNombre())) {
            // Priorizar nutrición
            productosFiltrados.sort(Comparator.comparing(obj -> {
                Producto p = (Producto) obj;
                return p.getInfoNutricional().getProteinas() +
                        p.getInfoNutricional().getCarbohidratos() / 2 +
                        p.getInfoNutricional().getGrasas() / 3;
            }).reversed());
        } else {
            // Equilibrado - Balance entre precio y nutrición
            productosFiltrados.sort(Comparator.comparing(Producto::getRelacionProteinaPrecio).reversed());
        }

        return productosFiltrados;
    }

    /**
     * Habilita o deshabilita el uso de la API de Mercadona
     * @param usar true para usar la API, false para usar solo datos locales
     */
    public void setUsarApiMercadona(boolean usar) {
        this.usarApiMercadona = usar && mercadonaApi != null && mercadonaApi.isApiDisponible();
    }

    /**
     * Verifica si la API de Mercadona está disponible
     * @return true si está disponible y habilitada
     */
    public boolean isApiMercadonaDisponible() {
        return usarApiMercadona;
    }

    /**
     * Cierra recursos del servicio
     */
    public void cerrar() {
        if (mercadonaApi != null) {
            mercadonaApi.cerrar();
        }
    }

    // Métodos adicionales para gestionar productos en BD

    /**
     * Guarda un producto en la base de datos
     * @param producto Producto a guardar
     * @return Producto guardado con ID asignado
     */
    public Producto guardarProducto(Producto producto) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.saveOrUpdate(producto);
            transaction.commit();
            return producto;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error guardando producto: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza un producto existente
     * @param producto Producto a actualizar
     * @return Producto actualizado
     */
    public Producto actualizarProducto(Producto producto) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(producto);
            transaction.commit();
            return producto;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error actualizando producto: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un producto por ID
     * @param id ID del producto a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean eliminarProducto(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Producto producto = session.get(Producto.class, id);
            if (producto != null) {
                session.delete(producto);
                transaction.commit();
                return true;
            } else {
                transaction.rollback();
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error eliminando producto: " + e.getMessage(), e);
        }
    }
}