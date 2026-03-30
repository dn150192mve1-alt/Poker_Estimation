package nic.ua.poker_estimation.adapters.inbound.rest.room.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CurrentRoundResponse(
    UUID roundId,
    String topic,
    String status,
    Instant startedAt,
    Instant expiresAt,
    Instant revealedAt,
    long durationSeconds,
    List<UUID> submittedParticipantIds,
    List<VoteResponse> revealedVotes
) {
}
