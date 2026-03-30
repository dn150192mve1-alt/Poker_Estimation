package nic.ua.poker_estimation.domain.model;

import java.util.UUID;
import nic.ua.poker_estimation.common.service.ValidationException;

public record Vote(UUID participantId, VoteValue value) {

    public static Vote create(UUID participantId, VoteValue value) {
        if (participantId == null) {
            throw new ValidationException("Vote participantId must not be null");
        }
        if (value == null) {
            throw new ValidationException("Vote value must not be null");
        }
        return new Vote(participantId, value);
    }
}
