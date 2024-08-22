package org.com.clockin.timeclock.presentation.controller.pendingUpdateApprovalController;

import jakarta.validation.Valid;
import org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.createPendingUpdateApprovalUsecase.dto.RequestUpdateTimeClockOutput;
import org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.createPendingUpdateApprovalUsecase.dto.UpdateTimeClockInput;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("time-clock")
public interface PendingUpdateApprovalController {
    @PostMapping("/update/{timeClockId}")
    ResponseEntity<RequestUpdateTimeClockOutput> update(@AuthenticationPrincipal Jwt jwt, @PathVariable("timeClockId") UUID timeClockId, @RequestBody @Valid UpdateTimeClockInput input);

    @DeleteMapping("/cancel/{pendingApprovalId}")
    ResponseEntity<Void> cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable("pendingApprovalId") UUID pendingApprovalId);
}
