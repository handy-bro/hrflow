package com.hrflow.hrflow_backend.config.seeders;

import com.hrflow.hrflow_backend.entity.User;
import com.hrflow.hrflow_backend.enums.Role;
import com.hrflow.hrflow_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedAdmin(
            @org.springframework.beans.factory.annotation.Value("${app.admin.email}") String adminEmail,
            @org.springframework.beans.factory.annotation.Value("${app.admin.password}") String adminPassword) {

        return args -> {
            if (userRepository.existsByEmail(adminEmail)) {
                return;
            }

            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .enabled(true) // No need to verify email for the first admin
                    .build();

            userRepository.save(admin);
            log.info("Default admin account created: {}", adminEmail);
        };
    }
}