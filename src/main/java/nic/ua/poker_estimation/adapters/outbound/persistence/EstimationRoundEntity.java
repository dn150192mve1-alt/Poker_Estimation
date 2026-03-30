package nic.ua.poker_estimation.adapters.outbound.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import nic.ua.poker_estimation.domain.model.RoundStatus;

@Entity
@Table(name = "estimation_rounds")
public class EstimationRoundEntity {

    @Id
    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RoundStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revealed_at")
    private Instant revealedAt;

    @Column(name = "duration_seconds", nullable = false)
    private long durationSeconds;

    protected EstimationRoundEntity() {
    }

    public EstimationRoundEntity(UUID roomId, UUID id, String topic, RoundStatus status, Instant startedAt, Instant expiresAt, Instant revealedAt, long durationSeconds) {
        this.roomId = roomId;
        this.id = id;
        this.topic = topic;
        this.status = status;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
        this.revealedAt = revealedAt;
        this.durationSeconds = durationSeconds;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public UUID getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public RoundStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevealedAt() {
        return revealedAt;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }
}
