package org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDateFiltersException extends RuntimeException {
    private static final String MESSAGE = "Filters can't be empty. Provide at least one filter!";

    public InvalidDateFiltersException() {
        super(MESSAGE);
    }
}
