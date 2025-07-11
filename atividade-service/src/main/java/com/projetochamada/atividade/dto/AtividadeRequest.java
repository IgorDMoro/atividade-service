package com.projetochamada.atividade.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtividadeRequest {
    private String titulo;
    private String descricao;
    private LocalDateTime dataEntrega;
    // Não incluir professorId aqui, ele virá do token do usuário autenticado
}