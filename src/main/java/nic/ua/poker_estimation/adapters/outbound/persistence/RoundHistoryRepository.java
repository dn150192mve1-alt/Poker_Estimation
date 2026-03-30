package nic.ua.poker_estimation.adapters.outbound.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoundHistoryRepository extends JpaRepository<RoundHistoryEntity, UUID> {

    List<RoundHistoryEntity> findAllByRoomIdOrderByStartedAtAsc(UUID roomId);

    void deleteAllByRoomId(UUID roomId);
}
