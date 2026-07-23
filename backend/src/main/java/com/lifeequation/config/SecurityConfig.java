package com.lifeequation.config;

import com.lifeequation.security.CustomAccessDeniedHandler;
import com.lifeequation.security.CustomAuthenticationEntryPoint;
import com.lifeequation.security.JwtAuthenticationFilter;
import com.lifeequation.security.XssFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 6.x configuration replacing the old JwtInterceptor + WebConfig approach.
 *
 * Architecture:
 *   Request → XssFilter → JwtAuthenticationFilter → Authorization → Controller
 *
 * Key design decisions:
 * - CSRF disabled (JWT Bearer tokens, no browser cookies)
 * - Stateless session (no HttpSession created)
 * - Custom JSON 401/403 responses matching project's Result format
 * - CORS merged here (replaces CorsConfig.java)
 * - @EnableMethodSecurity enables @PreAuthorize on controllers
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final XssFilter xssFilter;
    private final CustomAuthenticationEntryPoint authEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          XssFilter xssFilter,
                          CustomAuthenticationEntryPoint authEntryPoint,
                          CustomAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.xssFilter = xssFilter;
        this.authEntryPoint = authEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF — JWT uses Authorization header, not cookies
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Stateless session — no HttpSession, each request is independent
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Custom 401/403 JSON responses matching project's Result format
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                // 4. URL-based authorization rules (evaluated in order, first match wins)
                .authorizeHttpRequests(auth -> auth
                        // --- Public: no authentication needed ---
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/set-role", "/api/auth/wx-login").permitAll()
                        .requestMatchers("/api/home").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/diseases/**",
                                "/api/hospitals/**",
                                "/api/doctors/**",
                                "/api/articles/**",
                                "/api/drugs/**",
                                "/api/pharmacies/**"
                        ).permitAll()
                        // Consultations: public list/detail, but /my and POST require auth
                        .requestMatchers("/api/consultations/my").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/consultations").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/consultations/**").permitAll()
                        // Reviews: target reviews are public read, create/my require auth
                        .requestMatchers(HttpMethod.GET, "/api/reviews/target").permitAll()

                        // Swagger / Knife4j API 文档
                        .requestMatchers(
                                "/doc.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()

                        // Static resources
                        .requestMatchers("/uploads/**").permitAll()

                        // --- Role-based access ---
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/doctor/**").hasAnyRole("DOCTOR", "ADMIN")

                        // --- Everything else requires authentication ---
                        .anyRequest().authenticated())

                // 5. Add XSS filter before JWT filter
                .addFilterBefore(xssFilter, UsernamePasswordAuthenticationFilter.class)

                // 6. Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 7. CORS (replaces CorsConfig.java)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    /**
     * CORS configuration — permissive for development (replaces CorsConfig).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * BCryptPasswordEncoder — replaces Hutool's BCrypt.
     * Password migration is handled in UserServiceImpl with fallback logic.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
