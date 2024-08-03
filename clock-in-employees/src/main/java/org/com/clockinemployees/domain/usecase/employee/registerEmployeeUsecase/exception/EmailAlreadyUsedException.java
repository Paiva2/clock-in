package org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyUsedException extends RuntimeException {
    private final static String MESSAGE = "E-mail already being used!";

    public EmailAlreadyUsedException() {
        super(MESSAGE);
    }
}
