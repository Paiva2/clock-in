package org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.approvePendingApprovalUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TimeClockValidityException extends RuntimeException {
    public TimeClockValidityException(String message) {
        super(message);
    }
}
