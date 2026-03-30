package nic.ua.poker_estimation.adapters.inbound.rest.estimation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SubmitVoteRequest(
    @NotNull(message = "Vote participantId must not be null")
    UUID participantId,
    @NotBlank(message = "Vote value must not be blank")
    String value
) {
}
