package org.com.clockin.timeclock.presentation.controller.extraHour;

import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.usecase.extraHours.cleanEmployeeExtraHours.CleanEmployeeExtraHoursUsecase;
import org.com.clockin.timeclock.domain.usecase.extraHours.cleanEmployeeExtraHours.dto.CleanEmployeeExtraHoursInput;
import org.com.clockin.timeclock.domain.usecase.extraHours.filterEmployeeExtraHours.FilterEmployeExtraHourUsecase;
import org.com.clockin.timeclock.domain.usecase.extraHours.filterEmployeeExtraHours.dto.FilterEmployeeExtraHourOutput;
import org.com.clockin.timeclock.domain.usecase.extraHours.filterEmployeeExtraHours.dto.FindEmployeeExtraHourInput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ExtraHourControllerImpl implements ExtraHourController {
    private final FilterEmployeExtraHourUsecase filterEmployeExtraHourUsecase;
    private final CleanEmployeeExtraHoursUsecase cleanEmployeeExtraHoursUsecase;

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

    @Override
    public ResponseEntity<Void> cleanEmployeeExtraHours(Jwt jwt, Long employeeId, Integer month, Integer year) {
        cleanEmployeeExtraHoursUsecase.execute(mountBearer(jwt.getTokenValue()), employeeId, CleanEmployeeExtraHoursInput
            .builder()
            .month(month)
            .year(year)
            .build()
        );
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private String mountBearer(String token) {
        return "Bearer " + token;
    }
}
