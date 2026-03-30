package nic.ua.poker_estimation.adapters.inbound.rest.room.response;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(UUID messageId, UUID participantId, String participantDisplayName, String content, Instant sentAt) {
}
