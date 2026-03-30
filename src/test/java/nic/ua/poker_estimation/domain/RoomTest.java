package nic.ua.poker_estimation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import nic.ua.poker_estimation.common.service.ConflictException;
import nic.ua.poker_estimation.common.service.ValidationException;
import nic.ua.poker_estimation.domain.model.DeckType;
import nic.ua.poker_estimation.domain.model.Room;
import nic.ua.poker_estimation.domain.model.RoundStatus;
import org.junit.jupiter.api.Test;

class RoomTest {

    @Test
    void shouldCreateRoomJoinPlayersRunRoundRestartAndChat() {
        Instant now = Instant.parse("2026-03-11T20:00:00Z");
        Room room = Room.create(UUID.randomUUID(), "Team Alpha", DeckType.STORY_POINTS, UUID.randomUUID(), "Owner");
        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();

        room = room.joinParticipant(aliceId, "Alice").room();
        room = room.joinParticipant(bobId, "Bob").room();
        room = room.startRound("API", 10, now);
        room = room.submitVote(aliceId, "3", now.plusSeconds(1));
        room = room.submitVote(bobId, "5", now.plusSeconds(2));
        room = room.revealEstimations(now.plusSeconds(3));
        room = room.postMessage(aliceId, "Looks good", now.plusSeconds(4));
        room = room.startRound("API-2", 5, now.plusSeconds(5));
        room = room.revealEstimations(now.plusSeconds(6));
        room = room.restartRound(now.plusSeconds(7));

        assertThat(room.currentRound().topic()).isEqualTo("API-2");
        assertThat(room.currentRound().status()).isEqualTo(RoundStatus.ACTIVE);
        assertThat(room.roundHistory()).hasSize(2);
        assertThat(room.messages()).hasSize(1);
        assertThat(room.messages().getFirst().participantDisplayName()).isEqualTo("Alice");
    }

    @Test
    void shouldRejectDuplicateNamesUnsupportedVotesAndRestartWithoutFinishedRound() {
        Instant now = Instant.parse("2026-03-11T20:00:00Z");
        UUID ownerId = UUID.randomUUID();
        Room room = Room.create(UUID.randomUUID(), "Team Alpha", DeckType.HOURS, ownerId, "Owner");
        room = room.joinParticipant(UUID.randomUUID(), "Alice").room();

        Room currentRoom = room;
        assertThatThrownBy(() -> currentRoom.joinParticipant(UUID.randomUUID(), "alice"))
            .isInstanceOf(ConflictException.class)
            .hasMessage("Participant name already joined room: alice");

        assertThatThrownBy(() -> currentRoom.joinParticipant(UUID.randomUUID(), null))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Participant displayName must not be blank");

        Room roomWithRound = room.startRound("API", 10, now);
        assertThatThrownBy(() -> roomWithRound.submitVote(ownerId, "13", now.plusSeconds(1)))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Vote value '13' is not supported for deck HOURS");

        assertThatThrownBy(() -> roomWithRound.restartRound(now.plusSeconds(1)))
            .isInstanceOf(ConflictException.class)
            .hasMessage("Cannot restart active estimation round for room " + room.id());

        assertThatThrownBy(() -> roomWithRound.startRound("Other", 5, now.plusSeconds(1)))
            .isInstanceOf(ConflictException.class)
            .hasMessage("Active estimation round already exists for room " + room.id());
    }

    @Test
    void shouldValidateRoomCreationUnknownParticipantsAndMissingRounds() {
        UUID roomId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Room room = Room.create(roomId, "Room", DeckType.T_SHIRT, ownerId, "Owner");
        Instant now = Instant.parse("2026-03-11T20:00:00Z");

        assertThatThrownBy(() -> Room.create(null, "Room", DeckType.STORY_POINTS, ownerId, "Owner"))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Room id must not be null");

        assertThatThrownBy(() -> Room.create(UUID.randomUUID(), null, DeckType.STORY_POINTS, ownerId, "Owner"))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Room name must not be blank");

        assertThatThrownBy(() -> Room.create(UUID.randomUUID(), " ", DeckType.STORY_POINTS, ownerId, "Owner"))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Room name must not be blank");

        assertThatThrownBy(() -> Room.create(UUID.randomUUID(), "Room", null, ownerId, "Owner"))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Room deckType must not be null");

        assertThatThrownBy(() -> room.revealEstimations(now))
            .isInstanceOf(ConflictException.class)
            .hasMessage("There is no round to reveal for room " + room.id());

        assertThatThrownBy(() -> room.restartRound(now))
            .isInstanceOf(ConflictException.class)
            .hasMessage("There is no round to restart for room " + room.id());

        assertThatThrownBy(() -> room.postMessage(UUID.randomUUID(), "Hello", now))
            .isInstanceOf(ConflictException.class)
            .hasMessageStartingWith("Participant is not part of room " + room.id());

        assertThatThrownBy(() -> room.submitVote(UUID.randomUUID(), "XS", now))
            .isInstanceOf(ConflictException.class)
            .hasMessageStartingWith("Participant is not part of room " + room.id());

        Room closedRoom = room.startRound("Sizing", 1, now).closeRoundIfExpired(now.plusSeconds(2));
        assertThatThrownBy(() -> closedRoom.submitVote(ownerId, "XS", now.plusSeconds(3)))
            .isInstanceOf(ConflictException.class)
            .hasMessage("There is no active estimation round for room " + room.id());

        assertThat(room.closeRoundIfExpired(now)).isEqualTo(room);
    }
}
