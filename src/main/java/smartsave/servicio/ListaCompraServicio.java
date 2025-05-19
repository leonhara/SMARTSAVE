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

/**
 * Servicio optimizado para gestionar operaciones relacionadas con listas de compra
 * MIGRADO A HIBERNATE - Incluye optimizaciones para productos de Mercadona
 */
public class ListaCompraServicio {

    // Servicios relacionados
    private ProductoServicio productoServicio;
    private PerfilNutricionalServicio perfilServicio;
    private UsuarioServicio usuarioServicio;

    // Caché para listas de compra
    private final Map<Long, ListaCompra> cacheListas = new HashMap<>();
    private long ultimaLimpiezaCache = System.currentTimeMillis();
    private static final long TIEMPO_EXPIRACION_CACHE = TimeUnit.MINUTES.toMillis(5);

    // Métodos singleton para servicios
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

    /**
     * Limpia la caché si ha pasado el tiempo de expiración
     */
    private void limpiarCacheSiNecesario() {
        long tiempoActual = System.currentTimeMillis();
        if (tiempoActual - ultimaLimpiezaCache > TIEMPO_EXPIRACION_CACHE) {
            cacheListas.clear();
            ultimaLimpiezaCache = tiempoActual;
        }
    }

    /**
     * Crea una nueva lista de compra para un usuario
     * @param usuarioId ID del usuario
     * @param nombre Nombre de la lista
     * @param modalidadAhorro Modalidad de ahorro ("Máximo", "Equilibrado", "Estándar")
     * @param presupuestoMaximo Presupuesto máximo para la lista
     * @return La lista de compra creada
     */
    public ListaCompra crearListaCompra(Long usuarioId, String nombre, String modalidadAhorro, double presupuestoMaximo) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            ListaCompra lista = new ListaCompra(usuarioId, nombre, modalidadAhorro, BigDecimal.valueOf(presupuestoMaximo));
            session.save(lista);

            transaction.commit();

            // Actualizar caché
            cacheListas.put(lista.getId(), lista);

            return lista;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error creando lista de compra: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todas las listas de compra de un usuario
     * @param usuarioId ID del usuario
     * @return Lista de todas las listas de compra del usuario
     */
    public List<ListaCompra> obtenerListasCompraUsuario(Long usuarioId) {
        // Limpiar caché si es necesario
        limpiarCacheSiNecesario();

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<ListaCompra> query = session.createQuery(
                    "FROM ListaCompra l WHERE l.usuarioId = :usuarioId ORDER BY l.fechaCreacion DESC",
                    ListaCompra.class);
            query.setParameter("usuarioId", usuarioId);

            List<ListaCompra> listas = query.getResultList();

            // Forzar carga de items para evitar lazy loading issues
            for (ListaCompra lista : listas) {
                lista.getItems().size();
                // Actualizar caché
                cacheListas.put(lista.getId(), lista);
            }

            return listas;
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo listas de compra: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene una lista de compra específica por su ID
     * @param listaId ID de la lista
     * @param usuarioId ID del usuario
     * @return La lista de compra o null si no existe
     */
    public ListaCompra obtenerListaCompra(Long listaId, Long usuarioId) {
        // Verificar caché
        if (cacheListas.containsKey(listaId)) {
            ListaCompra listaCache = cacheListas.get(listaId);
            // Verificar que pertenezca al usuario correcto
            if (listaCache.getUsuarioId().equals(usuarioId)) {
                return listaCache;
            }
        }

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            ListaCompra lista = obtenerListaCompraConItems(session, listaId, usuarioId);

            // Actualizar caché si se encontró
            if (lista != null) {
                cacheListas.put(lista.getId(), lista);
            }

            return lista;
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo lista de compra: " + e.getMessage(), e);
        }
    }

    /**
     * Método auxiliar para obtener lista con items cargados
     */
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

