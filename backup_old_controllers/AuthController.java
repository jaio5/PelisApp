package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.dto.LoginRequest;
import alicanteweb.pelisapp.dto.LoginResponse;
import alicanteweb.pelisapp.dto.RegisterRequest;
import alicanteweb.pelisapp.dto.RefreshRequest;
import alicanteweb.pelisapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        LoginResponse resp = authService.register(req);
        return ResponseEntity.status(201).body(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse resp = authService.login(req);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        LoginResponse resp = authService.refresh(req.getRefreshToken());
        return ResponseEntity.ok(resp);
    }
}
