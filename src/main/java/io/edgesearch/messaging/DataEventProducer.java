package io.edgesearch.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.edgesearch.dto.DataEvent;
import io.quarkus.logging.Log;
import io.smallrye.faulttolerance.api.CircuitBreakerName;
import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.time.temporal.ChronoUnit;

/**
 * Producer Kafka responsável por enviar eventos de dados para o tópico Kafka.
 *
 * Utiliza SmallRye Reactive Messaging para publicar mensagens no tópico 'data_events'.
 * Os eventos são serializados para JSON usando Jackson ObjectMapper antes do envio.
 *
 * IMPORTANTE: Este producer possui Circuit Breaker para lidar com falhas do Kafka.
 *
 * Com Circuit Breaker:
 * - Quando Kafka falha, o circuit abre e usa fallback
 * - Mensagens são apenas logadas, não causam falha na aplicação
 * - Aplicação continua processando normalmente
 * - Após período, tenta reconectar automaticamente
 *
 * @author Edge Search
 * @version 2.0
 */
@ApplicationScoped
public class DataEventProducer {

    /**
     * Emitter para enviar mensagens ao canal Kafka 'data-events-out'.
     */
    @Channel("data-events-out")
    Emitter<Record<String, String>> emitter;

    /**
     * ObjectMapper do Jackson para serialização JSON.
     */
    @Inject
    ObjectMapper objectMapper;

    /**
     * Envia um evento de dados para o tópico Kafka com Circuit Breaker.
     *
     * Circuit Breaker configurado:
     * - requestVolumeThreshold: 4 requisições antes de avaliar
     * - failureRatio: 0.5 (50% de falhas abre o circuito)
     * - delay: 10000ms (10s) antes de tentar novamente
     * - successThreshold: 2 sucessos consecutivos para fechar
     *
     * @param event o evento a ser enviado
     */
    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(
        requestVolumeThreshold = 4,
        failureRatio = 0.5,
        delay = 10000,
        successThreshold = 2
    )
    @CircuitBreakerName("kafka-producer")
    @Fallback(fallbackMethod = "fallbackSendDataEvent")
    public void sendDataEvent(DataEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            emitter.send(Record.of("data-key", json));
            Log.infof("Evento enviado para Kafka: %s", event.getTitle());
        } catch (JsonProcessingException e) {
            Log.errorf("Erro ao serializar evento: %s", e.getMessage());
            throw new RuntimeException("Falha ao serializar evento", e);
        } catch (Exception e) {
            Log.errorf("Erro ao enviar evento para Kafka: %s", e.getMessage());
            throw new RuntimeException("Falha ao enviar evento para Kafka", e);
        }
    }

    /**
     * Fallback quando Circuit Breaker está aberto ou Kafka indisponível.
     *
     * Este método é chamado quando:
     * - Circuit breaker está OPEN (muitas falhas)
     * - Kafka está temporariamente indisponível
     * - Timeout é excedido
     *
     * Comportamento: Apenas loga a mensagem, não lança exceção.
     * A aplicação continua funcionando normalmente.
     *
     * @param event o evento que não pôde ser enviado
     */
    public void fallbackSendDataEvent(DataEvent event) {
        Log.warnf("Circuit breaker OPEN - Evento NÃO enviado para Kafka (será apenas logado): title=%s, timestamp=%s",
            event.getTitle(),
            event.getTimestamp());
        // NÃO lança exceção - permite que aplicação continue processando
    }
}
