# Frontend API Documentation

## Purpose

This backend uses a hybrid model:

- REST for commands that create or mutate room state
- WebSocket/STOMP for real-time delivery of the current room state

For the frontend, the main integration contract is the `RoomStateResponse` object. Most write operations return it immediately via REST, and the backend also pushes the same shape to the room topic over WebSocket.

## Base URL

- REST base URL: `http://<host>:<port>/api`
- WebSocket endpoint: `ws://<host>:<port>/ws`

Default local port in `docker-compose.yml`: `8080`.

## Authentication

There is no authentication or authorization layer in the current backend implementation.

Frontend must keep these identifiers client-side:

- `roomId`
- `participantId`
- `ownerParticipantId`

## Communication Model

Recommended frontend flow for a room screen:

1. Open STOMP connection to `/ws`
2. Subscribe to `/topic/rooms/{roomId}`
3. Send a snapshot request to `/app/rooms/{roomId}/snapshot`
4. Render the received `RoomStateResponse`
5. After every user action, optimistically wait for either:
   - REST response with updated `RoomStateResponse`
   - WebSocket push with the same updated state

Important behavior:

- All mutation use cases publish the updated room state to `/topic/rooms/{roomId}`
- `GET /api/rooms/{roomId}` also returns room state, but WebSocket is the primary real-time sync channel
- WebSocket payload shape is the same as REST `RoomStateResponse`

## Domain Values

### Participant roles

- `OWNER`
- `PLAYER`

### Round statuses

- `ACTIVE`
- `CLOSED`
- `REVEALED`

### Supported deck types

- `STORY_POINTS`
- `HOURS`
- `T_SHIRT`

### Available vote values by deck

`STORY_POINTS`

- `1`
- `2`
- `3`
- `5`
- `8`
- `13`
- `21`
- `Coffee`
- `?`

`HOURS`

- `1`
- `2`
- `3`
- `4`
- `5`
- `6`
- `7`
- `8`
- `1d`
- `2d`
- `3d`
- `4d`
- `5d`
- `5d+`
- `Coffee`

`T_SHIRT`

- `XS`
- `S`
- `M`
- `L`
- `XL`

Notes:

- `deckType` input is case-insensitive on the backend, but the frontend should send canonical enum values
- vote values are matched case-insensitively, but the backend returns canonical values

## Main Response Model

### RoomStateResponse

```json
{
  "roomId": "uuid",
  "roomName": "Team Alpha",
  "deckType": "STORY_POINTS",
  "availableVoteValues": ["1", "2", "3", "5", "8", "13", "21", "Coffee", "?"],
  "ownerParticipantId": "uuid",
  "participants": [
    {
      "participantId": "uuid",
      "displayName": "Lead",
      "role": "OWNER"
    }
  ],
  "currentRound": {
    "roundId": "uuid",
    "topic": "API",
    "status": "ACTIVE",
    "startedAt": "2026-03-30T10:15:30Z",
    "expiresAt": "2026-03-30T10:16:00Z",
    "revealedAt": null,
    "durationSeconds": 30,
    "submittedParticipantIds": ["uuid"],
    "revealedVotes": []
  },
  "roundHistory": [],
  "messages": [
    {
      "messageId": "uuid",
      "participantId": "uuid",
      "participantDisplayName": "Lead",
      "content": "Welcome",
      "sentAt": "2026-03-30T10:15:35Z"
    }
  ]
}
```

### Field semantics

- `availableVoteValues`: cards allowed for the room's selected deck
- `currentRound`: `null` if no round has ever been started
- `currentRound.submittedParticipantIds`: IDs of participants who have already voted
- `currentRound.revealedVotes`: empty until round status becomes `REVEALED`
- `roundHistory`: previous rounds only; current round is not duplicated there
- `messages`: room chat history sorted by `sentAt`, then `messageId`

## REST API

### 1. Create room

`POST /api/rooms`

Request:

```json
{
  "roomName": "Team Alpha",
  "ownerDisplayName": "Lead",
  "deckType": "STORY_POINTS"
}
```

Response: `201 Created`

```json
{
  "roomId": "uuid",
  "ownerParticipantId": "uuid",
  "ownerDisplayName": "Lead",
  "deckType": "STORY_POINTS"
}
```

Frontend notes:

- save `roomId` and `ownerParticipantId`
- after creation, connect to room topic and request snapshot

### 2. Get current room state

`GET /api/rooms/{roomId}`

Response: `200 OK`

Body: `RoomStateResponse`

Use cases:

- initial page load
- hard refresh recovery
- fallback if WebSocket reconnect is needed

### 3. Join room

`POST /api/rooms/{roomId}/participants`

Request:

```json
{
  "displayName": "Alice"
}
```

Response: `201 Created`

```json
{
  "roomId": "uuid",
  "participantId": "uuid",
  "displayName": "Alice",
  "role": "PLAYER"
}
```

Frontend notes:

- save returned `participantId`
- successful join also triggers a WebSocket room-state broadcast

### 4. Send chat message

`POST /api/rooms/{roomId}/messages`

Request:

```json
{
  "participantId": "uuid",
  "content": "Welcome"
}
```

Response: `202 Accepted`

Body: `RoomStateResponse`

Notes:

- sender must already belong to the room
- message is appended to `messages`

### 5. Start estimation round

`POST /api/rooms/{roomId}/rounds`

Request:

```json
{
  "topic": "API",
  "durationSeconds": 30
}
```

