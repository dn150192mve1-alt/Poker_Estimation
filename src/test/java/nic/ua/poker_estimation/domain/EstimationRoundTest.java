package nic.ua.poker_estimation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import nic.ua.poker_estimation.common.service.ConflictException;
import nic.ua.poker_estimation.common.service.ValidationException;
import nic.ua.poker_estimation.domain.model.DeckType;
import nic.ua.poker_estimation.domain.model.EstimationRound;
import nic.ua.poker_estimation.domain.model.RoundStatus;
import org.junit.jupiter.api.Test;

class EstimationRoundTest {

    @Test
    void shouldRejectInvalidRoundParameters() {
        Instant now = Instant.parse("2026-03-11T20:00:00Z");

        assertThatThrownBy(() -> EstimationRound.start(" ", 10, now))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Round topic must not be blank");

        assertThatThrownBy(() -> EstimationRound.start("Topic", 0, now))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Round durationSeconds must be greater than zero");

        assertThatThrownBy(() -> EstimationRound.start("Topic", 10, null))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Round startedAt must not be null");
    }

    @Test
    void shouldReplaceExistingVoteAndRevealValues() {
        Instant now = Instant.parse("2026-03-11T20:00:00Z");
        UUID participantOne = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID participantTwo = UUID.fromString("00000000-0000-0000-0000-000000000001");

        EstimationRound round = EstimationRound.start("Topic", 10, now)
            .submitVote(participantOne, DeckType.STORY_POINTS.createVoteValue("1"), now.plusSeconds(1))
            .submitVote(participantTwo, DeckType.STORY_POINTS.createVoteValue("2"), now.plusSeconds(2))
            .submitVote(participantOne, DeckType.STORY_POINTS.createVoteValue("8"), now.plusSeconds(3))
            .reveal(now.plusSeconds(4));

        assertThat(round.submittedParticipantIds()).containsExactly(participantTwo, participantOne);
        assertThat(round.revealedVotes()).extracting(vote -> vote.value().value()).containsExactly("2", "8");
        assertThat(round.status()).isEqualTo(RoundStatus.REVEALED);
        assertThat(round.reveal(now.plusSeconds(5))).isEqualTo(round);
    }

    @Test
    void shouldRejectVoteWhenClosedAndCloseExpiredRound() {
        Instant now = Instant.parse("2026-03-11T20:00:00Z");
        UUID participantId = UUID.randomUUID();
        EstimationRound round = EstimationRound.start("Topic", 1, now);

        assertThatThrownBy(() -> round.submitVote(participantId, DeckType.STORY_POINTS.createVoteValue("1"), now.plusSeconds(2)))
            .isInstanceOf(ConflictException.class)
            .hasMessage("Voting is closed for topic Topic");

        EstimationRound closedRound = round.closeIfExpired(now.plusSeconds(2));
        assertThat(closedRound.status()).isEqualTo(RoundStatus.CLOSED);
        assertThat(closedRound.closeIfExpired(now.plusSeconds(3))).isEqualTo(closedRound);

        EstimationRound stillActive = round.closeIfExpired(now);
        assertThat(stillActive.status()).isEqualTo(RoundStatus.ACTIVE);
    }
}
