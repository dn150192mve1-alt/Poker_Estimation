package nic.ua.poker_estimation.adapters.outbound.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import nic.ua.poker_estimation.domain.model.DeckType;

@Entity
@Table(name = "rooms")
public class RoomEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "deck_type", nullable = false)
    private DeckType deckType;

    @Column(name = "owner_participant_id")
    private UUID ownerParticipantId;

    protected RoomEntity() {
    }

    public RoomEntity(UUID id, String name, DeckType deckType, UUID ownerParticipantId) {
        this.id = id;
        this.name = name;
        this.deckType = deckType;
        this.ownerParticipantId = ownerParticipantId;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DeckType getDeckType() {
        return deckType;
    }

    public UUID getOwnerParticipantId() {
        return ownerParticipantId;
    }
}
