package nic.ua.poker_estimation.application.usecases;

import java.util.UUID;
import nic.ua.poker_estimation.application.ports.inbound.FindRoomStateInPort;
import nic.ua.poker_estimation.application.ports.outbound.LoadRoomOutPort;
import nic.ua.poker_estimation.common.service.NotFoundException;
import nic.ua.poker_estimation.domain.model.Room;
import org.springframework.stereotype.Service;

@Service
public final class FindRoomStateUseCase implements FindRoomStateInPort {

    private final LoadRoomOutPort loadRoomOutPort;

    public FindRoomStateUseCase(LoadRoomOutPort loadRoomOutPort) {
        this.loadRoomOutPort = loadRoomOutPort;
    }

    @Override
    public Room findRoomState(UUID roomId) {
        return loadRoomOutPort.load(roomId)
            .orElseThrow(() -> new NotFoundException("Room not found: " + roomId));
    }
}
