package nic.ua.poker_estimation.adapters.inbound.rest.room.request;

import jakarta.validation.constraints.NotBlank;

public record JoinParticipantRequest(
    @NotBlank(message = "Participant displayName must not be blank")
    String displayName
) {
}
