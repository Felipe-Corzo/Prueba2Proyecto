package com.logitrack.controller;

import com.logitrack.dto.AuthRequest;
import com.logitrack.dto.AuthResponse;
import com.logitrack.dto.RegisterRequest;
import com.logitrack.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<?> usuarioActual(Authentication authentication) {
        Map<String, Object> respuesta = new HashMap<>();

        respuesta.put("email", authentication.getName());
        respuesta.put("roles", authentication.getAuthorities());

        return ResponseEntity.ok(respuesta);
    }
}