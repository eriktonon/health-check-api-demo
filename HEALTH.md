# Implementação do SmallRye Health - Guia Completo

Este documento detalha passo a passo como foi implementado o SmallRye Health na aplicação Health API Demo.

---

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Passo 1: Adicionar Dependência](#passo-1-adicionar-dependência)
3. [Passo 2: Configurar Application Properties](#passo-2-configurar-application-properties)
4. [Passo 3: Implementar Health Checks Customizados](#passo-3-implementar-health-checks-customizados)
5. [Passo 4: Testar Health Checks](#passo-4-testar-health-checks)
6. [Passo 5: Integração com Kubernetes](#passo-5-integração-com-kubernetes)
7. [Troubleshooting](#troubleshooting)

---

## Visão Geral

O **SmallRye Health** é uma implementação da especificação MicroProfile Health que fornece endpoints padronizados para verificar o status de saúde de aplicações.

### Conceitos Principais

- **Liveness Probe**: Verifica se a aplicação está viva e funcionando
- **Readiness Probe**: Verifica se a aplicação está pronta para receber tráfego
- **Health Check**: Implementação customizada que verifica um componente específico

### Por que usar?

- ✅ Integração nativa com Kubernetes
- ✅ Monitoramento automático de dependências
- ✅ Endpoints padronizados
- ✅ Fácil implementação
- ✅ Suporte a checks customizados

---

## Passo 1: Adicionar Dependência

### 1.1. Editar o `pom.xml`

Adicione a dependência do SmallRye Health no arquivo `pom.xml`:

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-health</artifactId>
</dependency>
```

**Localização:** Entre as outras dependências do Quarkus

**Arquivo completo:** `pom.xml` (linhas 65-68)

```xml
<dependencies>
    <!-- ... outras dependências ... -->
    
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-smallrye-health</artifactId>
    </dependency>
    
    <!-- ... outras dependências ... -->
</dependencies>
```

### 1.2. Atualizar dependências

```bash
./mvnw clean install
```

### 1.3. Verificar instalação

Após adicionar a dependência e reiniciar a aplicação, os seguintes endpoints estarão disponíveis automaticamente:

- `GET /q/health` - Status geral
- `GET /q/health/live` - Liveness checks
- `GET /q/health/ready` - Readiness checks

---

## Passo 2: Configurar Application Properties

### 2.1. Editar `application.properties`

Adicione as configurações do SmallRye Health no arquivo `src/main/resources/application.properties`:

```properties
# SmallRye Health configuration
quarkus.smallrye-health.root-path=/health
quarkus.smallrye-health.liveness-path=/health/live
quarkus.smallrye-health.readiness-path=/health/ready
```

**Arquivo:** `src/main/resources/application.properties` (linhas 40-43)

### 2.2. Explicação das configurações

| Propriedade | Descrição | Valor Padrão | Valor Configurado |
|-------------|-----------|--------------|-------------------|
| `root-path` | Caminho base para todos os health checks | `/q/health` | `/health` |
| `liveness-path` | Caminho para liveness checks | `/q/health/live` | `/health/live` |
| `readiness-path` | Caminho para readiness checks | `/q/health/ready` | `/health/ready` |

**Por que customizar?**
- Remover o prefixo `/q/` para URLs mais limpas
- Facilitar integração com Kubernetes (paths mais simples)
- Seguir padrões da organização

---

## Passo 3: Implementar Health Checks Customizados

### 3.1. Estrutura de Diretórios

Crie o pacote para os health checks:

```
src/main/java/io/edgesearch/health/
├── DatabaseHealthCheck.java
├── KafkaHealthCheck.java
└── ExternalApiHealthCheck.java
```

---

### 3.2. Health Check #1: Database (PostgreSQL)

**Arquivo:** `src/main/java/io/edgesearch/health/DatabaseHealthCheck.java`

#### Passo 3.2.1: Criar a classe

```java
package io.edgesearch.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import javax.sql.DataSource;
import java.sql.Connection;

@Readiness                      // Marca como Readiness Probe
@ApplicationScoped              // Bean gerenciado pelo CDI
public class DatabaseHealthCheck implements HealthCheck {

    @Inject
    DataSource dataSource;      // Injeta o DataSource do PostgreSQL

    @Override
    public HealthCheckResponse call() {
        // Cria o builder da resposta
        HealthCheckResponseBuilder responseBuilder = 
            HealthCheckResponse.named("Database connection health check");

        try (Connection connection = dataSource.getConnection()) {
            // Verifica se a conexão é válida com timeout de 5 segundos
            boolean isValid = connection.isValid(5);
            
            if (isValid) {
                responseBuilder
                    .up()                               // Status UP
                    .withData("database", "PostgreSQL") // Dados adicionais
                    .withData("status", "connected");
            } else {
                responseBuilder
                    .down()                             // Status DOWN
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
```

#### Passo 3.2.2: Entendendo as anotações

| Anotação | Propósito |
|----------|-----------|
| `@Readiness` | Marca este check como Readiness Probe (aplicação pronta?) |
| `@ApplicationScoped` | Bean CDI com escopo de aplicação (singleton) |
| `@Inject` | Injeção de dependência do DataSource |

#### Passo 3.2.3: Explicação do método `call()`

1. **Nome do check**: Define um nome descritivo
2. **Try-with-resources**: Garante que a conexão seja fechada
3. **`connection.isValid(5)`**: Verifica se a conexão está ativa (timeout 5s)
4. **`.up()` ou `.down()`**: Define o status do check
5. **`.withData()`**: Adiciona informações extras na resposta JSON

---

### 3.3. Health Check #2: Kafka

**Arquivo:** `src/main/java/io/edgesearch/health/KafkaHealthCheck.java`

#### Passo 3.3.1: Criar a classe

```java
package io.edgesearch.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import java.util.Properties;

@Readiness
@ApplicationScoped
public class KafkaHealthCheck implements HealthCheck {

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String bootstrapServers;    // Injeta configuração do Kafka

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = 
            HealthCheckResponse.named("Kafka connection health check");

        try {
            // Configura o AdminClient do Kafka
            Properties props = new Properties();
            props.put("bootstrap.servers", bootstrapServers);
            props.put("client.id", "health-check-client");
            props.put("connections.max.idle.ms", "10000");
            props.put("request.timeout.ms", "5000");

            // Tenta conectar e listar tópicos
            try (org.apache.kafka.clients.admin.AdminClient adminClient = 
                    org.apache.kafka.clients.admin.AdminClient.create(props)) {
                
                // Timeout de 5 segundos
                adminClient.listTopics()
                    .listings()
                    .get(5, java.util.concurrent.TimeUnit.SECONDS);

                responseBuilder
                    .up()
                    .withData("broker", bootstrapServers)
                    .withData("status", "connected");
            }
        } catch (Exception e) {
            responseBuilder
                .down()
                .withData("broker", bootstrapServers)
                .withData("error", e.getMessage())
                .withData("status", "disconnected");
        }

        return responseBuilder.build();
    }
}
```

#### Passo 3.3.2: Entendendo a implementação

1. **`@ConfigProperty`**: Injeta valor do `application.properties`
2. **AdminClient**: Cliente Kafka para operações administrativas
3. **`listTopics()`**: Tenta listar tópicos (verifica conectividade)
4. **Timeout**: 5 segundos para evitar travamentos

#### Passo 3.3.3: Por que usar AdminClient?

- ✅ Verifica conectividade real com o broker
- ✅ Não depende de producers/consumers existentes
- ✅ Timeout configurável
- ✅ Operação leve (apenas metadados)

---

### 3.4. Health Check #3: API Externa

**Arquivo:** `src/main/java/io/edgesearch/health/ExternalApiHealthCheck.java`

#### Passo 3.4.1: Criar a classe

```java
package io.edgesearch.health;

import io.edgesearch.client.JsonPlaceholderClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Liveness                       // Marca como Liveness Probe
@ApplicationScoped
public class ExternalApiHealthCheck implements HealthCheck {

    @Inject
    @RestClient
    JsonPlaceholderClient jsonPlaceholderClient;  // Injeta o REST Client

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = 
            HealthCheckResponse.named("External API health check");

        try {
            // Faz uma chamada real para verificar se a API responde
            jsonPlaceholderClient.getTodoById(1L);
            
            responseBuilder
                .up()
                .withData("api", "JSONPlaceholder")
                .withData("url", "https://jsonplaceholder.typicode.com")
                .withData("status", "available");
        } catch (Exception e) {
            responseBuilder
                .down()
                .withData("api", "JSONPlaceholder")
                .withData("url", "https://jsonplaceholder.typicode.com")
                .withData("error", e.getMessage())
                .withData("status", "unavailable");
        }

        return responseBuilder.build();
    }
}
```

#### Passo 3.4.2: Por que Liveness em vez de Readiness?

| Aspecto | Liveness | Readiness |
|---------|----------|-----------|
| **Propósito** | Aplicação está viva? | Aplicação está pronta? |
| **Falha** | Reinicia o pod | Remove do Service |
| **API Externa** | ✅ Apropriado | ❌ Não recomendado |

**Justificativa:** 
- API externa fora do nosso controle
- Falha não impede processamento local
- Se API ficar indisponível, reiniciar pode resolver problemas de estado

#### Passo 3.4.3: Usando o REST Client existente

**Vantagens:**
- ✅ Reutiliza configuração existente
- ✅ Mesma lógica de timeout e retry
- ✅ Não duplica código
- ✅ Verifica a stack completa (REST Client + API)

---

## Passo 4: Testar Health Checks

### 4.1. Iniciar a aplicação

```bash
./mvnw quarkus:dev
```

### 4.2. Testar endpoint geral

```bash
curl http://localhost:8080/health
```

**Resposta esperada:**

```json
{
  "status": "UP",
  "checks": [
    {
      "name": "Database connection health check",
      "status": "UP",
      "data": {
        "database": "PostgreSQL",
        "status": "connected"
      }
    },
    {
      "name": "Kafka connection health check",
      "status": "UP",
      "data": {
        "broker": "localhost:9092",
        "status": "connected"
      }
    },
    {
      "name": "External API health check",
      "status": "UP",
      "data": {
        "api": "JSONPlaceholder",
        "url": "https://jsonplaceholder.typicode.com",
        "status": "available"
      }
    }
  ]
}
```

### 4.3. Testar Liveness

```bash
curl http://localhost:8080/health/live
```

**Resposta esperada:**

```json
{
  "status": "UP",
  "checks": [
    {
      "name": "External API health check",
      "status": "UP",
      "data": {
        "api": "JSONPlaceholder",
        "url": "https://jsonplaceholder.typicode.com",
        "status": "available"
      }
    }
  ]
}
```

### 4.4. Testar Readiness

```bash
curl http://localhost:8080/health/ready
```

**Resposta esperada:**

```json
{
  "status": "UP",
  "checks": [
    {
      "name": "Database connection health check",
      "status": "UP",
      "data": {
        "database": "PostgreSQL",
        "status": "connected"
      }
    },
    {
      "name": "Kafka connection health check",
      "status": "UP",
      "data": {
        "broker": "localhost:9092",
        "status": "connected"
      }
    }
  ]
}
```

### 4.5. Testar cenário de falha

#### 4.5.1. Parar o PostgreSQL

```bash
docker-compose stop postgres
```

Aguarde alguns segundos e consulte:

```bash
curl http://localhost:8080/health/ready
```

**Resposta esperada:**

```json
{
  "status": "DOWN",
  "checks": [
    {
      "name": "Database connection health check",
      "status": "DOWN",
      "data": {
        "database": "PostgreSQL",
        "error": "Connection refused"
      }
    },
    {
      "name": "Kafka connection health check",
      "status": "UP",
      "data": {
        "broker": "localhost:9092",
        "status": "connected"
      }
    }
  ]
}
```

**Observações:**
- Status geral é `DOWN` porque um check falhou
- Apenas o Database check está `DOWN`
- Kafka continua `UP`

#### 4.5.2. Restaurar o PostgreSQL

```bash
docker-compose start postgres
```

Aguarde alguns segundos e consulte novamente. O status deve voltar para `UP`.

---

## Passo 5: Integração com Kubernetes

### 5.1. Configuração no Deployment

No arquivo `k8s/application-deployment.yaml`, os probes são configurados assim:

```yaml
# LIVENESS PROBE
livenessProbe:
  httpGet:
    path: /health/live      # Endpoint que criamos
    port: 8080
    scheme: HTTP
  initialDelaySeconds: 60   # Aguarda 60s após start
  periodSeconds: 15         # Verifica a cada 15s
  timeoutSeconds: 5         # Timeout de 5s
  failureThreshold: 3       # 3 falhas → RESTART

# READINESS PROBE
readinessProbe:
  httpGet:
    path: /health/ready     # Endpoint que criamos
    port: 8080
    scheme: HTTP
  initialDelaySeconds: 30   # Aguarda 30s após start
  periodSeconds: 10         # Verifica a cada 10s
  timeoutSeconds: 5         # Timeout de 5s
  failureThreshold: 3       # 3 falhas → REMOVE DO SERVICE
```

### 5.2. Como o Kubernetes usa os probes

#### Liveness Probe

```
1. Aplicação inicia
2. Aguarda 60 segundos (initialDelaySeconds)
3. Chama GET /health/live
4. Se resposta for 200 OK com status UP → Continua
5. Se resposta for diferente → Incrementa contador de falhas
6. Após 3 falhas consecutivas → Kubernetes reinicia o pod
```

#### Readiness Probe

```
1. Aplicação inicia
2. Aguarda 30 segundos (initialDelaySeconds)
3. Chama GET /health/ready
4. Se resposta for 200 OK com status UP → Pod recebe tráfego
5. Se resposta for diferente → Remove pod do Service
6. Continua verificando a cada 10 segundos
7. Quando voltar a UP → Pod volta a receber tráfego
```

### 5.3. Fluxograma de decisão

```
┌─────────────────┐
│  Pod iniciado   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Startup Probe   │  (Opcional)
│ /health/live    │
└────────┬────────┘
         │
         ├─── Falha após 30 tentativas → Restart Pod
         │
         ▼
┌─────────────────┐
│ Liveness Probe  │
│ /health/live    │
└────────┬────────┘
         │
         ├─── Falha após 3 tentativas → Restart Pod
         │
         ▼
┌─────────────────┐
│ Readiness Probe │
│ /health/ready   │
└────────┬────────┘
         │
         ├─── UP → Pod no Service (recebe tráfego)
         │
         └─── DOWN → Pod fora do Service (não recebe tráfego)
```

---

## Troubleshooting

### Problema 1: Endpoint não encontrado (404)

**Sintoma:**
```bash
curl http://localhost:8080/health
# 404 Not Found
```

**Solução:**
1. Verificar se a dependência foi adicionada corretamente no `pom.xml`
2. Recompilar a aplicação: `./mvnw clean install`
3. Reiniciar a aplicação

**Verificação:**
```bash
./mvnw dependency:tree | grep smallrye-health
```

---

### Problema 2: Health check sempre DOWN

**Sintoma:**
```json
{
  "status": "DOWN",
  "checks": [
    {
      "name": "Database connection health check",
      "status": "DOWN"
    }
  ]
}
```

**Diagnóstico:**

1. **Verificar se o serviço está rodando:**
```bash
# PostgreSQL
docker-compose ps postgres

# Kafka
docker-compose ps kafka
```

2. **Verificar logs do health check:**
```bash
./mvnw quarkus:dev
# Observar mensagens de erro no console
```

3. **Testar conexão manualmente:**
```bash
# PostgreSQL
psql -h localhost -p 5432 -U postgres -d demo_db

# Kafka
docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092
```

**Soluções comuns:**
- Iniciar os serviços: `docker-compose up -d`
- Verificar configurações em `application.properties`
- Verificar firewall/portas

---

### Problema 3: Timeout no health check

**Sintoma:**
Health check demora muito e eventualmente falha por timeout.

**Solução:**

Ajustar o timeout no código do health check:

```java
// DatabaseHealthCheck.java
boolean isValid = connection.isValid(10); // Aumentar de 5 para 10 segundos

// KafkaHealthCheck.java
adminClient.listTopics()
    .listings()
    .get(10, TimeUnit.SECONDS); // Aumentar de 5 para 10 segundos
```

**Ou** ajustar no Kubernetes:

```yaml
livenessProbe:
  timeoutSeconds: 10  # Aumentar de 5 para 10
```

---

### Problema 4: Probe configurado mas não executado

**Sintoma:**
No Kubernetes, o pod nunca fica ready.

**Diagnóstico:**

```bash
# Descrever o pod para ver eventos
kubectl describe pod <pod-name> -n health-api-demo

# Ver logs do pod
kubectl logs <pod-name> -n health-api-demo
```

**Verificações:**

1. **Porta correta:**
```yaml
# Deve ser a porta do container, não do service
ports:
- containerPort: 8080

livenessProbe:
  httpGet:
    port: 8080  # Mesmo valor do containerPort
```

2. **Path correto:**
```yaml
livenessProbe:
  httpGet:
    path: /health/live  # Deve corresponder ao configurado
```

3. **Timing adequado:**
```yaml
initialDelaySeconds: 60  # Aplicação pode demorar para iniciar
periodSeconds: 10        # Frequência de verificação
```

---

## Boas Práticas

### ✅ DO (Faça)

1. **Separe liveness e readiness**
   - Liveness: Verifica se aplicação está viva
   - Readiness: Verifica dependências externas

2. **Use timeouts curtos**
   - 5-10 segundos no máximo
   - Evita travar a verificação

3. **Adicione dados úteis**
   ```java
   .withData("database", "PostgreSQL")
   .withData("status", "connected")
   .withData("responseTime", "150ms")
   ```

4. **Documente os health checks**
   - Explicar o que cada check verifica
   - Quando falha, o que pode ter causado

5. **Teste cenários de falha**
   - Simule falhas de rede
   - Teste recuperação automática

### ❌ DON'T (Não faça)

1. **Não faça operações pesadas**
   ```java
   // ❌ RUIM
   public HealthCheckResponse call() {
       // Não fazer SELECT COUNT(*) em tabela gigante
       long count = repository.countAll();
       // ...
   }
   ```

2. **Não ignore exceções**
   ```java
   // ❌ RUIM
   try {
       connection.isValid(5);
   } catch (Exception e) {
       // Não fazer nada
   }
   
   // ✅ BOM
   try {
       connection.isValid(5);
   } catch (Exception e) {
       responseBuilder.down().withData("error", e.getMessage());
   }
   ```

3. **Não use liveness para dependências externas críticas**
   - Use readiness para PostgreSQL/Kafka
   - Liveness apenas para estado interno da aplicação

4. **Não configure timeouts muito longos**
   - Pode causar atrasos em rolling updates
   - Kubernetes pode demorar para detectar falhas

---

## Resumo da Implementação

### Checklist de Implementação

- ✅ **Passo 1:** Adicionar dependência `quarkus-smallrye-health` no `pom.xml`
- ✅ **Passo 2:** Configurar paths em `application.properties`
- ✅ **Passo 3:** Criar `DatabaseHealthCheck` com `@Readiness`
- ✅ **Passo 4:** Criar `KafkaHealthCheck` com `@Readiness`
- ✅ **Passo 5:** Criar `ExternalApiHealthCheck` com `@Liveness`
- ✅ **Passo 6:** Testar endpoints `/health`, `/health/live`, `/health/ready`
- ✅ **Passo 7:** Configurar probes no Kubernetes deployment
- ✅ **Passo 8:** Testar em ambiente Kubernetes

### Arquivos Criados/Modificados

| Arquivo | Ação | Descrição |
|---------|------|-----------|
| `pom.xml` | Modificado | Adicionada dependência |
| `application.properties` | Modificado | Configurações de paths |
| `DatabaseHealthCheck.java` | Criado | Health check PostgreSQL |
| `KafkaHealthCheck.java` | Criado | Health check Kafka |
| `ExternalApiHealthCheck.java` | Criado | Health check API externa |
| `application-deployment.yaml` | Criado | Configuração Kubernetes |

### Endpoints Disponíveis

| Endpoint | Verifica | HTTP Status | Uso |
|----------|----------|-------------|-----|
| `/health` | Todos os checks | 200 se UP, 503 se DOWN | Monitoramento geral |
| `/health/live` | Liveness checks | 200 se UP, 503 se DOWN | Kubernetes liveness |
| `/health/ready` | Readiness checks | 200 se UP, 503 se DOWN | Kubernetes readiness |

---

**Documentação criada em:** 2025-11-02  
**Versão:** 1.0.0  
**Autor:** Edge Search  
**Aplicação:** Health API Demo
