package smartsave.servicio; // O el paquete que prefieras para esto

import smartsave.modelo.Usuario;

public class SessionManager {

    private static SessionManager instancia;
    private Usuario usuarioActual;

    private SessionManager() {
        // Constructor privado para el patrón Singleton
    }

    public static synchronized SessionManager getInstancia() {
        if (instancia == null) {
            instancia = new SessionManager();
        }
        return instancia;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void setUsuarioActual(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    public Long getUsuarioIdActual() {
        if (usuarioActual != null) {
            return usuarioActual.getId();
        }
        return null; // O lanzar una excepción si prefieres que siempre haya un ID
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    public boolean haySesionActiva() {
        return this.usuarioActual != null;
    }
}