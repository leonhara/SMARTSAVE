package smartsave.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import smartsave.modelo.*;

/**
 * Configuración de Hibernate para SmartSave
 * Clase singleton para gestionar la SessionFactory
 *
 * UBICACIÓN: src/main/java/smartsave/config/HibernateConfig.java
 */
public class HibernateConfig {
    private static SessionFactory sessionFactory;
    private static final Object lock = new Object();

    /**
     * Obtiene la instancia única de SessionFactory
     * @return SessionFactory configurada
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            synchronized (lock) {
                if (sessionFactory == null) {
                    createSessionFactory();
                }
            }
        }
        return sessionFactory;
    }

    /**
     * Crea la SessionFactory usando configuración programática
     */
    private static void createSessionFactory() {
        try {
            Configuration configuration = new Configuration();

            configuration.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
            configuration.setProperty("hibernate.connection.url",
                    "jdbc:mysql://127.0.0.1:3306/smartsave?useTimezone=true&serverTimezone=Europe/Madrid&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8");
            configuration.setProperty("hibernate.connection.username", "root");
            configuration.setProperty("hibernate.connection.password", "laco");
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
            configuration.setProperty("hibernate.connection.provider_class", "com.zaxxer.hikari.hibernate.HikariConnectionProvider");

            configuration.setProperty("hibernate.hbm2ddl.auto", "validate");

            configuration.setProperty("hibernate.show_sql", "true");
            configuration.setProperty("hibernate.format_sql", "true");
            configuration.setProperty("hibernate.use_sql_comments", "true");

            configuration.setProperty("hibernate.physical_naming_strategy",
                    "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");

            configuration.setProperty("hibernate.jdbc.batch_size", "25");
            configuration.setProperty("hibernate.jdbc.fetch_size", "50");

            //Aqui estan la configuración de pool de conexiones HikariCP
            configuration.setProperty("hibernate.hikari.minimumIdle", "5");
            configuration.setProperty("hibernate.hikari.maximumPoolSize", "20");
            configuration.setProperty("hibernate.hikari.connectionTimeout", "30000");
            configuration.setProperty("hibernate.hikari.idleTimeout", "600000");
            configuration.setProperty("hibernate.hikari.maxLifetime", "1800000");

            configuration.addAnnotatedClass(Usuario.class);
            configuration.addAnnotatedClass(ModalidadAhorro.class);
            configuration.addAnnotatedClass(Producto.class);
            configuration.addAnnotatedClass(Transaccion.class);
            configuration.addAnnotatedClass(PerfilNutricional.class);
            configuration.addAnnotatedClass(RestriccionNutricional.class);
            configuration.addAnnotatedClass(ListaCompra.class);
            configuration.addAnnotatedClass(ItemCompra.class);

            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

            sessionFactory = configuration.buildSessionFactory(serviceRegistry);

            System.out.println("SessionFactory creada exitosamente");

        } catch (Exception e) {
            System.err.println("Error creando SessionFactory: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating SessionFactory", e);
        }
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            try {
                sessionFactory.close();
                System.out.println("SessionFactory cerrada correctamente");
            } catch (Exception e) {
                System.err.println("Error cerrando SessionFactory: " + e.getMessage());
            }
        }
    }

    public static boolean isActive() {
        return sessionFactory != null && !sessionFactory.isClosed();
    }

    public static void restart() {
        shutdown();
        sessionFactory = null;
        getSessionFactory();
    }
}