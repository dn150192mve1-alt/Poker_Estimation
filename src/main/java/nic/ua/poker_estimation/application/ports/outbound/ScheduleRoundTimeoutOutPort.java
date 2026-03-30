package nic.ua.poker_estimation.application.ports.outbound;

import java.time.Instant;
import java.util.UUID;

public interface ScheduleRoundTimeoutOutPort {

    void schedule(UUID roomId, Instant expiresAt);
}
