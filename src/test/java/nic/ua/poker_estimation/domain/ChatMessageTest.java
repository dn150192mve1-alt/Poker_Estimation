package nic.ua.poker_estimation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import nic.ua.poker_estimation.common.service.ValidationException;
import nic.ua.poker_estimation.domain.model.ChatMessage;
import org.junit.jupiter.api.Test;

class ChatMessageTest {

    @Test
    void shouldCreateMessageWithTrimmedContent() {
        ChatMessage message = ChatMessage.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Alice",
            "  Hello  ",
            Instant.parse("2026-03-11T20:00:00Z")
        );

        assertThat(message.content()).isEqualTo("Hello");
    }

    @Test
    void shouldRejectInvalidMessageFields() {
        Instant now = Instant.parse("2026-03-11T20:00:00Z");
        UUID messageId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        assertThatThrownBy(() -> ChatMessage.create(null, participantId, "Alice", "Hi", now))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Message id must not be null");

        assertThatThrownBy(() -> ChatMessage.create(messageId, null, "Alice", "Hi", now))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Message participantId must not be null");

        assertThatThrownBy(() -> ChatMessage.create(messageId, participantId, null, "Hi", now))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Message participantDisplayName must not be blank");

        assertThatThrownBy(() -> ChatMessage.create(messageId, participantId, " ", "Hi", now))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Message participantDisplayName must not be blank");

        assertThatThrownBy(() -> ChatMessage.create(messageId, participantId, "Alice", null, now))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Message content must not be blank");

        assertThatThrownBy(() -> ChatMessage.create(messageId, participantId, "Alice", " ", now))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Message content must not be blank");

        assertThatThrownBy(() -> ChatMessage.create(messageId, participantId, "Alice", "Hi", null))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Message sentAt must not be null");
    }
}
