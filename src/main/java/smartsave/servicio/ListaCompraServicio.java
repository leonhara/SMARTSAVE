package smartsave.servicio;

import smartsave.modelo.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar operaciones relacionadas con listas de compra
 * (Por ahora utiliza una estructura en memoria, luego se conectará a la BD)
 */
public class ListaCompraServicio {

    // Simulación de base de datos (solo para demostración)
    private static final Map<Long, List<ListaCompra>> LISTAS_POR_USUARIO = new HashMap<>();
    private static Long ultimoIdLista = 0L;
    private static Long ultimoIdItem = 0L;

    // Servicios relacionados
    private ProductoServicio productoServicio = new ProductoServicio();
    private PerfilNutricionalServicio perfilServicio = new PerfilNutricionalServicio();

    /**
     * Crea una nueva lista de compra para un usuario
     * @param usuarioId ID del usuario
     * @param nombre Nombre de la lista
     * @param modalidadAhorro Modalidad de ahorro ("Máximo", "Equilibrado", "Estándar")
     * @param presupuestoMaximo Presupuesto máximo para la lista
     * @return La lista de compra creada
     */
    public ListaCompra crearListaCompra(Long usuarioId, String nombre, String modalidadAhorro, double presupuestoMaximo) {
        ListaCompra lista = new ListaCompra(usuarioId, nombre, modalidadAhorro, presupuestoMaximo);
        lista.setId(++ultimoIdLista);

        // Obtener o crear la lista de listas del usuario
        List<ListaCompra> listasUsuario = LISTAS_POR_USUARIO.getOrDefault(usuarioId, new ArrayList<>());

        // Añadir la nueva lista
        listasUsuario.add(lista);

        // Actualizar el mapa
        LISTAS_POR_USUARIO.put(usuarioId, listasUsuario);

        return lista;
    }

    /**
     * Obtiene todas las listas de compra de un usuario
     * @param usuarioId ID del usuario
     * @return Lista de todas las listas de compra del usuario
     */
    public List<ListaCompra> obtenerListasCompraUsuario(Long usuarioId) {
        return LISTAS_POR_USUARIO.getOrDefault(usuarioId, new ArrayList<>());
    }

