package nic.ua.poker_estimation.adapters.inbound.rest.estimation;

import jakarta.validation.Valid;
import java.util.UUID;
import nic.ua.poker_estimation.adapters.inbound.rest.estimation.request.StartRoundRequest;
import nic.ua.poker_estimation.adapters.inbound.rest.estimation.request.SubmitVoteRequest;
import nic.ua.poker_estimation.adapters.inbound.rest.room.RoomStateResponseMapper;
import nic.ua.poker_estimation.adapters.inbound.rest.room.response.RoomStateResponse;
import nic.ua.poker_estimation.application.ports.inbound.RestartEstimationRoundInPort;
import nic.ua.poker_estimation.application.ports.inbound.RevealEstimationsInPort;
import nic.ua.poker_estimation.application.ports.inbound.StartEstimationRoundInPort;
import nic.ua.poker_estimation.application.ports.inbound.SubmitVoteInPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms/{roomId}")
public final class EstimationRestController {

    private final StartEstimationRoundInPort startEstimationRoundInPort;
    private final SubmitVoteInPort submitVoteInPort;
    private final RevealEstimationsInPort revealEstimationsInPort;
    private final RestartEstimationRoundInPort restartEstimationRoundInPort;

    public EstimationRestController(
        StartEstimationRoundInPort startEstimationRoundInPort,
        SubmitVoteInPort submitVoteInPort,
        RevealEstimationsInPort revealEstimationsInPort,
        RestartEstimationRoundInPort restartEstimationRoundInPort
    ) {
        this.startEstimationRoundInPort = startEstimationRoundInPort;
        this.submitVoteInPort = submitVoteInPort;
        this.revealEstimationsInPort = revealEstimationsInPort;
        this.restartEstimationRoundInPort = restartEstimationRoundInPort;
    }

    @PostMapping("/rounds")
    @ResponseStatus(HttpStatus.ACCEPTED)
    RoomStateResponse startRound(@PathVariable UUID roomId, @Valid @RequestBody StartRoundRequest request) {
        return RoomStateResponseMapper.map(startEstimationRoundInPort.startEstimationRound(roomId, request.topic(), request.durationSeconds()));
    }

    @PostMapping("/votes")
    @ResponseStatus(HttpStatus.ACCEPTED)
    RoomStateResponse submitVote(@PathVariable UUID roomId, @Valid @RequestBody SubmitVoteRequest request) {
        return RoomStateResponseMapper.map(submitVoteInPort.submitVote(roomId, request.participantId(), request.value()));
    }

    @PostMapping("/rounds/reveal")
    @ResponseStatus(HttpStatus.ACCEPTED)
    RoomStateResponse reveal(@PathVariable UUID roomId) {
        return RoomStateResponseMapper.map(revealEstimationsInPort.revealEstimations(roomId));
    }

    @PostMapping("/rounds/restart")
    @ResponseStatus(HttpStatus.ACCEPTED)
    RoomStateResponse restart(@PathVariable UUID roomId) {
        return RoomStateResponseMapper.map(restartEstimationRoundInPort.restartEstimationRound(roomId));
    }
}
