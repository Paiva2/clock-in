package org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PositionNotFoundException extends RuntimeException {
    private static final String MESSAGE = "Position not found.";

    public PositionNotFoundException() {
        super(MESSAGE);
    }
}
