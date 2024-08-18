package org.com.clockin.timeclock.presentation.controller.timeClockController;

import org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.dto.ListTimeClockedOutput;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.dto.RegisterTimeClockOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("time-clock")
public interface TimeClockController {
    @PostMapping("/register")
    ResponseEntity<RegisterTimeClockOutput> register(@AuthenticationPrincipal Jwt jwt);

    @GetMapping("/list")
    ResponseEntity<ListTimeClockedOutput> listAll(@AuthenticationPrincipal Jwt jwt, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate);
}
