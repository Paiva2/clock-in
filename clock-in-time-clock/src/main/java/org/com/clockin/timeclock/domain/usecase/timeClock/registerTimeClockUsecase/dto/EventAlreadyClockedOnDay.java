package org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.dto;

import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

@ResponseStatus(HttpStatus.CONFLICT)
public class EventAlreadyClockedOnDay extends RuntimeException {
    private static final String MESSAGE = "An time clocked with this event: {0} already exists for that day.";

    public EventAlreadyClockedOnDay(TimeClock.Event event) {
        super(MessageFormat.format(MESSAGE, event));
    }
}
