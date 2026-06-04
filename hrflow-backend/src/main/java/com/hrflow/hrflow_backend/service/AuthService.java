package com.hrflow.hrflow_backend.service;

import com.hrflow.hrflow_backend.dto.AuthResponse;
import com.hrflow.hrflow_backend.dto.LoginRequest;
import com.hrflow.hrflow_backend.dto.RegisterRequest;
import com.hrflow.hrflow_backend.dto.ResendVerificationRequest;
import com.hrflow.hrflow_backend.entity.User;
import com.hrflow.hrflow_backend.enums.Role;
import com.hrflow.hrflow_backend.exceptionHandler.*;
import com.hrflow.hrflow_backend.repository.UserRepository;
import com.hrflow.hrflow_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyTakenException("Email already taken");
        }

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .enabled(false)
                .verificationToken(verificationToken)
                .verificationTokenExpiry(LocalDateTime.now().plusHours(24))
                .build();

        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        return AuthResponse.builder()
                .message("Registration successful! Please check your email to verify your account.")
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;

            if (cause instanceof BadCredentialsException) {
                throw new BadCredentialsException("Invalid email or password");
            }
            if (cause instanceof DisabledException) {
                return AuthResponse.builder()
                        .message("EMAIL_NOT_VERIFIED")
                        .email(request.getEmail())
                        .build();
            }
            throw new RuntimeException("Authentication failed");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }

    public String verifyEmail(String token) {

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() ->
                        new InvalidTokenException("The token is invalid"));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ExpiredTokenException("The token is expired");
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);

        return "Email verified successfully! You can now login.";
    }

    public AuthResponse resendVerificationEmail(ResendVerificationRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not found"));

        // Already verified
        if (user.isEnabled()) {
            throw new AccountAlreadyVerifiedException("This account is already verified");
        }

        // Generate a new token
        String newToken = UUID.randomUUID().toString();
        user.setVerificationToken(newToken);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        // Resend Link
        emailService.sendVerificationEmail(user.getEmail(), newToken);

        return AuthResponse.builder()
                .message("Verification email resent! Please check your inbox.")
                .email(user.getEmail())
                .build();
    }
}