package org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class MaxTimeClockedExceptionForDay extends RuntimeException {
    private final static String MESSAGE = "Max time clocked exceeded for today. Limit: 4. Proceed requesting an update on an time already clocked.";

    public MaxTimeClockedExceptionForDay() {
        super(MESSAGE);
    }
}
