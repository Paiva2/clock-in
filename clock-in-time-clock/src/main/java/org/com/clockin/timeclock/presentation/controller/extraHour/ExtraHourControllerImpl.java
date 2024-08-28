package org.com.clockin.timeclock.presentation.controller.extraHour;

import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.usecase.extraHours.FilterEmployeExtraHourUsecase;
import org.com.clockin.timeclock.domain.usecase.extraHours.dto.FilterEmployeeExtraHourOutput;
import org.com.clockin.timeclock.domain.usecase.extraHours.dto.FindEmployeeExtraHourInput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ExtraHourControllerImpl implements ExtraHourController {
    private final FilterEmployeExtraHourUsecase filterEmployeExtraHourUsecase;

    @Override
    public ResponseEntity<FilterEmployeeExtraHourOutput> getExtraHours(Jwt jwt, Integer page, Integer size, String period, String from, String to) {
        FilterEmployeeExtraHourOutput output = filterEmployeExtraHourUsecase.execute(mountBearer(jwt.getTokenValue()), FindEmployeeExtraHourInput
            .builder()
            .page(page)
            .perPage(size)
            .period(period)
            .periodFrom(from)
            .periodTo(to)
            .build()
        );
        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    private String mountBearer(String token) {
        return "Bearer " + token;
    }
}
