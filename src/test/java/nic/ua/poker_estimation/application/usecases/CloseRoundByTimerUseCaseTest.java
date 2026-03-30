package nic.ua.poker_estimation.application.usecases;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import nic.ua.poker_estimation.application.ports.outbound.LoadRoomOutPort;
import nic.ua.poker_estimation.application.ports.outbound.PublishRoomStateOutPort;
import nic.ua.poker_estimation.application.ports.outbound.SaveRoomOutPort;
import nic.ua.poker_estimation.domain.model.DeckType;
import nic.ua.poker_estimation.domain.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CloseRoundByTimerUseCaseTest {

    @Mock
    private LoadRoomOutPort loadRoomOutPort;

    @Mock
    private SaveRoomOutPort saveRoomOutPort;

    @Mock
    private PublishRoomStateOutPort publishRoomStateOutPort;

    private CloseRoundByTimerUseCase closeRoundByTimerUseCase;

    @BeforeEach
    void setUp() {
        closeRoundByTimerUseCase = new CloseRoundByTimerUseCase(
            loadRoomOutPort,
            saveRoomOutPort,
            publishRoomStateOutPort,
            Clock.fixed(Instant.parse("2026-03-11T20:00:02Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void shouldIgnoreMissingRoom() {
        when(loadRoomOutPort.load(any())).thenReturn(Optional.empty());

        closeRoundByTimerUseCase.closeRoundByTimer(UUID.randomUUID());

        verify(saveRoomOutPort, never()).save(any());
        verify(publishRoomStateOutPort, never()).publish(any());
    }

    @Test
    void shouldPersistAndPublishExpiredRoom() {
        UUID roomId = UUID.randomUUID();
        Room room = Room.create(roomId, "Room", DeckType.STORY_POINTS, UUID.randomUUID(), "Owner")
            .startRound("Topic", 1, Instant.parse("2026-03-11T20:00:00Z"));
        when(loadRoomOutPort.load(roomId)).thenReturn(Optional.of(room));

        closeRoundByTimerUseCase.closeRoundByTimer(roomId);

        verify(saveRoomOutPort).save(any(Room.class));
        verify(publishRoomStateOutPort).publish(any(Room.class));
    }

    @Test
    void shouldIgnoreRoomWithoutRound() {
        UUID roomId = UUID.randomUUID();
        when(loadRoomOutPort.load(roomId)).thenReturn(Optional.of(Room.create(roomId, "Room", DeckType.STORY_POINTS, UUID.randomUUID(), "Owner")));

        closeRoundByTimerUseCase.closeRoundByTimer(roomId);

        verify(saveRoomOutPort, never()).save(any());
        verify(publishRoomStateOutPort, never()).publish(any());
    }
}
