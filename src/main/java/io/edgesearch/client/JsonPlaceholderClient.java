package io.edgesearch.client;

import io.edgesearch.dto.TodoResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente REST para consumir a API JSONPlaceholder.
 *
 * Interface declarativa que utiliza MicroProfile Rest Client para realizar
 * chamadas HTTP para a API externa https://jsonplaceholder.typicode.com
 *
 * A configuração da URL base é feita via application.properties usando a chave
 * 'jsonplaceholder-api'.
 *
 * @author Edge Search
 * @version 1.0
 */
@Path("/todos")
@RegisterRestClient(configKey = "jsonplaceholder-api")
public interface JsonPlaceholderClient {

    /**
     * Busca um TODO pelo seu ID.
     *
     * @param id o identificador único do TODO
     * @return um objeto TodoResponse contendo os dados do TODO
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    TodoResponse getTodoById(@PathParam("id") Long id);
}
