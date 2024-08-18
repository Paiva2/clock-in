package org.com.clockin.timeclock.domain.strategy.dateFormatValidator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDataInputFormatException extends RuntimeException {
    private final static String MESSAGE = "Invalid date format on input {0}! ex: 00-00-0000";

    public InvalidDataInputFormatException(String field) {
        super(MessageFormat.format(MESSAGE, field));
    }
}
