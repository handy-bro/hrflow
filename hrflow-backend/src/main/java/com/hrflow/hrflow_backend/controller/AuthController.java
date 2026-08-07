package com.hrflow.hrflow_backend.controller;

import com.hrflow.hrflow_backend.dto.auth.*;
import com.hrflow.hrflow_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Authentication Endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user",
            description = "Creates a pending account and sends an activation link to set the password.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/validate-activation-token")
    @Operation(summary = "Validate an activation token before showing the password form")
    public ResponseEntity<Void> validateActivationToken(@RequestParam String token) {
        authService.validateActivationToken(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/set-password")
    @Operation(summary = "Set the initial password using a valid activation token")
    public ResponseEntity<AuthResponse> setPassword(@Valid @RequestBody SetPasswordRequest request) {
        return ResponseEntity.ok(authService.setInitialPassword(request));
    }

    @PostMapping("/resend-activation")
    @Operation(summary = "Resend the account activation email")
    public ResponseEntity<AuthResponse> resendActivation(@Valid @RequestBody ResendVerificationRequest request) {
        return ResponseEntity.ok(authService.resendActivationEmail(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}