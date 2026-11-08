package com.wynh.microservices.common;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Configuration(proxyBeanMethods = false)
@Import(ApiExceptionHandler.class)
public class ServiceConfiguration {
    @Bean
    FilterRegistrationBean<OncePerRequestFilter> internalAuthentication(@Value("${services.internal-token}") String token) {
        if (token.isBlank()) throw new IllegalArgumentException("services.internal-token must not be empty");
        var registration = new FilterRegistrationBean<OncePerRequestFilter>();
        registration.setFilter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {
                String path = request.getRequestURI();
                if (path.equals("/actuator/health") || path.startsWith("/actuator/health/") || path.equals("/actuator/prometheus")) {
                    chain.doFilter(request, response);
                    return;
                }
                String supplied = request.getHeader("X-Internal-Token");
                if (supplied == null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
                    response.setStatus(401);
                    response.setContentType("application/problem+json");
                    response.getWriter().write("{\"title\":\"Unauthorized\",\"status\":401}");
                    return;
                }
                chain.doFilter(request, response);
            }
        });
        registration.setOrder(-100);
        return registration;
    }
}
