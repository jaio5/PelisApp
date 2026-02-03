package alicanteweb.pelisapp.service;

/**
 * Interfaz común para servicios de email
 * Permite usar tanto MockEmailService como RealEmailService según configuración
 */
public interface IEmailService {

    /**
     * Envía email de confirmación para registro de usuario
     * @param toEmail Email de destino
     * @param username Nombre de usuario
     * @param confirmationToken Token de confirmación único
     */
    void sendConfirmationEmail(String toEmail, String username, String confirmationToken);

    /**
     * Reenvía email de confirmación
     * @param toEmail Email de destino
     * @param username Nombre de usuario
     * @param confirmationToken Token de confirmación único
     */
    default void sendSimpleConfirmationEmail(String toEmail, String username, String confirmationToken) {
        sendConfirmationEmail(toEmail, username, confirmationToken);
    }
}
