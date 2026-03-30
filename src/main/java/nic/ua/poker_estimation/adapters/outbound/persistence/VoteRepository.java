package nic.ua.poker_estimation.adapters.outbound.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<VoteEntity, UUID> {

    List<VoteEntity> findAllByRoundIdOrderByParticipantIdAsc(UUID roundId);

    void deleteAllByRoundId(UUID roundId);
}
