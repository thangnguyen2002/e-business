package com.example.shopapp.configs;

import com.example.shopapp.models.Role;
import com.example.shopapp.repositories.UserRepository;
import com.example.shopapp.shared.filters.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Value("${api.prefix}")
    private String apiPrefix;
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, JwtAuthFilter jwtAuthFilter) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorization -> authorization
                                .requestMatchers(apiPrefix + "/users/register").permitAll()
                                .requestMatchers(apiPrefix + "/users/login").permitAll()

                                .requestMatchers(HttpMethod.GET, apiPrefix + "/categories/**").hasAnyRole(Role.USER, Role.ADMIN)
                                .requestMatchers(HttpMethod.POST, apiPrefix + "/categories/**").hasRole(Role.ADMIN)
                                .requestMatchers(HttpMethod.PUT, apiPrefix + "/categories/**").hasRole(Role.ADMIN)
                                .requestMatchers(HttpMethod.DELETE, apiPrefix + "/categories/**").hasRole(Role.ADMIN)

                                .requestMatchers(HttpMethod.GET, apiPrefix + "/products/**").hasAnyRole(Role.USER, Role.ADMIN)
                                .requestMatchers(HttpMethod.POST, apiPrefix + "apiPrefix + /products/**").hasRole(Role.ADMIN)
                                .requestMatchers(HttpMethod.PUT, apiPrefix + "/products/**").hasRole(Role.ADMIN)
                                .requestMatchers(HttpMethod.DELETE, apiPrefix + "/products/**").hasRole(Role.ADMIN)

                                .requestMatchers(HttpMethod.GET, apiPrefix + "/orders/**").hasAnyRole(Role.USER, Role.ADMIN)
                                .requestMatchers(HttpMethod.POST, apiPrefix + "/orders/**").hasRole(Role.USER)
                                .requestMatchers(HttpMethod.PUT, apiPrefix + "/orders/**").hasRole(Role.ADMIN)
                                .requestMatchers(HttpMethod.DELETE, apiPrefix + "/orders/**").hasRole(Role.ADMIN)

                                .requestMatchers(HttpMethod.GET, apiPrefix + "/order_details/**").hasAnyRole(Role.USER, Role.ADMIN)
                                .requestMatchers(HttpMethod.POST, apiPrefix + "/order_details/**").hasRole(Role.USER)
                                .requestMatchers(HttpMethod.PUT, apiPrefix + "/order_details/**").hasRole(Role.ADMIN)
                                .requestMatchers(HttpMethod.DELETE, apiPrefix + "/order_details/**").hasRole(Role.ADMIN)
                                .requestMatchers(HttpMethod.DELETE, apiPrefix + "/order_details/**").hasRole(Role.ADMIN)

                                .anyRequest().authenticated()
                ).addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
