package nic.ua.poker_estimation.application.ports.inbound;

import java.util.UUID;

public interface CloseRoundByTimerInPort {

    void closeRoundByTimer(UUID roomId);
}
