package org.com.clockin.timeclock.presentation.controller.extraHour;

import org.com.clockin.timeclock.domain.usecase.extraHours.filterEmployeeExtraHours.dto.FilterEmployeeExtraHourOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RequestMapping("time-clock")
public interface ExtraHourController {
    @GetMapping("/employee/extra-hours")
    ResponseEntity<FilterEmployeeExtraHourOutput> getExtraHours(@AuthenticationPrincipal Jwt jwt, @RequestParam(value = "page", required = false, defaultValue = "1") Integer page, @RequestParam(value = "size", required = false, defaultValue = "20") Integer size, @RequestParam(value = "period", required = false) String period, @RequestParam(value = "from", required = false) String from, @RequestParam(value = "to", required = false) String to);

    @DeleteMapping("/employee/{employeeId}/extra-hours")
    ResponseEntity<Void> cleanEmployeeExtraHours(@AuthenticationPrincipal Jwt jwt, @PathVariable("employeeId") Long employeeId, @RequestParam("month") Integer month, @RequestParam("year") Integer year);
}
