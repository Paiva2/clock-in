package org.com.clockin.timeclock.presentation.controller.timeClockController;

import jakarta.validation.Valid;
import org.com.clockin.timeclock.domain.usecase.timeClock.deleteTimeClockUsecase.dto.DeleteTimeClockUsecaseOutput;
import org.com.clockin.timeclock.domain.usecase.timeClock.filterTimeClockUsecase.dto.FilterTimeClockOutput;
import org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.dto.ListTimeClockedOutput;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.dto.RegisterTimeClockInput;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.dto.RegisterTimeClockOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("time-clock")
public interface TimeClockController {
    @PostMapping("/register")
    ResponseEntity<RegisterTimeClockOutput> register(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid RegisterTimeClockInput input);

    @DeleteMapping("/delete/{timeClockId}")
    ResponseEntity<DeleteTimeClockUsecaseOutput> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable("timeClockId") UUID timeClockId);

    @GetMapping("/list")
    ResponseEntity<ListTimeClockedOutput> listAll(@AuthenticationPrincipal Jwt jwt, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate);

    @GetMapping("/{timeClockId}")
    ResponseEntity<FilterTimeClockOutput> filterSingle(@AuthenticationPrincipal Jwt jwt, @PathVariable("timeClockId") UUID timeClockId);
}
