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
@Table(name = "round_history")
public class RoundHistoryEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

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

    protected RoundHistoryEntity() {
    }

    public RoundHistoryEntity(UUID id, UUID roomId, String topic, RoundStatus status, Instant startedAt, Instant expiresAt, Instant revealedAt, long durationSeconds) {
        this.id = id;
        this.roomId = roomId;
        this.topic = topic;
        this.status = status;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
        this.revealedAt = revealedAt;
        this.durationSeconds = durationSeconds;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRoomId() {
        return roomId;
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
