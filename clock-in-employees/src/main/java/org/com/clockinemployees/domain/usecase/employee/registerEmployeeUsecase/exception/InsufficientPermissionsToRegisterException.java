package org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientPermissionsToRegisterException extends RuntimeException {
    private static final String MESSAGE = "Only CEO's or Human Resource members can create employees!";

    public InsufficientPermissionsToRegisterException() {
        super(MESSAGE);
    }
}
