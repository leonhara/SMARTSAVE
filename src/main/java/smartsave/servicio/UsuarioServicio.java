package smartsave.servicio;

import smartsave.modelo.Usuario;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para gestionar operaciones relacionadas con usuarios
 * (Por ahora utiliza un mapa en memoria, pero luego se conectará a la base de datos)
 */
public class UsuarioServicio {

    // Simulación de base de datos (solo para demostración)
    private static final Map<String, Usuario> USUARIOS = new HashMap<>();

    /**
     * Registra un nuevo usuario en el sistema
     * @param usuario El usuario a registrar
     * @return true si el registro fue exitoso, false si el email ya está en uso
     */
    public boolean registrarUsuario(Usuario usuario) {
        // Verificar si el email ya está registrado
        if (USUARIOS.containsKey(usuario.getEmail())) {
            return false;
        }

        // En un caso real, aquí se haría hash de la contraseña
        // String contraseñaHash = hashPassword(usuario.getContraseñaHash());
        // usuario.setContraseñaHash(contraseñaHash);

        // Guardar usuario en el mapa
        USUARIOS.put(usuario.getEmail(), usuario);
        return true;
    }

    /**
     * Verifica las credenciales de un usuario
     * @param email Email del usuario
     * @param contraseña Contraseña del usuario
     * @return El usuario si las credenciales son válidas, null en caso contrario
     */
    public Usuario verificarCredenciales(String email, String contrasena) {
        Usuario usuario = USUARIOS.get(email);

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
        return USUARIOS.get(email);
    }

    /**
     * Actualiza la información de un usuario existente
     * @param usuario El usuario con la información actualizada
     * @return true si la actualización fue exitosa
     */
    public boolean actualizarUsuario(Usuario usuario) {
        if (!USUARIOS.containsKey(usuario.getEmail())) {
            return false;
        }

        USUARIOS.put(usuario.getEmail(), usuario);
        return true;
    }

    /**
     * Elimina un usuario del sistema
     * @param email Email del usuario a eliminar
     * @return true si la eliminación fue exitosa
     */
    public boolean eliminarUsuario(String email) {
        return USUARIOS.remove(email) != null;
    }

    /**
     * Método auxiliar para obtener la cantidad de usuarios registrados
     * @return Número de usuarios registrados
     */
    public int obtenerCantidadUsuarios() {
        return USUARIOS.size();
    }
}