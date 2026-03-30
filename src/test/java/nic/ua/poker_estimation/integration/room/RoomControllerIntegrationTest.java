package nic.ua.poker_estimation.integration.room;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoomControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateRoomAndReturnSessionMetadata() throws Exception {
        String response = createRoom();
        String roomId = JsonTestHelper.readString(response, "$.roomId");

        mockMvc.perform(get("/api/rooms/{roomId}", roomId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.roomName").value("Team Alpha"))
            .andExpect(jsonPath("$.deckType").value("STORY_POINTS"))
            .andExpect(jsonPath("$.availableVoteValues.length()").value(9))
            .andExpect(jsonPath("$.participants[0].role").value("OWNER"));
    }

    @Test
    void shouldJoinParticipantAndSendMessage() throws Exception {
        String response = createRoom();
        String roomId = JsonTestHelper.readString(response, "$.roomId");
        String ownerId = JsonTestHelper.readString(response, "$.ownerParticipantId");

        mockMvc.perform(post("/api/rooms/{roomId}/participants", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "displayName": "Alice"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.role").value("PLAYER"));

        mockMvc.perform(post("/api/rooms/{roomId}/messages", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "participantId": "%s",
                      "content": "Welcome"
                    }
                    """.formatted(ownerId)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.messages[0].content").value("Welcome"));
    }

    private String createRoom() throws Exception {
        return mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "roomName": "Team Alpha",
                      "ownerDisplayName": "Lead",
                      "deckType": "STORY_POINTS"
                    }
                    """))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    }
}
