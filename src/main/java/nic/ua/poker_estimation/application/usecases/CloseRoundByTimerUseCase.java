package nic.ua.poker_estimation.application.usecases;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import nic.ua.poker_estimation.application.ports.inbound.CloseRoundByTimerInPort;
import nic.ua.poker_estimation.application.ports.outbound.LoadRoomOutPort;
import nic.ua.poker_estimation.application.ports.outbound.PublishRoomStateOutPort;
import nic.ua.poker_estimation.application.ports.outbound.SaveRoomOutPort;
import org.springframework.stereotype.Service;

@Service
public final class CloseRoundByTimerUseCase implements CloseRoundByTimerInPort {

    private final LoadRoomOutPort loadRoomOutPort;
    private final SaveRoomOutPort saveRoomOutPort;
    private final PublishRoomStateOutPort publishRoomStateOutPort;
    private final Clock clock;

    public CloseRoundByTimerUseCase(
        LoadRoomOutPort loadRoomOutPort,
        SaveRoomOutPort saveRoomOutPort,
        PublishRoomStateOutPort publishRoomStateOutPort,
        Clock clock
    ) {
        this.loadRoomOutPort = loadRoomOutPort;
        this.saveRoomOutPort = saveRoomOutPort;
        this.publishRoomStateOutPort = publishRoomStateOutPort;
        this.clock = clock;
    }

    @Override
    public void closeRoundByTimer(UUID roomId) {
        Instant now = Instant.now(clock);
        loadRoomOutPort.load(roomId)
            .map(room -> room.closeRoundIfExpired(now))
            .filter(updatedRoom -> updatedRoom.currentRound() != null)
            .ifPresent(updatedRoom -> {
                saveRoomOutPort.save(updatedRoom);
                publishRoomStateOutPort.publish(updatedRoom);
            });
    }
}
