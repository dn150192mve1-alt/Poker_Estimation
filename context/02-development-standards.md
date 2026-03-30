# Development Standards

## Architecture Principles

У проєкті використовуються такі архітектурні принципи:

- **TDD**
- **DDD**
- **Clean Hexagon**
- **Separation of Concerns**
- **Low Coupling**
- **High Cohesion**

## Clean Hexagon

У проєкті використовується комбінація **Hexagonal Architecture** та **Clean Architecture** з правилом залежностей: зовнішні шари залежать від внутрішніх.

Основна структура:

```text
com.example.poker
    adapters
        inbound
            rest
            websocket
        outbound
            persistence
            publishing
    application
        ports
            inbound
            outbound
        usecases
    domain
    configuration
```

### Layer Responsibilities

#### domain

- domain models
- domain events
- domain rules

Цей шар містить core business logic і не залежить від framework або transport details.

#### application

- inbound ports
- outbound ports
- use case implementations

Цей шар orchestration-oriented і координує виклики domain та adapters через ports.

#### adapters

- inbound adapters для REST API і WebSockets
- outbound adapters для persistence та event publishing

Цей шар інтегрує application layer із зовнішнім світом.

#### configuration

- Spring configuration
- bean wiring
- infrastructure configuration

## DDD Rules

Проєкт повинен будуватись навколо domain model.

Правила:

- business logic повинна жити в `domain`
- `application.usecases` містить orchestration, а не domain rules
- domain models повинні бути explicit, immutable і орієнтовані на business meaning
- domain state і behavior повинні бути виражені через чіткі domain types та назви

## Package Structure

Використовується структура в стилі **Clean Hexagon**.

Приклад:

```text
com.example.poker
    adapters
        inbound
            rest
            websocket
        outbound
            persistence
            publishing
    application
        ports
            inbound
            outbound
        usecases
    domain
    configuration
```

## Naming Conventions

### Use Cases

Формат:

```text
[Action][Entity]UseCase
```

Приклади:

- `CreateRoomUseCase`
- `JoinRoomUseCase`
- `StartEstimationRoundUseCase`
- `SubmitVoteUseCase`
- `RevealEstimationsUseCase`

### Ports

Формат:

```text
InPort:  [Action][Entity]InPort
OutPort: [Action][Entity]OutPort
```

Приклади:

- `CreateRoomInPort`
- `JoinRoomInPort`
- `SaveRoomOutPort`
- `FindRoomByIdOutPort`
- `PublishRoomStateOutPort`

Method naming rules:

- method name повинен відображати business action порту
- не використовувати generic method names на кшталт `handle`, `execute`, `process`
- приклади: `createRoom()`, `joinRoom(...)`, `startEstimationRound(...)`, `submitVote(...)`, `revealEstimations(...)`

### REST / WebSocket Adapters

Формат:

```text
REST:      [Action][Entity]RestAdapter або [Entity]RestController
WebSocket: [Action][Entity]WebSocketAdapter або [Entity]WebSocketController
```

Приклади:

- `RoomRestController`
- `EstimationRestController`
- `RoomWebSocketController`
- `PublishRoomStateWebSocketAdapter`

### Persistence Adapters

Формат:

```text
[Entity]PersistenceAdapter
```

Приклади:

- `RoomPersistenceAdapter`
- `ParticipantPersistenceAdapter`
- `EstimationRoundPersistenceAdapter`

### Mappers

Формат:

```text
[Action][Entity]Mapper
```

або

```text
[Entity]Mapper
```

Правила:

- `final class`
- private constructor
- only static methods

### Events

Формат:

```text
[Entity][Action]Event
```

Приклади:

- `RoomCreatedEvent`
- `ParticipantJoinedEvent`
- `EstimationRoundStartedEvent`
- `EstimationsRevealedEvent`

## Code Style

### Use Cases

Правила:

- `final class` завжди
- implements один **InPort**
- містить orchestration logic
- не містить domain rules
- public method use case повинен мати ту саму business-oriented назву, що і відповідний `InPort` method

### Ports

Правила:

- `public interface`
- один порт = одна чітка відповідальність
- тільки method signatures

### Domain Models

Правила:

- immutable
- орієнтовані на DDD semantics
- update operations повертають новий instance
- factory methods допускаються для creation logic

### Mappers

Правила:

- explicit mapping only
- не використовувати automatic mapper frameworks
- mapping logic повинен бути прозорим і testable

## WebSocket Standards

WebSockets є обов'язковою частиною MVP architecture.

Призначення:

- доставляти room state updates у real time
- синхронізувати participants без polling
- відображати changes у voting, timer та reveal flows

Правила:

- WebSockets не замінюють REST API, а доповнюють його
- commands повинні надходити через inbound REST adapters, якщо немає окремої явно визначеної потреби
- WebSockets повинні використовуватись для broadcasting state updates та room events
- WebSocket adapters не повинні містити business logic
- WebSocket communication повинна бути testable через integration tests

## Development Workflow

Розробка відбувається строго через **TDD**.

Workflow:

- **Red -> Green -> Refactor**
- tests пишуться перед production code
- **Integration tests** пишуться першими для формалізації user-facing scenarios
- після integration tests проєктується структура solution і пишуться **Unit tests**
- лише після цього пишеться production code
- **Unit tests** повинні покривати весь business logic
- **Integration tests** повинні покривати всі основні сценарії та integration boundaries

## Test Standards

### Test Package Structure

Структура тестів повинна відповідати структурі production packages.

Приклад:

```text
src/main/java/com/example/poker
    adapters
        inbound
        outbound
    application
        ports
        usecases
    domain

src/test/java/com/example/poker
    adapters
        inbound
        outbound
    application
        usecases
    domain
    integration
```

### Unit Test Rules

- весь business logic повинен бути покритий unit tests
- unit tests для use cases, mappers, domain models та adapters повинні бути focused і isolated
- minimum **line coverage**: **90%**
- minimum **branch coverage**: **90%**

### Integration Test Rules

- integration tests повинні перевіряти REST, WebSocket, persistence, migrations та інші важливі boundaries
- integration tests не повинні зводитися до одного великого сценарного тесту
- integration tests повинні бути organized by feature або by boundary
- integration tests разом з unit tests повинні підтримувати minimum **line coverage 90%** і **branch coverage 90%**

### Coverage Enforcement

- build повинен падати, якщо **line coverage** нижче **90%**
- build повинен падати, якщо **branch coverage** нижче **90%**
- coverage verification повинна виконуватись автоматично в test pipeline

### Test Naming Conventions

- `UseCase` tests: `CreateRoomUseCaseTest`
- `Adapter` tests: `RoomPersistenceAdapterTest`
- `Mapper` tests: `RoomMapperTest`
- `Integration` tests: `RoomRestIntegrationTest`, `RoomWebSocketIntegrationTest`
- method naming format: `shouldExpectedBehaviorWhenCondition`

## Architecture Decision Records (ADR)

### ADR-001: Use TDD as development workflow

Рішення: розробка відбувається через test-first approach.

### ADR-002: Use DDD for business modeling

Рішення: domain model є центром business logic, а application layer лише orchestrates use cases.

### ADR-003: Use Clean Hexagon architecture

Рішення: організувати код через `domain`, `application`, `adapters`, `configuration` з ports and adapters model.

### ADR-004: Use PostgreSQL as database

Рішення: використовувати PostgreSQL як основну relational database.
