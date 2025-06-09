package smartsave.servicio;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import smartsave.config.HibernateConfig;
import smartsave.modelo.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ListaCompraServicio {

    private ProductoServicio productoServicio;
    private PerfilNutricionalServicio perfilServicio;
    private UsuarioServicio usuarioServicio;

    private final Map<Long, ListaCompra> cacheListas = new HashMap<>();
    private long ultimaLimpiezaCache = System.currentTimeMillis();
    private static final long TIEMPO_EXPIRACION_CACHE = TimeUnit.MINUTES.toMillis(5);

    private ProductoServicio getProductoServicio() {
        if (productoServicio == null) {
            productoServicio = new ProductoServicio();
        }
        return productoServicio;
    }

    private PerfilNutricionalServicio getPerfilServicio() {
        if (perfilServicio == null) {
            perfilServicio = new PerfilNutricionalServicio();
        }
        return perfilServicio;
    }

    private UsuarioServicio getUsuarioServicio() {
        if (usuarioServicio == null) {
            usuarioServicio = new UsuarioServicio();
        }
        return usuarioServicio;
    }

    private void limpiarCacheSiNecesario() {
        long tiempoActual = System.currentTimeMillis();
        if (tiempoActual - ultimaLimpiezaCache > TIEMPO_EXPIRACION_CACHE) {
            cacheListas.clear();
            ultimaLimpiezaCache = tiempoActual;
        }
    }

    public ListaCompra crearListaCompra(Long usuarioId, String nombre, String modalidadAhorro, double presupuestoMaximo) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            ListaCompra lista = new ListaCompra(usuarioId, nombre, modalidadAhorro, BigDecimal.valueOf(presupuestoMaximo));
            session.save(lista);

            transaction.commit();

            cacheListas.put(lista.getId(), lista);

            return lista;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error creando lista de compra: " + e.getMessage(), e);
        }
    }

    public List<ListaCompra> obtenerListasCompraUsuario(Long usuarioId) {
        limpiarCacheSiNecesario();

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<ListaCompra> query = session.createQuery(
                    "FROM ListaCompra l WHERE l.usuarioId = :usuarioId ORDER BY l.fechaCreacion DESC",
                    ListaCompra.class);
            query.setParameter("usuarioId", usuarioId);

            List<ListaCompra> listas = query.getResultList();

            for (ListaCompra lista : listas) {
                lista.getItems().size();
                cacheListas.put(lista.getId(), lista);
            }

            return listas;
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo listas de compra: " + e.getMessage(), e);
        }
    }

    public ListaCompra obtenerListaCompra(Long listaId, Long usuarioId) {
        if (cacheListas.containsKey(listaId)) {
            ListaCompra listaCache = cacheListas.get(listaId);
            if (listaCache.getUsuarioId().equals(usuarioId)) {
                return listaCache;
            }
        }

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            ListaCompra lista = obtenerListaCompraConItems(session, listaId, usuarioId);

            if (lista != null) {
                cacheListas.put(lista.getId(), lista);
            }

            return lista;
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo lista de compra: " + e.getMessage(), e);
        }
    }

    private ListaCompra obtenerListaCompraConItems(Session session, Long listaId, Long usuarioId) {
        Query<ListaCompra> query = session.createQuery(
                "SELECT l FROM ListaCompra l " +
                        "LEFT JOIN FETCH l.items i " +
                        "LEFT JOIN FETCH i.producto " +
                        "WHERE l.id = :listaId AND l.usuarioId = :usuarioId",
                ListaCompra.class);
        query.setParameter("listaId", listaId);
        query.setParameter("usuarioId", usuarioId);

        return query.uniqueResult();
    }

    public ListaCompra agregarProductoALista(ListaCompra lista, Long productoId, Producto productoCompleto, int cantidad) {
        System.out.println("Iniciando adición de producto ID: " + productoId);

        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateConfig.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            Producto producto = session.get(Producto.class, productoId);
            System.out.println("Producto encontrado en BD: " + (producto != null ? "Sí" : "No"));

            if (producto == null) {
                if (productoCompleto != null) {
                    System.out.println("Usando producto proporcionado: " + productoCompleto.getNombre());
                    producto = productoCompleto;
                } else {
                    System.out.println("Buscando información real del producto en Mercadona...");
                    Producto productoMercadona = obtenerProductoMercadona(productoId);

                    if (productoMercadona != null) {
                        System.out.println("Producto encontrado en Mercadona: " + productoMercadona.getNombre());
                        producto = productoMercadona;
                    } else {
                        System.out.println("No se encontró información en Mercadona, creando producto básico");
                        producto = new Producto();
                        producto.setNombre("Producto " + productoId);
                        producto.setMarca("Mercadona");
                        producto.setCategoria("Otros");
                        producto.setPrecio(1.0);
                        producto.setSupermercado("Mercadona");
                        producto.setDisponible(true);
                    }
                }

                if (producto.getPrecioBD() == null) {
                    System.out.println("Precio nulo detectado, estableciendo a 1.0");
                    producto.setPrecioBD(BigDecimal.valueOf(1.0));
                }

                System.out.println("Creando nuevo producto en la base de datos");
                session.save(producto);
                session.flush();

                System.out.println("Nuevo producto guardado con ID generado: " + producto.getId());

                productoId = producto.getId();
            }

            ItemCompra itemExistente = null;
            try {
                itemExistente = session.createQuery(
                                "FROM ItemCompra i WHERE i.lista.id = :listaId AND i.producto.id = :productoId",
                                ItemCompra.class)
                        .setParameter("listaId", lista.getId())
                        .setParameter("productoId", productoId)
                        .uniqueResult();
            } catch (Exception e) {
                System.out.println("Error buscando item existente: " + e.getMessage());
            }

            System.out.println("Añadiendo item a lista ID: " + lista.getId());

            if (itemExistente != null) {
                System.out.println("Item ya existe, actualizando cantidad");
                itemExistente.setCantidad(itemExistente.getCantidad() + cantidad);
                session.update(itemExistente);
            } else {
                System.out.println("Creando nuevo item para el producto");

                ListaCompra listaFresca = session.get(ListaCompra.class, lista.getId());
                Producto productoFresco = session.get(Producto.class, productoId);

                if (listaFresca == null) {
                    throw new RuntimeException("No se encontró la lista con ID: " + lista.getId());
                }

                if (productoFresco == null) {
                    throw new RuntimeException("No se encontró el producto con ID: " + productoId);
                }

                ItemCompra nuevoItem = new ItemCompra(productoFresco, cantidad);
                nuevoItem.setLista(listaFresca);

                session.save(nuevoItem);
            }

            transaction.commit();
            transaction = null;

            System.out.println("Ítem añadido correctamente");

            cacheListas.remove(lista.getId());

            ListaCompra listaActualizada;
            try (Session nuevaSesion = HibernateConfig.getSessionFactory().openSession()) {
                listaActualizada = obtenerListaCompra(lista.getId(), lista.getUsuarioId());
            }

            return listaActualizada;

        } catch (Exception e) {
            System.out.println("Error durante la adición del producto: " + e.getMessage());
            e.printStackTrace();

            if (transaction != null) {
                try {
                    if (session != null && session.isOpen() && transaction.isActive()) {
                        transaction.rollback();
                    }
                } catch (Exception rollbackEx) {
                    System.err.println("Error durante rollback: " + rollbackEx.getMessage());
                }
            }

            throw new RuntimeException("Error agregando producto a lista: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                try {
                    session.close();
                } catch (Exception closeEx) {
                    System.err.println("Error cerrando sesión: " + closeEx.getMessage());
                }
            }
        }
    }

    public ListaCompra agregarProductoALista(ListaCompra lista, Long productoId, int cantidad) {
        return agregarProductoALista(lista, productoId, null, cantidad);
    }

    private Producto obtenerProductoMercadona(Long productoId) {
        try {
            ProductoServicio productoServicio = getProductoServicio();

            if (!productoServicio.isApiMercadonaDisponible()) {
                System.err.println("API de Mercadona no disponible");
                return null;
            }

            Producto producto = buscarProductoMercadona(productoId);
            if (producto != null) {
                System.out.println("Encontrado producto en API: " + producto.getNombre());
                return producto;
            }

            System.out.println("Producto con ID " + productoId + " no encontrado");
            return null;
        } catch (Exception e) {
            System.err.println("Error general obteniendo producto Mercadona: " + e.getMessage());
            return null;
        }
    }

    private Producto buscarProductoMercadona(Long productoId) {
        try {
            ProductoServicio productoServicio = getProductoServicio();

            if (!productoServicio.isApiMercadonaDisponible()) {
                return null;
            }

            CompletableFuture<List<Producto>> futureProductos = productoServicio.getMercadonaApiServicio()
                    .obtenerProductosNuevos();

            List<Producto> productos = futureProductos.get(10, TimeUnit.SECONDS);

            return productos.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(productoId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.err.println("Error buscando producto de Mercadona: " + e.getMessage());
            return null;
        }
    }

    public boolean eliminarItemDeLista(ListaCompra lista, Long itemId) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            ItemCompra item = session.get(ItemCompra.class, itemId);
            if (item != null && item.getLista().getId().equals(lista.getId())) {
                session.delete(item);
                transaction.commit();

                cacheListas.remove(lista.getId());

                return true;
            } else {
                transaction.rollback();
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error eliminando item de lista: " + e.getMessage(), e);
        }
    }

    public boolean actualizarEstadoItem(ListaCompra lista, Long itemId, boolean comprado) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            ItemCompra item = session.get(ItemCompra.class, itemId);
            if (item != null && item.getLista().getId().equals(lista.getId())) {
                item.setComprado(comprado);
                session.update(item);
                transaction.commit();

                cacheListas.remove(lista.getId());

                return true;
            } else {
                transaction.rollback();
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error actualizando estado de item: " + e.getMessage(), e);
        }
    }

    public boolean actualizarCantidadItem(ListaCompra lista, Long itemId, int nuevaCantidad) {
        if (nuevaCantidad <= 0) {
            return false;
        }

        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            ItemCompra item = session.get(ItemCompra.class, itemId);
            if (item != null && item.getLista().getId().equals(lista.getId())) {
                item.setCantidad(nuevaCantidad);
                session.update(item);
                transaction.commit();

                cacheListas.remove(lista.getId());

                return true;
            } else {
                transaction.rollback();
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error actualizando cantidad de item: " + e.getMessage(), e);
        }
    }

    public ListaCompra marcarListaComoCompletada(ListaCompra lista) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            lista.setCompletada(true);
            session.update(lista);

            transaction.commit();

            cacheListas.remove(lista.getId());

            return lista;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error marcando lista como completada: " + e.getMessage(), e);
        }
    }

    public List<ListaCompra> obtenerListasActivas(Long usuarioId) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<ListaCompra> query = session.createQuery(
                    "FROM ListaCompra l WHERE l.usuarioId = :usuarioId AND l.completada = false ORDER BY l.fechaCreacion DESC",
                    ListaCompra.class);
            query.setParameter("usuarioId", usuarioId);

            List<ListaCompra> listas = query.getResultList();
            listas.forEach(lista -> lista.getItems().size());
            return listas;
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo listas activas: " + e.getMessage(), e);
        }
    }

    public List<ListaCompra> obtenerListasCompletadas(Long usuarioId) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<ListaCompra> query = session.createQuery(
                    "FROM ListaCompra l WHERE l.usuarioId = :usuarioId AND l.completada = true ORDER BY l.fechaCreacion DESC",
                    ListaCompra.class);
            query.setParameter("usuarioId", usuarioId);

            List<ListaCompra> listas = query.getResultList();
            listas.forEach(lista -> lista.getItems().size());
            return listas;
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo listas completadas: " + e.getMessage(), e);
        }
    }

    public boolean actualizarListaCompra(ListaCompra lista) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.update(lista);
            transaction.commit();

            cacheListas.remove(lista.getId());
            cacheListas.put(lista.getId(), lista);

            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error actualizando lista de compra: " + e.getMessage(), e);
        }
    }

    public ListaCompra generarListaOptimizada(Long usuarioId, String nombre, String modalidadAhorro, double presupuestoMaximo) {
        ListaCompra lista = crearListaCompra(usuarioId, nombre, modalidadAhorro, presupuestoMaximo);

        ProductoServicio productoServicio = getProductoServicio();
        PerfilNutricionalServicio perfilServicio = getPerfilServicio();
        PerfilNutricional perfil = perfilServicio.obtenerPerfilPorUsuario(usuarioId);
        if (perfil == null) {
            return generarListaBasica(lista);
        }

        try {
            PerfilNutricional.MacronutrientesDiarios macros = perfil.getMacronutrientesDiarios();
            int caloriasDiarias = perfil.getCaloriasDiarias();

            double factorPresupuesto = getUsuarioServicio().obtenerFactorPresupuestoUsuario(usuarioId);
            double presupuestoAjustado = lista.getPresupuestoMaximoAsDouble() * factorPresupuesto;

            List<Producto> productosCompatibles = productoServicio.buscarProductosCompatibles(perfil.getRestricciones());

            Map<String, List<Producto>> productosPorCategoria = productosCompatibles.stream()
                    .collect(Collectors.groupingBy(Producto::getCategoria));

            double proteinasObjetivo = macros.getProteinas() * 7;
            double carbohidratosObjetivo = macros.getCarbohidratos() * 7;
            double grasasObjetivo = macros.getGrasas() * 7;
            double caloriasObjetivo = caloriasDiarias * 7;

            double proteinasActuales = 0;
            double carbohidratosActuales = 0;
            double grasasActuales = 0;
            double caloriasActuales = 0;
            double costoActual = 0;

            List<String> categoriasEsenciales = Arrays.asList(
                    "Carnes", "Lácteos", "Frutas", "Verduras", "Cereales", "Legumbres"
            );

            ModalidadAhorroServicio modalidadServicio = new ModalidadAhorroServicio();
            ModalidadAhorro modalidadObj = modalidadServicio.obtenerModalidadPorNombre(modalidadAhorro);
            int prioridadPrecio = modalidadObj != null ? modalidadObj.getPrioridadPrecio() : 6;
            int prioridadNutricion = modalidadObj != null ? modalidadObj.getPrioridadNutricion() : 7;

            for (String categoria : categoriasEsenciales) {
                List<Producto> productosCategoria = productosPorCategoria.getOrDefault(categoria, new ArrayList<>());

                if (productosCategoria.isEmpty()) {
                    continue;
                }

                if (prioridadPrecio > prioridadNutricion) {
                    productosCategoria.sort(Comparator.comparing(Producto::getPrecio));
                } else if (prioridadPrecio < prioridadNutricion) {
                    productosCategoria.sort(Comparator.comparing(obj -> {
                        Producto p = (Producto) obj;
                        return p.getInfoNutricional().getProteinas() +
                                p.getInfoNutricional().getCarbohidratos() / 2 +
                                p.getInfoNutricional().getGrasas() / 3;
                    }).reversed());
                } else {
                    productosCategoria.sort(Comparator.comparing(Producto::getRelacionProteinaPrecio).reversed());
                }

                if (!productosCategoria.isEmpty()) {
                    Producto producto = productosCategoria.get(0);

                    int cantidad = determinarCantidadPorCategoria(categoria);

                    if (costoActual + (producto.getPrecio() * cantidad) <= presupuestoAjustado) {
                        agregarProductoALista(lista, producto.getId(), producto, cantidad);

                        proteinasActuales += producto.getInfoNutricional().getProteinas() * cantidad;
                        carbohidratosActuales += producto.getInfoNutricional().getCarbohidratos() * cantidad;
                        grasasActuales += producto.getInfoNutricional().getGrasas() * cantidad;
                        caloriasActuales += producto.getInfoNutricional().getCalorias() * cantidad;
                        costoActual += producto.getPrecio() * cantidad;
                    }
                }
            }

            List<Producto> todosProductos = new ArrayList<>(productosCompatibles);

            if (prioridadPrecio > prioridadNutricion) {
                todosProductos.sort(Comparator.comparing(p -> p.getPrecio() / (p.getInfoNutricional().getProteinas() + 1)));
            } else if (prioridadPrecio < prioridadNutricion) {
                todosProductos.sort(Comparator.comparing(obj -> {
                    Producto p = (Producto) obj;
                    return (p.getInfoNutricional().getProteinas() * 4) +
                            (p.getInfoNutricional().getCarbohidratos() * 2) +
                            (p.getInfoNutricional().getGrasas() * 3);
                }).reversed());
            } else {
                todosProductos.sort(Comparator.comparing(Producto::getRelacionProteinaPrecio).reversed());
            }

            for (Producto producto : todosProductos) {
                ItemCompra itemExistente = productoYaEnLista(lista.getId(), producto.getId());
                if (itemExistente != null) {
                    continue;
                }

                double deficitProteinas = Math.max(0, proteinasObjetivo - proteinasActuales) / proteinasObjetivo;
                double deficitCarbohidratos = Math.max(0, carbohidratosObjetivo - carbohidratosActuales) / carbohidratosObjetivo;
                double deficitGrasas = Math.max(0, grasasObjetivo - grasasActuales) / grasasObjetivo;

                double proteinasProducto = producto.getInfoNutricional().getProteinas();
                double carbohidratosProducto = producto.getInfoNutricional().getCarbohidratos();
                double grasasProducto = producto.getInfoNutricional().getGrasas();

                boolean esRelevanteParaDeficit =
                        (deficitProteinas > 0.1 && proteinasProducto > 5) ||
                                (deficitCarbohidratos > 0.1 && carbohidratosProducto > 10) ||
                                (deficitGrasas > 0.1 && grasasProducto > 3);

                if (esRelevanteParaDeficit && costoActual + producto.getPrecio() <= presupuestoAjustado) {
                    int cantidad = 1;

                    agregarProductoALista(lista, producto.getId(), producto, cantidad);

                    proteinasActuales += proteinasProducto * cantidad;
                    carbohidratosActuales += carbohidratosProducto * cantidad;
                    grasasActuales += grasasProducto * cantidad;
                    caloriasActuales += producto.getInfoNutricional().getCalorias() * cantidad;
                    costoActual += producto.getPrecio() * cantidad;

                    if (proteinasActuales >= proteinasObjetivo * 0.9 &&
                            carbohidratosActuales >= carbohidratosObjetivo * 0.9 &&
                            grasasActuales >= grasasObjetivo * 0.9 &&
                            caloriasActuales >= caloriasObjetivo * 0.9) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error generando lista optimizada: " + e.getMessage());
        }

        return obtenerListaCompra(lista.getId(), lista.getUsuarioId());
    }

    private ItemCompra productoYaEnLista(Long listaId, Long productoId) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM ItemCompra i WHERE i.lista.id = :listaId AND i.producto.id = :productoId",
                            ItemCompra.class)
                    .setParameter("listaId", listaId)
                    .setParameter("productoId", productoId)
                    .uniqueResult();
        } catch (Exception e) {
            return null;
        }
    }

    private int determinarCantidadPorCategoria(String categoria) {
        switch (categoria) {
            case "Carnes":
            case "Pescados":
                return 3;
            case "Lácteos":
                return 7;
            case "Frutas":
            case "Verduras":
                return 14;
            case "Cereales":
            case "Legumbres":
                return 2;
            case "Panadería":
                return 3;
            default:
                return 1;
        }
    }

    private ListaCompra generarListaBasica(ListaCompra lista) {
        List<String> categoriasBasicas = Arrays.asList(
                "Carnes", "Lácteos", "Frutas", "Verduras", "Cereales", "Panadería"
        );

        double presupuestoRestante = lista.getPresupuestoMaximoAsDouble();
        ProductoServicio productoServicio = getProductoServicio();

        try {
            for (String categoria : categoriasBasicas) {
                List<Producto> productosCategoria = productoServicio.buscarPorCategoria(categoria);

                if (!productosCategoria.isEmpty()) {
                    productosCategoria.sort(Comparator.comparing(Producto::getPrecio));

                    Producto producto = productosCategoria.get(0);
                    int cantidad = determinarCantidadPorCategoria(categoria);

                    if (presupuestoRestante >= producto.getPrecio() * cantidad) {
                        agregarProductoALista(lista, producto.getId(), cantidad);
                        presupuestoRestante -= producto.getPrecio() * cantidad;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error generando lista básica: " + e.getMessage());
        }

        return obtenerListaCompra(lista.getId(), lista.getUsuarioId());
    }

    public boolean eliminarListaCompra(Long listaId, Long usuarioId) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            ListaCompra lista = session.get(ListaCompra.class, listaId);

            if (lista != null && lista.getUsuarioId().equals(usuarioId)) {

                Long transaccionAEliminarId = lista.getTransaccionIdAsociada();
                session.delete(lista);

                if (transaccionAEliminarId != null) {
                    TransaccionServicio transaccionServicio = new TransaccionServicio();
                    Transaccion transaccionAEliminar = transaccionServicio.obtenerTransaccionPorId(transaccionAEliminarId, usuarioId);
                    if (transaccionAEliminar != null) {
                        session.delete(transaccionAEliminar);
                    }
                }

                transaction.commit();

                cacheListas.remove(listaId);
                return true;

            } else {
                transaction.rollback();
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error eliminando lista de compra: " + e.getMessage(), e);
        }
    }
}

