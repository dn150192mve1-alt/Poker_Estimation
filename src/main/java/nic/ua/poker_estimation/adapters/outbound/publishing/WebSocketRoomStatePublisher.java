package nic.ua.poker_estimation.adapters.outbound.publishing;

import nic.ua.poker_estimation.adapters.inbound.rest.room.RoomStateResponseMapper;
import nic.ua.poker_estimation.application.ports.outbound.PublishRoomStateOutPort;
import nic.ua.poker_estimation.domain.model.Room;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public final class WebSocketRoomStatePublisher implements PublishRoomStateOutPort {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public WebSocketRoomStatePublisher(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void publish(Room room) {
        simpMessagingTemplate.convertAndSend("/topic/rooms/" + room.id(), RoomStateResponseMapper.map(room));
    }
}
