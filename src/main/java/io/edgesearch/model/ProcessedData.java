package io.edgesearch.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade JPA que representa dados processados pela aplicação.
 *
 * Esta entidade armazena informações de títulos obtidos de fontes externas
 * e persistidos no banco de dados PostgreSQL.
 *
 * @author Edge Search
 * @version 1.0
 */
@Entity
@Table(name = "processed_data")
public class ProcessedData {

    /**
     * Identificador único do registro.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Título do dado processado, obtido da API externa.
     */
    @Column(nullable = false)
    private String title;

    /**
     * Data e hora em que o dado foi processado.
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * Construtor padrão requerido pelo JPA.
     */
    public ProcessedData() {
    }

    /**
     * Construtor para criar um novo dado processado.
     *
     * @param title o título a ser armazenado
     */
    public ProcessedData(String title) {
        this.title = title;
        this.processedAt = LocalDateTime.now();
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

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
