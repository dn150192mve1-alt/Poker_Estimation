package nic.ua.poker_estimation.adapters.inbound.rest.room.response;

import java.util.List;
import java.util.UUID;

public record RoomStateResponse(
    UUID roomId,
    String roomName,
    String deckType,
    List<String> availableVoteValues,
    UUID ownerParticipantId,
    List<ParticipantResponse> participants,
    CurrentRoundResponse currentRound,
    List<CurrentRoundResponse> roundHistory,
    List<MessageResponse> messages
) {
}
