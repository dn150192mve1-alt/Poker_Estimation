# Project Context

## Project Overview

Проєкт є web application для **Planning Poker**, який допомагає команді проводити estimation session у спільному просторі.

Основні можливості:
- створення кімнат для сесій оцінювання
- приєднання учасників до кімнати
- оцінювання задач у межах окремого раунду
- використання timer для обмеження часу голосування

## Communication Model

Проєкт використовує hybrid communication model:

- **REST API** для command-oriented operations
- **WebSockets** для real-time state updates

REST API використовується для дій, які змінюють стан системи, наприклад:

- create room
- join room
- start estimation round
- submit vote
- reveal estimations

WebSockets використовуються для доставки актуального стану кімнати всім connected participants без polling.

Через WebSockets система повинна передавати:

- changes у складі participants
- changes у current room state
- round status updates
- timer-related updates
- reveal results

## Core Concepts

### Room

**Room** це спільний простір, у межах якого відбувається estimation session.

Роль у системі:
- об'єднує учасників однієї сесії
- зберігає поточний стан голосування
- є джерелом room state, який broadcast-иться через WebSockets

### Participant

**Participant** це користувач, який приєднався до конкретної кімнати для участі в оцінюванні.

Роль у системі:
- представляє окремого учасника сесії
- надсилає estimation value під час раунду
- реагує на real-time updates без необхідності polling

### Estimation Round

**Estimation Round** це окремий цикл голосування для однієї задачі або одного питання на оцінку.

Роль у системі:
- визначає активний етап estimation process
- керує початком, перебігом і завершенням голосування
- пов'язує votes учасників у межах одного раунду

### Vote

**Vote** це estimation value, яку participant надсилає під час estimation round.

Роль у системі:
- фіксує індивідуальну оцінку учасника
- використовується для подальшого reveal estimations
- є базовою одиницею результату голосування

### Timer

**Timer** це механізм контролю часу для активного estimation round.

Роль у системі:
- обмежує тривалість voting phase
- сигналізує про завершення часу голосування
- допомагає синхронізувати стан раунду для всіх учасників