    /**
     * Obtiene una lista de compra específica por su ID
     * @param listaId ID de la lista
     * @param usuarioId ID del usuario
     * @return La lista de compra o null si no existe
     */
    public ListaCompra obtenerListaCompra(Long listaId, Long usuarioId) {
        List<ListaCompra> listasUsuario = LISTAS_POR_USUARIO.get(usuarioId);
        if (listasUsuario == null) {
            return null;
        }

        return listasUsuario.stream()
                .filter(l -> l.getId().equals(listaId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Añade un producto a una lista de compra
     * @param lista Lista de compra a modificar
     * @param productoId ID del producto a añadir
     * @param cantidad Cantidad del producto
     * @return La lista actualizada
     */
    public ListaCompra agregarProductoALista(ListaCompra lista, Long productoId, int cantidad) {
        Producto producto = productoServicio.obtenerProductoPorId(productoId);
        if (producto == null) {
            return lista;
        }

        ItemCompra item = new ItemCompra(producto, cantidad);
        item.setId(++ultimoIdItem);

        lista.agregarItem(item);

        return lista;
    }

    /**
     * Elimina un item de una lista de compra
     * @param lista Lista de compra a modificar
     * @param itemId ID del item a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean eliminarItemDeLista(ListaCompra lista, Long itemId) {
        Optional<ItemCompra> itemOptional = lista.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst();

        if (itemOptional.isPresent()) {
            return lista.eliminarItem(itemOptional.get());
        }

        return false;
    }

    /**
     * Marca un item como comprado o no comprado
     * @param lista Lista de compra a modificar
     * @param itemId ID del item
     * @param comprado true si está comprado, false si no
     * @return true si se actualizó correctamente
     */
    public boolean actualizarEstadoItem(ListaCompra lista, Long itemId, boolean comprado) {
        Optional<ItemCompra> itemOptional = lista.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst();

        if (itemOptional.isPresent()) {
            itemOptional.get().setComprado(comprado);
            return true;
        }

        return false;
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

        Optional<ItemCompra> itemOptional = lista.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst();

        if (itemOptional.isPresent()) {
            itemOptional.get().setCantidad(nuevaCantidad);
            return true;
        }

        return false;
    }

    /**
     * Marca una lista de compra como completada
     * @param lista Lista de compra a marcar
     * @return La lista actualizada
     */
    public ListaCompra marcarListaComoCompletada(ListaCompra lista) {
        lista.setCompletada(true);
        return lista;
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
        PerfilNutricional perfil = perfilServicio.obtenerPerfilPorUsuario(usuarioId);
        if (perfil == null) {
            // Si no hay perfil, generar una lista básica
            return generarListaBasica(lista);
        }

        // Obtener las necesidades nutricionales según el perfil
        PerfilNutricional.MacronutrientesDiarios macros = perfil.getMacronutrientesDiarios();
        int caloriasDiarias = perfil.getCaloriasDiarias();

        // Aplicar factor según modalidad de ahorro
        double factorPresupuesto;
        switch (modalidadAhorro) {
            case "Máximo":
                factorPresupuesto = 0.7; // 70% del presupuesto para ahorrar más
                break;
            case "Equilibrado":
                factorPresupuesto = 0.85; // 85% del presupuesto
                break;
            case "Estándar":
            default:
                factorPresupuesto = 1.0; // 100% del presupuesto
                break;
        }

        double presupuestoAjustado = presupuestoMaximo * factorPresupuesto;

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

        // Priorizar categorías según necesidades
        for (String categoria : categoriasEsenciales) {
            List<Producto> productosCategoria = productosPorCategoria.getOrDefault(categoria, new ArrayList<>());

            if (productosCategoria.isEmpty()) {
                continue;
            }

            // Ordenar productos según la modalidad de ahorro
            if ("Máximo".equals(modalidadAhorro)) {
                // En modo máximo ahorro, priorizar precio
                productosCategoria.sort(Comparator.comparing(Producto::getPrecio));
            } else if ("Equilibrado".equals(modalidadAhorro)) {
                // En modo equilibrado, priorizar relación nutrición/precio
                productosCategoria.sort(Comparator.comparing(Producto::getRelacionProteinaPrecio).reversed());
            } else {
                // En modo estándar, priorizar calidad nutricional
                productosCategoria.sort(Comparator.comparing(obj -> {
                    Producto p = (Producto) obj;
                    return p.getInfoNutricional().getProteinas() +
                            p.getInfoNutricional().getCarbohidratos() / 2 +
                            p.getInfoNutricional().getGrasas() / 3;
                }).reversed());
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

        // Ordenar según prioridad nutricional y precio
        if ("Máximo".equals(modalidadAhorro)) {
            // Priorizar precio pero asegurando algo de nutrición
            todosProductos.sort(Comparator.comparing(p -> p.getPrecio() / (p.getInfoNutricional().getProteinas() + 1)));
        } else if ("Equilibrado".equals(modalidadAhorro)) {
            // Balance entre nutrición y precio
            todosProductos.sort(Comparator.comparing(Producto::getRelacionProteinaPrecio).reversed());
        } else {
            // Priorizar nutrición
            todosProductos.sort(Comparator.comparing(obj -> {
                Producto p = (Producto) obj;
                return (p.getInfoNutricional().getProteinas() * 4) +
                        (p.getInfoNutricional().getCarbohidratos() * 2) +
                        (p.getInfoNutricional().getGrasas() * 3);
            }).reversed());
        }

        // Añadir productos hasta alcanzar objetivos o agotar presupuesto
        for (Producto producto : todosProductos) {
            // Evitar duplicados
            if (lista.getItems().stream().anyMatch(i -> i.getProducto().getId().equals(producto.getId()))) {
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

        double presupuestoRestante = lista.getPresupuestoMaximo();

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

        return lista;
    }

    /**
     * Elimina una lista de compra
     * @param listaId ID de la lista a eliminar
     * @param usuarioId ID del usuario
     * @return true si se eliminó correctamente
     */
    public boolean eliminarListaCompra(Long listaId, Long usuarioId) {
        List<ListaCompra> listasUsuario = LISTAS_POR_USUARIO.get(usuarioId);
        if (listasUsuario == null) {
            return false;
        }

        int tamanioAnterior = listasUsuario.size();
        listasUsuario.removeIf(l -> l.getId().equals(listaId));

        LISTAS_POR_USUARIO.put(usuarioId, listasUsuario);

        return tamanioAnterior > listasUsuario.size();
    }

    /**
     * Obtiene las listas de compra activas (no completadas) de un usuario
     * @param usuarioId ID del usuario
     * @return Lista de listas de compra activas
     */
    public List<ListaCompra> obtenerListasActivas(Long usuarioId) {
        return LISTAS_POR_USUARIO.getOrDefault(usuarioId, new ArrayList<>())
                .stream()
                .filter(l -> !l.isCompletada())
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las listas de compra completadas de un usuario
     * @param usuarioId ID del usuario
     * @return Lista de listas de compra completadas
     */
    public List<ListaCompra> obtenerListasCompletadas(Long usuarioId) {
        return LISTAS_POR_USUARIO.getOrDefault(usuarioId, new ArrayList<>())
                .stream()
                .filter(ListaCompra::isCompletada)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza los detalles de una lista de compra
     * @param lista Lista de compra con datos actualizados
     * @return true si se actualizó correctamente
     */
    public boolean actualizarListaCompra(ListaCompra lista) {
        if (lista.getId() == null || lista.getUsuarioId() == null) {
            return false;
        }

        List<ListaCompra> listasUsuario = LISTAS_POR_USUARIO.get(lista.getUsuarioId());
        if (listasUsuario == null) {
            return false;
        }

        // Buscar y actualizar la lista
        for (int i = 0; i < listasUsuario.size(); i++) {
            if (listasUsuario.get(i).getId().equals(lista.getId())) {
                listasUsuario.set(i, lista);
                LISTAS_POR_USUARIO.put(lista.getUsuarioId(), listasUsuario);
                return true;
            }
        }

        return false;
    }
}