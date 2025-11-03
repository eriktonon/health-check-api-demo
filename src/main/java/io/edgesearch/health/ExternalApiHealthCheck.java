package io.edgesearch.health;

import io.edgesearch.client.JsonPlaceholderClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Health check para verificar a disponibilidade da API externa JSONPlaceholder.
 *
 * Este check é marcado como @Liveness, indicando que verifica se a aplicação
 * está viva e funcionando corretamente.
 *
 * @author Edge Search
 * @version 1.0
 */
@Liveness
@ApplicationScoped
public class ExternalApiHealthCheck implements HealthCheck {

    @Inject
    @RestClient
    JsonPlaceholderClient jsonPlaceholderClient;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("External API health check");

        try {
            // Tenta buscar um TODO simples para verificar se a API está respondendo
            jsonPlaceholderClient.getTodoById(1L);
            
            responseBuilder
                .up()
                .withData("api", "JSONPlaceholder")
                .withData("url", "https://jsonplaceholder.typicode.com")
                .withData("status", "available");
        } catch (Exception e) {
            responseBuilder
                .down()
                .withData("api", "JSONPlaceholder")
                .withData("url", "https://jsonplaceholder.typicode.com")
                .withData("error", e.getMessage())
                .withData("status", "unavailable");
        }

        return responseBuilder.build();
    }
}
