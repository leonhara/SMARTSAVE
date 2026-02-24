package smartsave.servicio;
import smartsave.modelo.Usuario;

public class SessionManager {

    private static SessionManager instancia;
    private Usuario usuarioActual;

    private SessionManager() {
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
        return null;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    public boolean haySesionActiva() {
        return this.usuarioActual != null;
    }
}