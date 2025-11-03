package io.edgesearch.service;

import io.edgesearch.client.JsonPlaceholderClient;
import io.edgesearch.dto.DataEvent;
import io.edgesearch.dto.TodoResponse;
import io.edgesearch.messaging.DataEventProducer;
import io.edgesearch.model.ProcessedData;
import io.edgesearch.repository.ProcessedDataRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Serviço responsável pela orquestração do fluxo completo de processamento de dados.
 *
 * Este serviço implementa a arquitetura da aplicação integrando:
 * 1. Chamada à API externa JSONPlaceholder
 * 2. Persistência dos dados no PostgreSQL
 * 3. Publicação de eventos no Kafka
 *
 * O método principal executa todas as etapas em uma transação para garantir consistência.
 *
 * @author Edge Search
 * @version 1.0
 */
@ApplicationScoped
public class DataProcessingService {

    /**
     * Cliente REST para consumir a API externa JSONPlaceholder.
     */
    @Inject
    @RestClient
    JsonPlaceholderClient jsonPlaceholderClient;

    /**
     * Repositório para persistência de dados processados.
     */
    @Inject
    ProcessedDataRepository repository;

    /**
     * Producer Kafka para envio de eventos.
     */
    @Inject
    DataEventProducer eventProducer;

    /**
     * Busca dados da API externa, processa, salva no banco e publica no Kafka.
     *
     * O fluxo de processamento:
     * 1. Chama a API externa para buscar um TODO pelo ID
     * 2. Extrai o título do TODO recebido
     * 3. Persiste os dados no PostgreSQL
     * 4. Publica um evento no tópico Kafka 'data_events'
     *
     * @param todoId o identificador do TODO a ser buscado
     * @return os dados processados e salvos no banco
     */
    @Transactional
    public ProcessedData fetchAndProcessData(Long todoId) {
        // 1. Chamar API externa
        TodoResponse todoResponse = jsonPlaceholderClient.getTodoById(todoId);

        // 2. Processar dados (extrair título)
        String title = todoResponse.getTitle();

        // 3. Salvar no PostgreSQL
        ProcessedData processedData = new ProcessedData(title);
        repository.persist(processedData);

        // 4. Enviar mensagem para o Kafka
        DataEvent event = new DataEvent(title);
        eventProducer.sendDataEvent(event);

        return processedData;
    }
}
