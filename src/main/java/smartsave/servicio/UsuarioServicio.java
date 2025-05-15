package smartsave.servicio;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import smartsave.config.HibernateConfig;
import smartsave.modelo.Usuario;

import java.util.List;

/**
 * Servicio para gestionar operaciones relacionadas con usuarios
 * MIGRADO A HIBERNATE - Reemplaza el Map en memoria
 */
public class UsuarioServicio {

    /**
     * Registra un nuevo usuario en el sistema
     * @param usuario El usuario a registrar
     * @return true si el registro fue exitoso, false si el email ya está en uso
     */
    public boolean registrarUsuario(Usuario usuario) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Verificar si el email ya existe
            if (existsByEmail(usuario.getEmail())) {
                transaction.rollback();
                return false;
            }

            // Guardar usuario
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

    /**
     * Verifica las credenciales de un usuario
     * @param email Email del usuario
     * @param contrasena Contraseña del usuario
     * @return El usuario si las credenciales son válidas, null en caso contrario
     */
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
                // Actualizar fecha de último login
                usuario.actualizarUltimoLogin();
                actualizarUsuario(usuario);
                return usuario;
            }

            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error verificando credenciales: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene un usuario por su email
     * @param email Email del usuario
     * @return El usuario si existe, null en caso contrario
     */
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

    /**
     * Obtiene un usuario por su ID
     * @param id ID del usuario
     * @return El usuario si existe, null en caso contrario
     */
    public Usuario obtenerUsuarioPorId(Long id) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.get(Usuario.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo usuario por ID: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza la información de un usuario existente
     * @param usuario El usuario con la información actualizada
     * @return true si la actualización fue exitosa
     */
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

    /**
     * Elimina un usuario del sistema
     * @param email Email del usuario a eliminar
     * @return true si la eliminación fue exitosa
     */
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

    /**
     * Método auxiliar para obtener la cantidad de usuarios registrados
     * @return Número de usuarios registrados
     */
    public int obtenerCantidadUsuarios() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class);
            return query.uniqueResult().intValue();
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo cantidad de usuarios: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza la modalidad de ahorro seleccionada por el usuario
     * @param usuarioId ID del usuario
     * @param modalidad Nombre de la modalidad de ahorro ("Máximo", "Equilibrado", "Estándar")
     * @return true si la actualización fue exitosa
     */
    public boolean actualizarModalidadAhorro(Long usuarioId, String modalidad) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);

        if (usuario != null) {
            usuario.setModalidadAhorroSeleccionada(modalidad);
            return actualizarUsuario(usuario);
        }

        return false;
    }

    /**
     * Obtiene la modalidad de ahorro seleccionada por el usuario
     * @param usuarioId ID del usuario
     * @return Nombre de la modalidad o null si el usuario no existe o no ha seleccionado ninguna
     */
    public String obtenerModalidadAhorroUsuario(Long usuarioId) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);

        if (usuario != null) {
            return usuario.getModalidadAhorroSeleccionada();
        }

        return null;
    }

    /**
     * Obtiene el factor de presupuesto según la modalidad del usuario
     * @param usuarioId ID del usuario
     * @return Factor de presupuesto (0.7 para Máximo, 0.85 para Equilibrado, 1.0 para Estándar)
     */
    public double obtenerFactorPresupuestoUsuario(Long usuarioId) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);

        if (usuario != null && usuario.tieneModalidadAhorro()) {
            return usuario.getFactorPresupuesto();
        }

        return 0.85; // Valor por defecto (Equilibrado)
    }

    /**
     * Obtiene todos los usuarios registrados
     * @return Lista de usuarios
     */
    public List<Usuario> obtenerTodosUsuarios() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Usuario> query = session.createQuery("FROM Usuario u ORDER BY u.fechaRegistro DESC", Usuario.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo todos los usuarios: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si existe un usuario con el email especificado
     * @param email Email a verificar
     * @return true si existe, false en caso contrario
     */
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