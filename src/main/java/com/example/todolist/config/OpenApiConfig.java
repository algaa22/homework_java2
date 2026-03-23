package com.example.todolist.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.api.version:2.0.0}")
    private String apiVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("To-Do List API")
                        .version(apiVersion)
                        .description("REST API for managing tasks with file attachments, favorites and user preferences")
                        .contact(new Contact()
                                .name("API Support")
                                .email("anikanova.aa@phystech.edu")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Local server")
                ));
    }
}