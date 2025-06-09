package smartsave.servicio;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import smartsave.config.HibernateConfig;
import smartsave.modelo.Usuario;

import java.util.List;

public class UsuarioServicio {

    public boolean registrarUsuario(Usuario usuario) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            if (existsByEmail(usuario.getEmail())) {
                transaction.rollback();
                return false;
            }

            session.save(usuario);
            transaction.commit();
            return true;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error registrando usuario: " + e.getMessage(), e);
        }
    }

    public Usuario verificarCredenciales(String email, String contrasena) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Usuario> query = session.createQuery(
                    "FROM Usuario u WHERE u.email = :email AND u.contrasenaHash = :contrasena",
                    Usuario.class);
            query.setParameter("email", email);
            query.setParameter("contrasena", contrasena);

            List<Usuario> usuarios = query.getResultList();

            if (!usuarios.isEmpty()) {
                Usuario usuario = usuarios.get(0);
                usuario.actualizarUltimoLogin();
                actualizarUsuario(usuario);
                return usuario;
            }

            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error verificando credenciales: " + e.getMessage(), e);
        }
    }


    public Usuario obtenerUsuarioPorEmail(String email) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Usuario> query = session.createQuery(
                    "FROM Usuario u WHERE u.email = :email", Usuario.class);
            query.setParameter("email", email);

            List<Usuario> usuarios = query.getResultList();
            return usuarios.isEmpty() ? null : usuarios.get(0);
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo usuario por email: " + e.getMessage(), e);
        }
    }

    public Usuario obtenerUsuarioPorId(Long id) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.get(Usuario.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo usuario por ID: " + e.getMessage(), e);
        }
    }

    public boolean actualizarUsuario(Usuario usuario) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.update(usuario);
            transaction.commit();
            return true;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error actualizando usuario: " + e.getMessage(), e);
        }
    }

    public boolean eliminarUsuario(String email) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Usuario usuario = obtenerUsuarioPorEmail(email);
            if (usuario != null) {
                session.delete(usuario);
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
            throw new RuntimeException("Error eliminando usuario: " + e.getMessage(), e);
        }
    }

    public int obtenerCantidadUsuarios() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class);
            return query.uniqueResult().intValue();
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo cantidad de usuarios: " + e.getMessage(), e);
        }
    }

    public boolean actualizarModalidadAhorro(Long usuarioId, String modalidad) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);

        if (usuario != null) {
            usuario.setModalidadAhorroSeleccionada(modalidad);
            return actualizarUsuario(usuario);
        }

        return false;
    }

    public String obtenerModalidadAhorroUsuario(Long usuarioId) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);

        if (usuario != null) {
            return usuario.getModalidadAhorroSeleccionada();
        }

        return null;
    }

    public double obtenerFactorPresupuestoUsuario(Long usuarioId) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);

        if (usuario != null && usuario.tieneModalidadAhorro()) {
            return usuario.getFactorPresupuesto();
        }

        return 0.85;
    }

    public List<Usuario> obtenerTodosUsuarios() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Usuario> query = session.createQuery("FROM Usuario u ORDER BY u.fechaRegistro DESC", Usuario.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo todos los usuarios: " + e.getMessage(), e);
        }
    }

    private boolean existsByEmail(String email) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(u) FROM Usuario u WHERE u.email = :email", Long.class);
            query.setParameter("email", email);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Error verificando existencia de email: " + e.getMessage(), e);
        }
    }
}