package io.edgesearch.controller;

import io.edgesearch.model.ProcessedData;
import io.edgesearch.service.DataProcessingService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Controller REST que expõe o endpoint principal da aplicação.
 *
 * Este controller fornece o endpoint /fetch que inicia o fluxo completo de:
 * - Busca de dados de API externa
 * - Persistência no banco de dados
 * - Publicação de eventos no Kafka
 *
 * @author Edge Search
 * @version 1.0
 */
@Path("/fetch")
@Tag(name = "Data Processing", description = "Operações de processamento e integração de dados")
public class DataController {

    /**
     * Serviço de processamento de dados.
     */
    @Inject
    DataProcessingService dataProcessingService;

    /**
     * Endpoint GET que busca e processa dados de uma fonte externa.
     *
     * Este endpoint:
     * 1. Aceita um parâmetro opcional 'todoId'
     * 2. Usa o ID 1 como padrão se nenhum for fornecido
     * 3. Delega o processamento para o DataProcessingService
     * 4. Retorna os dados processados em formato JSON
     *
     * Exemplo de uso:
     * - GET /fetch           (usa todoId=1)
     * - GET /fetch?todoId=5  (usa todoId=5)
     *
     * @param todoId o identificador do TODO a ser buscado (opcional, padrão=1)
     * @return os dados processados e salvos no banco de dados
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Buscar e processar dados externos",
        description = "Busca um TODO da API JSONPlaceholder, salva no banco de dados PostgreSQL e publica um evento no Kafka"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Dados processados com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ProcessedData.class)
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Erro interno ao processar os dados"
        )
    })
    public ProcessedData fetchData(
        @Parameter(
            description = "ID do TODO a ser buscado na API externa (padrão: 1)",
            example = "1"
        )
        @QueryParam("todoId") Long todoId
    ) {
        if (todoId == null) {
            todoId = 1L; // Default to 1 as per specification
        }
        return dataProcessingService.fetchAndProcessData(todoId);
    }
}
