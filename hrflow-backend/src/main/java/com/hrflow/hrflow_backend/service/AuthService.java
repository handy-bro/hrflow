package com.hrflow.hrflow_backend.service;

import com.hrflow.hrflow_backend.dto.auth.*;
import com.hrflow.hrflow_backend.entity.User;
import com.hrflow.hrflow_backend.enums.Role;
import com.hrflow.hrflow_backend.exceptionHandler.auth.*;
import com.hrflow.hrflow_backend.repository.UserRepository;
import com.hrflow.hrflow_backend.security.JwtService;
import com.hrflow.hrflow_backend.utils.TokenUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    private static final long VERIFICATION_TOKEN_HOURS = 24;
    private static final long ACTIVATION_TOKEN_HOURS = 24;
    private static final long RESET_TOKEN_HOURS = 1;

    // ==================================================================
    // FLOW 1 — Self-registration (password provided by the client)
    // ==================================================================

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyTakenException("Email already taken");
        }

        Role role = resolvePublicRole(request.getRole());
        String rawToken = TokenUtil.generateRawToken();

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(false)
                .verificationTokenHash(TokenUtil.hash(rawToken))
                .verificationTokenExpiry(LocalDateTime.now().plusHours(VERIFICATION_TOKEN_HOURS))
                .build();

        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), rawToken);

        return AuthResponse.builder()
                .message("Registration successful! Please check your email to verify your account.")
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    private Role resolvePublicRole(String requested) {
        Role role;
        try {
            role = Role.valueOf(requested.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleException("Invalid role: " + requested);
        }
        if (role == Role.ADMIN) { // or any list of roles that cannot be self-assigned
            throw new InvalidRoleException("This role cannot be self-assigned");
        }
        return role;
    }

    @Transactional
    public String verifyEmail(String rawToken) {

        String hash = TokenUtil.hash(rawToken);

        User user = userRepository.findByVerificationTokenHash(hash)
                .orElseThrow(() -> new InvalidTokenException("The token is invalid"));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ExpiredTokenException("The token is expired");
        }

        user.setEnabled(true);
        user.setVerificationTokenHash(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);

        return "Email verified successfully! You can now login.";
    }

    @Transactional
    public AuthResponse resendVerificationEmail(ResendVerificationRequest request) {

        userRepository.findByEmail(request.getEmail())
                .filter(user -> !user.isEnabled())
                .filter(user -> user.getPassword() != null) // excludes accounts "pending activation"
                .ifPresent(user -> {
                    String rawToken = TokenUtil.generateRawToken();
                    user.setVerificationTokenHash(TokenUtil.hash(rawToken));
                    user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(VERIFICATION_TOKEN_HOURS));
                    userRepository.save(user);
                    emailService.sendVerificationEmail(user.getEmail(), rawToken);
                });

        return AuthResponse.builder()
                .message("If an account exists and is not yet verified, a new verification email has been sent.")
                .build();
    }

    // ==================================================================
    // FLOW 2 — Created by a third party (HR creates an employee, no password)
    // Called from EmployeeService, not directly exposed to the client.
    // ==================================================================

    @Transactional
    public User createPendingUser(String email, Role role) {

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyTakenException("Email already taken");
        }

        String rawToken = TokenUtil.generateRawToken();

        User user = User.builder()
                .email(email)
                .password(null)
                .role(role)
                .enabled(false)
                .activationTokenHash(TokenUtil.hash(rawToken))
                .activationTokenExpiry(LocalDateTime.now().plusHours(ACTIVATION_TOKEN_HOURS))
                .build();

        // Save the user entity to the database
        userRepository.save(user);

        // Send an account activation email to the user with their email address and a raw activation token
        emailService.sendAccountActivationEmail(user.getEmail(), rawToken);

        return user;
    }

    public void validateActivationToken(String rawToken) {
        String hash = TokenUtil.hash(rawToken);

        User user = userRepository.findByActivationTokenHash(hash)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired token"));

        if (user.getActivationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ExpiredTokenException("This activation link has expired");
        }
    }

    @Transactional
    public AuthResponse setInitialPassword(SetPasswordRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        String hash = TokenUtil.hash(request.getToken());

        User user = userRepository.findByActivationTokenHash(hash)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired token"));

        if (user.getActivationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ExpiredTokenException("This activation link has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setActivationTokenHash(null);
        user.setActivationTokenExpiry(null);
        userRepository.save(user);

        return AuthResponse.builder()
                .message("Account activated successfully! You can now login.")
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public AuthResponse resendActivationEmail(ResendVerificationRequest request) {

        userRepository.findByEmail(request.getEmail())
                .filter(user -> !user.isEnabled())
                .filter(user -> user.getPassword() == null) // only accounts created by a third party
                .ifPresent(user -> {
                    String rawToken = TokenUtil.generateRawToken();
                    user.setActivationTokenHash(TokenUtil.hash(rawToken));
                    user.setActivationTokenExpiry(LocalDateTime.now().plusHours(ACTIVATION_TOKEN_HOURS));
                    userRepository.save(user);
                    emailService.sendAccountActivationEmail(user.getEmail(), rawToken);
                });

        return AuthResponse.builder()
                .message("If an account exists and is not yet activated, a new activation email has been sent.")
                .build();
    }

    // ==================================================================
    // Login (common to both flows)
    // ==================================================================

    public AuthResponse login(LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (DisabledException e) {
            throw new EmailNotVerifiedException("Please verify or activate your account before logging in");
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return AuthResponse.builder()
                .accessToken(jwtService.generateToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }

    // ==================================================================
    // Forgot password (common to both flows, once the account is active)
    // ==================================================================

    @Transactional
    public AuthResponse forgotPassword(ForgotPasswordRequest request) {

        userRepository.findByEmail(request.getEmail())
                .filter(User::isEnabled)
                .ifPresent(user -> {
                    String rawToken = TokenUtil.generateRawToken();
                    user.setResetPasswordTokenHash(TokenUtil.hash(rawToken));
                    user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(RESET_TOKEN_HOURS));
                    userRepository.save(user);
                    emailService.sendResetPasswordEmail(user.getEmail(), rawToken);
                });

        return AuthResponse.builder()
                .message("If an account exists with this email, a password reset link has been sent.")
                .build();
    }

    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        String hash = TokenUtil.hash(request.getToken());

        User user = userRepository.findByResetPasswordTokenHash(hash)
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ExpiredTokenException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordTokenHash(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);

        return AuthResponse.builder()
                .message("Password reset successfully! You can now login.")
                .build();
    }
}