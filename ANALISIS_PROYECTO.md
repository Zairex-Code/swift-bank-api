# Análisis del Proyecto — Swift Bank API

## 1. Resumen ejecutivo

Swift Bank API es un microservicio bancario (apertura de cuentas, depósitos y transferencias) diseñado bajo **Arquitectura Hexagonal** con **CQRS ligero**, persistencia en **MongoDB**, caché **cache-aside** sobre **Redis** y despliegue contenerizado. El objetivo es mantener el dominio libre de frameworks, con reglas de negocio explícitas y mutaciones atómicas, sin sobreingeniería.

## 2. Contexto de negocio

El dominio modela operaciones bancarias básicas:

- **Apertura de cuenta** con saldo inicial opcional.
- **Depósito** de fondos en una cuenta.
- **Transferencia** atómica entre dos cuentas.
- **Consulta** de cuentas y auditoría de transacciones.

Cada operación de escritura genera un registro de auditoría (`Transaction`) con estado `COMPLETED` o `REJECTED`, incluyendo el motivo del rechazo. Esto permite reconstruir el histórico y justificar decisiones de negocio.

## 3. Stack tecnológico

| Componente | Tecnología | Motivo |
|---|---|---|
| Lenguaje | Java 21 (LTS) | Records, Pattern Matching, tipos modernos |
| Framework | Quarkus 3.15.1 | Arranque rápido, bajo consumo, CDI/Arc |
| REST | `quarkus-rest-jackson` | Serialización Jackson sobre REST reactivo |
| Validación | Jakarta Bean Validation | Contratos de entrada declarativos |
| Persistencia | `quarkus-mongodb-client` | Driver nativo, control total, sin acoplar el dominio |
| Caché | `quarkus-redis-client` | Cliente Vert.x Redis para cache-aside |
| Mapeo | MapStruct 1.6 (`cdi`) + Lombok | Conversión DTO↔dominio en compile-time |
| Tests | JUnit 5 + Mockito | Unitarios rápidos y aislados |
| Contenedores | Docker multi-stage | Imagen de runtime mínima (JRE Alpine) |
| Nube | ACI + Atlas/Cosmos + Azure Cache for Redis | Despliegue simple y gestionado |

## 4. Arquitectura Hexagonal (Ports & Adapters)

El principio rector es la **inversión de dependencias**: el dominio define los puertos y la infraestructura los implementa.

```
domain/         ← núcleo puro (sin Quarkus, sin Jackson, sin driver Mongo)
application/    ← orquestación de casos de uso
adapter/in/     ← entrada (REST, seeder)
adapter/out/    ← salida (MongoDB, Redis)
```

- El paquete `domain` no depende de ningún framework salvo Lombok (solo en modelos).
- Los puertos de entrada (`...port.in.command` / `...port.in.query`) definen **qué** se puede hacer.
- Los puertos de salida (`...port.out`) definen **qué necesita** el dominio del exterior.
- Los adaptadores son intercambiables: cambiar MongoDB por otro almacén solo requiere un nuevo adaptador.

## 5. CQRS ligero

Se separan responsabilidades de escritura y lectura sin duplicar el datastore ni introducir event sourcing:

| Lado | Puertos de entrada | Servicios | Puertos de salida |
|---|---|---|---|
| Command | `AccountCommandUseCase`, `TransactionCommandUseCase` | `AccountCommandService`, `TransactionCommandService` | `AccountCommandPort`, `TransactionCommandPort`, `TransactionManagerPort` |
| Query | `AccountQueryUseCase`, `TransactionQueryUseCase` | `AccountQueryService`, `TransactionQueryService` | `AccountQueryPort`, `TransactionQueryPort` |

Ventajas: los caminos de lectura pueden optimizarse con caché sin afectar la escritura, y las reglas de mutación quedan aisladas. Es un CQRS pragmático: sin proyecciones ni consistencia eventual, adecuado para la escala del servicio.

## 6. Cache-aside con Redis

- El decorador `CachedAccountQueryAdapter` (`@Decorator` sobre `AccountQueryPort`) intercepta las lecturas.
- En `findById`: consulta Redis (`account:{id}`); si hay **hit**, retorna sin tocar MongoDB; si hay **miss**, delega en el adaptador Mongo y guarda el resultado con TTL (`SET ... EX`).
- Las escrituras invalidan la entrada mediante `AccountCachePort.evict(id)` (débito, crédito, transferencia) para evitar datos obsoletos.
- La caché **degrada de forma segura**: cualquier fallo de Redis se registra y se continúa contra MongoDB.

## 7. Modelo de datos (MongoDB)

Base de datos: `swift_bank`.

| Colección | Documento |
|---|---|
| `accounts` | `{ _id, accountNumber, holderName, balance }` |
| `transactions` | `{ _id, sourceAccountId, targetAccountId, amount, status, rejectionReason, timestamp }` |

Decisiones:

- `_id` es un `String` (UUID generado en el dominio) para desacoplar del `ObjectId` de Mongo.
- Los importes se almacenan como **`Decimal128`** y se convierten a `BigDecimal` con escala 2 y redondeo `HALF_EVEN`, evitando errores de coma flotante.
- `timestamp` se almacena como fecha BSON en UTC.
- Índice **único** en `accounts.accountNumber` y descendente en `transactions.timestamp` (`MongoIndexInitializer`).

