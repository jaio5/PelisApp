package alicanteweb.pelisapp.constants;

/**
 * Constantes de validación para la aplicación.
 * Centraliza valores de validación para consistencia y código limpio.
 */
public final class ValidationConstants {

    // Review validation
    public static final int MIN_REVIEW_STARS = 1;
    public static final int MAX_REVIEW_STARS = 5;
    public static final int MIN_REVIEW_TEXT_LENGTH = 10;
    public static final int MAX_REVIEW_TEXT_LENGTH = 2000;

    // User validation básica
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int MIN_PASSWORD_LENGTH = 8; // Aumentado para seguridad
    public static final int MAX_PASSWORD_LENGTH = 128;

    // Movie validation
    public static final int MIN_MOVIE_TITLE_LENGTH = 1;
    public static final int MAX_MOVIE_TITLE_LENGTH = 255;

    // Validaciones avanzadas de usuario
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]+$";
    public static final String SPECIAL_CHARACTERS = "!@#$%^&*()_+-=[]{}|;':\",./<>?";

    // Email validation
    public static final int MIN_EMAIL_LENGTH = 5;
    public static final int MAX_EMAIL_LENGTH = 100;
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    // Display name validation
    public static final int MAX_DISPLAY_NAME_LENGTH = 50;

    // Usernames prohibidos
    public static final String[] PROHIBITED_USERNAMES = {
        "admin", "administrator", "root", "user", "test", "guest",
        "api", "support", "info", "contact", "help", "service",
        "mail", "email", "www", "ftp", "system", "null", "undefined"
    };

    // Patrones sospechosos para prevenir inyecciones
    public static final String[] SUSPICIOUS_PATTERNS = {
        "<script", "javascript:", "vbscript:", "onload=", "onerror=",
        "eval(", "document.", "window.", "alert(", "confirm(",
        "drop table", "delete from", "insert into", "update set",
        "union select", "' or '", "' and '", "--", "/*", "*/"
    };

    // Mensajes de error estándar
    public static final String ERROR_PASSWORD_TOO_SHORT = "La contraseña es demasiado corta";
    public static final String ERROR_PASSWORD_WEAK = "La contraseña es demasiado débil";
    public static final String ERROR_USERNAME_INVALID = "Nombre de usuario inválido";
    public static final String ERROR_EMAIL_INVALID = "Formato de email inválido";
    public static final String ERROR_PASSWORDS_DONT_MATCH = "Las contraseñas no coinciden";
    public static final String ERROR_SUSPICIOUS_CONTENT = "Contenido sospechoso detectado";

    private ValidationConstants() {
        // Clase de constantes - no instanciable
    }
}
