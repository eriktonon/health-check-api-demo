package io.edgesearch.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.edgesearch.dto.DataEvent;
import io.quarkus.logging.Log;
import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

/**
 * Consumer Kafka responsável por consumir eventos de dados do tópico Kafka.
 *
 * Utiliza SmallRye Reactive Messaging para escutar mensagens do tópico 'data_events'.
 * As mensagens JSON são deserializadas para objetos DataEvent usando Jackson ObjectMapper.
 *
 * @author Edge Search
 * @version 1.0
 */
@ApplicationScoped
public class DataEventConsumer {

    /**
     * ObjectMapper do Jackson para deserialização JSON.
     */
    @Inject
    ObjectMapper objectMapper;

    /**
     * Consome mensagens do tópico Kafka e processa eventos de dados.
     *
     * Este método é chamado automaticamente sempre que uma nova mensagem
     * chega no canal 'data-events-in'.
     *
     * @param record o registro Kafka contendo a mensagem em formato JSON
     */
    @Incoming("data-events-in")
    public void consume(Record<String, String> record) {
        try {
            String json = record.value();
            DataEvent event = objectMapper.readValue(json, DataEvent.class);
            Log.infof("Mensagem recebida do Kafka: %s", event);
        } catch (Exception e) {
            Log.errorf("Erro ao deserializar evento: %s", e.getMessage());
        }
    }
}