Response: `202 Accepted`

Body: `RoomStateResponse`

Behavior:

- creates a new `currentRound`
- round starts with status `ACTIVE`
- timeout is scheduled until `expiresAt`
- if a previous current round exists, it is moved to `roundHistory`
- if an active round already exists, backend returns `409 Conflict`

### 6. Submit vote

`POST /api/rooms/{roomId}/votes`

Request:

```json
{
  "participantId": "uuid",
  "value": "3"
}
```

Response: `202 Accepted`

Body: `RoomStateResponse`

Behavior:

- voter must belong to the room
- there must be an active round
- vote is upserted: resubmitting replaces previous vote for the same participant
- until reveal, only `submittedParticipantIds` is exposed, not the vote values

### 7. Reveal estimations

`POST /api/rooms/{roomId}/rounds/reveal`

Request body: none

Response: `202 Accepted`

Body: `RoomStateResponse`

Behavior:

- if round was expired, backend first closes it, then reveals it
- after reveal:
  - `currentRound.status = REVEALED`
  - `currentRound.revealedAt` is filled
  - `currentRound.revealedVotes` contains all submitted votes

### 8. Restart round

`POST /api/rooms/{roomId}/rounds/restart`

Request body: none

Response: `202 Accepted`

Body: `RoomStateResponse`

Behavior:

- allowed only when there is a non-active current round
- backend starts a fresh round using the previous round's:
  - `topic`
  - `durationSeconds`
- old round is moved to `roundHistory`
- new round has empty votes and status `ACTIVE`

## WebSocket / STOMP API

### Connection

- endpoint: `/ws`
- protocol: STOMP over WebSocket

### Topic subscription

Subscribe:

`/topic/rooms/{roomId}`

Payload:

- `RoomStateResponse`

This topic receives updates after:

- room creation
- participant join
- chat message send
- round start
- vote submit
- reveal
- restart
- timer-driven round close

### Snapshot request

Send message to:

`/app/rooms/{roomId}/snapshot`

Body:

- `null` / empty payload

Result:

- backend publishes current `RoomStateResponse` to `/topic/rooms/{roomId}`

Recommended usage:

- call immediately after subscription
- call again after reconnect

## Error Contract

### Error response shape

For REST errors, backend returns:

```json
{
  "timestamp": "2026-03-30T10:20:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Participant displayName must not be blank"
}
```

### Status codes

- `404 Not Found`
  - room does not exist
- `409 Conflict`
  - bean validation errors
  - business rule conflicts
  - unsupported values

Important implementation detail:

- backend currently uses `409 Conflict` not only for business conflicts, but also for validation failures

## Known Error Messages

Frontend can display backend `message` directly.

### Room errors

- `Room not found: {roomId}`
- `Room name must not be blank`
- `Room deckType must not be blank`
- `Room deckType is not supported: {value}`

### Participant errors

- `Participant displayName must not be blank`
- `Participant name already joined room: {displayName}`
- `Participant is not part of room {roomId}: {participantId}`

### Round errors

- `Round topic must not be blank`
- `Round durationSeconds must be greater than zero`
- `Active estimation round already exists for room {roomId}`
- `There is no active estimation round for room {roomId}`
- `There is no round to reveal for room {roomId}`
- `There is no round to restart for room {roomId}`
- `Cannot restart active estimation round for room {roomId}`
- `Voting is closed for topic {topic}`

### Vote and message errors

- `Vote participantId must not be null`
- `Vote value must not be blank`
- `Vote value '{value}' is not supported for deck {deckType}`
- `Message participantId must not be null`
- `Message content must not be blank`

## Frontend Integration Recommendations

### Store locally

Persist at least:

- `roomId`
- `participantId`
- `ownerParticipantId`
- optionally `displayName`

### Detect user vote state

To show whether current user already voted:

- check whether current user's `participantId` is present in `currentRound.submittedParticipantIds`

### Detect owner UI

To determine if current user is room owner:

- compare local `participantId` with `ownerParticipantId`

### Show timer

Frontend should derive countdown from:

- `currentRound.expiresAt`
- current client time

When timer reaches zero:

- expect room state to switch to `CLOSED` through WebSocket or next fetch
- submitting votes after expiration returns `409 Conflict`

### Reveal privacy

Before reveal:

- do not expect any vote values from backend
- only submission presence is exposed

After reveal:

- use `currentRound.revealedVotes`

## Suggested Frontend Flows

### Create room flow

1. `POST /api/rooms`
2. Save `roomId` and `ownerParticipantId`
3. Treat owner's local `participantId` as `ownerParticipantId`
4. Connect WebSocket
5. Subscribe to `/topic/rooms/{roomId}`
6. Send `/app/rooms/{roomId}/snapshot`

### Join room flow

1. `POST /api/rooms/{roomId}/participants`
2. Save returned `participantId`
3. Connect WebSocket
4. Subscribe to `/topic/rooms/{roomId}`
5. Send `/app/rooms/{roomId}/snapshot`

### In-room sync strategy

1. Use WebSocket topic as primary source of live updates
2. Use REST mutation responses as immediate confirmations
3. On reconnect or page reload, request snapshot or call `GET /api/rooms/{roomId}`

## Current Backend Constraints

- no leave-room API
- no participant removal API
- no dedicated owner-only protection on round actions
- no authentication, sessions, or tokens
- no pagination for messages or round history
- no separate DTO for partial updates; backend always works with whole-room state snapshots
