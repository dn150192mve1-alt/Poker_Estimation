package nic.ua.poker_estimation.adapters.inbound.websocket;

import java.util.UUID;
import nic.ua.poker_estimation.adapters.inbound.rest.room.RoomStateResponseMapper;
import nic.ua.poker_estimation.application.ports.inbound.FindRoomStateInPort;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public final class RoomWebSocketController {

    private final FindRoomStateInPort findRoomStateInPort;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public RoomWebSocketController(FindRoomStateInPort findRoomStateInPort, SimpMessagingTemplate simpMessagingTemplate) {
        this.findRoomStateInPort = findRoomStateInPort;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/rooms/{roomId}/snapshot")
    public void sendSnapshot(@DestinationVariable UUID roomId) {
        simpMessagingTemplate.convertAndSend(
            "/topic/rooms/" + roomId,
            RoomStateResponseMapper.map(findRoomStateInPort.findRoomState(roomId))
        );
    }
}
