package org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EmployeePositionNotFoundException extends RuntimeException {
    private final static String MESSAGE = "Employee position not found!";

    public EmployeePositionNotFoundException() {
        super(MESSAGE);
    }
}
