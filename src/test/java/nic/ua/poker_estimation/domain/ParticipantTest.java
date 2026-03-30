package nic.ua.poker_estimation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import nic.ua.poker_estimation.common.service.ValidationException;
import nic.ua.poker_estimation.domain.model.Participant;
import nic.ua.poker_estimation.domain.model.ParticipantRole;
import org.junit.jupiter.api.Test;

class ParticipantTest {

    @Test
    void shouldCreateParticipantWithTrimmedNameAndRole() {
        Participant participant = Participant.create(UUID.randomUUID(), "  Alice  ", ParticipantRole.PLAYER);

        assertThat(participant.displayName()).isEqualTo("Alice");
        assertThat(participant.role()).isEqualTo(ParticipantRole.PLAYER);
    }

    @Test
    void shouldRejectInvalidParticipantValues() {
        assertThatThrownBy(() -> Participant.create(UUID.randomUUID(), " ", ParticipantRole.PLAYER))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Participant displayName must not be blank");

        assertThatThrownBy(() -> Participant.create(UUID.randomUUID(), null, ParticipantRole.PLAYER))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Participant displayName must not be blank");

        assertThatThrownBy(() -> Participant.create(null, "Alice", ParticipantRole.PLAYER))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Participant id must not be null");

        assertThatThrownBy(() -> Participant.create(UUID.randomUUID(), "Alice", null))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Participant role must not be null");
    }
}
