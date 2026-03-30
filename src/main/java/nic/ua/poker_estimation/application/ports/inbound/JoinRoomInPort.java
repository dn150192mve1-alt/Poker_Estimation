package nic.ua.poker_estimation.application.ports.inbound;

import java.util.UUID;
import nic.ua.poker_estimation.domain.model.ParticipantRole;
import nic.ua.poker_estimation.domain.model.Room;

public interface JoinRoomInPort {

    JoinRoomResult joinRoom(UUID roomId, String displayName);

    record JoinRoomResult(Room room, UUID participantId, String displayName, ParticipantRole role) {
    }
}
