package org.com.clockin.timeclock.domain.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class InsufficientPositionsException extends RuntimeException {
    public InsufficientPositionsException(String message) {
        super(message);
    }
}