    /**
     * Añade un producto a una lista de compra con manejo mejorado de productos de Mercadona
     * @param lista Lista de compra a modificar
     * @param productoId ID del producto a añadir
     * @param cantidad Cantidad del producto
     * @return La lista actualizada
     */
    public ListaCompra agregarProductoALista(ListaCompra lista, Long productoId, int cantidad) {
        System.out.println("Iniciando adición de producto ID: " + productoId);

        Session session = null;
        Transaction transaction = null;

        try {
            // Abrir una nueva sesión
            session = HibernateConfig.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // 1. Verificar si el producto ya existe en la base de datos
            Producto producto = session.get(Producto.class, productoId);
            System.out.println("Producto encontrado en BD: " + (producto != null ? "Sí" : "No"));

            // 2. Si no existe, obtener los datos del producto de Mercadona
            if (producto == null) {
                System.out.println("Buscando información real del producto en Mercadona...");

                // Obtener información del producto de Mercadona
                Producto productoMercadona = obtenerProductoMercadona(productoId);

                if (productoMercadona != null) {
                    System.out.println("Producto encontrado en Mercadona: " + productoMercadona.getNombre());

                    // Usar los datos reales del producto
                    producto = new Producto();
                    producto.setNombre(productoMercadona.getNombre());
                    producto.setMarca(productoMercadona.getMarca());
                    producto.setCategoria(productoMercadona.getCategoria());
                    producto.setPrecio(productoMercadona.getPrecio());
                    producto.setSupermercado("Mercadona");
                    producto.setDisponible(true);

                    // Copiar información nutricional si está disponible
                    if (productoMercadona.getInfoNutricional() != null) {
                        Producto.NutricionProducto info = new Producto.NutricionProducto();
                        info.setCalorias(productoMercadona.getInfoNutricional().getCalorias());
                        info.setProteinas(productoMercadona.getInfoNutricional().getProteinas());
                        info.setCarbohidratos(productoMercadona.getInfoNutricional().getCarbohidratos());
                        info.setGrasas(productoMercadona.getInfoNutricional().getGrasas());
                        info.setFibra(productoMercadona.getInfoNutricional().getFibra());
                        info.setSodio(productoMercadona.getInfoNutricional().getSodio());
                        info.setAzucares(productoMercadona.getInfoNutricional().getAzucares());
                        producto.setInfoNutricional(info);
                    }
                } else {
                    // Si no se encuentra, crear un producto básico
                    System.out.println("No se encontró información en Mercadona, creando producto básico");
                    producto = new Producto();
                    producto.setNombre("Producto " + productoId);
                    producto.setMarca("Mercadona");
                    producto.setCategoria("Otros");
                    producto.setPrecio(1.0);
                    producto.setSupermercado("Mercadona");
                    producto.setDisponible(true);
                }

                // Verificar que el precio no sea nulo
                if (producto.getPrecioBD() == null) {
                    System.out.println("Precio nulo detectado, estableciendo a 1.0");
                    producto.setPrecioBD(BigDecimal.valueOf(1.0));
                }

                // Guardar el nuevo producto en la BD
                System.out.println("Creando nuevo producto en la base de datos");
                session.save(producto);
                session.flush();

                System.out.println("Nuevo producto guardado con ID generado: " + producto.getId());

                // Usar el ID generado por la base de datos
                productoId = producto.getId();
            }

            // 3. Verificar si el producto ya está en la lista
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
                // Si ya existe, aumentar la cantidad
                System.out.println("Item ya existe, actualizando cantidad");
                itemExistente.setCantidad(itemExistente.getCantidad() + cantidad);
                session.update(itemExistente);
            } else {
                // Si no existe, crear nuevo item
                System.out.println("Creando nuevo item para el producto");

                // Obtener referencias frescas
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

                // Guardar el nuevo item
                session.save(nuevoItem);
            }

            // Confirmar la transacción
            transaction.commit();
            transaction = null; // Marcar como completada para evitar rollback en finally

            System.out.println("Ítem añadido correctamente");

            // Limpiar caché para forzar recarga fresca
            cacheListas.remove(lista.getId());

            // Recargar la lista actualizada
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

    /**
     * Obtiene la información real de un producto de Mercadona usando el servicio de API
     * @param productoId ID del producto a buscar
     * @return Objeto Producto con la información real o null si no se encuentra
     */
    private Producto obtenerProductoMercadona(Long productoId) {
        try {
            ProductoServicio productoServicio = getProductoServicio();

            if (!productoServicio.isApiMercadonaDisponible()) {
                System.err.println("API de Mercadona no disponible");
                return null;
            }

            // Buscar en resultados de búsqueda de términos comunes
            List<String> terminosBusqueda = Arrays.asList("aceitunas", "pan", "leche", "fruta", "verdura", "carne", "pescado");
            List<Producto> productosCandidatos = new ArrayList<>();

            // Realizar búsquedas en paralelo
            for (String termino : terminosBusqueda) {
                try {
                    CompletableFuture<List<Producto>> future = productoServicio.getMercadonaApiServicio()
                            .buscarProductos(termino);
                    List<Producto> resultados = future.get(5, TimeUnit.SECONDS);
                    productosCandidatos.addAll(resultados);
                } catch (Exception e) {
                    System.err.println("Error en búsqueda de productos con término " + termino + ": " + e.getMessage());
                }
            }

            // También buscar en productos nuevos
            try {
                CompletableFuture<List<Producto>> futureNuevos = productoServicio.getMercadonaApiServicio()
                        .obtenerProductosNuevos();
                List<Producto> nuevos = futureNuevos.get(5, TimeUnit.SECONDS);
                productosCandidatos.addAll(nuevos);
            } catch (Exception e) {
                System.err.println("Error obteniendo productos nuevos: " + e.getMessage());
            }

            // Buscar el producto específico por ID
            for (Producto candidato : productosCandidatos) {
                if (candidato.getId() != null && candidato.getId().equals(productoId)) {
                    System.out.println("Encontrado producto en API: " + candidato.getNombre());
                    return candidato;
                }
            }

            System.out.println("Producto con ID " + productoId + " no encontrado en los resultados de Mercadona");
            return null;
        } catch (Exception e) {
            System.err.println("Error general obteniendo producto Mercadona: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca un producto de Mercadona por su ID
     * @param productoId ID del producto
     * @return Producto encontrado o null
     */
    private Producto buscarProductoMercadona(Long productoId) {
        try {
            ProductoServicio productoServicio = getProductoServicio();

            // Si la API no está disponible, no seguir intentando
            if (!productoServicio.isApiMercadonaDisponible()) {
                return null;
            }

            // Buscar usando nuevos términos comunes de alimentación para aumentar probabilidades
            CompletableFuture<List<Producto>> futureLeche = productoServicio.getMercadonaApiServicio().buscarProductos("leche");
            CompletableFuture<List<Producto>> futurePan = productoServicio.getMercadonaApiServicio().buscarProductos("pan");
            CompletableFuture<List<Producto>> futureFruta = productoServicio.getMercadonaApiServicio().buscarProductos("fruta");

            // También obtener productos nuevos
            CompletableFuture<List<Producto>> futureNuevos = productoServicio.getMercadonaApiServicio().obtenerProductosNuevos();

            // Esperar a que se completen todas las búsquedas
            CompletableFuture.allOf(futureLeche, futurePan, futureFruta, futureNuevos).join();

            // Crear una lista con todos los productos encontrados
            List<Producto> todosProductos = new ArrayList<>();
            todosProductos.addAll(futureLeche.get());
            todosProductos.addAll(futurePan.get());
            todosProductos.addAll(futureFruta.get());
            todosProductos.addAll(futureNuevos.get());

            // Buscar el producto con el ID específico
            return todosProductos.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(productoId))
                    .findFirst()
                    .orElse(null);

        } catch (Exception e) {
            System.err.println("Error buscando producto de Mercadona: " + e.getMessage());
            return null;
        }
    }

    /**
     * Elimina un item de una lista de compra
     * @param lista Lista de compra a modificar
     * @param itemId ID del item a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean eliminarItemDeLista(ListaCompra lista, Long itemId) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            ItemCompra item = session.get(ItemCompra.class, itemId);
            if (item != null && item.getLista().getId().equals(lista.getId())) {
                session.delete(item);
                transaction.commit();

                // Actualizar o eliminar de caché
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

    /**
     * Marca un item como comprado o no comprado
     * @param lista Lista de compra a modificar
     * @param itemId ID del item
     * @param comprado true si está comprado, false si no
     * @return true si se actualizó correctamente
     */
    public boolean actualizarEstadoItem(ListaCompra lista, Long itemId, boolean comprado) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            ItemCompra item = session.get(ItemCompra.class, itemId);
            if (item != null && item.getLista().getId().equals(lista.getId())) {
                item.setComprado(comprado);
                session.update(item);
                transaction.commit();

                // Actualizar o eliminar de caché
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

    /**
     * Actualiza la cantidad de un item
     * @param lista Lista de compra a modificar
     * @param itemId ID del item
     * @param nuevaCantidad Nueva cantidad
     * @return true si se actualizó correctamente
     */
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

                // Actualizar o eliminar de caché
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

    /**
     * Marca una lista de compra como completada
     * @param lista Lista de compra a marcar
     * @return La lista actualizada
     */
    public ListaCompra marcarListaComoCompletada(ListaCompra lista) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            lista.setCompletada(true);
            session.update(lista);

            transaction.commit();

            // Actualizar o eliminar de caché
            cacheListas.remove(lista.getId());

            return lista;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error marcando lista como completada: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene las listas de compra activas (no completadas) de un usuario
     * @param usuarioId ID del usuario
     * @return Lista de listas de compra activas
     */
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

    /**
     * Obtiene las listas de compra completadas de un usuario
     * @param usuarioId ID del usuario
     * @return Lista de listas de compra completadas
     */
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

    /**
     * Actualiza los detalles de una lista de compra
     * @param lista Lista de compra con datos actualizados
     * @return true si se actualizó correctamente
     */
    public boolean actualizarListaCompra(ListaCompra lista) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.update(lista);
            transaction.commit();

            // Actualizar caché
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

    /**
     * Genera una lista de compra optimizada según el perfil nutricional y el presupuesto
     * @param usuarioId ID del usuario
     * @param nombre Nombre de la lista
     * @param modalidadAhorro Modalidad de ahorro
     * @param presupuestoMaximo Presupuesto máximo
     * @return Lista de compra generada automáticamente
     */
    public ListaCompra generarListaOptimizada(Long usuarioId, String nombre, String modalidadAhorro, double presupuestoMaximo) {
        // Crear la lista vacía
        ListaCompra lista = crearListaCompra(usuarioId, nombre, modalidadAhorro, presupuestoMaximo);

        // Verificar si el usuario tiene perfil nutricional
        ProductoServicio productoServicio = getProductoServicio();
        PerfilNutricionalServicio perfilServicio = getPerfilServicio();
        PerfilNutricional perfil = perfilServicio.obtenerPerfilPorUsuario(usuarioId);
        if (perfil == null) {
            // Si no hay perfil, generar una lista básica
            return generarListaBasica(lista);
        }

        try {
            // Obtener las necesidades nutricionales según el perfil
            PerfilNutricional.MacronutrientesDiarios macros = perfil.getMacronutrientesDiarios();
            int caloriasDiarias = perfil.getCaloriasDiarias();

            // Aplicar factor según modalidad de ahorro
            double factorPresupuesto = getUsuarioServicio().obtenerFactorPresupuestoUsuario(usuarioId);
            double presupuestoAjustado = lista.getPresupuestoMaximoAsDouble() * factorPresupuesto;

            // Conseguir productos que cumplan con las restricciones alimentarias
            List<Producto> productosCompatibles = productoServicio.buscarProductosCompatibles(perfil.getRestricciones());

            // Organizar productos por categorías para asegurar variedad
            Map<String, List<Producto>> productosPorCategoria = productosCompatibles.stream()
                    .collect(Collectors.groupingBy(Producto::getCategoria));

            // Calcular macronutrientes para 7 días (semana)
            double proteinasObjetivo = macros.getProteinas() * 7;
            double carbohidratosObjetivo = macros.getCarbohidratos() * 7;
            double grasasObjetivo = macros.getGrasas() * 7;
            double caloriasObjetivo = caloriasDiarias * 7;

            // Variables para seguimiento
            double proteinasActuales = 0;
            double carbohidratosActuales = 0;
            double grasasActuales = 0;
            double caloriasActuales = 0;
            double costoActual = 0;

            // Asegurarnos de incluir alimentos de cada categoría importante
            List<String> categoriasEsenciales = Arrays.asList(
                    "Carnes", "Lácteos", "Frutas", "Verduras", "Cereales", "Legumbres"
            );

            // Obtener prioridades según modalidad
            ModalidadAhorroServicio modalidadServicio = new ModalidadAhorroServicio();
            ModalidadAhorro modalidadObj = modalidadServicio.obtenerModalidadPorNombre(modalidadAhorro);
            int prioridadPrecio = modalidadObj != null ? modalidadObj.getPrioridadPrecio() : 6;
            int prioridadNutricion = modalidadObj != null ? modalidadObj.getPrioridadNutricion() : 7;

            // Priorizar categorías según necesidades
            for (String categoria : categoriasEsenciales) {
                List<Producto> productosCategoria = productosPorCategoria.getOrDefault(categoria, new ArrayList<>());

                if (productosCategoria.isEmpty()) {
                    continue;
                }

                // Ordenar productos según las prioridades de la modalidad de ahorro
                if (prioridadPrecio > prioridadNutricion) {
                    // Priorizar precio (modalidad máximo ahorro o similar)
                    productosCategoria.sort(Comparator.comparing(Producto::getPrecio));
                } else if (prioridadPrecio < prioridadNutricion) {
                    // Priorizar nutrición (modalidad estándar o similar)
                    productosCategoria.sort(Comparator.comparing(obj -> {
                        Producto p = (Producto) obj;
                        return p.getInfoNutricional().getProteinas() +
                                p.getInfoNutricional().getCarbohidratos() / 2 +
                                p.getInfoNutricional().getGrasas() / 3;
                    }).reversed());
                } else {
                    // Balance entre precio y nutrición (modalidad equilibrada)
                    productosCategoria.sort(Comparator.comparing(Producto::getRelacionProteinaPrecio).reversed());
                }

                // Añadir al menos un producto de cada categoría esencial
                if (!productosCategoria.isEmpty()) {
                    Producto producto = productosCategoria.get(0);

                    // Determinar cantidad apropiada según la categoría
                    int cantidad = determinarCantidadPorCategoria(categoria);

                    // Verificar si añadir este producto nos excede del presupuesto
                    if (costoActual + (producto.getPrecio() * cantidad) <= presupuestoAjustado) {
                        // Añadir a la lista
                        agregarProductoALista(lista, producto.getId(), cantidad);

                        // Actualizar contadores
                        proteinasActuales += producto.getInfoNutricional().getProteinas() * cantidad;
                        carbohidratosActuales += producto.getInfoNutricional().getCarbohidratos() * cantidad;
                        grasasActuales += producto.getInfoNutricional().getGrasas() * cantidad;
                        caloriasActuales += producto.getInfoNutricional().getCalorias() * cantidad;
                        costoActual += producto.getPrecio() * cantidad;
                    }
                }
            }

            // Completar con más productos para alcanzar objetivos nutricionales
            List<Producto> todosProductos = new ArrayList<>(productosCompatibles);

            // Ordenar según prioridades de modalidad
            if (prioridadPrecio > prioridadNutricion) {
                // Priorizar precio pero asegurando algo de nutrición
                todosProductos.sort(Comparator.comparing(p -> p.getPrecio() / (p.getInfoNutricional().getProteinas() + 1)));
            } else if (prioridadPrecio < prioridadNutricion) {
                // Priorizar nutrición
                todosProductos.sort(Comparator.comparing(obj -> {
                    Producto p = (Producto) obj;
                    return (p.getInfoNutricional().getProteinas() * 4) +
                            (p.getInfoNutricional().getCarbohidratos() * 2) +
                            (p.getInfoNutricional().getGrasas() * 3);
                }).reversed());
            } else {
                // Balance entre nutrición y precio
                todosProductos.sort(Comparator.comparing(Producto::getRelacionProteinaPrecio).reversed());
            }

            // Añadir productos hasta alcanzar objetivos o agotar presupuesto
            for (Producto producto : todosProductos) {
                // Evitar duplicados verificando en BD
                ItemCompra itemExistente = productoYaEnLista(lista.getId(), producto.getId());
                if (itemExistente != null) {
                    continue;
                }

                // Determinar qué macro está más lejos de su objetivo
                double deficitProteinas = Math.max(0, proteinasObjetivo - proteinasActuales) / proteinasObjetivo;
                double deficitCarbohidratos = Math.max(0, carbohidratosObjetivo - carbohidratosActuales) / carbohidratosObjetivo;
                double deficitGrasas = Math.max(0, grasasObjetivo - grasasActuales) / grasasObjetivo;

                // Determinar el macro predominante en este producto
                double proteinasProducto = producto.getInfoNutricional().getProteinas();
                double carbohidratosProducto = producto.getInfoNutricional().getCarbohidratos();
                double grasasProducto = producto.getInfoNutricional().getGrasas();

                // ¿Es este producto relevante para nuestros déficits nutricionales?
                boolean esRelevanteParaDeficit =
                        (deficitProteinas > 0.1 && proteinasProducto > 5) ||
                                (deficitCarbohidratos > 0.1 && carbohidratosProducto > 10) ||
                                (deficitGrasas > 0.1 && grasasProducto > 3);

                // Si el producto ayuda con nuestros déficits y tenemos presupuesto, añadirlo
                if (esRelevanteParaDeficit && costoActual + producto.getPrecio() <= presupuestoAjustado) {
                    // Determinar cantidad
                    int cantidad = 1;

                    // Añadir a la lista
                    agregarProductoALista(lista, producto.getId(), cantidad);

                    // Actualizar contadores
                    proteinasActuales += proteinasProducto * cantidad;
                    carbohidratosActuales += carbohidratosProducto * cantidad;
                    grasasActuales += grasasProducto * cantidad;
                    caloriasActuales += producto.getInfoNutricional().getCalorias() * cantidad;
                    costoActual += producto.getPrecio() * cantidad;

                    // Si hemos alcanzado objetivos, parar
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
            // Si hay error, seguir adelante con lo que tenemos
        }

        return obtenerListaCompra(lista.getId(), lista.getUsuarioId());
    }

    /**
     * Verifica si un producto ya está en una lista
     */
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

    /**
     * Determina una cantidad apropiada según la categoría del producto
     * @param categoria Categoría del producto
     * @return Cantidad recomendada
     */
    private int determinarCantidadPorCategoria(String categoria) {
        switch (categoria) {
            case "Carnes":
            case "Pescados":
                return 3; // 3 piezas/paquetes por semana
            case "Lácteos":
                return 7; // 7 unidades por semana (1 por día)
            case "Frutas":
            case "Verduras":
                return 14; // 14 piezas por semana (2 por día)
            case "Cereales":
            case "Legumbres":
                return 2; // 2 paquetes por semana
            case "Panadería":
                return 3; // 3 unidades por semana
            default:
                return 1;
        }
    }

    /**
     * Genera una lista básica con productos esenciales
     * @param lista Lista de compra vacía a poblar
     * @return Lista de compra con productos básicos
     */
    private ListaCompra generarListaBasica(ListaCompra lista) {
        // Añadir algunos productos básicos
        List<String> categoriasBasicas = Arrays.asList(
                "Carnes", "Lácteos", "Frutas", "Verduras", "Cereales", "Panadería"
        );

        double presupuestoRestante = lista.getPresupuestoMaximoAsDouble();
        ProductoServicio productoServicio = getProductoServicio();

        try {
            for (String categoria : categoriasBasicas) {
                List<Producto> productosCategoria = productoServicio.buscarPorCategoria(categoria);

                if (!productosCategoria.isEmpty()) {
                    // Ordenar por precio ascendente
                    productosCategoria.sort(Comparator.comparing(Producto::getPrecio));

                    // Añadir el producto más económico de cada categoría
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
            // Si hay error, seguir adelante con lo que tenemos
        }

        return obtenerListaCompra(lista.getId(), lista.getUsuarioId());
    }

    /**
     * Elimina una lista de compra
     * @param listaId ID de la lista a eliminar
     * @param usuarioId ID del usuario
     * @return true si se eliminó correctamente
     */
    public boolean eliminarListaCompra(Long listaId, Long usuarioId) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            ListaCompra lista = obtenerListaCompra(listaId, usuarioId);
            if (lista != null) {
                session.delete(lista);
                transaction.commit();

                // Eliminar de caché
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

