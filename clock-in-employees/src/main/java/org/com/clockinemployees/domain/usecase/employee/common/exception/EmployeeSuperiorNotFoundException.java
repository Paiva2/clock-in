package org.com.clockinemployees.domain.usecase.employee.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EmployeeSuperiorNotFoundException extends RuntimeException {
    private final static String MESSAGE = "Provided superior not found as employee superior!";

    public EmployeeSuperiorNotFoundException() {
        super(MESSAGE);
    }
}
