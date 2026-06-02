package com.hrflow.hrflow_backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "HRFlow API",
                version = "1.0",
                description = """
                        ## HRFlow — Human Resource Management System
                        
                        REST API for managing human resources including employees,\s
                                             departments, leaves, attendances and payslips.
                        """,
                contact = @Contact(
                        name = "Melong Handy",
                        email = "handymelong237@gmail.com"
                )
        ),
        servers = {
                @Server(
                        description = "LOCAL environment",
                        url = "http://localhost:8080/api"
                ),
                @Server(
                        description = "PROD environment",
                        url = "https://production-server.com/api"
                )
        },
        security = {
                @SecurityRequirement(name = "BearerAuthentication")
        }
)
@SecurityScheme(
        name = "BearerAuthentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "Authentication based on JWT"
)
public class OpenApiConfig {
}
