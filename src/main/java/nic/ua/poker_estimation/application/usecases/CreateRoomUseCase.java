package nic.ua.poker_estimation.application.usecases;

import java.util.UUID;
import nic.ua.poker_estimation.application.ports.inbound.CreateRoomInPort;
import nic.ua.poker_estimation.application.ports.outbound.PublishRoomStateOutPort;
import nic.ua.poker_estimation.application.ports.outbound.SaveRoomOutPort;
import nic.ua.poker_estimation.domain.model.DeckType;
import nic.ua.poker_estimation.domain.model.Room;
import org.springframework.stereotype.Service;

@Service
public final class CreateRoomUseCase implements CreateRoomInPort {

    private final SaveRoomOutPort saveRoomOutPort;
    private final PublishRoomStateOutPort publishRoomStateOutPort;

    public CreateRoomUseCase(SaveRoomOutPort saveRoomOutPort, PublishRoomStateOutPort publishRoomStateOutPort) {
        this.saveRoomOutPort = saveRoomOutPort;
        this.publishRoomStateOutPort = publishRoomStateOutPort;
    }

    @Override
    public Room createRoom(String roomName, String ownerDisplayName, DeckType deckType) {
        Room room = saveRoomOutPort.save(Room.create(UUID.randomUUID(), roomName, deckType, UUID.randomUUID(), ownerDisplayName));
        publishRoomStateOutPort.publish(room);
        return room;
    }
}
