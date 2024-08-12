package org.com.clockinemployees.infra.keycloack.employee.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserResourceNotFoundException extends RuntimeException {
    private static final String MESSAGE = "User with id {0} resource not found on resource server!";

    public UserResourceNotFoundException(String id) {
        super(MessageFormat.format(MESSAGE, id));
    }
}
