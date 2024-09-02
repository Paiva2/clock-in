package org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ItineraryNotFoundException extends RuntimeException {
    private final static String MESSAGE = "Employee with id {0} has no itinerary!";

    public ItineraryNotFoundException(Long id) {
        super(MessageFormat.format(MESSAGE, id));
    }
}
