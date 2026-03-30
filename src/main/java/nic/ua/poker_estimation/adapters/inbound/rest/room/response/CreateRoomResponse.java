package nic.ua.poker_estimation.adapters.inbound.rest.room.response;

import java.util.UUID;

public record CreateRoomResponse(UUID roomId, UUID ownerParticipantId, String ownerDisplayName, String deckType) {
}
