package alicanteweb.pelisapp.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Value("${app.dev-mode:false}")
    private boolean devMode;

    public SecurityConfig(JwtTokenProvider tokenProvider, CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(tokenProvider, userDetailsService);

        http
            .csrf(AbstractHttpConfigurer::disable)
            // Usar sesiones para login web, JWT para API
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> {
                // Rutas públicas
                auth.requestMatchers("/", "/login", "/register").permitAll();
                auth.requestMatchers("/confirm-account", "/resend-confirmation").permitAll();
                auth.requestMatchers("/api/auth/**").permitAll();
                auth.requestMatchers("/css/**", "/js/**", "/images/**").permitAll();
                auth.requestMatchers("/error").permitAll();
                auth.requestMatchers("/pelicula/**").permitAll(); // Permitir ver detalles sin login

                // Rutas que requieren autenticación
                auth.requestMatchers("/perfil/**", "/admin/**").authenticated();
                auth.requestMatchers("/pelicula/*/review").authenticated(); // Solo valorar requiere login
                auth.requestMatchers("/review/*/like").authenticated();

                // Por defecto, permitir acceso (modo permisivo para desarrollo)
                auth.anyRequest().permitAll();
            })
            // Configuración de login por formulario
            .formLogin(form -> form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/", true)
                    .failureUrl("/login?error=true")
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .permitAll()
            )
            // Configuración de logout
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
            )
            // Solo añadir JWT filter para rutas de API
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }
}
