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

public class ProductoServicio {

    private MercadonaApiServicio mercadonaApi;
    private boolean usarApiMercadona = true;

    private final Map<Long, Producto> cacheProductos = new HashMap<>();
    private final Map<String, List<Producto>> cacheBusquedas = new HashMap<>();
    private long ultimaLimpiezaCache = System.currentTimeMillis();

    public ProductoServicio() {
        this("14010"); //Codigo del mercadona de Salesianos
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

    public MercadonaApiServicio getMercadonaApiServicio() {
        return this.mercadonaApi;
    }

    public Producto buscarYGuardarProductoMercadona(Long productoId) {
        if (cacheProductos.containsKey(productoId)) {
            return cacheProductos.get(productoId);
        }

        Producto productoExistente = obtenerProductoPorId(productoId);
        if (productoExistente != null) {
            cacheProductos.put(productoId, productoExistente);
            return productoExistente;
        }

        if (!usarApiMercadona) {
            return null;
        }

        try {
            List<Producto> productosMercadona = new ArrayList<>();

            CompletableFuture<List<Producto>> futureProductos = mercadonaApi.obtenerProductosNuevos();

            CompletableFuture<List<Producto>> futureLeche = mercadonaApi.buscarProductos("leche");
            CompletableFuture<List<Producto>> futurePan = mercadonaApi.buscarProductos("pan");

            try {
                productosMercadona.addAll(futureProductos.get(5, TimeUnit.SECONDS));
                productosMercadona.addAll(futureLeche.get(5, TimeUnit.SECONDS));
                productosMercadona.addAll(futurePan.get(5, TimeUnit.SECONDS));
            } catch (TimeoutException e) {
                System.err.println("Timeout esperando respuesta de Mercadona API");
            }

            Producto productoEncontrado = productosMercadona.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(productoId))
                    .findFirst()
                    .orElse(null);

            if (productoEncontrado != null) {
                Producto guardado = guardarProducto(productoEncontrado);
                cacheProductos.put(productoId, guardado);
                return guardado;
            }

            return null;
        } catch (Exception e) {
            System.err.println("Error buscando producto de Mercadona con ID " + productoId + ": " + e.getMessage());
            return null;
        }
    }

    public List<Producto> obtenerTodosProductos() {
        String cacheKey = "todos_productos";
        if (cacheBusquedas.containsKey(cacheKey)) {
            limpiarCacheSiNecesario();
            return new ArrayList<>(cacheBusquedas.get(cacheKey));
        }

        List<Producto> todosProductos = new ArrayList<>();

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Producto> query = session.createQuery(
                    "FROM Producto p WHERE p.disponible = true",
                    Producto.class);
            todosProductos.addAll(query.getResultList());
        } catch (Exception e) {
            System.err.println("Error obteniendo productos de BD: " + e.getMessage());
        }

        if (usarApiMercadona) {
            try {
                CompletableFuture<List<Producto>> futureProductos = mercadonaApi.obtenerProductosNuevos();
                List<Producto> productosMercadona = futureProductos.get(10, TimeUnit.SECONDS);

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

        cacheBusquedas.put(cacheKey, new ArrayList<>(todosProductos));

        return todosProductos;
    }

    public List<Producto> buscarProductos(String termino, ModalidadAhorro modalidad) {
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

        if (resultados.isEmpty() || modalidad == null) {
            return resultados;
        }
        resultados.sort(Comparator.comparing(Producto::getPrecio));

        String nombreModalidad = modalidad.getNombre();
        int totalProductos = resultados.size();

        switch (nombreModalidad.toLowerCase()) {
            case "máximo":
                int limiteMaximo = Math.max(5, (int) (totalProductos * 0.5));
                return resultados.stream().limit(limiteMaximo).collect(Collectors.toList());

            case "equilibrado":
                int inicioEquilibrado = (int) (totalProductos * 0.20);
                int finEquilibrado = (int) (totalProductos * 0.80);
                if (finEquilibrado <= inicioEquilibrado) {
                    return resultados;
                }
                return resultados.subList(inicioEquilibrado, finEquilibrado);

            case "estándar":
                int inicioEstandar = (int) (totalProductos * 0.50);
                if (totalProductos <= 5) {
                    return resultados;
                }
                return resultados.subList(inicioEstandar, totalProductos);

            default:
                return resultados;
        }
    }

    private void limpiarCacheSiNecesario() {
        long tiempoActual = System.currentTimeMillis();
        if (tiempoActual - ultimaLimpiezaCache > TimeUnit.MINUTES.toMillis(10)) {
            cacheProductos.clear();
            cacheBusquedas.clear();
            ultimaLimpiezaCache = tiempoActual;
        }
    }

    public Producto obtenerProductoPorId(Long id) {

        if (cacheProductos.containsKey(id)) {
            return cacheProductos.get(id);
        }

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Producto producto = session.get(Producto.class, id);
            if (producto != null) {
                cacheProductos.put(id, producto);
            }
            return producto;
        } catch (Exception e) {
            System.err.println("Error obteniendo producto por ID: " + e.getMessage());
            return null;
        }
    }

    public List<Producto> buscarPorCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            return obtenerTodosProductos();
        }

        List<Producto> resultados = new ArrayList<>();

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Producto> query = session.createQuery(
                    "FROM Producto p WHERE p.disponible = true AND LOWER(p.categoria) = :categoria",
                    Producto.class);
            query.setParameter("categoria", categoria.toLowerCase());
            resultados.addAll(query.getResultList());
        } catch (Exception e) {
            System.err.println("Error buscando por categoría en BD: " + e.getMessage());
        }

        if (isApiMercadonaDisponible()) {
            try {
                CompletableFuture<List<Producto>> futureProductos = mercadonaApi.buscarProductos(categoria);
                List<Producto> productosMercadona = futureProductos.get(5, TimeUnit.SECONDS);

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

    public List<Producto> buscarProductosCompatibles(List<String> restricciones) {
        if (restricciones == null || restricciones.isEmpty()) {
            return obtenerTodosProductos();
        }

        List<Producto> todosProductos = obtenerTodosProductos();

        return todosProductos.stream()
                .filter(p -> cumpleRestricciones(p, restricciones))
                .collect(Collectors.toList());
    }

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

    public Producto guardarProducto(Producto producto) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            if (producto.getId() != null) {
                Producto existente = session.get(Producto.class, producto.getId());
                if (existente != null) {
                    existente.setNombre(producto.getNombre());
                    existente.setMarca(producto.getMarca());
                    existente.setCategoria(producto.getCategoria());
                    existente.setPrecioBD(producto.getPrecioBD());
                    existente.setSupermercado(producto.getSupermercado());
                    existente.setDisponible(producto.isDisponible());

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

                    cacheProductos.put(existente.getId(), existente);
                    return existente;
                }
            }

            session.saveOrUpdate(producto);
            transaction.commit();

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

    public boolean isApiMercadonaDisponible() {
        return usarApiMercadona && mercadonaApi != null && mercadonaApi.isApiDisponible();
    }

    public void cerrar() {
        if (mercadonaApi != null) {
            mercadonaApi.cerrar();
        }
        cacheProductos.clear();
        cacheBusquedas.clear();
    }
}