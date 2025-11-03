package io.edgesearch.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Health check para verificar a conectividade com o banco de dados PostgreSQL.
 *
 * Este check é marcado como @Readiness, indicando que a aplicação só está pronta
 * para receber tráfego quando o banco de dados estiver acessível.
 *
 * @author Edge Search
 * @version 1.0
 */
@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    @Inject
    DataSource dataSource;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Database connection health check");

        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5); // timeout de 5 segundos
            
            if (isValid) {
                responseBuilder
                    .up()
                    .withData("database", "PostgreSQL")
                    .withData("status", "connected");
            } else {
                responseBuilder
                    .down()
                    .withData("database", "PostgreSQL")
                    .withData("status", "connection invalid");
            }
        } catch (Exception e) {
            responseBuilder
                .down()
                .withData("database", "PostgreSQL")
                .withData("error", e.getMessage());
        }

        return responseBuilder.build();
    }
}
