package com.logitrack.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI logitrackOpenAPI() {
        String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("LogiTrack API")
                        .version("1.0")
                        .description("API REST para gestión de bodegas, productos, movimientos de inventario, auditorías y reportes.")
                        .contact(new Contact()
                                .name("LogiTrack S.A.")
                                .email("soporte@logitrack.com")
                        )
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName)
                )
                .components(new Components()
                        .addSecuritySchemes(
                                securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}