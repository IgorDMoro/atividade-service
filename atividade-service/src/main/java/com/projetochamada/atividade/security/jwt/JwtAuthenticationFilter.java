package com.projetochamada.atividade.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Importe esta classe
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections; // Importe para Collections.emptyList()

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // O construtor deste filtro no microserviço de atividades não precisa de UserDetailsServiceImpl
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Verifica se o cabeçalho Authorization existe e começa com "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Continua a cadeia de filtros
            return;
        }

        // Extrai o token JWT
        jwt = authHeader.substring(7);
        username = jwtUtil.extractUsername(jwt);

        // Se o username foi extraído e não há autenticação no contexto de segurança atual
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Extrai o ID do usuário e as roles dos claims do JWT
            // Estas claims foram adicionadas no JwtUtil do serviço principal
            Long userId = jwtUtil.extractAllClaims(jwt).get("userId", Long.class);
            List<String> userRoles = jwtUtil.extractAllClaims(jwt).get("roles", List.class);

            // Se o ID do usuário não estiver no token (erro ou token malformado), continue sem autenticar
            if (userId == null) {
                System.err.println("JWT does not contain 'userId' claim. Cannot authenticate.");
                filterChain.doFilter(request, response);
                return;
            }

            // Garante que a lista de roles não é nula
            if (userRoles == null) {
                userRoles = Collections.emptyList();
            }

            // Cria um objeto UserDetails com base nas informações do token
            // A senha é vazia porque não há autenticação por senha neste microserviço
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    username,
                    "", // Senha dummy ou vazia, pois a autenticação já foi feita via token
                    userRoles.stream()
                            .map(SimpleGrantedAuthority::new) // Converte strings de role para GrantedAuthority
                            .collect(Collectors.toList())
            );

            // Valida o token (expiração e username)
            if (jwtUtil.validateToken(jwt, userDetails)) {
                // Cria um UsernamePasswordAuthenticationToken para setar no SecurityContext
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, // O UserDetails contém username e autoridades (roles)
                        null,        // Credenciais (senha) não são mais necessárias
                        userDetails.getAuthorities() // As autoridades vêm do UserDetails
                );
                // Adiciona detalhes da requisição (como IP do cliente) ao token de autenticação
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Define o token de autenticação no SecurityContextHolder do Spring
                // Isso permite que o Spring Security saiba quem é o usuário autenticado para as verificações de @PreAuthorize
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // *** PASSO CRUCIAL: Passe o userId extraído do JWT para o controlador via atributo da requisição ***
                // O controlador poderá recuperar este ID para vincular atividades ao professor.
                request.setAttribute("authenticatedUserId", userId);
            }
        }
        filterChain.doFilter(request, response); // Continua a cadeia de filtros
    }
}