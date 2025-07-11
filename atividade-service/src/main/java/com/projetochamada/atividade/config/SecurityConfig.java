package com.projetochamada.atividade.config;

import com.projetochamada.atividade.security.jwt.JwtAuthenticationFilter; // Importe o filtro JWT
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections; // Importe se usar Collections.singletonList ou emptyList

@Configuration
@EnableWebSecurity // Habilita a segurança web do Spring
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    // Injeta o filtro JWT que criamos
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Habilita CORS com a configuração customizada
                .csrf(csrf -> csrf.disable()) // Desabilita CSRF para APIs REST sem estado
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Configura sessão sem estado
                .authorizeHttpRequests(auth -> auth
                        // Permite requisições OPTIONS (preflight CORS) para qualquer caminho
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**", "/h2-console-atividade/**").permitAll()
                        // Regras para os endpoints de Atividades
                        // PROFESSOR pode criar, editar e excluir
                        .requestMatchers(HttpMethod.POST, "/atividades").hasRole("PROFESSOR")
                        .requestMatchers(HttpMethod.PUT, "/atividades/**").hasRole("PROFESSOR")
                        .requestMatchers(HttpMethod.DELETE, "/atividades/**").hasRole("PROFESSOR")
                        // ALUNO e PROFESSOR podem visualizar
                        .requestMatchers(HttpMethod.GET, "/atividades/**").hasAnyRole("PROFESSOR", "ALUNO")
                        .requestMatchers(HttpMethod.GET, "/atividades").hasAnyRole("PROFESSOR", "ALUNO")
                        // Todas as outras requisições devem ser autenticadas
                        .anyRequest().authenticated()
                )
                // Adiciona o filtro JWT antes do filtro de autenticação padrão do Spring Security
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Habilita as opções de cabeçalho, permitindo iframes do mesmo domínio (útil para H2 console)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permite o frontend rodando em localhost:4200 (ajuste se for diferente)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        // Permite os métodos HTTP que serão usados
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Permite os cabeçalhos Authorization (para JWT) e Content-Type
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true); // Permite credenciais (cookies, headers de autorização)
        configuration.setMaxAge(3600L); // Tempo máximo em segundos para cachear resultados de preflight

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica a configuração para todas as rotas
        return source;
    }

    // Você não precisa de um PasswordEncoder ou AuthenticationManager aqui,
    // pois a autenticação é feita pelo serviço principal.
}