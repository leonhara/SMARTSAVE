package smartsave.servicio;

import smartsave.modelo.Usuario;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestionar operaciones relacionadas con usuarios
 * (Por ahora utiliza un mapa en memoria, pero luego se conectará a la base de datos)
 */
public class UsuarioServicio {

    // Simulación de base de datos (solo para demostración)
    private static final Map<String, Usuario> USUARIOS_POR_EMAIL = new HashMap<>();
    private static final Map<Long, Usuario> USUARIOS_POR_ID = new HashMap<>();
    private static Long ultimoId = 0L;

    /**
     * Registra un nuevo usuario en el sistema
     * @param usuario El usuario a registrar
     * @return true si el registro fue exitoso, false si el email ya está en uso
     */
    public boolean registrarUsuario(Usuario usuario) {
        // Verificar si el email ya está registrado
        if (USUARIOS_POR_EMAIL.containsKey(usuario.getEmail())) {
            return false;
        }

        // En un caso real, aquí se haría hash de la contraseña
        // String contraseñaHash = hashPassword(usuario.getContraseñaHash());
        // usuario.setContraseñaHash(contraseñaHash);

        // Asignar ID
        usuario.setId(++ultimoId);

        // Guardar usuario en los mapas
        USUARIOS_POR_EMAIL.put(usuario.getEmail(), usuario);
        USUARIOS_POR_ID.put(usuario.getId(), usuario);

        return true;
    }

    /**
     * Verifica las credenciales de un usuario
     * @param email Email del usuario
     * @param contrasena Contraseña del usuario
     * @return El usuario si las credenciales son válidas, null en caso contrario
     */
    public Usuario verificarCredenciales(String email, String contrasena) {
        Usuario usuario = USUARIOS_POR_EMAIL.get(email);

        if (usuario != null && usuario.getContrasenaHash().equals(contrasena)) {
            // Actualizar fecha de último login
            usuario.actualizarUltimoLogin();
            return usuario;
        }

        return null;
    }

    /**
     * Obtiene un usuario por su email
     * @param email Email del usuario
     * @return El usuario si existe, null en caso contrario
     */
    public Usuario obtenerUsuarioPorEmail(String email) {
        return USUARIOS_POR_EMAIL.get(email);
    }

    /**
     * Obtiene un usuario por su ID
     * @param id ID del usuario
     * @return El usuario si existe, null en caso contrario
     */
    public Usuario obtenerUsuarioPorId(Long id) {
        return USUARIOS_POR_ID.get(id);
    }

    /**
     * Actualiza la información de un usuario existente
     * @param usuario El usuario con la información actualizada
     * @return true si la actualización fue exitosa
     */
    public boolean actualizarUsuario(Usuario usuario) {
        if (!USUARIOS_POR_ID.containsKey(usuario.getId())) {
            return false;
        }

        // Actualizar referencias en ambos mapas
        String emailAnterior = USUARIOS_POR_ID.get(usuario.getId()).getEmail();
        if (!emailAnterior.equals(usuario.getEmail())) {
            // Si cambió el email, hay que actualizar las claves del mapa
            USUARIOS_POR_EMAIL.remove(emailAnterior);
            USUARIOS_POR_EMAIL.put(usuario.getEmail(), usuario);
        } else {
            // Si no cambió el email, solo actualizamos el valor
            USUARIOS_POR_EMAIL.put(usuario.getEmail(), usuario);
        }

        // Actualizar en el mapa por ID
        USUARIOS_POR_ID.put(usuario.getId(), usuario);

        return true;
    }

    /**
     * Elimina un usuario del sistema
     * @param email Email del usuario a eliminar
     * @return true si la eliminación fue exitosa
     */
    public boolean eliminarUsuario(String email) {
        Usuario usuario = USUARIOS_POR_EMAIL.get(email);
        if (usuario != null) {
            USUARIOS_POR_ID.remove(usuario.getId());
            USUARIOS_POR_EMAIL.remove(email);
            return true;
        }
        return false;
    }

    /**
     * Método auxiliar para obtener la cantidad de usuarios registrados
     * @return Número de usuarios registrados
     */
    public int obtenerCantidadUsuarios() {
        return USUARIOS_POR_EMAIL.size();
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
            return true;
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
        return new ArrayList<>(USUARIOS_POR_ID.values());
    }
}