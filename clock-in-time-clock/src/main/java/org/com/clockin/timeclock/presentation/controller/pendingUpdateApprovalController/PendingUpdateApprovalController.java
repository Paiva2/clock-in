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
    @PostMapping("/create/pending/{timeClockId}")
    ResponseEntity<RequestUpdateTimeClockOutput> create(@AuthenticationPrincipal Jwt jwt, @PathVariable("timeClockId") UUID timeClockId, @RequestBody @Valid UpdateTimeClockInput input);

    @PatchMapping("/approve/pending/{pendingId}")
    ResponseEntity<Void> approve(@AuthenticationPrincipal Jwt jwt, @PathVariable("pendingId") UUID pendingId);

    @PatchMapping("/deny/pending/{pendingId}")
    ResponseEntity<Void> deny(@AuthenticationPrincipal Jwt jwt, @PathVariable("pendingId") UUID pendingId);

    @DeleteMapping("/cancel/pending/{pendingApprovalId}")
    ResponseEntity<Void> cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable("pendingApprovalId") UUID pendingApprovalId);
}
