package org.com.clockinemployees.domain.usecase.position.listPositionsUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientPermissionPositionListException extends RuntimeException {
    private final static String MESSAGE = "Only CEO's and Human Resource can list positions!";

    public InsufficientPermissionPositionListException() {
        super(MESSAGE);
    }
}
