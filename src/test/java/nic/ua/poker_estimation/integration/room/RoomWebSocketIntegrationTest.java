package nic.ua.poker_estimation.integration.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import nic.ua.poker_estimation.adapters.inbound.rest.room.response.RoomStateResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoomWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    private StompSession session;

    @AfterEach
    void tearDown() throws Exception {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    @Test
    void shouldPublishRoomSnapshotWhenParticipantJoins() throws Exception {
        String response = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "roomName": "Team Alpha",
                      "ownerDisplayName": "Lead",
                      "deckType": "STORY_POINTS"
                    }
                    """))
            .andReturn()
            .getResponse()
            .getContentAsString();
        String roomId = JsonTestHelper.readString(response, "$.roomId");

        BlockingQueue<RoomStateResponse> queue = new LinkedBlockingQueue<>();
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new MappingJackson2MessageConverter());

        session = client.connectAsync("ws://localhost:%d/ws".formatted(port), new StompSessionHandlerAdapter() { })
            .get(5, TimeUnit.SECONDS);

        session.subscribe("/topic/rooms/" + roomId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return RoomStateResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue.add((RoomStateResponse) payload);
            }
        });

        session.send("/app/rooms/%s/snapshot".formatted(roomId), null);

        RoomStateResponse initialSnapshot = queue.poll(5, TimeUnit.SECONDS);
        assertThat(initialSnapshot).isNotNull();
        assertThat(initialSnapshot.roomId()).isEqualTo(UUID.fromString(roomId));
        assertThat(initialSnapshot.participants()).hasSize(1);

        mockMvc.perform(post("/api/rooms/{roomId}/participants", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "displayName": "Alice"
                    }
                    """))
            .andReturn();

        RoomStateResponse update = queue.poll(5, TimeUnit.SECONDS);
        assertThat(update).isNotNull();
        assertThat(update.participants()).hasSize(2);
        assertThat(update.participants()).extracting(participant -> participant.displayName()).contains("Alice");
    }
}
