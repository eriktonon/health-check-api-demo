package io.edgesearch.dto;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) que representa um evento de dados enviado ao Kafka.
 *
 * Esta classe encapsula informações sobre dados processados que serão enviados
 * como mensagens para o tópico Kafka 'data_events'.
 *
 * @author Edge Search
 * @version 1.0
 */
public class DataEvent {

    /**
     * Título do dado processado.
     */
    private String title;

    /**
     * Timestamp de quando o evento foi criado.
     */
    private LocalDateTime timestamp;

    /**
     * Construtor padrão requerido para deserialização JSON.
     */
    public DataEvent() {
    }

    /**
     * Construtor para criar um novo evento de dados.
     *
     * @param title o título a ser incluído no evento
     */
    public DataEvent(String title) {
        this.title = title;
        this.timestamp = LocalDateTime.now();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "DataEvent{" +
                "title='" + title + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
