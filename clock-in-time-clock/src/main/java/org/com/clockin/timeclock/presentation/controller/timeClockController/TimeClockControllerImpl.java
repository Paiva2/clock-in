package org.com.clockin.timeclock.presentation.controller.timeClockController;

import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.usecase.timeClock.deleteTimeClockUsecase.DeleteTimeClockUsecase;
import org.com.clockin.timeclock.domain.usecase.timeClock.deleteTimeClockUsecase.dto.DeleteTimeClockUsecaseOutput;
import org.com.clockin.timeclock.domain.usecase.timeClock.filterTimeClockUsecase.FilterTimeClockUsecase;
import org.com.clockin.timeclock.domain.usecase.timeClock.filterTimeClockUsecase.dto.FilterTimeClockOutput;
import org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.ListTimeClockedUsecase;
import org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.dto.ListTimeClockedInputFilters;
import org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.dto.ListTimeClockedOutput;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.RegisterTimeClockUsecase;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.dto.RegisterTimeClockInput;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.dto.RegisterTimeClockOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class TimeClockControllerImpl implements TimeClockController {
    private final RegisterTimeClockUsecase registerTimeClockUsecase;
    private final ListTimeClockedUsecase listTimeClockedUsecase;
    private final DeleteTimeClockUsecase deleteTimeClockUsecase;
    private final FilterTimeClockUsecase filterTimeClockUsecase;

    @Override
    public ResponseEntity<RegisterTimeClockOutput> register(Jwt jwt, RegisterTimeClockInput input) {
        RegisterTimeClockOutput output = registerTimeClockUsecase.execute(mountBearer(jwt.getTokenValue()), input);
        return new ResponseEntity<>(output, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<DeleteTimeClockUsecaseOutput> delete(Jwt jwt, UUID timeClockId) {
        DeleteTimeClockUsecaseOutput output = deleteTimeClockUsecase.execute(mountBearer(jwt.getTokenValue()), timeClockId);
        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ListTimeClockedOutput> listAll(Jwt jwt, String startDate, String endDate) {
        ListTimeClockedOutput output = listTimeClockedUsecase.execute(
            mountBearer(jwt.getTokenValue()),
            ListTimeClockedInputFilters.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build()
        );
        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<FilterTimeClockOutput> filterSingle(Jwt jwt, UUID timeClockId) {
        FilterTimeClockOutput output = filterTimeClockUsecase.execute(mountBearer(jwt.getTokenValue()), timeClockId);
        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    private String mountBearer(String tokenValue) {
        return "Bearer " + tokenValue;
    }
}
