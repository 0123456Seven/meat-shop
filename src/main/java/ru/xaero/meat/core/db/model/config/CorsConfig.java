package ru.xaero.meat.core.db.model.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:3000",
                                "http://127.0.0.1:3000",
                                "http://localhost:5500",  // Live Server
                                "http://127.0.0.1:5500",  // Live Server
                                "http://localhost:8080",
                                "http://localhost:63342", // WebStorm
                                "http://localhost:8000",  // Python server
                                "http://127.0.0.1:8000",
                                "http://localhost",       // Без порта
                                "http://127.0.0.1",       // Без порта
                                "http://localhost:*",     // Любой порт
                                "http://127.0.0.1:*"      // Любой порт
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600); // Кешируем на 1 час
            }
        };
    }
}