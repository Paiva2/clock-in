package org.com.clockin.timeclock.domain.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PendingUpdateApprovalNotFoundException extends RuntimeException {
    private final static String MESSAGE = "Pending update approval not found!";

    public PendingUpdateApprovalNotFoundException() {
        super(MESSAGE);
    }
}
