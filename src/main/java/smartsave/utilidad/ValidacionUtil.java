package smartsave.utilidad;

import java.util.regex.Pattern;

public class ValidacionUtil {

    private static final Pattern PATRON_EMAIL = Pattern.compile(
            "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    public static boolean esEmailValido(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return PATRON_EMAIL.matcher(email).matches();
    }

    public static boolean esContrasenaValida(String contrasena) {
        return contrasena != null && contrasena.length() >= 6;
    }

    public static boolean soloLetras(String texto) {
        if (texto == null || texto.isEmpty()) {
            return false;
        }
        return texto.matches("^[\\p{L} ]+$");
    }

    public static boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}