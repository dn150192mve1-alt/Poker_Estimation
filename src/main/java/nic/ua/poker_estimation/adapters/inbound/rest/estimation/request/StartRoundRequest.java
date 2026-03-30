package nic.ua.poker_estimation.adapters.inbound.rest.estimation.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record StartRoundRequest(
    @NotBlank(message = "Round topic must not be blank")
    String topic,
    @Min(value = 1, message = "Round durationSeconds must be greater than zero")
    long durationSeconds
) {
}
