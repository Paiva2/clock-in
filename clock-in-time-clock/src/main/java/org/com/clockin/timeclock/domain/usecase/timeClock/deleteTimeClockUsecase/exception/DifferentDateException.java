package org.com.clockin.timeclock.domain.usecase.timeClock.deleteTimeClockUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DifferentDateException extends RuntimeException {
    private static final String MESSAGE = "You can't remove time clocked on different months or years!";

    public DifferentDateException() {
        super(MESSAGE);
    }
}
