package smartsave.configuracion;

// Opción 1: Cambiar ConfiguracionMercadona.java
public class ConfiguracionMercadona {
    private static String codigoPostal = "14010"; //Salesianos bonito
    private static smartsave.servicio.ProductoServicio productoServicio;

    public static void setCodigoPostal(String codigo) {
        codigoPostal = codigo;
    }

    public static String getCodigoPostal() {
        return codigoPostal;
    }

    // Setter para inyectar ProductoServicio
    public static void setProductoServicio(smartsave.servicio.ProductoServicio servicio) {
        productoServicio = servicio;
    }

    // Método modificado sin usar getInstance
    public static void cambiarCodigoPostal(String nuevoCodigoPostal) {
        codigoPostal = nuevoCodigoPostal;
        // Solo notificar si existe una instancia
        if (productoServicio != null) {
            productoServicio.cambiarCodigoPostal(nuevoCodigoPostal);
        }
    }
}