package com.mbclab.lablink.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI labLinkOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LabLink API Documentation")
                        .description("Platform Manajemen Terpadu Laboratorium Riset")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("LabLink Team")
                                .email("admin@mbclab.com"))
                        .license(new License()
                                .name("Private License")
                                .url("https://mbclab.com")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createSecurityScheme()));
    }

    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .name("Bearer Authentication")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Masukkan token JWT yang didapat dari login endpoint");
    }
}
