package smartsave.servicio;

import smartsave.modelo.Transaccion;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar operaciones relacionadas con transacciones
 * (Por ahora utiliza estructuras en memoria, luego se conectará a la BD)
 */
public class TransaccionServicio {

    // Simulación de base de datos (solo para demostración)
    private static final Map<Long, List<Transaccion>> TRANSACCIONES_POR_USUARIO = new HashMap<>();
    private static Long ultimoId = 0L;

    /**
     * Agrega una nueva transacción asociada a un usuario
     * @param transaccion La transacción a registrar
     * @param usuarioId ID del usuario al que pertenece la transacción
     * @return La transacción con su ID asignado
     */
    public Transaccion agregarTransaccion(Transaccion transaccion, Long usuarioId) {
        // Asignar ID y usuario
        transaccion.setId(++ultimoId);
        transaccion.setUsuarioId(usuarioId);

        // Obtener la lista de transacciones del usuario o crear una nueva
        List<Transaccion> transaccionesUsuario = TRANSACCIONES_POR_USUARIO.getOrDefault(usuarioId, new ArrayList<>());

        // Añadir la transacción
        transaccionesUsuario.add(transaccion);

        // Actualizar el mapa
        TRANSACCIONES_POR_USUARIO.put(usuarioId, transaccionesUsuario);

        return transaccion;
    }

