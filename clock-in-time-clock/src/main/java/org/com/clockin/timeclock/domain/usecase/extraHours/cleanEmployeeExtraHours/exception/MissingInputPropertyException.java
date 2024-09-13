package org.com.clockin.timeclock.domain.usecase.extraHours.cleanEmployeeExtraHours.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MissingInputPropertyException extends RuntimeException {
    private final static String MESSAGE = "The required field {0} is missing!";

    public MissingInputPropertyException(String field) {
        super(MessageFormat.format(MESSAGE, field));
    }

}
