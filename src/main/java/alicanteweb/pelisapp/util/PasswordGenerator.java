package alicanteweb.pelisapp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Generar hash para admin123
        String plainPassword = "admin123";
        String hashedPassword = encoder.encode(plainPassword);

        System.out.println("Contraseña original: " + plainPassword);
        System.out.println("Hash BCrypt: " + hashedPassword);

        // Verificar que el hash funciona
        boolean matches = encoder.matches(plainPassword, hashedPassword);
        System.out.println("Verificación: " + matches);
    }
}
