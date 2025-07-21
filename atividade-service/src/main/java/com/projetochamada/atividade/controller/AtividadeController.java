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

    // POST /atividades
    @PostMapping
    public ResponseEntity<AtividadeResponse> criarAtividade(
            @RequestBody AtividadeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long professorId = extractUserIdFromUserDetails(userDetails); // Implementar este método

            AtividadeResponse response = atividadeService.criarAtividade(request, professorId);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // GET /atividades/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AtividadeResponse> buscarAtividadePorId(@PathVariable Long id) {
        return atividadeService.buscarAtividadePorId(id)
                .map(atividade -> new ResponseEntity<>(atividade, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // GET /atividades
    @GetMapping
    public ResponseEntity<List<AtividadeResponse>> listarTodasAtividades() {
        List<AtividadeResponse> atividades = atividadeService.listarTodasAtividades();
        return new ResponseEntity<>(atividades, HttpStatus.OK);
    }

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

    // DELETE /atividades/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirAtividade(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long professorId = extractUserIdFromUserDetails(userDetails);
            if (atividadeService.excluirAtividade(id, professorId)) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    private Long extractUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails.getUsername().equals("professor1")) {
            return 1L;
        }
        // Retorna um ID padrão ou lança uma exceção se não for um professor conhecido
        return -1L;
    }
}