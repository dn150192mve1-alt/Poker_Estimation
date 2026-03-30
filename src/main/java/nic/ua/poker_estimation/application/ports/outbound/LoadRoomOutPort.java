package nic.ua.poker_estimation.application.ports.outbound;

import java.util.Optional;
import java.util.UUID;
import nic.ua.poker_estimation.domain.model.Room;

public interface LoadRoomOutPort {

    Optional<Room> load(UUID roomId);
}
