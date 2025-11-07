package com.tiffin_sathi.config;

import com.tiffin_sathi.repository.UserRepository;
import com.tiffin_sathi.repository.VendorRepository;
import com.tiffin_sathi.model.User;
import com.tiffin_sathi.model.Vendor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class ApplicationConfiguration {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;

    public ApplicationConfiguration(UserRepository userRepository, VendorRepository vendorRepository) {
        this.userRepository = userRepository;
        this.vendorRepository = vendorRepository;
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            // Try normal user
            User user = userRepository.findByEmail(username).orElse(null);
            if (user != null) return user;

            // Try vendor
            Vendor vendor = vendorRepository.findByBusinessEmail(username).orElse(null);
            if (vendor != null) return vendor;

            throw new UsernameNotFoundException("User or Vendor not found with email: " + username);
        };
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
