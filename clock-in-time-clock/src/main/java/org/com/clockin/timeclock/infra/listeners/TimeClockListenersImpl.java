package org.com.clockin.timeclock.infra.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.clockin.timeclock.domain.usecase.timeClock.createTimeClockUsecase.CreateTimeClockUsecase;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class TimeClockListenersImpl implements TimeClockListeners {
    private final CreateTimeClockUsecase createTimeClockUsecase;

    @Override
    @RabbitListener(queues = "${rabbit.queue.time-clock}")
    public void registerTimeClock(@Payload String message) {
        log.info("Received a new time clock!");

        try {
            Long employeeId = Long.valueOf(message);
            createTimeClockUsecase.execute(employeeId);
        } catch (Exception e) {
            log.error("Error while time clocking!");
            throw new RuntimeException(e.getMessage());
        }
    }
}
