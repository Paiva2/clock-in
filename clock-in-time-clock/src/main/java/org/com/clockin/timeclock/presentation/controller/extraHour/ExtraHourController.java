package org.com.clockin.timeclock.presentation.controller.extraHour;

import org.com.clockin.timeclock.domain.usecase.extraHours.dto.FilterEmployeeExtraHourOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("time-clock")
public interface ExtraHourController {
    @GetMapping("/employee/extra-hours")
    ResponseEntity<FilterEmployeeExtraHourOutput> getExtraHours(@AuthenticationPrincipal Jwt jwt, @RequestParam(value = "page", required = false, defaultValue = "1") Integer page, @RequestParam(value = "size", required = false, defaultValue = "20") Integer size, @RequestParam(value = "period", required = false) String period, @RequestParam(value = "from", required = false) String from, @RequestParam(value = "to", required = false) String to);
}
