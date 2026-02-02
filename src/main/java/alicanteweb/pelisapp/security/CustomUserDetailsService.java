package alicanteweb.pelisapp.security;

import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user: {}", username);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                log.warn("User not found: {}", username);
                return new UsernameNotFoundException("Usuario no encontrado: " + username);
            });

        // Verificar que el email esté confirmado
        if (!user.isEmailConfirmed()) {
            log.warn("User {} attempted login but email not confirmed", username);
            throw new UsernameNotFoundException("Cuenta no confirmada. Revisa tu email para confirmar tu cuenta.");
        }

        log.info("User {} loaded successfully with {} roles", username, user.getRoles().size());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                    .collect(Collectors.toList()))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
