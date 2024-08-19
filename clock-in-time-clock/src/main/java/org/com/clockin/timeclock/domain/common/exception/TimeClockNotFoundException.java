package org.com.clockin.timeclock.domain.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TimeClockNotFoundException extends RuntimeException {
    private final static String MESSAGE = "Time Clock not found!";

    public TimeClockNotFoundException() {
        super(MESSAGE);
    }
}