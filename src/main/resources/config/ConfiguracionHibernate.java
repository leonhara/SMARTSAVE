package smartsave.configuracion;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import smartsave.modelo.Usuario;
import smartsave.modelo.Transaccion;

/**
 * Clase de configuración para Hibernate
 * Implementa el patrón Singleton para mantener una única instancia de SessionFactory
 */
public class ConfiguracionHibernate {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Crear el SessionFactory desde hibernate.cfg.xml
            Configuration configuracion = new Configuration().configure();

            // Añadir clases de entidad
            configuracion.addAnnotatedClass(Usuario.class);
            configuracion.addAnnotatedClass(Transaccion.class);
            // Agregar más entidades aquí según sea necesario

            return configuracion.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Error al crear SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void cerrar() {
        // Cerrar caché y conexiones de pool de conexiones
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}