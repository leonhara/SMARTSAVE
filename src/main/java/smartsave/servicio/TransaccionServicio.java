package smartsave.servicio;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import smartsave.config.HibernateConfig;
import smartsave.modelo.Transaccion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestionar operaciones relacionadas con transacciones
 * MIGRADO A HIBERNATE - Reemplaza estructuras en memoria
 */
public class TransaccionServicio {

    /**
     * Agrega una nueva transacción asociada a un usuario
     * @param transaccion La transacción a registrar
     * @param usuarioId ID del usuario al que pertenece la transacción
     * @return La transacción con su ID asignado
     */
    public Transaccion agregarTransaccion(Transaccion transaccion, Long usuarioId) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            transaccion.setUsuarioId(usuarioId);
            session.save(transaccion);

            transaction.commit();
            return transaccion;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error agregando transacción: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todas las transacciones de un usuario
     * @param usuarioId ID del usuario
     * @return Lista de transacciones del usuario
     */
    public List<Transaccion> obtenerTransaccionesPorUsuario(Long usuarioId) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Transaccion> query = session.createQuery(
                    "FROM Transaccion t WHERE t.usuarioId = :usuarioId ORDER BY t.fecha DESC",
                    Transaccion.class);
            query.setParameter("usuarioId", usuarioId);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo transacciones por usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene transacciones de un usuario filtradas por fecha
     * @param usuarioId ID del usuario
     * @param fechaInicio Fecha de inicio del periodo
     * @param fechaFin Fecha de fin del periodo
     * @return Lista de transacciones filtradas por periodo
     */
    public List<Transaccion> obtenerTransaccionesPorPeriodo(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            // Si las fechas son null, usar valores por defecto
            LocalDate inicio = fechaInicio != null ? fechaInicio : LocalDate.of(2000, 1, 1);
            LocalDate fin = fechaFin != null ? fechaFin : LocalDate.now();

            Query<Transaccion> query = session.createQuery(
                    "FROM Transaccion t WHERE t.usuarioId = :usuarioId " +
                            "AND t.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY t.fecha DESC",
                    Transaccion.class);
            query.setParameter("usuarioId", usuarioId);
            query.setParameter("fechaInicio", inicio);
            query.setParameter("fechaFin", fin);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo transacciones por periodo: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene transacciones de un usuario filtradas por tipo
     * @param usuarioId ID del usuario
     * @param tipo Tipo de transacción ('Ingreso' o 'Gasto')
     * @return Lista de transacciones filtradas por tipo
     */
    public List<Transaccion> obtenerTransaccionesPorTipo(Long usuarioId, String tipo) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Transaccion> query = session.createQuery(
                    "FROM Transaccion t WHERE t.usuarioId = :usuarioId AND t.tipo = :tipo ORDER BY t.fecha DESC",
                    Transaccion.class);
            query.setParameter("usuarioId", usuarioId);
            // LÍNEA CORREGIDA: Convertir el String al tipo Enum esperado por Hibernate
            query.setParameter("tipo", Transaccion.TipoTransaccion.valueOf(tipo));
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo transacciones por tipo: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene transacciones de un usuario filtradas por categoría
     * @param usuarioId ID del usuario
     * @param categoria Categoría de la transacción
     * @return Lista de transacciones filtradas por categoría
     */
    public List<Transaccion> obtenerTransaccionesPorCategoria(Long usuarioId, String categoria) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Transaccion> query = session.createQuery(
                    "FROM Transaccion t WHERE t.usuarioId = :usuarioId AND t.categoria = :categoria ORDER BY t.fecha DESC",
                    Transaccion.class);
            query.setParameter("usuarioId", usuarioId);
            query.setParameter("categoria", categoria);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo transacciones por categoría: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el total de ingresos de un usuario en un periodo determinado
     * @param usuarioId ID del usuario
     * @param fechaInicio Fecha de inicio del periodo (puede ser null para considerar todo)
     * @param fechaFin Fecha de fin del periodo (puede ser null para considerar hasta hoy)
     * @return Suma total de ingresos
     */
    public double obtenerTotalIngresos(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            LocalDate inicio = fechaInicio != null ? fechaInicio : LocalDate.of(2000, 1, 1);
            LocalDate fin = fechaFin != null ? fechaFin : LocalDate.now();

            // CAMBIO: Usar BigDecimal en lugar de Double
            Query<BigDecimal> query = session.createQuery(
                    "SELECT COALESCE(SUM(t.monto), 0.0) FROM Transaccion t " +
                            "WHERE t.usuarioId = :usuarioId AND t.tipo = 'Ingreso' " +
                            "AND t.fecha BETWEEN :fechaInicio AND :fechaFin",
                    BigDecimal.class); // <-- CAMBIO AQUÍ
            query.setParameter("usuarioId", usuarioId);
            query.setParameter("fechaInicio", inicio);
            query.setParameter("fechaFin", fin);

            // CAMBIO: Convertir BigDecimal a double
            BigDecimal resultado = query.uniqueResult();
            return resultado != null ? resultado.doubleValue() : 0.0;
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo total de ingresos: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el total de gastos de un usuario en un periodo determinado
     * @param usuarioId ID del usuario
     * @param fechaInicio Fecha de inicio del periodo (puede ser null para considerar todo)
     * @param fechaFin Fecha de fin del periodo (puede ser null para considerar hasta hoy)
     * @return Suma total de gastos
     */
    public double obtenerTotalGastos(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            LocalDate inicio = fechaInicio != null ? fechaInicio : LocalDate.of(2000, 1, 1);
            LocalDate fin = fechaFin != null ? fechaFin : LocalDate.now();

            // CAMBIO: Usar BigDecimal en lugar de Double
            Query<BigDecimal> query = session.createQuery(
                    "SELECT COALESCE(SUM(t.monto), 0.0) FROM Transaccion t " +
                            "WHERE t.usuarioId = :usuarioId AND t.tipo = 'Gasto' " +
                            "AND t.fecha BETWEEN :fechaInicio AND :fechaFin",
                    BigDecimal.class); // <-- CAMBIO AQUÍ
            query.setParameter("usuarioId", usuarioId);
            query.setParameter("fechaInicio", inicio);
            query.setParameter("fechaFin", fin);

            // CAMBIO: Convertir BigDecimal a double
            BigDecimal resultado = query.uniqueResult();
            return resultado != null ? resultado.doubleValue() : 0.0;
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo total de gastos: " + e.getMessage(), e);
        }
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
     * @param fechaFin Fecha de fin del periode
     * @return Mapa con categorías como clave y el total gastado como valor
     */
    public Map<String, Double> obtenerGastosPorCategoria(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            // Si las fechas son null, usar valores por defecto
            LocalDate inicio = fechaInicio != null ? fechaInicio : LocalDate.of(2000, 1, 1);
            LocalDate fin = fechaFin != null ? fechaFin : LocalDate.now();

            Query<Object[]> query = session.createQuery(
                    "SELECT t.categoria, SUM(t.monto) FROM Transaccion t " +
                            "WHERE t.usuarioId = :usuarioId AND t.tipo = 'Gasto' " +
                            "AND t.fecha BETWEEN :fechaInicio AND :fechaFin " +
                            "GROUP BY t.categoria",
                    Object[].class);
            query.setParameter("usuarioId", usuarioId);
            query.setParameter("fechaInicio", inicio);
            query.setParameter("fechaFin", fin);

            List<Object[]> resultados = query.getResultList();

            Map<String, Double> gastosPorCategoria = new HashMap<>();
            for (Object[] resultado : resultados) {
                String categoria = (String) resultado[0];
                // CAMBIO: Convertir BigDecimal a Double
                BigDecimal totalBigDecimal = (BigDecimal) resultado[1];
                Double total = totalBigDecimal.doubleValue();
                gastosPorCategoria.put(categoria, total);
            }

            return gastosPorCategoria;
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo gastos por categoría: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina una transacción específica
     * @param transaccionId ID de la transacción a eliminar
     * @param usuarioId ID del usuario propietario de la transacción
     * @return true si se eliminó correctamente
     */
    public boolean eliminarTransaccion(Long transaccionId, Long usuarioId) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Verificar que la transacción pertenece al usuario
            Transaccion transaccion = session.get(Transaccion.class, transaccionId);
            if (transaccion != null && transaccion.getUsuarioId().equals(usuarioId)) {
                session.delete(transaccion);
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
            throw new RuntimeException("Error eliminando transacción: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza una transacción existente
     * @param transaccion La transacción con datos actualizados
     * @return true si se actualizó correctamente
     */
    public boolean actualizarTransaccion(Transaccion transaccion) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.update(transaccion);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error actualizando transacción: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene una transacción específica por su ID
     * @param transaccionId ID de la transacción
     * @param usuarioId ID del usuario
     * @return La transacción si existe, null en caso contrario
     */
    public Transaccion obtenerTransaccionPorId(Long transaccionId, Long usuarioId) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Transaccion> query = session.createQuery(
                    "FROM Transaccion t WHERE t.id = :transaccionId AND t.usuarioId = :usuarioId",
                    Transaccion.class);
            query.setParameter("transaccionId", transaccionId);
            query.setParameter("usuarioId", usuarioId);

            List<Transaccion> resultados = query.getResultList();
            return resultados.isEmpty() ? null : resultados.get(0);
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo transacción por ID: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene las categorías de gastos disponibles
     * @return Lista de categorías de gastos
     */
    public List<String> obtenerCategoriasGastos() {
        // Mantenemos la lista estática por ahora
        return List.of(
                "Alimentación", "Vivienda", "Transporte", "Entretenimiento",
                "Salud", "Educación", "Ropa", "Servicios", "Otros"
        );
    }

    /**
     * Obtiene las categorías de ingresos disponibles
     * @return Lista de categorías de ingresos
     */
    public List<String> obtenerCategoriasIngresos() {
        // Mantenemos la lista estática por ahora
        return List.of(
                "Salario", "Inversiones", "Regalos", "Otros"
        );
    }
}