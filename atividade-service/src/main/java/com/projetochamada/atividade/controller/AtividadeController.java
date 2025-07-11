package com.projetochamada.atividade.controller;

import com.projetochamada.atividade.dto.AtividadeRequest;
import com.projetochamada.atividade.dto.AtividadeResponse;
import com.projetochamada.atividade.service.AtividadeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/atividades")
public class AtividadeController {

    private final AtividadeService atividadeService;

    public AtividadeController(AtividadeService atividadeService) {
        this.atividadeService = atividadeService;
    }

    // Endpoint para criar uma nova atividade (Somente PROFESSOR)
    // POST /atividades
    @PostMapping
    public ResponseEntity<AtividadeResponse> criarAtividade(
            @RequestBody AtividadeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Em um microserviço real, você precisaria de um mecanismo para obter o ID do professor
            // a partir do username do token. Por simplicidade, vamos assumir que o ID do professor
            // pode ser obtido de alguma forma (ex: de um serviço de usuário ou um campo customizado no JWT).
            // Por enquanto, usaremos um valor mock ou o próprio username como ID, ou você precisará
            // de um endpoint no seu backend Auth que retorne o ID do usuário dado o username.
            // Para este exemplo, usaremos o ID 1L como professorId mock.
            // No futuro, você pode expandir userDetails para ter o ID do usuário diretamente.
            Long professorId = extractUserIdFromUserDetails(userDetails); // Implementar este método

            AtividadeResponse response = atividadeService.criarAtividade(request, professorId);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Endpoint para buscar uma atividade por ID (PROFESSOR e ALUNO)
    // GET /atividades/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AtividadeResponse> buscarAtividadePorId(@PathVariable Long id) {
        return atividadeService.buscarAtividadePorId(id)
                .map(atividade -> new ResponseEntity<>(atividade, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Endpoint para listar todas as atividades (PROFESSOR e ALUNO)
    // GET /atividades
    @GetMapping
    public ResponseEntity<List<AtividadeResponse>> listarTodasAtividades() {
        List<AtividadeResponse> atividades = atividadeService.listarTodasAtividades();
        return new ResponseEntity<>(atividades, HttpStatus.OK);
    }

    // Endpoint para editar uma atividade (Somente PROFESSOR que a criou)
    // PUT /atividades/{id}
    @PutMapping("/{id}")
    public ResponseEntity<AtividadeResponse> editarAtividade(
            @PathVariable Long id,
            @RequestBody AtividadeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long professorId = extractUserIdFromUserDetails(userDetails); // Implementar este método
            return atividadeService.editarAtividade(id, request, professorId)
                    .map(atividade -> new ResponseEntity<>(atividade, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (RuntimeException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.FORBIDDEN); // Ou BAD_REQUEST
        }
    }

    // Endpoint para excluir uma atividade (Somente PROFESSOR que a criou)
    // DELETE /atividades/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirAtividade(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long professorId = extractUserIdFromUserDetails(userDetails); // Implementar este método
            if (atividadeService.excluirAtividade(id, professorId)) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    // IMPORTANTE: Este é um método placeholder. No seu projeto real, você precisará
    // obter o ID do usuário (professor) de forma segura do token JWT ou de um
    // serviço de usuário. O UserDetails padrão do Spring Security não expõe o ID.
    // Uma solução comum é:
    // 1. Incluir o ID do usuário como um "claim" customizado no JWT no seu microserviço de autenticação.
    // 2. Criar uma classe customizada que implementa UserDetails e que contém o ID do usuário.
    // 3. Modificar o JwtAuthenticationFilter para extrair este ID do token e setá-lo no seu UserDetails customizado.
    private Long extractUserIdFromUserDetails(UserDetails userDetails) {
        // Exemplo MOCK para testes iniciais:
        // Se o username for "professor1", retorna 1L. Isso DEVE ser substituído por uma lógica real.
        if (userDetails.getUsername().equals("professor1")) {
            return 1L;
        }
        // Retorna um ID padrão ou lança uma exceção se não for um professor conhecido
        return -1L; // Indique um erro ou obtenha o ID real
        // Ou, se o ID do professor for o próprio username (se for numérico), você pode fazer:
        // try {
        //     return Long.parseLong(userDetails.getUsername());
        // } catch (NumberFormatException e) {
        //     throw new RuntimeException("Não foi possível extrair o ID do professor do token.");
        // }
    }
}