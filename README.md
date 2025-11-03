# Demo de Integração: API Externa, Kafka e PostgreSQL

Esta é uma aplicação demo que demonstra uma arquitetura de microsserviços integrando três componentes principais:

- **API Externa (JSONPlaceholder)**: Usada como fonte de dados
- **PostgreSQL**: Usado como banco de dados persistente
- **Apache Kafka**: Usado como broker de mensagens para comunicação assíncrona

## Arquitetura

O fluxo da aplicação é o seguinte:

1. Um usuário faz uma requisição GET para o endpoint `/fetch` da aplicação Quarkus
2. A aplicação chama a API externa (https://jsonplaceholder.typicode.com/todos/1) para buscar dados
3. A aplicação processa os dados recebidos (extrai o título)
4. Os dados são salvos no banco de dados PostgreSQL (tabela `processed_data`)
5. Uma mensagem contendo os dados é enviada para um tópico no Kafka (`data_events`)
6. Um consumer do Kafka escuta esse tópico e imprime a mensagem recebida no console

## Estrutura do Projeto

```
src/main/java/io/edgesearch/
├── client/
│   └── JsonPlaceholderClient.java      # Cliente REST para API externa
├── controller/
│   └── DataController.java             # Controller REST com endpoint /fetch
├── dto/
│   ├── DataEvent.java                  # DTO para mensagens Kafka
│   └── TodoResponse.java               # DTO para resposta da API externa
├── messaging/
│   ├── DataEventConsumer.java          # Consumer Kafka
│   └── DataEventProducer.java          # Producer Kafka
├── model/
│   └── ProcessedData.java              # Entidade JPA
├── repository/
│   └── ProcessedDataRepository.java    # Repositório Panache
└── service/
    └── DataProcessingService.java      # Serviço de processamento
```

## Pré-requisitos

- Java 21+
- Maven
- Docker e Docker Compose

## Configuração e Execução

### 1. Iniciar PostgreSQL e Kafka

```bash
docker-compose up -d
```

Isso irá iniciar:
- PostgreSQL na porta 5432
- Kafka na porta 9092
- Zookeeper na porta 2181

### 2. Executar a aplicação em modo dev

```bash
./mvnw quarkus:dev
```

> **Nota:** O Quarkus possui uma Dev UI disponível em: http://localhost:8080/q/dev/

### 3. Testar a aplicação

Faça uma requisição GET para o endpoint:

```bash
curl http://localhost:8080/fetch
```

Ou com um ID específico:

```bash
curl http://localhost:8080/fetch?todoId=2
```

### 4. Verificar os logs

No console da aplicação, você verá:
- A chamada à API externa
- A persistência no PostgreSQL
- O envio da mensagem para o Kafka
- O recebimento da mensagem pelo consumer

Exemplo de log:
```
Mensagem recebida do Kafka: DataEvent{title='delectus aut autem', timestamp=2025-11-02T...}
```

## Configuração

As configurações estão em `src/main/resources/application.properties`:

- **Banco de dados**: `jdbc:postgresql://localhost:5432/demo_db`
- **Kafka**: `localhost:9092`
- **Tópico Kafka**: `data_events`
- **API Externa**: `https://jsonplaceholder.typicode.com`

## Tecnologias Utilizadas

- **Quarkus 3.29.0**: Framework Java supersônico e subatômico
- **Hibernate ORM with Panache**: Simplificação do JPA
- **SmallRye Reactive Messaging**: Integração com Kafka
- **PostgreSQL**: Banco de dados relacional
- **Apache Kafka**: Plataforma de streaming de eventos
- **REST Client**: Cliente HTTP declarativo
- **Jackson**: Serialização/deserialização JSON

## Endpoints

### Processamento de Dados
- `GET /fetch` - Busca dados da API externa, salva no banco e envia para o Kafka
- `GET /fetch?todoId={id}` - Busca um TODO específico pelo ID

### Consulta de Dados Processados
- `GET /processed-data` - Lista todos os dados processados (com paginação)
  - Parâmetros: `?page=0&size=10`
- `GET /processed-data/{id}` - Busca um dado processado por ID
- `GET /processed-data/recent?limit=10` - Lista os dados mais recentes
- `GET /processed-data/stats` - Retorna estatísticas dos dados processados

### Health Checks
- `GET /health` - Status geral de saúde da aplicação
- `GET /health/live` - Verifica se a aplicação está viva (liveness probe)
  - Verifica: API Externa JSONPlaceholder
- `GET /health/ready` - Verifica se a aplicação está pronta (readiness probe)
  - Verifica: PostgreSQL e Kafka

### Documentação
- `GET /swagger-ui` - Interface Swagger para testar os endpoints
- `GET /q/openapi` - Especificação OpenAPI em formato JSON
- `GET /q/dev` - Quarkus Dev UI (apenas em modo dev)

## Parar os serviços

```bash
docker-compose down
```

Para remover os volumes também:

```bash
docker-compose down -v
```

## Desenvolvimento

Para compilar e empacotar a aplicação:

```bash
./mvnw package
```

Para executar os testes:

```bash
./mvnw test
```

## Links Úteis

- [Quarkus](https://quarkus.io/)
- [Quarkus Kafka Guide](https://quarkus.io/guides/kafka)
- [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [JSONPlaceholder API](https://jsonplaceholder.typicode.com/)
