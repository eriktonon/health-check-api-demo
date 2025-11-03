package io.edgesearch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO (Data Transfer Object) para mapear a resposta da API JSONPlaceholder.
 *
 * Representa um TODO retornado pela API externa https://jsonplaceholder.typicode.com/todos
 * A anotação @JsonIgnoreProperties permite ignorar propriedades desconhecidas durante a deserialização.
 *
 * @author Edge Search
 * @version 1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TodoResponse {

    /**
     * ID do usuário proprietário do TODO.
     */
    private Long userId;

    /**
     * ID único do TODO.
     */
    private Long id;

    /**
     * Título/descrição do TODO.
     */
    private String title;

    /**
     * Status de conclusão do TODO.
     */
    private Boolean completed;

    /**
     * Construtor padrão.
     */
    public TodoResponse() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
