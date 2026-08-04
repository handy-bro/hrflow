package com.hrflow.hrflow_backend.repository;

import com.hrflow.hrflow_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByActivationTokenHash(String activationTokenHash);
    Optional<User> findByVerificationTokenHash(String verificationTokenHash);
    Optional<User> findByResetPasswordTokenHash(String resetPasswordTokenHash);
}
