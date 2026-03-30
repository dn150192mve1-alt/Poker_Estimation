package nic.ua.poker_estimation.adapters.inbound.rest.room.response;

import java.util.UUID;

public record VoteResponse(UUID participantId, String value) {
}
