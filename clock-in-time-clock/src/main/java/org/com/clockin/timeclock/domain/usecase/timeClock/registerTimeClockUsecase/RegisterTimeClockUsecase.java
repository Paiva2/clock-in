package org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase;

import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.dto.RegisterTimeClockOutput;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.EmployeeNotFoundException;
import org.com.clockin.timeclock.infra.dataProvider.TimeClockDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.external.EmployeeDataProvider;
import org.com.clockin.timeclock.infra.publishers.TimeClockPublisher;
import org.com.clockin.timeclock.infra.publishers.dto.PublishNewTimeClockedInput;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;

@AllArgsConstructor
@Builder
@Service
public class RegisterTimeClockUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final TimeClockDataProvider timeClockDataProvider;
    private final TimeClockPublisher timeClockPublisher;

    public RegisterTimeClockOutput execute(String externalAuthorization) {
        Employee employee = findEmployee(externalAuthorization);

        Date timeClocked = new Date();

        publishTimeClock(PublishNewTimeClockedInput.builder()
            .employeeId(employee.getId())
            .timeClocked(timeClocked)
            .build()
        );

        return mountOutput(employee.getId(), timeClocked);
    }

    private Employee findEmployee(String externalAuthorization) {
        Employee employee = null;

        try {
            employee = employeeDataProvider.findEmployeeByResourceServerId(externalAuthorization).getBody();
        } catch (FeignException exception) {
            if (exception.status() == HttpStatus.NOT_FOUND.value()) {
                throw new EmployeeNotFoundException("Error while searching for Employee, resource not found!");
            } else {
                throw new RuntimeException(exception.getMessage());
            }
        }

        return employee;
    }

    private void publishTimeClock(PublishNewTimeClockedInput input) {
        timeClockPublisher.publishNewTimeClocked(input);
    }
    
    private RegisterTimeClockOutput mountOutput(Long externalEmployeeId, Date timeClocked) {
        return RegisterTimeClockOutput.builder()
            .employeeId(externalEmployeeId)
            .timeClocked(timeClocked)
            .build();
    }
}
