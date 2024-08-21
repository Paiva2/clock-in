package org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidHourFormatException extends RuntimeException {
    private static final String MESSAGE = "Invalid hour format on field: {0}.";

    public InvalidHourFormatException(String field) {
        super(MessageFormat.format(MESSAGE, field));
    }
}
