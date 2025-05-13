package smartsave.configuracion;

public class ConfiguracionMercadona {
    private static String codigoPostal = "14010"; //Salesianos bonito

    public static void setCodigoPostal(String codigo) {
        codigoPostal = codigo;
    }

    public static String getCodigoPostal() {
        return codigoPostal;
    }
    // En ConfiguracionMercadona.java
    public static void cambiarCodigoPostal(String nuevoCodigoPostal) {
        codigoPostal = nuevoCodigoPostal;
        // Notificar a ProductoServicio del cambio
        ProductoServicio.getInstance().cambiarCodigoPostal(nuevoCodigoPostal);
    }
}