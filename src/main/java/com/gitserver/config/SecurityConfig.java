package com.gitserver.config;

import com.gitserver.repository.UserRepository;
import com.gitserver.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Git Server.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF protection is disabled because:
            // 1. This is a stateless REST API using HTTP Basic Authentication
            // 2. No session cookies are used (SessionCreationPolicy.STATELESS)
            // 3. All state-changing operations require Basic Auth credentials
            // 4. Git protocol endpoints are inherently stateless
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/git/**", "/h2-console/**"))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/users/register").permitAll()
                .requestMatchers("/api/repos/public").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/repos/{owner}/{name}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/repos/{owner}/{name}/branches/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/repos/{owner}/{name}/commits/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/repos/{owner}/{name}/contents/**").permitAll()
                
                // Git protocol endpoints (require authentication for push, allow public for fetch)
                .requestMatchers(HttpMethod.GET, "/git/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/git/**").authenticated()
                
                // Swagger/OpenAPI
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                
                // H2 Console
                .requestMatchers("/h2-console/**").permitAll()
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
