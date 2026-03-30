package nic.ua.poker_estimation.adapters.inbound.rest.room;

import nic.ua.poker_estimation.adapters.inbound.rest.room.response.CurrentRoundResponse;
import nic.ua.poker_estimation.adapters.inbound.rest.room.response.MessageResponse;
import nic.ua.poker_estimation.adapters.inbound.rest.room.response.ParticipantResponse;
import nic.ua.poker_estimation.adapters.inbound.rest.room.response.RoomStateResponse;
import nic.ua.poker_estimation.adapters.inbound.rest.room.response.VoteResponse;
import nic.ua.poker_estimation.domain.model.ChatMessage;
import nic.ua.poker_estimation.domain.model.EstimationRound;
import nic.ua.poker_estimation.domain.model.Participant;
import nic.ua.poker_estimation.domain.model.Room;

public final class RoomStateResponseMapper {

    private RoomStateResponseMapper() {
    }

    public static RoomStateResponse map(Room room) {
        return new RoomStateResponse(
            room.id(),
            room.name(),
            room.deckType().name(),
            room.deckType().allowedValues(),
            room.ownerParticipantId(),
            room.participants().stream().map(RoomStateResponseMapper::mapParticipant).toList(),
            room.currentRound() == null ? null : mapRound(room.currentRound()),
            room.roundHistory().stream().map(RoomStateResponseMapper::mapRound).toList(),
            room.messages().stream().map(RoomStateResponseMapper::mapMessage).toList()
        );
    }

    private static ParticipantResponse mapParticipant(Participant participant) {
        return new ParticipantResponse(participant.id(), participant.displayName(), participant.role().name());
    }

    private static CurrentRoundResponse mapRound(EstimationRound round) {
        return new CurrentRoundResponse(
            round.id(),
            round.topic(),
            round.status().name(),
            round.startedAt(),
            round.expiresAt(),
            round.revealedAt(),
            round.durationSeconds(),
            round.submittedParticipantIds(),
            round.revealedVotes().stream()
                .map(vote -> new VoteResponse(vote.participantId(), vote.value().value()))
                .toList()
        );
    }

    private static MessageResponse mapMessage(ChatMessage message) {
        return new MessageResponse(message.id(), message.participantId(), message.participantDisplayName(), message.content(), message.sentAt());
    }
}
