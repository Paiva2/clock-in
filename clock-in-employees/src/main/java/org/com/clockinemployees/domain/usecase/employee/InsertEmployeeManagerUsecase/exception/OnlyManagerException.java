package org.com.clockinemployees.domain.usecase.employee.InsertEmployeeManagerUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OnlyManagerException extends RuntimeException {
    private final static String MESSAGE = "Only Managers can have an Employee attached to him!";

    public OnlyManagerException() {
        super(MESSAGE);
    }
}
