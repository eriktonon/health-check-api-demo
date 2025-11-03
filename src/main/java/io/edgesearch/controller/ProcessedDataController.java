package io.edgesearch.controller;

import io.edgesearch.dto.PageResponse;
import io.edgesearch.model.ProcessedData;
import io.edgesearch.service.ProcessedDataQueryService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

/**
 * Controller REST para consultas de dados processados.
 *
 * Fornece endpoints para listar e buscar dados processados salvos no banco de dados,
 * com suporte a paginação.
 *
 * @author Edge Search
 * @version 1.0
 */
@Path("/processed-data")
@Tag(name = "Processed Data", description = "Consultas de dados processados")
public class ProcessedDataController {

    @Inject
    ProcessedDataQueryService queryService;

    /**
     * Lista todos os dados processados com paginação.
     *
     * @param page número da página (padrão: 0)
     * @param size tamanho da página (padrão: 10, máximo: 100)
     * @return resposta paginada com os dados processados
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Listar dados processados",
        description = "Retorna uma lista paginada de todos os dados processados, ordenados por data de processamento (mais recentes primeiro)"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Lista de dados processados retornada com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = PageResponse.class)
            )
        )
    })
    public PageResponse<ProcessedData> list(
        @Parameter(description = "Número da página (começando em 0)", example = "0")
        @QueryParam("page") @DefaultValue("0") int page,
        
        @Parameter(description = "Tamanho da página (1-100)", example = "10")
        @QueryParam("size") @DefaultValue("10") int size
    ) {
        return queryService.findAll(page, size);
    }

    /**
     * Busca um dado processado por ID.
     *
     * @param id identificador do registro
     * @return o dado processado ou 404 se não encontrado
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Buscar dado processado por ID",
        description = "Retorna um único registro de dado processado pelo seu identificador"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Dado processado encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ProcessedData.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Dado processado não encontrado"
        )
    })
    public Response getById(
        @Parameter(description = "ID do dado processado", example = "1", required = true)
        @PathParam("id") Long id
    ) {
        return queryService.findById(id)
                .map(data -> Response.ok(data).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    /**
     * Lista os dados processados mais recentes.
     *
     * @param limit quantidade de registros (padrão: 10, máximo: 100)
     * @return lista dos dados mais recentes
     */
    @GET
    @Path("/recent")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Listar dados mais recentes",
        description = "Retorna os dados processados mais recentes, sem paginação"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Lista de dados mais recentes retornada com sucesso"
        )
    })
    public List<ProcessedData> recent(
        @Parameter(description = "Quantidade de registros (1-100)", example = "10")
        @QueryParam("limit") @DefaultValue("10") int limit
    ) {
        return queryService.findRecent(limit);
    }

    /**
     * Retorna estatísticas sobre os dados processados.
     *
     * @return objeto com estatísticas
     */
    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Estatísticas de dados processados",
        description = "Retorna informações estatísticas sobre os dados processados"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Estatísticas retornadas com sucesso"
        )
    })
    public Response stats() {
        long total = queryService.count();
        return Response.ok()
                .entity(new Stats(total))
                .build();
    }

    /**
     * Classe interna para resposta de estatísticas.
     */
    public static class Stats {
        public long totalRecords;

        public Stats(long totalRecords) {
            this.totalRecords = totalRecords;
        }
    }
}