## 8. Manejo de errores

El dominio define una jerarquía de excepciones (`BankingException` como base):

| Excepción | HTTP |
|---|---|
| `AccountNotFoundException` | 404 |
| `DuplicateAccountException` | 409 |
| `InvalidAmountException` | 400 |
| `InsufficientFundsException` | 422 |
| `TransferFailedException` | 422 |
| Validación Jakarta | 400 |
| No controlada | 500 |

`BankingExceptionMapper`, `ValidationExceptionMapper` y `GenericExceptionMapper` (JAX-RS `@Provider`) traducen a un cuerpo uniforme `ErrorResponse`. Además, antes de lanzar una excepción de negocio se persiste la transacción `REJECTED` para conservar la **auditoría**.

## 9. Concurrencia y atomicidad

El caso crítico es la transferencia. La estrategia es doble:

1. **Atomicidad multi-documento**: `MongoTransactionManager` abre una `ClientSession`, inicia transacción y la propaga vía `MongoTransactionContext` (ThreadLocal). Débito, crédito y registro de auditoría se confirman o revierten juntos.
2. **Atomicidad a nivel de documento**: el débito usa `findOneAndUpdate` con filtro `balance >= amount` e `$inc`, de modo que dos transferencias concurrentes no pueden dejar el saldo negativo. Si el documento ya no cumple el filtro, la operación falla y la transacción se aborta.

En local, Docker Compose levanta MongoDB como **replica set de un nodo** (`rs0`) porque las transacciones multi-documento requieren replica set. Atlas y Cosmos ya lo soportan.

## 10. Configuración

`application.properties` usa placeholders con variables de entorno y valores por defecto para local, permitiendo el mismo artefacto en local y Azure:

```properties
quarkus.mongodb.connection-string=${MONGO_CONNECTION_STRING:mongodb://localhost:27017/?replicaSet=rs0}
quarkus.mongodb.database=${MONGO_DATABASE:swift_bank}
quarkus.redis.hosts=${REDIS_HOSTS:redis://localhost:6379}
app.cache.account-ttl-seconds=${ACCOUNT_CACHE_TTL_SECONDS:60}
app.seed.enabled=${SEED_ENABLED:false}
```

## 11. Contenedores y despliegue

- **Dockerfile multi-stage**: build con `maven:3.9-eclipse-temurin-21-alpine`, runtime con `eclipse-temurin:21-jre-alpine`, usuario no privilegiado y puerto 8080.
- **Docker Compose local**: MongoDB (replica set) + Redis + app, con healthchecks e inicialización del replica set.
- **Azure**: la app se desplega en Azure Container Instances y se conecta a MongoDB Atlas/Cosmos DB y Azure Cache for Redis mediante variables de entorno. La imagen se construye con `az acr build`.

## 12. Seguridad

Estado actual (orientado a demo/MVP):

- Validación de entrada en el perímetro (Jakarta Validation).
- Usuario no root en el contenedor.
- Secretos por variables de entorno (no en el repositorio).

Mejoras recomendadas para producción: autenticación/autorización (OIDC), TLS, Key Vault para secretos, managed identity para ACR, límites de tasa y auditoría de accesos.

## 13. Observabilidad

- Logging estructurado de Quarkus (JBoss LogManager).
- Health checks en `/q/health` (SmallRye Health, incluye conectividad MongoDB/Redis).
- Pendiente: métricas Micrometer/Prometheus y trazas OpenTelemetry.

## 14. Testing

Unitarios con JUnit 5 + Mockito, sin dependencias externas:

- `AccountCommandServiceTest` / `TransactionCommandServiceTest`: reglas de creación, depósitos, transferencias, auditoría y eviction.
- `AccountQueryServiceTest`: consultas y excepciones.
- `CachedAccountQueryAdapterTest`: hit/miss/eviction del cache-aside.
- Mappers MapStruct y `BankingExceptionMapper`.

Ejecución: `./mvnw test`.

## 15. Decisiones y trade-offs

| Decisión | Alternativa descartada | Razón |
|---|---|---|
| `mongodb-client` nativo | Panache | Mantiene el dominio libre de frameworks y da control de transacciones |
| CQRS ligero | CQRS con read model separado | Balance coste/beneficio para el alcance actual |
| Transacciones Mongo | Compensación manual (débito/refund) | Garantía real de atomicidad |
| `Decimal128` + `BigDecimal` | `double` | Precisión monetaria |
| Cache-aside con decorador | Caché dentro del servicio | La infraestructura de caché no contamina la aplicación |
| Excepciones + auditoría REJECTED | Solo excepciones | Conserva trazabilidad de rechazos |

## 16. Roadmap

- Autenticación OIDC y autorización por roles.
- Métricas y trazas distribuidas.
- Paginación y filtros en listados.
- Idempotencia de operaciones (clave de idempotencia).
- Migración a Azure Container Apps + Key Vault.
- Cobertura de tests de integración con Testcontainers.
