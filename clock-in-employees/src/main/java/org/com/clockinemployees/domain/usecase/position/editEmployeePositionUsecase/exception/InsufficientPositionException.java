package org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientPositionException extends RuntimeException {
    private final static String MESSAGE = "Only CEO's and Human Resource members can handle positions!";
    private final static String CEO_MESSAGE = "Only CEO's can handle CEO's positions!";

    public InsufficientPositionException(Boolean ceoError) {
        super(ceoError ? CEO_MESSAGE : MESSAGE);
    }
}
