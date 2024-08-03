package org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WeakPasswordException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Weak password. Password must have at least 6 characters, one upper letter, and a special character.";

    public WeakPasswordException() {
        super(DEFAULT_MESSAGE);
    }
}
