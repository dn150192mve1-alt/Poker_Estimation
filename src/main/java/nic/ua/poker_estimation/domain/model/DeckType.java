package nic.ua.poker_estimation.domain.model;

import java.util.List;
import java.util.Locale;
import nic.ua.poker_estimation.common.service.ValidationException;

public enum DeckType {
    STORY_POINTS(List.of("1", "2", "3", "5", "8", "13", "21", "Coffee", "?")),
    HOURS(List.of("1", "2", "3", "4", "5", "6", "7", "8", "1d", "2d", "3d", "4d", "5d", "5d+", "Coffee")),
    T_SHIRT(List.of("XS", "S", "M", "L", "XL"));

    private final List<String> allowedValues;

    DeckType(List<String> allowedValues) {
        this.allowedValues = List.copyOf(allowedValues);
    }

    public List<String> allowedValues() {
        return allowedValues;
    }

    public VoteValue createVoteValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new ValidationException("Vote value must not be blank");
        }
        String normalized = rawValue.trim();
        String canonicalValue = allowedValues.stream()
            .filter(allowedValue -> allowedValue.equalsIgnoreCase(normalized))
            .findFirst()
            .orElseThrow(() -> new ValidationException("Vote value '%s' is not supported for deck %s".formatted(normalized, name())));
        return VoteValue.create(canonicalValue);
    }

    public static DeckType fromValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new ValidationException("Room deckType must not be blank");
        }
        try {
            return DeckType.valueOf(rawValue.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("Room deckType is not supported: " + rawValue.trim());
        }
    }
}
