package alicanteweb.pelisapp.constants;

/**
 * Constantes para roles de usuario en el sistema.
 * Centraliza los nombres de roles para evitar hardcoding.
 */
public final class RoleConstants {

    // Roles del sistema
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_MODERATOR = "ROLE_MODERATOR";
    public static final String ROLE_SUPERADMIN = "ROLE_SUPERADMIN";

    // Nombres de roles sin prefijo para la base de datos
    public static final String USER = "USER";
    public static final String ADMIN = "ADMIN";
    public static final String MODERATOR = "MODERATOR";
    public static final String SUPERADMIN = "SUPERADMIN";

    private RoleConstants() {
        // Clase de constantes - no instanciable
    }
}
