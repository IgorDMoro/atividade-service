package com.projetochamada.atividade.service;

import com.projetochamada.atividade.dto.AtividadeRequest;
import com.projetochamada.atividade.dto.AtividadeResponse;
import com.projetochamada.atividade.model.Atividade;
import com.projetochamada.atividade.repository.AtividadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AtividadeService {

    private final AtividadeRepository atividadeRepository;

    public AtividadeService(AtividadeRepository atividadeRepository) {
        this.atividadeRepository = atividadeRepository;
    }

    @Transactional
    public AtividadeResponse criarAtividade(AtividadeRequest request, Long professorId) {
        Atividade novaAtividade = new Atividade();
        novaAtividade.setTitulo(request.getTitulo());
        novaAtividade.setDescricao(request.getDescricao());
        novaAtividade.setDataCriacao(LocalDateTime.now()); // Data de criação automática
        novaAtividade.setDataEntrega(request.getDataEntrega());
        novaAtividade.setProfessorId(professorId); // Associa ao professor autenticado

        Atividade savedAtividade = atividadeRepository.save(novaAtividade);
        return convertToResponse(savedAtividade);
    }

    public Optional<AtividadeResponse> buscarAtividadePorId(Long id) {
        return atividadeRepository.findById(id)
                .map(this::convertToResponse);
    }

    public List<AtividadeResponse> listarTodasAtividades() {
        return atividadeRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<AtividadeResponse> editarAtividade(Long id, AtividadeRequest request, Long professorId) {
        return atividadeRepository.findById(id)
                .map(atividadeExistente -> {
                    // Verifica se o professor autenticado é o criador da atividade
                    if (!atividadeExistente.getProfessorId().equals(professorId)) {
                        throw new RuntimeException("Você não tem permissão para editar esta atividade.");
                    }

                    atividadeExistente.setTitulo(request.getTitulo());
                    atividadeExistente.setDescricao(request.getDescricao());
                    atividadeExistente.setDataEntrega(request.getDataEntrega());

                    Atividade updatedAtividade = atividadeRepository.save(atividadeExistente);
                    return convertToResponse(updatedAtividade);
                });
    }

    @Transactional
    public boolean excluirAtividade(Long id, Long professorId) {
        Optional<Atividade> atividadeOptional = atividadeRepository.findById(id);
        if (atividadeOptional.isPresent()) {
            Atividade atividade = atividadeOptional.get();
            // Verifica se o professor autenticado é o criador da atividade
            if (!atividade.getProfessorId().equals(professorId)) {
                throw new RuntimeException("Você não tem permissão para excluir esta atividade.");
            }
            atividadeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Método utilitário para converter Entidade Atividade em AtividadeResponse DTO
    private AtividadeResponse convertToResponse(Atividade atividade) {
        AtividadeResponse response = new AtividadeResponse();
        response.setId(atividade.getId());
        response.setTitulo(atividade.getTitulo());
        response.setDescricao(atividade.getDescricao());
        response.setDataCriacao(atividade.getDataCriacao());
        response.setDataEntrega(atividade.getDataEntrega());
        response.setProfessorId(atividade.getProfessorId());
        return response;
    }
}