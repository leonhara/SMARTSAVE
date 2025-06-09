package smartsave.servicio;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import smartsave.config.HibernateConfig;
import smartsave.modelo.ModalidadAhorro;

import java.util.ArrayList;
import java.util.List;

public class ModalidadAhorroServicio {

    public List<ModalidadAhorro> obtenerTodasModalidades() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<ModalidadAhorro> query = session.createQuery(
                    "FROM ModalidadAhorro m ORDER BY m.id", ModalidadAhorro.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo modalidades de ahorro: " + e.getMessage(), e);
        }
    }

    public ModalidadAhorro obtenerModalidadPorId(Long id) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.get(ModalidadAhorro.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo modalidad por ID: " + e.getMessage(), e);
        }
    }

    public ModalidadAhorro obtenerModalidadPorNombre(String nombre) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<ModalidadAhorro> query = session.createQuery(
                    "FROM ModalidadAhorro m WHERE m.nombre = :nombre", ModalidadAhorro.class);
            query.setParameter("nombre", nombre);

            List<ModalidadAhorro> resultados = query.getResultList();
            return resultados.isEmpty() ? null : resultados.get(0);
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo modalidad por nombre: " + e.getMessage(), e);
        }
    }

    public ModalidadAhorro guardarModalidad(ModalidadAhorro modalidad) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(modalidad);
            transaction.commit();
            return modalidad;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error guardando modalidad: " + e.getMessage(), e);
        }
    }

    public ModalidadAhorro actualizarModalidad(ModalidadAhorro modalidad) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(modalidad);
            transaction.commit();
            return modalidad;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error actualizando modalidad: " + e.getMessage(), e);
        }
    }

    public boolean eliminarModalidad(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            ModalidadAhorro modalidad = session.get(ModalidadAhorro.class, id);
            if (modalidad != null) {
                session.delete(modalidad);
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
            throw new RuntimeException("Error eliminando modalidad: " + e.getMessage(), e);
        }
    }

    public double calcularPresupuestoAjustado(double presupuestoOriginal, ModalidadAhorro modalidad) {
        if (modalidad == null) {
            return presupuestoOriginal;
        }
        return presupuestoOriginal * modalidad.getFactorPresupuestoAsDouble();
    }

    public List<String> obtenerConsejosAhorro(ModalidadAhorro modalidad) {
        List<String> consejos = new ArrayList<>();

        if (modalidad == null) {
            consejos.add("Planifica tus compras con antelación");
            consejos.add("Compara precios entre supermercados");
            consejos.add("Evita el desperdicio de alimentos");
            return consejos;
        }

        switch (modalidad.getNombre()) {
            case "Máximo":
                consejos.add("Compra productos de marca blanca o genéricos");
                consejos.add("Aprovecha ofertas y descuentos");
                consejos.add("Planifica las compras con antelación para evitar compras impulsivas");
                consejos.add("Compra alimentos de temporada");
                consejos.add("Considera comprar a granel para productos no perecederos");
                break;
            case "Equilibrado":
                consejos.add("Balancea calidad y precio en tus compras");
                consejos.add("Prioriza proteínas de buena calidad");
                consejos.add("Compra frutas y verduras de temporada");
                consejos.add("Busca ofertas en productos de calidad");
                consejos.add("Cocina en casa en lugar de comer fuera");
                break;
            case "Estándar":
                consejos.add("Invierte en alimentos de alta calidad nutricional");
                consejos.add("Prioriza alimentos frescos y orgánicos cuando sea posible");
                consejos.add("Compra productos locales para mejor frescura");
                consejos.add("Considera servicios de suscripción de cajas de productos frescos");
                consejos.add("Compara la relación calidad-precio entre marcas");
                break;
            default:
                consejos.add("Planifica tus compras con antelación");
                consejos.add("Compara precios entre supermercados");
                consejos.add("Evita el desperdicio de alimentos");
        }

        return consejos;
    }

    public void inicializarModalidadesPredefinidas() {
        try {
            List<ModalidadAhorro> modalidades = obtenerTodasModalidades();

            if (modalidades.isEmpty()) {
                ModalidadAhorro maximoAhorro = new ModalidadAhorro(
                        "Máximo",
                        "Prioriza el ahorro por encima de todo. Busca las opciones más económicas aunque sacrifique algo de calidad nutricional.",
                        0.7,
                        9,
                        4
                );
                guardarModalidad(maximoAhorro);

                ModalidadAhorro equilibrado = new ModalidadAhorro(
                        "Equilibrado",
                        "Busca un balance entre ahorro y nutrición. Recomendado para la mayoría de usuarios.",
                        0.85,
                        6,
                        7
                );
                guardarModalidad(equilibrado);

                ModalidadAhorro estandar = new ModalidadAhorro(
                        "Estándar",
                        "Enfocado en la calidad nutricional manteniendo un presupuesto razonable.",
                        1.0,
                        3,
                        9
                );
                guardarModalidad(estandar);

                System.out.println("Modalidades de ahorro predefinidas creadas exitosamente.");
            }
        } catch (Exception e) {
            System.err.println("Error inicializando modalidades predefinidas: " + e.getMessage());
        }
    }

    public java.util.Map<String, Long> obtenerEstadisticasUso() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Object[]> query = session.createQuery(
                    "SELECT u.modalidadAhorroSeleccionada, COUNT(u) " +
                            "FROM Usuario u " +
                            "WHERE u.modalidadAhorroSeleccionada IS NOT NULL " +
                            "GROUP BY u.modalidadAhorroSeleccionada",
                    Object[].class);

            List<Object[]> resultados = query.getResultList();
            java.util.Map<String, Long> estadisticas = new java.util.HashMap<>();

            for (Object[] resultado : resultados) {
                String modalidad = (String) resultado[0];
                Long count = (Long) resultado[1];
                estadisticas.put(modalidad, count);
            }

            return estadisticas;
        } catch (Exception e) {
            return new java.util.HashMap<>();
        }
    }
}