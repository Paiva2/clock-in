package org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception;

import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NecessaryEventMissingException extends RuntimeException {
    private static final String MESSAGE = "You must register the event {0} before register the event {1}.";

    public NecessaryEventMissingException(TimeClock.Event necessary, TimeClock.Event desired) {
        super(MessageFormat.format(MESSAGE, necessary, desired));
    }
}
