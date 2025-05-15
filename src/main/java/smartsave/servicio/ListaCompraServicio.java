package smartsave.servicio;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import smartsave.config.HibernateConfig;
import smartsave.modelo.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar operaciones relacionadas con listas de compra
 * MIGRADO A HIBERNATE - Reemplaza estructuras en memoria
 */
public class ListaCompraServicio {

    // Servicios relacionados
    private ProductoServicio productoServicio;
    private PerfilNutricionalServicio perfilServicio;
    private UsuarioServicio usuarioServicio;

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
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<ListaCompra> query = session.createQuery(
                    "FROM ListaCompra l WHERE l.usuarioId = :usuarioId ORDER BY l.fechaCreacion DESC",
                    ListaCompra.class);
            query.setParameter("usuarioId", usuarioId);

            List<ListaCompra> listas = query.getResultList();
            // Forzar carga de items para evitar lazy loading issues
            listas.forEach(lista -> lista.getItems().size());
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
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<ListaCompra> query = session.createQuery(
                    "FROM ListaCompra l WHERE l.id = :listaId AND l.usuarioId = :usuarioId",
                    ListaCompra.class);
            query.setParameter("listaId", listaId);
            query.setParameter("usuarioId", usuarioId);

            List<ListaCompra> listas = query.getResultList();
            if (!listas.isEmpty()) {
                ListaCompra lista = listas.get(0);
                lista.getItems().size(); // Forzar carga de items
                return lista;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo lista de compra: " + e.getMessage(), e);
        }
    }

    /**
     * Añade un producto a una lista de compra
     * @param lista Lista de compra a modificar
     * @param productoId ID del producto a añadir
     * @param cantidad Cantidad del producto
     * @return La lista actualizada
     */
    public ListaCompra agregarProductoALista(ListaCompra lista, Long productoId, int cantidad) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Reattach de la lista si es necesario
            lista = session.merge(lista);

            // Obtener el producto
            Producto producto = getProductoServicio().obtenerProductoPorId(productoId);
            if (producto == null) {
                transaction.rollback();
                return lista;
            }

            // Verificar si el producto ya está en la lista
            boolean productoExiste = lista.getItems().stream()
                    .anyMatch(item -> item.getProducto().getId().equals(productoId));

            if (productoExiste) {
                // Si ya existe, aumentar la cantidad
                for (ItemCompra item : lista.getItems()) {
                    if (item.getProducto().getId().equals(productoId)) {
                        item.setCantidad(item.getCantidad() + cantidad);
                        session.update(item);
                        break;
                    }
                }
            } else {
                // Si no existe, crear nuevo item
                ItemCompra nuevoItem = new ItemCompra(producto, cantidad);
                lista.agregarItem(nuevoItem);
                session.save(nuevoItem);
            }

            session.update(lista);
            transaction.commit();
            return lista;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error agregando producto a lista: " + e.getMessage(), e);
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
            return lista;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error marcando lista como completada: " + e.getMessage(), e);
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
        // mientras respetamos el presupuesto
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
            // Evitar duplicados
            boolean yaEstaEnLista = false;
            try (Session session = HibernateConfig.getSessionFactory().openSession()) {
                ListaCompra actualizada = session.get(ListaCompra.class, lista.getId());
                actualizada.getItems().size(); // Forzar carga
                yaEstaEnLista = actualizada.getItems().stream()
                        .anyMatch(i -> i.getProducto().getId().equals(producto.getId()));
            }

            if (yaEstaEnLista) {
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

        return lista;
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

        for (String categoria : categoriasBasicas) {
            List<Producto> productosCategoria = getProductoServicio().buscarPorCategoria(categoria);

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

        return lista;
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
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error actualizando lista de compra: " + e.getMessage(), e);
        }
    }


}