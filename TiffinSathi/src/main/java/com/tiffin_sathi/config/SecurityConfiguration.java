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
                .csrf(csrf -> csrf.disable()) // Disable CSRF for API and payment callbacks
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (no authentication required)
                        .requestMatchers("/auth/**").permitAll()

                        // Payment callback endpoints - MUST BE PUBLIC for eSewa/Khalti to work
                        .requestMatchers(HttpMethod.GET, "/api/payments/callback/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payments/callback/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/payments/status/**").permitAll()

                        // Public meal packages endpoints (no authentication)
                        .requestMatchers(HttpMethod.GET, "/api/meal-packages").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/meal-packages/**").permitAll()

                        // Public meal sets endpoints (no authentication)
                        .requestMatchers(HttpMethod.GET, "/api/meal-sets").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/meal-sets/**").permitAll()

                        // Public vendor endpoints for user portal
                        .requestMatchers(HttpMethod.GET, "/api/vendors/public/**").permitAll()

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

                        // Payment endpoints - allow authenticated users to initiate payments
                        .requestMatchers(HttpMethod.POST, "/api/payments/initiate").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/payments/admin/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/payments/{paymentId}/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/payments/{paymentId}").hasAnyRole("USER", "ADMIN")

                        // Delivery partners endpoints - VENDOR only
                        .requestMatchers("/api/delivery-partners/**").hasRole("VENDOR")
                        .requestMatchers("/api/delivery-partners/vendor/**").hasRole("VENDOR")

                        // Delivery partner self-management endpoints
                        .requestMatchers("/api/delivery/change-password").hasRole("DELIVERY")
                        .requestMatchers("/api/delivery/profile").hasRole("DELIVERY")

                        // Meal sets vendor endpoints - VENDOR only
                        .requestMatchers(HttpMethod.POST, "/api/meal-sets").hasRole("VENDOR")
                        .requestMatchers("/api/meal-sets/vendor/**").hasRole("VENDOR")

                        // Meal packages vendor endpoints - VENDOR only
                        .requestMatchers(HttpMethod.POST, "/api/meal-packages").hasRole("VENDOR")
                        .requestMatchers("/api/meal-packages/vendor/**").hasRole("VENDOR")

                        // Subscription endpoints
                        .requestMatchers(HttpMethod.POST, "/api/subscriptions").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/subscriptions/user").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/subscriptions/user/**").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/subscriptions/**").hasAnyRole("USER", "VENDOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/subscriptions/**").hasAnyRole("USER", "VENDOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/subscriptions/**").hasRole("USER")

                        // Order endpoints
                        .requestMatchers(HttpMethod.GET, "/api/orders/today").hasAnyRole("VENDOR", "DELIVERY", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/orders/date/**").hasAnyRole("VENDOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/orders/status/**").hasAnyRole("VENDOR", "DELIVERY", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/orders/*/status").hasAnyRole("VENDOR", "DELIVERY", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/orders/*/assign-delivery").hasAnyRole("VENDOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/orders/delivery/**").hasRole("DELIVERY")

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

        // Allow all origins for payment callbacks (you can restrict in production)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://localhost:8080",
                "https://rc-epay.esewa.com.np",  // eSewa payment gateway
                "https://dev.khalti.com"         // Khalti payment gateway
        ));

        // Allow all methods for payment callbacks
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allow all headers
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Allow credentials
        configuration.setAllowCredentials(true);

        // Expose headers
        configuration.setExposedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));

        // Set max age for preflight requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}