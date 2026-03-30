package nic.ua.poker_estimation.adapters.inbound.rest.room.response;

import java.util.UUID;

public record ParticipantResponse(UUID participantId, String displayName, String role) {
}
