package org.com.clockin.timeclock.infra.publishers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.com.clockin.timeclock.infra.publishers.dto.PublishNewTimeClockedInput;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimeClockPublisherImpl implements TimeClockPublisher {
    @Value("${rabbit.queue.time-clock}")
    private String timeClockQueueName;

    private final AmqpTemplate amqpTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void publishNewTimeClocked(PublishNewTimeClockedInput input) {
        try {
            amqpTemplate.convertAndSend(timeClockQueueName, objectMapper.writeValueAsString(input));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
