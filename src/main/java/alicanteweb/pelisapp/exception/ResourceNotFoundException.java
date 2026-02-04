package alicanteweb.pelisapp.exception;

/**
 * Excepción personalizada para recursos no encontrados.
 * Extiende RuntimeException para ser una excepción no verificada.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
