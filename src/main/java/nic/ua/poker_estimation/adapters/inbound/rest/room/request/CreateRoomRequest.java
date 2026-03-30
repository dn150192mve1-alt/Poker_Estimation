package nic.ua.poker_estimation.adapters.inbound.rest.room.request;

import jakarta.validation.constraints.NotBlank;

public record CreateRoomRequest(
    @NotBlank(message = "Room name must not be blank")
    String roomName,
    @NotBlank(message = "Participant displayName must not be blank")
    String ownerDisplayName,
    @NotBlank(message = "Room deckType must not be blank")
    String deckType
) {
}
