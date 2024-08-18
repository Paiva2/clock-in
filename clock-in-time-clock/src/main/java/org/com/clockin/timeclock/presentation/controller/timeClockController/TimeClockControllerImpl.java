package org.com.clockin.timeclock.presentation.controller.timeClockController;

import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.ListTimeClockedUsecase;
import org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.dto.ListTimeClockedInputFilters;
import org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.dto.ListTimeClockedOutput;
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
    private final ListTimeClockedUsecase listTimeClockedUsecase;

    @Override
    public ResponseEntity<RegisterTimeClockOutput> register(Jwt jwt) {
        RegisterTimeClockOutput output = registerTimeClockUsecase.execute(formatBearer(jwt.getTokenValue()));
        return new ResponseEntity<>(output, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ListTimeClockedOutput> listAll(Jwt jwt, String startDate, String endDate) {
        ListTimeClockedOutput output = listTimeClockedUsecase.execute(
            formatBearer(jwt.getTokenValue()),
            ListTimeClockedInputFilters.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build()
        );
        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    private String formatBearer(String tokenValue) {
        return "Bearer " + tokenValue;
    }
}
