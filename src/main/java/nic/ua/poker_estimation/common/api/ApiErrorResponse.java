package nic.ua.poker_estimation.common.api;

import java.time.Instant;

public record ApiErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message
) {
}
