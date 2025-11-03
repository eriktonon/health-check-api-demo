package io.edgesearch.service;

import io.edgesearch.dto.PageResponse;
import io.edgesearch.model.ProcessedData;
import io.edgesearch.repository.ProcessedDataRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável por consultas de dados processados.
 *
 * Fornece operações de leitura com suporte a paginação e ordenação.
 *
 * @author Edge Search
 * @version 1.0
 */
@ApplicationScoped
public class ProcessedDataQueryService {

    @Inject
    ProcessedDataRepository repository;

    /**
     * Lista todos os dados processados com paginação.
     *
     * @param page número da página (começando em 0)
     * @param size tamanho da página
     * @return resposta paginada com os dados processados
     */
    public PageResponse<ProcessedData> findAll(int page, int size) {
        // Garantir valores mínimos
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100; // Limitar tamanho máximo

        // Buscar dados paginados ordenados por data decrescente
        List<ProcessedData> content = repository
                .findAll(Sort.by("processedAt").descending())
                .page(Page.of(page, size))
                .list();

        // Contar total de elementos
        long totalElements = repository.count();

        return new PageResponse<>(content, page, size, totalElements);
    }

    /**
     * Busca um dado processado por ID.
     *
     * @param id o identificador do registro
     * @return Optional contendo o dado processado se encontrado
     */
    public Optional<ProcessedData> findById(Long id) {
        return repository.findByIdOptional(id);
    }

    /**
     * Lista os dados processados mais recentes.
     *
     * @param limit quantidade máxima de registros
     * @return lista dos dados mais recentes
     */
    public List<ProcessedData> findRecent(int limit) {
        if (limit < 1) limit = 10;
        if (limit > 100) limit = 100;

        return repository
                .findAll(Sort.by("processedAt").descending())
                .page(Page.ofSize(limit))
                .list();
    }

    /**
     * Conta o total de dados processados.
     *
     * @return total de registros
     */
    public long count() {
        return repository.count();
    }
}
