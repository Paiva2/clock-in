package org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.approvePendingApprovalUsecase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;
import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class PendingUpdateApprovalAlreadyResolvedException extends RuntimeException {
    private final static String MESSAGE = "Pending update approval with id {0} is already resolved!";

    public PendingUpdateApprovalAlreadyResolvedException(UUID id) {
        super(MessageFormat.format(MESSAGE, id));
    }
}
