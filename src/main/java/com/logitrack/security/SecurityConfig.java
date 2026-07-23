package com.logitrack.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            UserDetailsServiceImpl userDetailsService
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // Rutas públicas de autenticación
                        .requestMatchers("/auth/**").permitAll()

                        // Swagger/OpenAPI
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()

                        // Archivos estáticos del frontend
                        .requestMatchers("/", "/index.html", "/login.html", "/dashboard.html").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()

                        // Bodegas - Consultas para ADMIN y EMPLEADO
                        .requestMatchers(HttpMethod.GET, "/bodegas/**").hasAnyRole("ADMIN", "EMPLEADO")

                        // Bodegas - Cambios solo ADMIN
                        .requestMatchers(HttpMethod.POST, "/bodegas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/bodegas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/bodegas/**").hasRole("ADMIN")

                        // Productos - Consultas para ADMIN y EMPLEADO
                        .requestMatchers(HttpMethod.GET, "/productos/**").hasAnyRole("ADMIN", "EMPLEADO")

                        // Productos - Cambios solo ADMIN
                        .requestMatchers(HttpMethod.POST, "/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/productos/**").hasRole("ADMIN")

                        // Movimientos - Lo dejamos listo para la siguiente fase
                        .requestMatchers("/movimientos/**").hasAnyRole("ADMIN", "EMPLEADO")

                        // Auditorías y reportes - Solo ADMIN
                        .requestMatchers("/auditorias/**").hasRole("ADMIN")
                        .requestMatchers("/reportes/**").hasRole("ADMIN")

                        // Cualquier otra ruta necesita autenticación
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}