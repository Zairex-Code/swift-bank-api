# Swift Bank API

Microservicio bancario atómico, ligero y desacoplado construido con **Quarkus 3.15**, **Java 21** y **Arquitectura Hexagonal (Ports & Adapters)**. Persiste en **MongoDB**, usa **Redis** para cache-aside y separa escritura/lectura siguiendo **CQRS ligero**. Está preparado para ejecutarse en local con Docker Compose y desplegarse en **Microsoft Azure (ACI + MongoDB Atlas/Cosmos DB + Azure Cache for Redis)**.

## Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 (Records, Pattern Matching) |
| Framework | Quarkus 3.15.1 (`quarkus-rest-jackson`, `quarkus-hibernate-validator`, `quarkus-arc`) |
| Persistencia | MongoDB 7 vía `quarkus-mongodb-client` (driver nativo) |
| Caché | Redis 7 vía `quarkus-redis-client` (patrón cache-aside) |
| Mapeo | MapStruct 1.6 (`componentModel = "cdi"`) + Lombok |
| Validación | Jakarta Bean Validation |
| Tests | JUnit 5 + Mockito |
| Contenedores | Docker multi-stage (`maven` build → `eclipse-temurin:21-jre-alpine` runtime) |
| Nube | Azure Container Instances, MongoDB Atlas / Azure Cosmos DB, Azure Cache for Redis |

## Arquitectura

```
com... => org.zairex_code
├── domain/
│   ├── model/            Account, Transaction, TransactionStatus
│   ├── exception/        BankingException + excepciones tipadas
│   └── port/
│       ├── in/command/   AccountCommandUseCase, TransactionCommandUseCase
│       ├── in/query/     AccountQueryUseCase, TransactionQueryUseCase
│       └── out/          AccountCommandPort, AccountQueryPort,
│                         TransactionCommandPort, TransactionQueryPort,
│                         TransactionManagerPort, AccountCachePort
├── application/
│   ├── command/          AccountCommandService, TransactionCommandService
│   └── query/            AccountQueryService, TransactionQueryService
└── adapter/
    ├── in/
    │   ├── rest/         BankingResource, TransactionResource, dto/, mapper/, error/
    │   └── seed/         SeedDataLoader
    └── out/
        ├── mongo/        adaptadores + MongoTransactionManager + mapper BSON
        └── cache/        CachedAccountQueryAdapter (@Decorator), RedisAccountCacheAdapter
```

- **CQRS ligero**: mismos datastore, puertos y servicios separados para escritura y lectura.
- **Cache-aside**: `CachedAccountQueryAdapter` decora `AccountQueryPort`; lee de Redis y, en caso de miss, consulta MongoDB y puebla la caché. Las escrituras invalidan la clave vía `AccountCachePort`.
- **Atomicidad**: débito + crédito + auditoría se ejecutan en una única transacción de MongoDB (`MongoTransactionManager` + `ClientSession`). El débito usa `findOneAndUpdate` con filtro `balance >= amount` para evitar condiciones de carrera.
- **Excepciones**: las validaciones lanzan excepciones tipadas mapeadas a HTTP; antes de lanzar se persiste la transacción `REJECTED` como auditoría.

## Requisitos

- Docker + Docker Compose (vía recomendada) **o** Java 21 + Maven.
- Para Azure: Azure CLI y una instancia de MongoDB (Atlas/Cosmos) y Redis.

## Ejecución local con Docker

```bash
cp .env.example .env
docker compose up --build
```

Esto levanta:

| Servicio | Puerto | Descripción |
|---|---|---|
| `mongo` | 27017 | MongoDB 7 como replica set single-node (`rs0`) |
| `mongo-init` | — | Inicializa el replica set y espera al primario |
| `redis` | 6379 | Caché cache-aside |
| `app` | 8080 | Microservicio Quarkus |

La API queda disponible en `http://localhost:8080`.

### Poblar la base de datos con datos falsos

```bash
docker compose exec -T mongo mongosh --file /seed/seed-data.js
```

El script `seed/seed-data.js` crea 2 cuentas y 3 transacciones de ejemplo (incluida una `REJECTED`).

