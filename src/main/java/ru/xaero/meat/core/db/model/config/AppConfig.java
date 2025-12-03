package ru.xaero.meat.core.db.model.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class AppConfig {
    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer paginationCustomizer() {
        return pageableResolver -> {
            pageableResolver.setOneIndexedParameters(true); // Если хотите, чтобы страницы начинались с 1
            pageableResolver.setMaxPageSize(100);
            pageableResolver.setFallbackPageable(PageRequest.of(0, 20));
        };
    }
}
