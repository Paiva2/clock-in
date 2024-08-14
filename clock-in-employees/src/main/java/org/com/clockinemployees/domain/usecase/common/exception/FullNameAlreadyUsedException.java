package org.com.clockinemployees.domain.usecase.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class FullNameAlreadyUsedException extends RuntimeException {
    private final static String MESSAGE = "Provided first name and last name is already being used!";

    public FullNameAlreadyUsedException() {
        super(MESSAGE);
    }
}
