package nic.ua.poker_estimation.adapters.outbound.scheduling;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import nic.ua.poker_estimation.application.ports.inbound.CloseRoundByTimerInPort;
import nic.ua.poker_estimation.application.ports.outbound.ScheduleRoundTimeoutOutPort;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
public final class RoundTimerScheduler implements ScheduleRoundTimeoutOutPort {

    private final TaskScheduler taskScheduler;
    private final CloseRoundByTimerInPort closeRoundByTimerInPort;
    private final Map<UUID, ScheduledFuture<?>> scheduledRounds = new ConcurrentHashMap<>();

    public RoundTimerScheduler(TaskScheduler taskScheduler, CloseRoundByTimerInPort closeRoundByTimerInPort) {
        this.taskScheduler = taskScheduler;
        this.closeRoundByTimerInPort = closeRoundByTimerInPort;
    }

    @Override
    public void schedule(UUID roomId, Instant expiresAt) {
        ScheduledFuture<?> existingTask = scheduledRounds.remove(roomId);
        if (existingTask != null) {
            existingTask.cancel(false);
        }
        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(
            () -> closeRoundByTimerInPort.closeRoundByTimer(roomId),
            Date.from(expiresAt)
        );
        scheduledRounds.put(roomId, scheduledFuture);
    }
}
