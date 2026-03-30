package nic.ua.poker_estimation.adapters.outbound.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "room_messages")
public class RoomMessageEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "participant_id", nullable = false)
    private UUID participantId;

    @Column(name = "participant_display_name", nullable = false)
    private String participantDisplayName;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    protected RoomMessageEntity() {
    }

    public RoomMessageEntity(UUID id, UUID roomId, UUID participantId, String participantDisplayName, String content, Instant sentAt) {
        this.id = id;
        this.roomId = roomId;
        this.participantId = participantId;
        this.participantDisplayName = participantDisplayName;
        this.content = content;
        this.sentAt = sentAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public UUID getParticipantId() {
        return participantId;
    }

    public String getParticipantDisplayName() {
        return participantDisplayName;
    }

    public String getContent() {
        return content;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
