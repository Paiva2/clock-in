package org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPhoneException extends RuntimeException {
    private final static String MESSAGE = "Invalid phone format.";

    public InvalidPhoneException() {
        super(MESSAGE);
    }
}
