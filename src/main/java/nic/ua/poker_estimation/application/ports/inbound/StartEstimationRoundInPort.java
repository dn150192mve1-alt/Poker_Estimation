package nic.ua.poker_estimation.application.ports.inbound;

import java.util.UUID;
import nic.ua.poker_estimation.domain.model.Room;

public interface StartEstimationRoundInPort {

    Room startEstimationRound(UUID roomId, String topic, long durationSeconds);
}
