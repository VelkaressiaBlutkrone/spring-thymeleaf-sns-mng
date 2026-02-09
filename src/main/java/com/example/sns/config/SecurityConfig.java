package com.example.sns.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.sns.exception.ErrorCode;
import com.example.sns.exception.ErrorResponse;
import com.example.sns.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security 설정.
 *
 * 보안 헤더 및 Actuator 제한 (RULE 1.6.1).
 * JWT 인증 필터 적용 (Step 6).
 * Step 7: /api/admin/** ROLE_ADMIN, deny-by-default, CORS allow-list, 403 로깅.
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsProperties corsProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; script-src 'self' https://dapi.kakao.com; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self'; connect-src 'self' https://dapi.kakao.com")))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").denyAll()
                        .requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/auth/logout").permitAll()
                        .requestMatchers("/api/members").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/sample/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/posts", "/api/posts/*").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/posts").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/posts/*").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/posts/*").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/image-posts", "/api/image-posts/*").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/image-posts").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/image-posts/*").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/image-posts/*").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/pins/nearby").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/pins/*/posts", "/api/pins/*/image-posts").permitAll()
                        .requestMatchers("/api/pins", "/api/pins/**").authenticated()
                        .requestMatchers("/", "/error", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/pins/*/posts", "/posts/*", "/image-posts/*").permitAll()
                        .requestMatchers("/posts/create", "/image-posts/create").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/posts", "/image-posts").authenticated()
                        .anyRequest().denyAll())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((HttpServletRequest request, HttpServletResponse response,
                                                  org.springframework.security.core.AuthenticationException authException) -> {
                            String accept = request.getHeader("Accept");
                            if (accept != null && accept.contains("text/html")) {
                                response.sendRedirect("/login");
                                return;
                            }
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"code\":\"E002\",\"message\":\"인증이 필요합니다.\"}");
                        })
                        .accessDeniedHandler(accessDeniedHandler()));

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (HttpServletRequest request, HttpServletResponse response,
                org.springframework.security.access.AccessDeniedException accessDeniedException) -> {
            log.warn("인가 실패(403): path={}, principal={}, message={}",
                    request.getRequestURI(),
                    request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous",
                    accessDeniedException.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.FORBIDDEN);
            String json = String.format("{\"code\":\"%s\",\"message\":\"%s\"}",
                    errorResponse.getCode(), errorResponse.getMessage());
            response.getWriter().write(json);
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = corsProperties.getAllowedOrigins();
        if (origins != null && !origins.isEmpty()) {
            config.setAllowedOrigins(origins);
        }
        config.setAllowedMethods(List.of(HttpMethod.GET.name(), HttpMethod.POST.name(),
                HttpMethod.PUT.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name()));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
