package org.com.clockin.timeclock.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class RabbitConfig {
    @Value("${rabbit.queue.time-clock}")
    private String timeClockQueue;

    @Bean
    public Queue timeClockQueue() {
        return new Queue(timeClockQueue, false);
    }
}
