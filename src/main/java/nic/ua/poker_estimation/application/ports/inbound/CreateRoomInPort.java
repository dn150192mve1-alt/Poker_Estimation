package nic.ua.poker_estimation.application.ports.inbound;

import nic.ua.poker_estimation.domain.model.DeckType;
import nic.ua.poker_estimation.domain.model.Room;

public interface CreateRoomInPort {

    Room createRoom(String roomName, String ownerDisplayName, DeckType deckType);
}
