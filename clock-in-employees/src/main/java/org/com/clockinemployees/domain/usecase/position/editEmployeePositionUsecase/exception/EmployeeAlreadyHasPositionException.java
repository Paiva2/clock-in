package org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmployeeAlreadyHasPositionException extends RuntimeException {
    private final static String MESSAGE = "Can''t assign position. Employee already has desired position: {0}";

    public EmployeeAlreadyHasPositionException(String position) {
        super(MessageFormat.format(MESSAGE, position));
    }
}
