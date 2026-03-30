package nic.ua.poker_estimation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import nic.ua.poker_estimation.common.service.ValidationException;
import nic.ua.poker_estimation.domain.model.DeckType;
import nic.ua.poker_estimation.domain.model.Vote;
import nic.ua.poker_estimation.domain.model.VoteValue;
import org.junit.jupiter.api.Test;

class VoteTest {

    @Test
    void shouldCreateVoteWithDeckSpecificValue() {
        Vote vote = Vote.create(UUID.randomUUID(), DeckType.STORY_POINTS.createVoteValue("coffee"));

        assertThat(vote.value().value()).isEqualTo("Coffee");
        assertThat(DeckType.fromValue("hours")).isEqualTo(DeckType.HOURS);
    }

    @Test
    void shouldRejectNullOrUnsupportedValues() {
        assertThatThrownBy(() -> Vote.create(null, DeckType.STORY_POINTS.createVoteValue("1")))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Vote participantId must not be null");

        assertThatThrownBy(() -> Vote.create(UUID.randomUUID(), null))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Vote value must not be null");

        assertThatThrownBy(() -> VoteValue.create(null))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Vote value must not be blank");

        assertThatThrownBy(() -> VoteValue.create(" "))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Vote value must not be blank");

        assertThatThrownBy(() -> DeckType.HOURS.createVoteValue(null))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Vote value must not be blank");

        assertThatThrownBy(() -> DeckType.HOURS.createVoteValue("13"))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Vote value '13' is not supported for deck HOURS");

        assertThatThrownBy(() -> DeckType.STORY_POINTS.createVoteValue(" "))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Vote value must not be blank");

        assertThatThrownBy(() -> DeckType.fromValue(null))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Room deckType must not be blank");

        assertThatThrownBy(() -> DeckType.fromValue(" "))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Room deckType must not be blank");

        assertThatThrownBy(() -> DeckType.fromValue("unknown"))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Room deckType is not supported: unknown");
    }
}
