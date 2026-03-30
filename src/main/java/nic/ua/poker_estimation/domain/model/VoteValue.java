package nic.ua.poker_estimation.domain.model;

import nic.ua.poker_estimation.common.service.ValidationException;

public record VoteValue(String value) {

    public static VoteValue create(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Vote value must not be blank");
        }
        return new VoteValue(value.trim());
    }
}
