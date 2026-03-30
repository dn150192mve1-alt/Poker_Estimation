package nic.ua.poker_estimation.adapters.inbound.rest.room;

import jakarta.validation.Valid;
import java.util.UUID;
import nic.ua.poker_estimation.adapters.inbound.rest.room.request.CreateRoomRequest;
import nic.ua.poker_estimation.adapters.inbound.rest.room.request.JoinParticipantRequest;
import nic.ua.poker_estimation.adapters.inbound.rest.room.request.SendRoomMessageRequest;
import nic.ua.poker_estimation.adapters.inbound.rest.room.response.CreateRoomResponse;
import nic.ua.poker_estimation.adapters.inbound.rest.room.response.JoinParticipantResponse;
import nic.ua.poker_estimation.adapters.inbound.rest.room.response.RoomStateResponse;
import nic.ua.poker_estimation.application.ports.inbound.CreateRoomInPort;
import nic.ua.poker_estimation.application.ports.inbound.FindRoomStateInPort;
import nic.ua.poker_estimation.application.ports.inbound.JoinRoomInPort;
import nic.ua.poker_estimation.application.ports.inbound.SendRoomMessageInPort;
import nic.ua.poker_estimation.domain.model.DeckType;
import nic.ua.poker_estimation.domain.model.Room;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
public final class RoomRestController {

    private final CreateRoomInPort createRoomInPort;
    private final FindRoomStateInPort findRoomStateInPort;
    private final JoinRoomInPort joinRoomInPort;
    private final SendRoomMessageInPort sendRoomMessageInPort;

    public RoomRestController(
        CreateRoomInPort createRoomInPort,
        FindRoomStateInPort findRoomStateInPort,
        JoinRoomInPort joinRoomInPort,
        SendRoomMessageInPort sendRoomMessageInPort
    ) {
        this.createRoomInPort = createRoomInPort;
        this.findRoomStateInPort = findRoomStateInPort;
        this.joinRoomInPort = joinRoomInPort;
        this.sendRoomMessageInPort = sendRoomMessageInPort;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CreateRoomResponse createRoom(@Valid @RequestBody CreateRoomRequest request) {
        Room room = createRoomInPort.createRoom(request.roomName(), request.ownerDisplayName(), DeckType.fromValue(request.deckType()));
        return new CreateRoomResponse(room.id(), room.ownerParticipantId(), room.participants().getFirst().displayName(), room.deckType().name());
    }

    @GetMapping("/{roomId}")
    RoomStateResponse getRoom(@PathVariable UUID roomId) {
        return RoomStateResponseMapper.map(findRoomStateInPort.findRoomState(roomId));
    }

    @PostMapping("/{roomId}/participants")
    @ResponseStatus(HttpStatus.CREATED)
    JoinParticipantResponse joinParticipant(@PathVariable UUID roomId, @Valid @RequestBody JoinParticipantRequest request) {
        JoinRoomInPort.JoinRoomResult result = joinRoomInPort.joinRoom(roomId, request.displayName());
        return new JoinParticipantResponse(result.room().id(), result.participantId(), result.displayName(), result.role().name());
    }

    @PostMapping("/{roomId}/messages")
    @ResponseStatus(HttpStatus.ACCEPTED)
    RoomStateResponse sendMessage(@PathVariable UUID roomId, @Valid @RequestBody SendRoomMessageRequest request) {
        return RoomStateResponseMapper.map(sendRoomMessageInPort.sendRoomMessage(roomId, request.participantId(), request.content()));
    }
}
