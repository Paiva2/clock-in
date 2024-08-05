package org.com.clockinemployees.domain.usecase.employee.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EmployeeNotFoundException extends RuntimeException {
    private final static String MESSAGE = "Employee not found!";

    public EmployeeNotFoundException() {
        super(MESSAGE);
    }
}
