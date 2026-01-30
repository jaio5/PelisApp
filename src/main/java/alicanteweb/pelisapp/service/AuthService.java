package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.dto.LoginResponse;
import alicanteweb.pelisapp.dto.LoginRequest;
import alicanteweb.pelisapp.dto.RegisterRequest;
import alicanteweb.pelisapp.entity.Role;
import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.repository.RoleRepository;
import alicanteweb.pelisapp.repository.UserRepository;
import alicanteweb.pelisapp.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public LoginResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("username already exists");
        }
        if (req.getEmail() != null && userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("email already exists");
        }

        User u = new User();
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setDisplayName(req.getDisplayName());
        u.setRegisteredAt(Instant.now());

        Role defaultRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_USER");
            r.setDescription("Default role");
            return roleRepository.save(r);
        });

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        u.setRoles(roles);

        userRepository.save(u);

        String access = jwtTokenProvider.createAccessToken(u.getUsername(), Collections.singleton("ROLE_USER"));
        String refresh = jwtTokenProvider.createRefreshToken(u.getUsername());
        long expires = jwtTokenProvider.getExpiryMillis(access);
        return new LoginResponse(access, expires, refresh);
    }

    public LoginResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
        Set<String> roles = principal.getAuthorities().stream().map(a -> a.getAuthority()).collect(java.util.stream.Collectors.toSet());
        String access = jwtTokenProvider.createAccessToken(principal.getUsername(), roles);
        String refresh = jwtTokenProvider.createRefreshToken(principal.getUsername());
        long expires = jwtTokenProvider.getExpiryMillis(access);
        return new LoginResponse(access, expires, refresh);
    }

    public LoginResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        String username = jwtTokenProvider.getUsername(refreshToken);
        User u = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Set<String> roles = u.getRoles() == null ? Collections.emptySet() : u.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet());
        String access = jwtTokenProvider.createAccessToken(username, roles);
        String refresh = jwtTokenProvider.createRefreshToken(username);
        long expires = jwtTokenProvider.getExpiryMillis(access);
        return new LoginResponse(access, expires, refresh);
    }

}
