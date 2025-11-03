package io.edgesearch.repository;

import io.edgesearch.model.ProcessedData;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repositório para operações de persistência de ProcessedData.
 *
 * Utiliza Panache Repository para fornecer métodos CRUD prontos sem necessidade
 * de implementação adicional. Panache simplifica o acesso a dados no Quarkus.
 *
 * Métodos disponíveis incluem: persist, findById, listAll, delete, entre outros.
 *
 * @author Edge Search
 * @version 1.0
 */
@ApplicationScoped
public class ProcessedDataRepository implements PanacheRepository<ProcessedData> {
}
