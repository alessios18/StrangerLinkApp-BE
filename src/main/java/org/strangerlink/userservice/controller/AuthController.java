package org.strangerlink.userservice.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.strangerlink.userservice.dto.AuthDto.AuthResponse;
import org.strangerlink.userservice.dto.AuthDto.LoginRequest;
import org.strangerlink.userservice.dto.AuthDto.RegisterRequest;
import org.strangerlink.userservice.exception.ApiException;
import org.strangerlink.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (ApiException e) {
            // Restituisci lo stato HTTP appropriato insieme al messaggio di errore
            return ResponseEntity.status(e.getStatus()).body(
                    AuthResponse.builder()
                            .error(e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/oauth2/authorization/google")
    public void initiateGoogleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }
}