package nic.ua.poker_estimation.application.usecases;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import nic.ua.poker_estimation.application.ports.inbound.RestartEstimationRoundInPort;
import nic.ua.poker_estimation.application.ports.outbound.LoadRoomOutPort;
import nic.ua.poker_estimation.application.ports.outbound.PublishRoomStateOutPort;
import nic.ua.poker_estimation.application.ports.outbound.SaveRoomOutPort;
import nic.ua.poker_estimation.application.ports.outbound.ScheduleRoundTimeoutOutPort;
import nic.ua.poker_estimation.common.service.NotFoundException;
import nic.ua.poker_estimation.domain.model.Room;
import org.springframework.stereotype.Service;

@Service
public final class RestartEstimationRoundUseCase implements RestartEstimationRoundInPort {

    private final LoadRoomOutPort loadRoomOutPort;
    private final SaveRoomOutPort saveRoomOutPort;
    private final PublishRoomStateOutPort publishRoomStateOutPort;
    private final ScheduleRoundTimeoutOutPort scheduleRoundTimeoutOutPort;
    private final Clock clock;

    public RestartEstimationRoundUseCase(
        LoadRoomOutPort loadRoomOutPort,
        SaveRoomOutPort saveRoomOutPort,
        PublishRoomStateOutPort publishRoomStateOutPort,
        ScheduleRoundTimeoutOutPort scheduleRoundTimeoutOutPort,
        Clock clock
    ) {
        this.loadRoomOutPort = loadRoomOutPort;
        this.saveRoomOutPort = saveRoomOutPort;
        this.publishRoomStateOutPort = publishRoomStateOutPort;
        this.scheduleRoundTimeoutOutPort = scheduleRoundTimeoutOutPort;
        this.clock = clock;
    }

    @Override
    public Room restartEstimationRound(UUID roomId) {
        Instant now = Instant.now(clock);
        Room room = loadRoomOutPort.load(roomId)
            .orElseThrow(() -> new NotFoundException("Room not found: " + roomId));
        Room savedRoom = saveRoomOutPort.save(room.restartRound(now));
        scheduleRoundTimeoutOutPort.schedule(roomId, savedRoom.currentRound().expiresAt());
        publishRoomStateOutPort.publish(savedRoom);
        return savedRoom;
    }
}
