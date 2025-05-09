package smartsave.utilidad;

import java.util.regex.Pattern;

/**
 * Clase de utilidad para validación de datos de entrada
 */
public class ValidacionUtil {

    // Patrón de expresión regular para validar emails
    private static final Pattern PATRON_EMAIL = Pattern.compile(
            "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    /**
     * Valida si un email tiene formato correcto
     * @param email El email a validar
     * @return true si el email es válido, false en caso contrario
     */
    public static boolean esEmailValido(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return PATRON_EMAIL.matcher(email).matches();
    }

    /**
     * Valida si una contraseña cumple con los requisitos mínimos
     * @param contraseña La contraseña a validar
     * @return true si la contraseña cumple los requisitos
     */
    public static boolean esContraseñaValida(String contraseña) {
        // Requisitos: al menos 6 caracteres
        return contraseña != null && contraseña.length() >= 6;
    }

    /**
     * Valida si una cadena de texto contiene solo letras
     * @param texto El texto a validar
     * @return true si contiene solo letras
     */
    public static boolean soloLetras(String texto) {
        if (texto == null || texto.isEmpty()) {
            return false;
        }
        return texto.matches("^[\\p{L} ]+$"); // Coincide con letras unicode y espacios
    }

    /**
     * Verifica si una cadena está vacía o solo contiene espacios
     * @param texto El texto a verificar
     * @return true si está vacío o solo tiene espacios
     */
    public static boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}