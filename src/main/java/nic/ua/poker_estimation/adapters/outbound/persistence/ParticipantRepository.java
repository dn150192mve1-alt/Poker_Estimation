package nic.ua.poker_estimation.adapters.outbound.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<ParticipantEntity, UUID> {

    List<ParticipantEntity> findAllByRoomIdOrderByIdAsc(UUID roomId);

    void deleteAllByRoomId(UUID roomId);
}
