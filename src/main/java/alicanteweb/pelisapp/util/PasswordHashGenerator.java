
package alicanteweb.pelisapp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String password = "admin123";
        String hash = encoder.encode(password);

        System.out.println("Contraseña: " + password);
        System.out.println("Hash BCrypt (strength 12): " + hash);
        System.out.println();
        System.out.println("SQL para actualizar admin:");
        System.out.println("UPDATE usuario SET password = '" + hash + "' WHERE username = 'admin';");
    }
}
