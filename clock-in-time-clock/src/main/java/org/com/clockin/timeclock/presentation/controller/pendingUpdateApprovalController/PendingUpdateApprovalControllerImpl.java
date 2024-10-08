package org.com.clockin.timeclock.presentation.controller.pendingUpdateApprovalController;

import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.approvePendingApprovalUsecase.ApprovePendingApprovalUsecase;
import org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.cancelPendingUpdateApprovalUsecase.CancelPendingUpdateApprovalUsecase;
import org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.createPendingUpdateApprovalUsecase.CreatePendingUpdateApprovalUsecase;
import org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.createPendingUpdateApprovalUsecase.dto.RequestUpdateTimeClockOutput;
import org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.createPendingUpdateApprovalUsecase.dto.UpdateTimeClockInput;
import org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.denyPendingUpdateApprovalUsecase.DenyPendingUpdateApprovalUsecase;
import org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.listEmployeePendingApprovals.ListEmployeePendingApprovalsUsecase;
import org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.listEmployeePendingApprovals.dto.ListEmployeePendingApprovalOutput;
import org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.listEmployeePendingApprovals.dto.ListEmployeePendingApprovalsInput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class PendingUpdateApprovalControllerImpl implements PendingUpdateApprovalController {
    private final CreatePendingUpdateApprovalUsecase createPendingUpdateApprovalUsecase;
    private final CancelPendingUpdateApprovalUsecase cancelPendingUpdateApprovalUsecase;
    private final ApprovePendingApprovalUsecase approvePendingApprovalUsecase;
    private final DenyPendingUpdateApprovalUsecase denyPendingUpdateApprovalUsecase;
    private final ListEmployeePendingApprovalsUsecase listEmployeePendingApprovalsUsecase;

    @Override
    public ResponseEntity<RequestUpdateTimeClockOutput> create(Jwt jwt, UUID timeClockId, UpdateTimeClockInput input) {
        RequestUpdateTimeClockOutput output = createPendingUpdateApprovalUsecase.execute(
            mountBearer(jwt.getTokenValue()),
            timeClockId,
            input
        );
        return new ResponseEntity<>(output, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> approve(Jwt jwt, UUID pendingId) {
        approvePendingApprovalUsecase.execute(mountBearer(jwt.getTokenValue()), pendingId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> deny(Jwt jwt, UUID pendingId) {
        denyPendingUpdateApprovalUsecase.execute(mountBearer(jwt.getTokenValue()), pendingId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> cancel(Jwt jwt, UUID pendingApprovalId) {
        cancelPendingUpdateApprovalUsecase.execute(mountBearer(jwt.getTokenValue()), pendingApprovalId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ListEmployeePendingApprovalOutput> listEmployeePendingApprovals(Jwt jwt, Long employeeId, Integer page, Integer size) {
        ListEmployeePendingApprovalOutput output = listEmployeePendingApprovalsUsecase.execute(mountBearer(jwt.getTokenValue()), employeeId, ListEmployeePendingApprovalsInput.builder()
            .page(page)
            .size(size)
            .build()
        );
        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    private String mountBearer(String token) {
        return "Bearer " + token;
    }
}
