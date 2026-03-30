package nic.ua.poker_estimation.adapters.outbound.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "round_history_votes")
public class RoundHistoryVoteEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "round_id", nullable = false)
    private UUID roundId;

    @Column(name = "participant_id", nullable = false)
    private UUID participantId;

    @Column(name = "vote_value", nullable = false)
    private String value;

    protected RoundHistoryVoteEntity() {
    }

    public RoundHistoryVoteEntity(UUID id, UUID roundId, UUID participantId, String value) {
        this.id = id;
        this.roundId = roundId;
        this.participantId = participantId;
        this.value = value;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRoundId() {
        return roundId;
    }

    public UUID getParticipantId() {
        return participantId;
    }

    public String getValue() {
        return value;
    }
}
