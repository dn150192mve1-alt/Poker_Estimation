package nic.ua.poker_estimation.domain.model;

import java.util.UUID;
import nic.ua.poker_estimation.common.service.ValidationException;

public record Participant(UUID id, String displayName, ParticipantRole role) {

    public static Participant createOwner(UUID id, String displayName) {
        return create(id, displayName, ParticipantRole.OWNER);
    }

    public static Participant createPlayer(UUID id, String displayName) {
        return create(id, displayName, ParticipantRole.PLAYER);
    }

    public static Participant create(UUID id, String displayName, ParticipantRole role) {
        if (id == null) {
            throw new ValidationException("Participant id must not be null");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new ValidationException("Participant displayName must not be blank");
        }
        if (role == null) {
            throw new ValidationException("Participant role must not be null");
        }
        return new Participant(id, displayName.trim(), role);
    }
}
