package nic.ua.poker_estimation.application.ports.outbound;

import nic.ua.poker_estimation.domain.model.Room;

public interface SaveRoomOutPort {

    Room save(Room room);
}
