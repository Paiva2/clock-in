package org.com.clockin.timeclock.domain.strategy.dateFormatValidator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidHourTimeFormatException extends RuntimeException {
    private final static String MESSAGE = "Invalid updated time clock hour format. Valid format ex: 00:00:00";

    public InvalidHourTimeFormatException() {
        super(MESSAGE);
    }
}
