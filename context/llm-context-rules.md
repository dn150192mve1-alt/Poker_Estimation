# LLM Context Rules

## Purpose

Ці файли формують **context knowledge base** для LLM, який бере участь у розробці цього проєкту.

## Rules for AI Assistants

LLM повинен:

- використовувати ці файли як **source of truth**
- генерувати код відповідно до **TDD + DDD + Clean Hexagon**
- будувати структуру через `domain`, `application`, `adapters`, `configuration`
- дотримуватись `ports/inbound`, `ports/outbound`, `usecases`
- розглядати **WebSockets** як обов'язкову частину MVP architecture
- починати реалізацію з **integration tests**
- після integration tests писати **unit tests**
- підтримувати minimum **line coverage 90%**
- підтримувати minimum **branch coverage 90%**

## Naming Alignment

LLM повинен дотримуватись таких naming conventions:

- Use Cases: `[Action][Entity]UseCase`
- In Ports: `[Action][Entity]InPort`
- Out Ports: `[Action][Entity]OutPort`
- Persistence Adapters: `[Entity]PersistenceAdapter`
- Events: `[Entity][Action]Event`
- Tests: `[ClassName]Test`, `[Boundary]IntegrationTest`
- InPort і UseCase methods повинні мати business-oriented names, наприклад `createRoom`, `joinRoom`, `startEstimationRound`, а не generic `handle`

## Development Alignment

Будь-який згенерований код повинен:

- відповідати стандартам з `02-development-standards.md`
- відповідати scope з `03-mvp.md`
- не виходити за рамки MVP без явної вимоги
- включати WebSocket-based real-time synchronization для room state

Будь-який згенерований test code повинен:

- відповідати test structure rules з `02-development-standards.md`
- покривати scenarios через focused integration tests
- покривати domain, use cases, mappers та adapters через unit tests
- слідувати flow: **integration tests -> unit tests -> production code**
- не залишати build у стані, де coverage verification падає

## Scope Preservation

LLM не повинен змінювати business scope без явної вимоги.

LLM повинен змінювати лише ті частини архітектури, package structure та naming, які явно випливають з цього context set.
