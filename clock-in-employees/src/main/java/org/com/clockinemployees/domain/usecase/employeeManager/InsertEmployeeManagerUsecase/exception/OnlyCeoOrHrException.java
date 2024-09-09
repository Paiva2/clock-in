package org.com.clockinemployees.domain.usecase.employeeManager.InsertEmployeeManagerUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OnlyCeoOrHrException extends RuntimeException {
    private final static String MESSAGE = "Only CEO or Human Resource member can handle this resource!";

    public OnlyCeoOrHrException() {
        super(MESSAGE);
    }
}
