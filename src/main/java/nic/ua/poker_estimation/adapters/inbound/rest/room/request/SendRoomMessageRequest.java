package nic.ua.poker_estimation.adapters.inbound.rest.room.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SendRoomMessageRequest(
    @NotNull(message = "Message participantId must not be null")
    UUID participantId,
    @NotBlank(message = "Message content must not be blank")
    String content
) {
}
