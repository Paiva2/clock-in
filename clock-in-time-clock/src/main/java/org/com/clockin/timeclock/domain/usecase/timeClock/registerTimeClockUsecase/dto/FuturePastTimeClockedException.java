package org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.dto;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FuturePastTimeClockedException extends RuntimeException {
    private final static String MESSAGE = "Time clocked can't be in the future or past month/year!";

    public FuturePastTimeClockedException() {
        super(MESSAGE);
    }
}
