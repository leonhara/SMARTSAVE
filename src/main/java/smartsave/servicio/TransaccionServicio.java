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

public class TransaccionServicio {

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

    public List<Transaccion> obtenerTransaccionesPorPeriodo(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
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

    public List<Transaccion> obtenerTransaccionesPorTipo(Long usuarioId, String tipo) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Transaccion> query = session.createQuery(
                    "FROM Transaccion t WHERE t.usuarioId = :usuarioId AND t.tipo = :tipo ORDER BY t.fecha DESC",
                    Transaccion.class);
            query.setParameter("usuarioId", usuarioId);
            query.setParameter("tipo", Transaccion.TipoTransaccion.valueOf(tipo));
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo transacciones por tipo: " + e.getMessage(), e);
        }
    }

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

    public double obtenerTotalIngresos(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            LocalDate inicio = fechaInicio != null ? fechaInicio : LocalDate.of(2000, 1, 1);
            LocalDate fin = fechaFin != null ? fechaFin : LocalDate.now();

            Query<BigDecimal> query = session.createQuery(
                    "SELECT COALESCE(SUM(t.monto), 0.0) FROM Transaccion t " +
                            "WHERE t.usuarioId = :usuarioId AND t.tipo = 'Ingreso' " +
                            "AND t.fecha BETWEEN :fechaInicio AND :fechaFin",
                    BigDecimal.class);
            query.setParameter("usuarioId", usuarioId);
            query.setParameter("fechaInicio", inicio);
            query.setParameter("fechaFin", fin);

            BigDecimal resultado = query.uniqueResult();
            return resultado != null ? resultado.doubleValue() : 0.0;
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo total de ingresos: " + e.getMessage(), e);
        }
    }

    public double obtenerTotalGastos(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            LocalDate inicio = fechaInicio != null ? fechaInicio : LocalDate.of(2000, 1, 1);
            LocalDate fin = fechaFin != null ? fechaFin : LocalDate.now();

            Query<BigDecimal> query = session.createQuery(
                    "SELECT COALESCE(SUM(t.monto), 0.0) FROM Transaccion t " +
                            "WHERE t.usuarioId = :usuarioId AND t.tipo = 'Gasto' " +
                            "AND t.fecha BETWEEN :fechaInicio AND :fechaFin",
                    BigDecimal.class);
            query.setParameter("usuarioId", usuarioId);
            query.setParameter("fechaInicio", inicio);
            query.setParameter("fechaFin", fin);

            BigDecimal resultado = query.uniqueResult();
            return resultado != null ? resultado.doubleValue() : 0.0;
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo total de gastos: " + e.getMessage(), e);
        }
    }

    public double obtenerBalance(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        double ingresos = obtenerTotalIngresos(usuarioId, fechaInicio, fechaFin);
        double gastos = obtenerTotalGastos(usuarioId, fechaInicio, fechaFin);
        return ingresos - gastos;
    }


    public Map<String, Double> obtenerGastosPorCategoria(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
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
                BigDecimal totalBigDecimal = (BigDecimal) resultado[1];
                Double total = totalBigDecimal.doubleValue();
                gastosPorCategoria.put(categoria, total);
            }

            return gastosPorCategoria;
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo gastos por categoría: " + e.getMessage(), e);
        }
    }

    public boolean eliminarTransaccion(Long transaccionId, Long usuarioId) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

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


    public List<String> obtenerCategoriasGastos() {
        return List.of(
                "Alimentación", "Vivienda", "Transporte", "Entretenimiento",
                "Salud", "Educación", "Ropa", "Servicios", "Otros"
        );
    }

    public List<String> obtenerCategoriasIngresos() {
        return List.of(
                "Salario", "Inversiones", "Regalos", "Otros"
        );
    }
}