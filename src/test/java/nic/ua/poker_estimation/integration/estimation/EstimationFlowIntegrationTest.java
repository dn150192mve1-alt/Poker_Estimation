package nic.ua.poker_estimation.integration.estimation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nic.ua.poker_estimation.integration.room.JsonTestHelper;
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
class EstimationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRunRoundSubmitVotesRevealAndRestartWithHistory() throws Exception {
        String roomResponse = createRoom("STORY_POINTS");
        String roomId = JsonTestHelper.readString(roomResponse, "$.roomId");
        String ownerId = JsonTestHelper.readString(roomResponse, "$.ownerParticipantId");
        String bobId = joinParticipant(roomId, "Bob");

        mockMvc.perform(post("/api/rooms/{roomId}/rounds", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "topic": "API",
                      "durationSeconds": 30
                    }
                    """))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.currentRound.topic").value("API"))
            .andExpect(jsonPath("$.currentRound.status").value("ACTIVE"));

        mockMvc.perform(post("/api/rooms/{roomId}/votes", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "participantId": "%s",
                      "value": "3"
                    }
                    """.formatted(ownerId)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.currentRound.submittedParticipantIds.length()").value(1));

        mockMvc.perform(post("/api/rooms/{roomId}/votes", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "participantId": "%s",
                      "value": "5"
                    }
                    """.formatted(bobId)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.currentRound.submittedParticipantIds.length()").value(2));

        mockMvc.perform(post("/api/rooms/{roomId}/rounds/reveal", roomId))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.currentRound.status").value("REVEALED"))
            .andExpect(jsonPath("$.currentRound.revealedVotes.length()").value(2));

        mockMvc.perform(post("/api/rooms/{roomId}/rounds/restart", roomId))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.currentRound.status").value("ACTIVE"))
            .andExpect(jsonPath("$.roundHistory.length()").value(1))
            .andExpect(jsonPath("$.roundHistory[0].status").value("REVEALED"));

        mockMvc.perform(get("/api/rooms/{roomId}", roomId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentRound.revealedVotes").isEmpty())
            .andExpect(jsonPath("$.roundHistory[0].revealedVotes.length()").value(2));
    }

    @Test
    void shouldRejectUnsupportedVoteForDeck() throws Exception {
        String roomResponse = createRoom("HOURS");
        String roomId = JsonTestHelper.readString(roomResponse, "$.roomId");
        String ownerId = JsonTestHelper.readString(roomResponse, "$.ownerParticipantId");

        mockMvc.perform(post("/api/rooms/{roomId}/rounds", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "topic": "API",
                      "durationSeconds": 30
                    }
                    """))
            .andExpect(status().isAccepted());

        mockMvc.perform(post("/api/rooms/{roomId}/votes", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "participantId": "%s",
                      "value": "13"
                    }
                    """.formatted(ownerId)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Vote value '13' is not supported for deck HOURS"));
    }

    private String createRoom(String deckType) throws Exception {
        return mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "roomName": "Team Alpha",
                      "ownerDisplayName": "Lead",
                      "deckType": "%s"
                    }
                    """.formatted(deckType)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    }

    private String joinParticipant(String roomId, String displayName) throws Exception {
        String response = mockMvc.perform(post("/api/rooms/{roomId}/participants", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "displayName": "%s"
                    }
                    """.formatted(displayName)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return JsonTestHelper.readString(response, "$.participantId");
    }
}
