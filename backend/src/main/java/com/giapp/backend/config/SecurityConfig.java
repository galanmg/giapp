package com.giapp.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFiltro jwtFiltro;
    private final ManejadorErroresSeguridad manejadorErroresSeguridad;

    public SecurityConfig(JwtFiltro jwtFiltro,
                          ManejadorErroresSeguridad manejadorErroresSeguridad) {
        this.jwtFiltro = jwtFiltro;
        this.manejadorErroresSeguridad = manejadorErroresSeguridad;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sesion -> sesion
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(excepciones -> excepciones
                        .authenticationEntryPoint(manejadorErroresSeguridad)
                        .accessDeniedHandler(manejadorErroresSeguridad))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/perfil/vehiculos/**").authenticated()
                        .requestMatchers("/api/perfil/vehiculos/**").hasRole("CLIENTE")
                        .requestMatchers("/api/perfil/**").authenticated()
                        .requestMatchers("/api/vehiculos/**").hasRole("GESTOR")
                        .requestMatchers("/api/clientes/**").hasRole("GESTOR")
                        .requestMatchers("/api/aparcamientos/**").hasRole("GESTOR")
                        .requestMatchers("/api/plantas/**").hasRole("GESTOR")
                        .requestMatchers("/api/tarifas/**").hasRole("GESTOR")
                        .requestMatchers("/api/recibos/**").hasRole("GESTOR")
                        .requestMatchers("/api/promociones/**").hasRole("GESTOR")
                        .requestMatchers("/api/plazas/**").hasRole("GESTOR")
                        .requestMatchers("/api/contratos/**").hasRole("GESTOR")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();

        configuracion.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);

        return fuente;
    }
}