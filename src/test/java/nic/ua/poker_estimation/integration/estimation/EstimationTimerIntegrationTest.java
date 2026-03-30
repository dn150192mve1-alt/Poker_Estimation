package nic.ua.poker_estimation.integration.estimation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nic.ua.poker_estimation.integration.room.JsonTestHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EstimationTimerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCloseVotingWhenTimerExpires() throws Exception {
        String roomResponse = createRoom();
        String roomId = JsonTestHelper.readString(roomResponse, "$.roomId");
        String ownerId = JsonTestHelper.readString(roomResponse, "$.ownerParticipantId");

        mockMvc.perform(post("/api/rooms/{roomId}/rounds", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "topic": "Timeout",
                      "durationSeconds": 1
                    }
                    """))
            .andExpect(status().isAccepted());

        waitUntilRoundStatus(roomId, "CLOSED");

        mockMvc.perform(post("/api/rooms/{roomId}/votes", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "participantId": "%s",
                      "value": "2"
                    }
                    """.formatted(ownerId)))
            .andExpect(status().isConflict());
    }

    private void waitUntilRoundStatus(String roomId, String expectedStatus) throws Exception {
        long deadline = System.currentTimeMillis() + 5_000;
        while (System.currentTimeMillis() < deadline) {
            MvcResult result = mockMvc.perform(get("/api/rooms/{roomId}", roomId))
                .andExpect(status().isOk())
                .andReturn();
            String response = result.getResponse().getContentAsString();
            String currentStatus = JsonTestHelper.readString(response, "$.currentRound.status");
            if (expectedStatus.equals(currentStatus)) {
                return;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Round status did not become " + expectedStatus);
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
