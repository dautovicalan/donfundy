package hr.algebra.donfundy.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "DonFundy API",
                version = "1.0",
                description = "REST API for DonFundy donation platform - manage campaigns, donations, and donors",
                contact = @Contact(
                        name = "DonFundy Support",
                        email = "support@donfundy.com"
                )
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT token authentication. Get token from /auth/login endpoint."
)
public class OpenApiConfig {
}
