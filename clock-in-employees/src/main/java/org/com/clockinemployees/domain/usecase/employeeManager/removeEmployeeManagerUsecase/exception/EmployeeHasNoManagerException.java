package org.com.clockinemployees.domain.usecase.employeeManager.removeEmployeeManagerUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EmployeeHasNoManagerException extends RuntimeException {
    private final static String MESSAGE = "Employee has no Manager with id: {0}";

    public EmployeeHasNoManagerException(Long managerId) {
        super(MessageFormat.format(MESSAGE, managerId));
    }
}
