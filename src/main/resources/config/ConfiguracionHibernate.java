package smartsave.configuracion;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import smartsave.modelo.Usuario;
import smartsave.modelo.Transaccion;


public class ConfiguracionHibernate {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuracion = new Configuration().configure();

            configuracion.addAnnotatedClass(Usuario.class);
            configuracion.addAnnotatedClass(Transaccion.class);

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
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}