package org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ManagerNotFoundException extends RuntimeException {
    private final static String MESSAGE = "Manager not found.";

    public ManagerNotFoundException() {
        super(MESSAGE);
    }
}
