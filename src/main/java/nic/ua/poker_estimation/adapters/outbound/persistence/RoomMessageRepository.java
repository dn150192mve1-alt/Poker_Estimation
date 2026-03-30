package nic.ua.poker_estimation.adapters.outbound.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomMessageRepository extends JpaRepository<RoomMessageEntity, UUID> {

    List<RoomMessageEntity> findAllByRoomIdOrderBySentAtAscIdAsc(UUID roomId);

    void deleteAllByRoomId(UUID roomId);
}
