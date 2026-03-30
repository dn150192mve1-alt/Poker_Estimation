package nic.ua.poker_estimation.adapters.outbound.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoundHistoryVoteRepository extends JpaRepository<RoundHistoryVoteEntity, UUID> {

    List<RoundHistoryVoteEntity> findAllByRoundIdOrderByParticipantIdAsc(UUID roundId);

    void deleteAllByRoundId(UUID roundId);
}