    /**
     * Obtiene todas las transacciones de un usuario
     * @param usuarioId ID del usuario
     * @return Lista de transacciones del usuario
     */
    public List<Transaccion> obtenerTransaccionesPorUsuario(Long usuarioId) {
        return TRANSACCIONES_POR_USUARIO.getOrDefault(usuarioId, new ArrayList<>())
                .stream()
                .sorted(Comparator.comparing(Transaccion::getFecha).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Obtiene transacciones de un usuario filtradas por fecha
     * @param usuarioId ID del usuario
     * @param fechaInicio Fecha de inicio del periodo
     * @param fechaFin Fecha de fin del periodo
     * @return Lista de transacciones filtradas por periodo
     */
    public List<Transaccion> obtenerTransaccionesPorPeriodo(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        // Crear copias locales
        LocalDate inicio = fechaInicio;
        LocalDate fin = fechaFin;

        if (inicio == null) {
            inicio = LocalDate.of(2000, 1, 1);
        }
        if (fin == null) {
            fin = LocalDate.now();
        }

        // Variables finales para usar en lambda
        final LocalDate finalInicio = inicio;
        final LocalDate finalFin = fin;

        return TRANSACCIONES_POR_USUARIO.getOrDefault(usuarioId, new ArrayList<>())
                .stream()
                .filter(t -> !t.getFecha().isBefore(finalInicio) && !t.getFecha().isAfter(finalFin))
                .sorted(Comparator.comparing(Transaccion::getFecha).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Obtiene transacciones de un usuario filtradas por tipo
     * @param usuarioId ID del usuario
     * @param tipo Tipo de transacción ('Ingreso' o 'Gasto')
     * @return Lista de transacciones filtradas por tipo
     */
    public List<Transaccion> obtenerTransaccionesPorTipo(Long usuarioId, String tipo) {
        return TRANSACCIONES_POR_USUARIO.getOrDefault(usuarioId, new ArrayList<>())
                .stream()
                .filter(t -> t.getTipo().equalsIgnoreCase(tipo))
                .sorted(Comparator.comparing(Transaccion::getFecha).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Obtiene transacciones de un usuario filtradas por categoría
     * @param usuarioId ID del usuario
     * @param categoria Categoría de la transacción
     * @return Lista de transacciones filtradas por categoría
     */
    public List<Transaccion> obtenerTransaccionesPorCategoria(Long usuarioId, String categoria) {
        return TRANSACCIONES_POR_USUARIO.getOrDefault(usuarioId, new ArrayList<>())
                .stream()
                .filter(t -> t.getCategoria().equalsIgnoreCase(categoria))
                .sorted(Comparator.comparing(Transaccion::getFecha).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el total de ingresos de un usuario en un periodo determinado
     * @param usuarioId ID del usuario
     * @param fechaInicio Fecha de inicio del periodo (puede ser null para considerar todo)
     * @param fechaFin Fecha de fin del periodo (puede ser null para considerar hasta hoy)
     * @return Suma total de ingresos
     */
    public double obtenerTotalIngresos(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        // Crear copias locales
        LocalDate inicio = fechaInicio;
        LocalDate fin = fechaFin;

        if (inicio == null) {
            inicio = LocalDate.of(2000, 1, 1); // Fecha anterior a cualquier registro
        }
        if (fin == null) {
            fin = LocalDate.now(); // Hoy
        }

        // Variables finales para usar en lambda
        final LocalDate finalInicio = inicio;
        final LocalDate finalFin = fin;

        return TRANSACCIONES_POR_USUARIO.getOrDefault(usuarioId, new ArrayList<>())
                .stream()
                .filter(t -> t.getTipo().equalsIgnoreCase("Ingreso"))
                .filter(t -> !t.getFecha().isBefore(finalInicio) && !t.getFecha().isAfter(finalFin))
                .mapToDouble(Transaccion::getMonto)
                .sum();
    }

    /**
     * Obtiene el total de gastos de un usuario en un periodo determinado
     * @param usuarioId ID del usuario
     * @param fechaInicio Fecha de inicio del periodo (puede ser null para considerar todo)
     * @param fechaFin Fecha de fin del periodo (puede ser null para considerar hasta hoy)
     * @return Suma total de gastos
     */
    public double obtenerTotalGastos(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        // Crear copias locales
        LocalDate inicio = fechaInicio;
        LocalDate fin = fechaFin;

        if (inicio == null) {
            inicio = LocalDate.of(2000, 1, 1); // Fecha anterior a cualquier registro
        }
        if (fin == null) {
            fin = LocalDate.now(); // Hoy
        }

        // Variables finales para usar en lambda
        final LocalDate finalInicio = inicio;
        final LocalDate finalFin = fin;

        return TRANSACCIONES_POR_USUARIO.getOrDefault(usuarioId, new ArrayList<>())
                .stream()
                .filter(t -> t.getTipo().equalsIgnoreCase("Gasto"))
                .filter(t -> !t.getFecha().isBefore(finalInicio) && !t.getFecha().isAfter(finalFin))
                .mapToDouble(Transaccion::getMonto)
                .sum();
    }

    /**
     * Obtiene el balance (ingresos - gastos) de un usuario en un periodo
     * @param usuarioId ID del usuario
     * @param fechaInicio Fecha de inicio del periodo (puede ser null para considerar todo)
     * @param fechaFin Fecha de fin del periodo (puede ser null para considerar hasta hoy)
     * @return Balance del periodo
     */
    public double obtenerBalance(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        double ingresos = obtenerTotalIngresos(usuarioId, fechaInicio, fechaFin);
        double gastos = obtenerTotalGastos(usuarioId, fechaInicio, fechaFin);
        return ingresos - gastos;
    }

    /**
     * Obtiene los gastos por categoría en un periodo determinado
     * @param usuarioId ID del usuario
     * @param fechaInicio Fecha de inicio del periodo
     * @param fechaFin Fecha de fin del periodo
     * @return Mapa con categorías como clave y el total gastado como valor
     */
    public Map<String, Double> obtenerGastosPorCategoria(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        // Crear copias locales
        LocalDate inicio = fechaInicio;
        LocalDate fin = fechaFin;

        if (inicio == null) {
            inicio = LocalDate.of(2000, 1, 1);
        }
        if (fin == null) {
            fin = LocalDate.now();
        }

        // Variables finales para usar en lambda
        final LocalDate finalInicio = inicio;
        final LocalDate finalFin = fin;

        return TRANSACCIONES_POR_USUARIO.getOrDefault(usuarioId, new ArrayList<>())
                .stream()
                .filter(t -> t.getTipo().equalsIgnoreCase("Gasto"))
                .filter(t -> !t.getFecha().isBefore(finalInicio) && !t.getFecha().isAfter(finalFin))
                .collect(Collectors.groupingBy(
                        Transaccion::getCategoria,
                        Collectors.summingDouble(Transaccion::getMonto)
                ));
    }

    /**
     * Elimina una transacción específica
     * @param transaccionId ID de la transacción a eliminar
     * @param usuarioId ID del usuario propietario de la transacción
     * @return true si se eliminó correctamente
     */
    public boolean eliminarTransaccion(Long transaccionId, Long usuarioId) {
        List<Transaccion> transaccionesUsuario = TRANSACCIONES_POR_USUARIO.get(usuarioId);
        if (transaccionesUsuario == null) {
            return false;
        }

        int tamanioAnterior = transaccionesUsuario.size();
        transaccionesUsuario.removeIf(t -> t.getId().equals(transaccionId));

        // Actualizar la lista en el mapa
        TRANSACCIONES_POR_USUARIO.put(usuarioId, transaccionesUsuario);

        return tamanioAnterior > transaccionesUsuario.size();
    }

    /**
     * Actualiza una transacción existente
     * @param transaccion La transacción con datos actualizados
     * @return true si se actualizó correctamente
     */
    public boolean actualizarTransaccion(Transaccion transaccion) {
        if (transaccion.getId() == null || transaccion.getUsuarioId() == null) {
            return false;
        }

        List<Transaccion> transaccionesUsuario = TRANSACCIONES_POR_USUARIO.get(transaccion.getUsuarioId());
        if (transaccionesUsuario == null) {
            return false;
        }

        // Buscar la transacción por ID
        boolean encontrada = false;
        for (int i = 0; i < transaccionesUsuario.size(); i++) {
            if (transaccionesUsuario.get(i).getId().equals(transaccion.getId())) {
                transaccionesUsuario.set(i, transaccion);
                encontrada = true;
                break;
            }
        }

        // Si se encontró, actualizar la lista en el mapa
        if (encontrada) {
            TRANSACCIONES_POR_USUARIO.put(transaccion.getUsuarioId(), transaccionesUsuario);
        }

        return encontrada;
    }

    /**
     * Obtiene una transacción específica por su ID
     * @param transaccionId ID de la transacción
     * @param usuarioId ID del usuario
     * @return La transacción si existe, null en caso contrario
     */
    public Transaccion obtenerTransaccionPorId(Long transaccionId, Long usuarioId) {
        List<Transaccion> transaccionesUsuario = TRANSACCIONES_POR_USUARIO.get(usuarioId);
        if (transaccionesUsuario == null) {
            return null;
        }

        return transaccionesUsuario.stream()
                .filter(t -> t.getId().equals(transaccionId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Obtiene las categorías de gastos disponibles
     * (Esto podría venir de una tabla de categorías en la BD)
     * @return Lista de categorías de gastos
     */
    public List<String> obtenerCategoriasGastos() {
        return Arrays.asList(
                "Alimentación", "Vivienda", "Transporte", "Entretenimiento",
                "Salud", "Educación", "Ropa", "Servicios", "Otros"
        );
    }

    /**
     * Obtiene las categorías de ingresos disponibles
     * (Esto podría venir de una tabla de categorías en la BD)
     * @return Lista de categorías de ingresos
     */
    public List<String> obtenerCategoriasIngresos() {
        return Arrays.asList(
                "Salario", "Inversiones", "Regalos", "Otros"
        );
    }
}