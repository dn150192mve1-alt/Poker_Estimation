package nic.ua.poker_estimation.application.ports.inbound;

import java.util.UUID;
import nic.ua.poker_estimation.domain.model.Room;

public interface RevealEstimationsInPort {

    Room revealEstimations(UUID roomId);
}
