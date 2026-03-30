package nic.ua.poker_estimation.adapters.outbound.scheduling;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import nic.ua.poker_estimation.application.ports.inbound.CloseRoundByTimerInPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

@ExtendWith(MockitoExtension.class)
class RoundTimerSchedulerTest {

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private CloseRoundByTimerInPort closeRoundByTimerInPort;

    @Mock
    private ScheduledFuture<Object> firstFuture;

    @Mock
    private ScheduledFuture<Object> secondFuture;

    @Test
    void shouldCancelExistingTaskWhenReschedulingSameRoom() {
        doReturn(firstFuture, secondFuture)
            .when(taskScheduler)
            .schedule(any(Runnable.class), any(Date.class));

        RoundTimerScheduler scheduler = new RoundTimerScheduler(taskScheduler, closeRoundByTimerInPort);
        UUID roomId = UUID.randomUUID();

        scheduler.schedule(roomId, Instant.parse("2026-03-11T20:00:01Z"));
        scheduler.schedule(roomId, Instant.parse("2026-03-11T20:00:02Z"));

        verify(firstFuture).cancel(false);
    }
}
