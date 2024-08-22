package org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.cancelPendingUpdateApprovalUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PendingUpdateApprovalAlreadyApprovedException extends RuntimeException {
    private final static String MESSAGE = "Cancel request error. Pending update is already revised!";

    public PendingUpdateApprovalAlreadyApprovedException() {
        super(MESSAGE);
    }
}
