package org.com.clockin.timeclock.presentation.controller.timeClockController;

import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.RegisterTimeClockUsecase;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.dto.RegisterTimeClockOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TimeClockControllerImpl implements TimeClockController {
    private final RegisterTimeClockUsecase registerTimeClockUsecase;

    @Override
    public ResponseEntity<RegisterTimeClockOutput> register(Jwt jwt) {
        RegisterTimeClockOutput output = registerTimeClockUsecase.execute(formatBearer(jwt.getTokenValue()));
        return new ResponseEntity<>(output, HttpStatus.CREATED);
    }

    private String formatBearer(String tokenValue) {
        return "Bearer " + tokenValue;
    }
}
