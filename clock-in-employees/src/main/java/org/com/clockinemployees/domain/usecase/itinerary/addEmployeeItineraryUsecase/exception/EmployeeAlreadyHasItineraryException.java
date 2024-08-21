package org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmployeeAlreadyHasItineraryException extends RuntimeException {
    private static final String MESSAGE = "Employee already has an itinerary!";

    public EmployeeAlreadyHasItineraryException() {
        super(MESSAGE);
    }
}
