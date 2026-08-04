package com.hrflow.hrflow_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"password", "confirmPassword"})
public class SetPasswordRequest {

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String confirmPassword;

    @NotBlank(message = "Token is required")
    private String token;
}