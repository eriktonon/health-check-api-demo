# Kubernetes Deployment Guide

Este diretório contém os manifestos Kubernetes para deploy da aplicação Health API Demo.

## 📋 Estrutura dos Manifestos

```
k8s/
├── namespace.yaml                # Namespace isolado
├── configmap.yaml               # Configurações da aplicação
├── secret.yaml                  # Credenciais sensíveis
├── postgres-deployment.yaml     # PostgreSQL + Service + PVC
├── kafka-deployment.yaml        # Kafka + Zookeeper + Services
├── application-deployment.yaml  # Aplicação Quarkus + Services
└── README.md                    # Este arquivo
```

## 🚀 Deploy Completo

### 1. Criar o Namespace

```bash
kubectl apply -f namespace.yaml
```

### 2. Aplicar ConfigMap e Secrets

```bash
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml
```

### 3. Deploy do PostgreSQL

```bash
kubectl apply -f postgres-deployment.yaml
```

Verificar status:
```bash
kubectl get pods -n health-api-demo -l app=postgres
kubectl logs -n health-api-demo -l app=postgres
```

### 4. Deploy do Kafka e Zookeeper

```bash
kubectl apply -f kafka-deployment.yaml
```

Verificar status:
```bash
kubectl get pods -n health-api-demo -l app=zookeeper
kubectl get pods -n health-api-demo -l app=kafka
```

### 5. Deploy da Aplicação

```bash
kubectl apply -f application-deployment.yaml
```

Verificar status:
```bash
kubectl get pods -n health-api-demo -l app=health-api-demo
kubectl logs -n health-api-demo -l app=health-api-demo
```

### 6. Verificar Services

```bash
kubectl get services -n health-api-demo
```

## 🔍 Health Probes Configurados

### Liveness Probe (Aplicação)

**Endpoint:** `GET /health/live`  
**Verifica:** API Externa JSONPlaceholder

```yaml
livenessProbe:
  httpGet:
    path: /health/live
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 15
  timeoutSeconds: 5
  failureThreshold: 3
```

**Comportamento:**
- Aguarda 60s após o start do container
- Verifica a cada 15 segundos
- Timeout de 5 segundos
- Após 3 falhas consecutivas → **Kubernetes reinicia o pod**

**O que é verificado:**
- ✅ Conectividade com JSONPlaceholder API

---

### Readiness Probe (Aplicação)

**Endpoint:** `GET /health/ready`  
**Verifica:** PostgreSQL + Kafka

```yaml
readinessProbe:
  httpGet:
    path: /health/ready
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3
```

**Comportamento:**
- Aguarda 30s após o start do container
- Verifica a cada 10 segundos
- Timeout de 5 segundos
- Após 3 falhas consecutivas → **Pod removido do Service (não recebe tráfego)**

**O que é verificado:**
- ✅ Conexão com PostgreSQL
- ✅ Conexão com Kafka

---

### Startup Probe (Aplicação)

**Endpoint:** `GET /health/live`

```yaml
startupProbe:
  httpGet:
    path: /health/live
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 10
  failureThreshold: 30
```

**Comportamento:**
- Permite até 5 minutos (10s × 30 tentativas) para a aplicação iniciar
- Desabilita liveness e readiness até que seja bem-sucedido
- Protege aplicações com inicialização lenta

---

### PostgreSQL Probes

```yaml
livenessProbe:
  exec:
    command: [pg_isready, -U, postgres]
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  exec:
    command: [pg_isready, -U, postgres]
  initialDelaySeconds: 10
  periodSeconds: 5
```

---

### Kafka Probes

```yaml
livenessProbe:
  tcpSocket:
    port: 9092
  initialDelaySeconds: 60
  periodSeconds: 10

readinessProbe:
  tcpSocket:
    port: 9092
  initialDelaySeconds: 30
  periodSeconds: 5
```

---

## 📊 Monitoramento

### Verificar Health Checks

```bash
# Status geral dos pods
kubectl get pods -n health-api-demo

# Descrever um pod específico (mostra eventos de probes)
kubectl describe pod <pod-name> -n health-api-demo

# Ver eventos do namespace
kubectl get events -n health-api-demo --sort-by='.lastTimestamp'
```

