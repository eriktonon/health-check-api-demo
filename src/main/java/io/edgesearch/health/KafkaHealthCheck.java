package io.edgesearch.health;

import io.smallrye.faulttolerance.api.CircuitBreakerName;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

import java.time.temporal.ChronoUnit;
import java.util.Properties;

/**
 * Health check para verificar a conectividade com o Apache Kafka.
 *
 * IMPORTANTE: Este check foi alterado de @Readiness para @Liveness com Circuit Breaker.
 *
 * Com Circuit Breaker:
 * - Quando Kafka falha, o circuit abre e usa fallback
 * - Aplicação reporta problema mas CONTINUA FUNCIONANDO
 * - Health check retorna DOWN mas não remove pod do service
 * - Após período, tenta reconectar automaticamente
 *
 * @author Edge Search
 * @version 2.0
 */
@Liveness  // Alterado de @Readiness para @Liveness
@ApplicationScoped
public class KafkaHealthCheck implements HealthCheck {

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String bootstrapServers;

    private volatile boolean kafkaAvailable = true;
    private volatile String lastError = null;

    @Override
    public HealthCheckResponse call() {
        // Tenta verificar Kafka com circuit breaker
        checkKafkaConnection();

        HealthCheckResponseBuilder responseBuilder =
            HealthCheckResponse.named("Kafka connection health check");

        if (kafkaAvailable) {
            responseBuilder
                .up()
                .withData("broker", bootstrapServers)
                .withData("status", "connected")
                .withData("circuitBreaker", "CLOSED");
        } else {
            // Kafka indisponível mas aplicação continua funcionando
            responseBuilder
                .down()
                .withData("broker", bootstrapServers)
                .withData("status", "disconnected")
                .withData("circuitBreaker", "OPEN")
                .withData("error", lastError != null ? lastError : "Connection failed")
                .withData("impact", "Messages will be logged but not sent to Kafka");
        }

        return responseBuilder.build();
    }

    /**
     * Verifica conexão com Kafka usando Circuit Breaker.
     *
     * Circuit Breaker configurado:
     * - requestVolumeThreshold: 4 requisições antes de avaliar
     * - failureRatio: 0.5 (50% de falhas abre o circuito)
     * - delay: 10000ms (10s) antes de tentar novamente
     * - successThreshold: 2 sucessos consecutivos para fechar
     */
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(
        requestVolumeThreshold = 4,
        failureRatio = 0.5,
        delay = 10000,
        successThreshold = 2
    )
    @CircuitBreakerName("kafka-health-check")
    @Fallback(fallbackMethod = "fallbackKafkaCheck")
    public void checkKafkaConnection() {
        try {
            Properties props = new Properties();
            props.put("bootstrap.servers", bootstrapServers);
            props.put("client.id", "health-check-client");
            props.put("connections.max.idle.ms", "10000");
            props.put("request.timeout.ms", "5000");

            try (org.apache.kafka.clients.admin.AdminClient adminClient =
                    org.apache.kafka.clients.admin.AdminClient.create(props)) {

                adminClient.listTopics()
                    .listings()
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);

                kafkaAvailable = true;
                lastError = null;
            }
        } catch (Exception e) {
            kafkaAvailable = false;
            lastError = e.getMessage();
            throw new RuntimeException("Kafka unavailable: " + e.getMessage(), e);
        }
    }

    /**
     * Fallback quando Circuit Breaker está aberto.
     *
     * Este método é chamado quando:
     * - Circuit breaker está OPEN (muitas falhas)
     * - Kafka está temporariamente indisponível
     *
     * Comportamento: Apenas registra o problema, não lança exceção.
     */
    public void fallbackKafkaCheck() {
        kafkaAvailable = false;
        if (lastError == null) {
            lastError = "Circuit breaker is OPEN - too many failures";
        }
        // NÃO lança exceção - permite que aplicação continue
    }
}
