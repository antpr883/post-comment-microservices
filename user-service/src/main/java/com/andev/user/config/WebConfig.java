package com.andev.user.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.andev.user.config.interceptor.RequestResponseLoggingInterceptor;

/**
 * Web MVC configuration for User Service.
 *
 * Configures request/response logging and other web-related settings.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RequestResponseLoggingInterceptor requestResponseLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestResponseLoggingInterceptor)
                .addPathPatterns("/api/**") // Only log API endpoints
                .excludePathPatterns(
                        "/actuator/**", // Exclude health checks
                        "/swagger-ui/**", // Exclude swagger UI
                        "/v3/api-docs/**", // Exclude API docs
                        "/favicon.ico" // Exclude favicon
                        );
    }
}
