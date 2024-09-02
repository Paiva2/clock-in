package org.com.clockinemployees.domain.usecase.employee.InsertEmployeeManagerUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmployeeAlreadyHasManagerException extends RuntimeException {
    private final static String MESSAGE = "Employee with id {0} already contains Manager with id {1}";

    public EmployeeAlreadyHasManagerException(Long employeeId, Long managerId) {
        super(MessageFormat.format(MESSAGE, employeeId, managerId));
    }
}
