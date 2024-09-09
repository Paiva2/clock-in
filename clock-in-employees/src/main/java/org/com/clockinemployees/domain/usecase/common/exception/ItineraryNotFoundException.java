package org.com.clockinemployees.domain.usecase.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ItineraryNotFoundException extends RuntimeException {
    private final static String MESSAGE = "Itinerary not found!";

    public ItineraryNotFoundException() {
        super(MESSAGE);
    }
}