### Testar Health Endpoints Manualmente

```bash
# Port-forward para a aplicação
kubectl port-forward -n health-api-demo service/health-api-service-internal 8080:8080

# Em outro terminal:
curl http://localhost:8080/health
curl http://localhost:8080/health/live
curl http://localhost:8080/health/ready
```

### Verificar Logs

```bash
# Logs da aplicação
kubectl logs -n health-api-demo -l app=health-api-demo --tail=100 -f

# Logs do PostgreSQL
kubectl logs -n health-api-demo -l app=postgres --tail=50

# Logs do Kafka
kubectl logs -n health-api-demo -l app=kafka --tail=50
```

## 🔄 Atualizações e Rollbacks

### Atualizar a Aplicação

```bash
# Editar a imagem no deployment
kubectl set image deployment/health-api-demo health-api-demo=health-api-demo:1.1.0 -n health-api-demo

# Ou aplicar o manifesto atualizado
kubectl apply -f application-deployment.yaml

# Acompanhar o rollout
kubectl rollout status deployment/health-api-demo -n health-api-demo
```

### Rollback

```bash
# Ver histórico de revisões
kubectl rollout history deployment/health-api-demo -n health-api-demo

# Fazer rollback para a revisão anterior
kubectl rollout undo deployment/health-api-demo -n health-api-demo

# Ou para uma revisão específica
kubectl rollout undo deployment/health-api-demo --to-revision=2 -n health-api-demo
```

## 🧪 Testes de Resiliência

### Simular Falha do PostgreSQL

```bash
# Deletar o pod do PostgreSQL
kubectl delete pod -n health-api-demo -l app=postgres

# Observar:
# - Readiness probe da aplicação falha
# - Pods da aplicação são removidos do Service
# - PostgreSQL é recriado automaticamente
# - Readiness probe volta a passar quando PostgreSQL está pronto
```

### Simular Falha do Kafka

```bash
# Deletar o pod do Kafka
kubectl delete pod -n health-api-demo -l app=kafka

# Observar comportamento similar ao PostgreSQL
```

### Simular Falha da API Externa

```bash
# A API externa está fora do cluster
# Simular falha via firewall ou iptables (se aplicável)

# Observar:
# - Liveness probe falha
# - Após 3 falhas, pod é reiniciado
# - Startup probe dá tempo para recuperação
```

## 📈 Escalabilidade

### Escalar a Aplicação

```bash
# Escalar manualmente
kubectl scale deployment/health-api-demo --replicas=5 -n health-api-demo

# Configurar auto-scaling (HPA)
kubectl autoscale deployment/health-api-demo \
  --min=3 \
  --max=10 \
  --cpu-percent=70 \
  -n health-api-demo
```

## 🗑️ Limpeza

### Deletar tudo

```bash
# Deletar todos os recursos do namespace
kubectl delete namespace health-api-demo

# Ou deletar recursos individualmente
kubectl delete -f application-deployment.yaml
kubectl delete -f kafka-deployment.yaml
kubectl delete -f postgres-deployment.yaml
kubectl delete -f secret.yaml
kubectl delete -f configmap.yaml
kubectl delete -f namespace.yaml
```

## 🔐 Segurança

### Melhores Práticas

1. **Secrets**: Use ferramentas como Sealed Secrets ou External Secrets Operator para produção
2. **RBAC**: Configure Service Accounts e Role Bindings apropriados
3. **Network Policies**: Restrinja comunicação entre pods
4. **Pod Security Standards**: Aplique políticas de segurança

Exemplo de Network Policy:

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-app-to-postgres
  namespace: health-api-demo
spec:
  podSelector:
    matchLabels:
      app: postgres
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: health-api-demo
    ports:
    - protocol: TCP
      port: 5432
```

## 📚 Recursos Adicionais

- [Kubernetes Probes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)
- [Quarkus Kubernetes Extension](https://quarkus.io/guides/deploying-to-kubernetes)
- [SmallRye Health](https://quarkus.io/guides/smallrye-health)

---

**Versão:** 1.0.0  
**Última Atualização:** 2025-11-02
