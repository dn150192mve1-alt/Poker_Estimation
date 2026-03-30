package nic.ua.poker_estimation.domain.model;

import java.time.Instant;
import java.util.UUID;
import nic.ua.poker_estimation.common.service.ValidationException;

public record ChatMessage(
    UUID id,
    UUID participantId,
    String participantDisplayName,
    String content,
    Instant sentAt
) {

    public static ChatMessage create(UUID id, UUID participantId, String participantDisplayName, String content, Instant sentAt) {
        if (id == null) {
            throw new ValidationException("Message id must not be null");
        }
        if (participantId == null) {
            throw new ValidationException("Message participantId must not be null");
        }
        if (participantDisplayName == null || participantDisplayName.isBlank()) {
            throw new ValidationException("Message participantDisplayName must not be blank");
        }
        if (content == null || content.isBlank()) {
            throw new ValidationException("Message content must not be blank");
        }
        if (sentAt == null) {
            throw new ValidationException("Message sentAt must not be null");
        }
        return new ChatMessage(id, participantId, participantDisplayName.trim(), content.trim(), sentAt);
    }
}
