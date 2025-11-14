package com.tiffin_sathi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter,
                                 AuthenticationProvider authenticationProvider) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (no authentication required)
                        .requestMatchers("/auth/**").permitAll()

                        // Public meal packages endpoints (no authentication)
                        .requestMatchers(HttpMethod.GET, "/api/meal-packages").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/meal-packages/**").permitAll()

                        // Public meal sets endpoints (no authentication)
                        .requestMatchers(HttpMethod.GET, "/api/meal-sets").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/meal-sets/**").permitAll()

                        // Vendor management endpoints
                        .requestMatchers("/api/vendors/status/**").hasRole("ADMIN")
                        .requestMatchers("/api/vendors").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/vendors/{vendorId}/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/vendors/{vendorId}").hasAnyRole("VENDOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/vendors/{vendorId}").hasAnyRole("VENDOR", "ADMIN")
                        .requestMatchers("/api/vendors/{vendorId}/change-password").hasRole("VENDOR")

                        // User management endpoints
                        .requestMatchers("/api/users").hasRole("ADMIN")
                        .requestMatchers("/api/users/{userId}/status").hasRole("ADMIN")
                        .requestMatchers("/api/users/{userId}/role").hasRole("ADMIN")
                        .requestMatchers("/api/users/{userId}/change-password").hasRole("USER")

                        // Delivery partners endpoints - VENDOR only
                        .requestMatchers("/api/delivery-partners/**").hasRole("VENDOR")

                        // Meal sets vendor endpoints - VENDOR only
                        .requestMatchers(HttpMethod.POST, "/api/meal-sets").hasRole("VENDOR")
                        .requestMatchers("/api/meal-sets/vendor/**").hasRole("VENDOR")

                        // Meal packages vendor endpoints - VENDOR only
                        .requestMatchers(HttpMethod.POST, "/api/meal-packages").hasRole("VENDOR")
                        .requestMatchers("/api/meal-packages/vendor/**").hasRole("VENDOR")

                        // Role-based access for general endpoints
                        .requestMatchers("/vendor/**").hasRole("VENDOR")
                        .requestMatchers("/user/**").hasRole("USER")
                        .requestMatchers("/delivery/**").hasRole("DELIVERY")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Any other request requires authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://localhost:8080"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}