Alternativamente, el `SeedDataLoader` de Java inserta 2 cuentas al arrancar cuando `SEED_ENABLED=true` y la base está vacía (activado por defecto en `docker-compose.yml`).

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/v1/accounts` | Apertura de cuenta |
| `GET` | `/api/v1/accounts/{id}` | Consulta de cuenta |
| `GET` | `/api/v1/accounts` | Listado de cuentas |
| `POST` | `/api/v1/accounts/{id}/deposits` | Abono de fondos |
| `POST` | `/api/v1/accounts/transfers` | Transferencia entre cuentas |
| `GET` | `/api/v1/transactions` | Historial de transacciones |
| `GET` | `/q/health` | Health checks |

### Ejemplos

```bash
# Crear cuenta
curl -s -X POST localhost:8080/api/v1/accounts \
  -H 'Content-Type: application/json' \
  -d '{"accountNumber":"ACC-3001","holderName":"Carol Danvers","initialBalance":1000.00}'

# Listar cuentas
curl -s localhost:8080/api/v1/accounts

# Consultar cuenta
curl -s localhost:8080/api/v1/accounts/<id>

# Depositar
curl -s -X POST localhost:8080/api/v1/accounts/<id>/deposits \
  -H 'Content-Type: application/json' \
  -d '{"amount":250.00}'

# Transferir
curl -s -X POST localhost:8080/api/v1/accounts/transfers \
  -H 'Content-Type: application/json' \
  -d '{"sourceAccountId":"<id-origen>","targetAccountId":"<id-destino>","amount":100.00}'

# Transacciones
curl -s localhost:8080/api/v1/transactions
```

Códigos de error: `404` cuenta no encontrada, `409` cuenta duplicada, `400` validación/importe inválido, `422` saldo insuficiente o transferencia rechazada, `500` error inesperado.

## Variables de entorno

| Variable | Descripción | Default |
|---|---|---|
| `MONGO_CONNECTION_STRING` | Cadena de conexión MongoDB | `mongodb://localhost:27017/?replicaSet=rs0` |
| `MONGO_DATABASE` | Nombre de la base de datos | `swift_bank` |
| `REDIS_HOSTS` | Host(s) Redis | `redis://localhost:6379` |
| `ACCOUNT_CACHE_TTL_SECONDS` | TTL de caché de cuentas | `60` |
| `SEED_ENABLED` | Carga datos demo al arrancar | `false` |
| `QUARKUS_HTTP_PORT` | Puerto HTTP | `8080` |

## Tests

```bash
./mvnw test
```

Tests unitarios con JUnit 5 + Mockito para servicios de comando/consulta, decorador de caché, mappers MapStruct y mapeo de excepciones HTTP.

## Ejecución sin Docker

```bash
# Requiere MongoDB (replica set) y Redis accesibles según application.properties
./mvnw quarkus:dev
```

## Despliegue en Azure (ACI + Atlas/Cosmos + Azure Cache for Redis)

1. **Datos gestionados**
   - Crea un clúster en **MongoDB Atlas** (gratuito) o una cuenta **Azure Cosmos DB for MongoDB API** y obtén la cadena de conexión.
   - Crea **Azure Cache for Redis** y obtén el host (`rediss://...`).

2. **Registro e imagen**

```bash
az group create --name rg-banking-core --location eastus
az acr create --resource-group rg-banking-core --name swiftbankacr --sku Basic
az acr build --registry swiftbankacr --image swift-bank-api:latest .
```

3. **Desplegar en ACI**

```bash
az container create \
  --resource-group rg-banking-core \
  --name minicore-banking-api \
  --image swiftbankacr.azurecr.io/swift-bank-api:latest \
  --registry-login-server swiftbankacr.azurecr.io \
  --registry-username <acr-user> \
  --registry-password <acr-password> \
  --dns-name-label minicore-bank-api-$RANDOM \
  --ports 8080 \
  --cpu 1 \
  --memory 1.0 \
  --environment-variables \
    QUARKUS_HTTP_PORT=8080 \
    MONGO_DATABASE=swift_bank \
    SEED_ENABLED=false \
    MONGO_CONNECTION_STRING="mongodb+srv://<user>:<pass>@<cluster>/..." \
    REDIS_HOSTS="rediss://:<key>@<cache>.redis.cache.windows.net:6380"
```

La API quedará accesible en `http://minicore-bank-api-<random>.<region>.azurecontainer.io:8080`.

> Para producción se recomienda Azure Container Apps, Key Vault para secretos, autenticación ACR con managed identity y réplicas del replica set de MongoDB.

## Documentación adicional

- [`ANALISIS_PROYECTO.md`](ANALISIS_PROYECTO.md): análisis de contexto, arquitectura y decisiones técnicas.
