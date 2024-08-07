package org.com.clockinemployees.domain.usecase.employee.authenticateEmployeeUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class EmployeeInvalidCredentials extends RuntimeException {
    private static final String MESSAGE = "Invalid Credentials.";

    public EmployeeInvalidCredentials() {
        super(MESSAGE);
    }
}
