package org.com.clockinemployees.domain.usecase.employee.editEmployeeProfileUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PersonalDataNotFoundException extends RuntimeException {
    private final static String MESSAGE = "Employee personal data not found!";

    public PersonalDataNotFoundException() {
        super(MESSAGE);
    }
}
