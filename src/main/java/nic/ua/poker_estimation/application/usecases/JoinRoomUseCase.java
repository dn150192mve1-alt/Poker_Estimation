package nic.ua.poker_estimation.application.usecases;

import java.util.UUID;
import nic.ua.poker_estimation.application.ports.inbound.JoinRoomInPort;
import nic.ua.poker_estimation.application.ports.outbound.LoadRoomOutPort;
import nic.ua.poker_estimation.application.ports.outbound.PublishRoomStateOutPort;
import nic.ua.poker_estimation.application.ports.outbound.SaveRoomOutPort;
import nic.ua.poker_estimation.common.service.NotFoundException;
import nic.ua.poker_estimation.domain.model.Room;
import org.springframework.stereotype.Service;

@Service
public final class JoinRoomUseCase implements JoinRoomInPort {

    private final LoadRoomOutPort loadRoomOutPort;
    private final SaveRoomOutPort saveRoomOutPort;
    private final PublishRoomStateOutPort publishRoomStateOutPort;

    public JoinRoomUseCase(
        LoadRoomOutPort loadRoomOutPort,
        SaveRoomOutPort saveRoomOutPort,
        PublishRoomStateOutPort publishRoomStateOutPort
    ) {
        this.loadRoomOutPort = loadRoomOutPort;
        this.saveRoomOutPort = saveRoomOutPort;
        this.publishRoomStateOutPort = publishRoomStateOutPort;
    }

    @Override
    public JoinRoomResult joinRoom(UUID roomId, String displayName) {
        Room room = loadRoomOutPort.load(roomId)
            .orElseThrow(() -> new NotFoundException("Room not found: " + roomId));
        Room.JoinParticipantResult joinResult = room.joinParticipant(UUID.randomUUID(), displayName);
        Room savedRoom = saveRoomOutPort.save(joinResult.room());
        publishRoomStateOutPort.publish(savedRoom);
        return new JoinRoomResult(savedRoom, joinResult.participant().id(), joinResult.participant().displayName(), joinResult.participant().role());
    }
}
