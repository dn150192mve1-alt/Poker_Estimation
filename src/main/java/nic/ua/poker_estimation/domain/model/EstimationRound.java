package nic.ua.poker_estimation.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import nic.ua.poker_estimation.common.service.ConflictException;
import nic.ua.poker_estimation.common.service.ValidationException;

public record EstimationRound(
    UUID id,
    String topic,
    RoundStatus status,
    Instant startedAt,
    Instant expiresAt,
    Instant revealedAt,
    long durationSeconds,
    List<Vote> votes
) {

    public EstimationRound {
        votes = List.copyOf(votes);
    }

    public static EstimationRound start(String topic, long durationSeconds, Instant startedAt) {
        if (topic == null || topic.isBlank()) {
            throw new ValidationException("Round topic must not be blank");
        }
        if (durationSeconds <= 0) {
            throw new ValidationException("Round durationSeconds must be greater than zero");
        }
        if (startedAt == null) {
            throw new ValidationException("Round startedAt must not be null");
        }
        return new EstimationRound(
            UUID.randomUUID(),
            topic.trim(),
            RoundStatus.ACTIVE,
            startedAt,
            startedAt.plusSeconds(durationSeconds),
            null,
            durationSeconds,
            List.of()
        );
    }

    public EstimationRound submitVote(UUID participantId, VoteValue value, Instant now) {
        ensureRoundIsOpen(now);

        Vote newVote = Vote.create(participantId, value);
        List<Vote> updatedVotes = new ArrayList<>(votes);
        updatedVotes.removeIf(vote -> vote.participantId().equals(participantId));
        updatedVotes.add(newVote);
        updatedVotes.sort(Comparator.comparing(Vote::participantId));
        return new EstimationRound(id, topic, status, startedAt, expiresAt, revealedAt, durationSeconds, updatedVotes);
    }

    public EstimationRound reveal(Instant now) {
        if (status == RoundStatus.REVEALED) {
            return this;
        }
        return new EstimationRound(id, topic, RoundStatus.REVEALED, startedAt, expiresAt, now, durationSeconds, votes);
    }

    public EstimationRound closeIfExpired(Instant now) {
        if (status != RoundStatus.ACTIVE || expiresAt.isAfter(now)) {
            return this;
        }
        return new EstimationRound(id, topic, RoundStatus.CLOSED, startedAt, expiresAt, revealedAt, durationSeconds, votes);
    }

    public List<UUID> submittedParticipantIds() {
        return votes.stream()
            .map(Vote::participantId)
            .sorted()
            .toList();
    }

    public List<Vote> revealedVotes() {
        return status == RoundStatus.REVEALED ? votes : List.of();
    }

    private void ensureRoundIsOpen(Instant now) {
        if (status != RoundStatus.ACTIVE || !expiresAt.isAfter(now)) {
            throw new ConflictException("Voting is closed for topic " + topic);
        }
    }
}
