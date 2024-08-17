package org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase;

import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.dto.RegisterTimeClockOutput;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.EmployeeNotFoundException;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.MaxTimeClockedExceptionForDay;
import org.com.clockin.timeclock.infra.dataProvider.TimeClockDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.external.EmployeeDataProvider;
import org.com.clockin.timeclock.infra.publishers.TimeClockPublisher;
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

        checkTimeClocksToday(employee);

        publishTimeClock(employee.getId());

        return mountOutput(employee.getId());
    }

    private Employee findEmployee(String externalAuthorization) {
        Employee employee = null;

        try {
            employee = employeeDataProvider.findEmployeeByResourceServerId(externalAuthorization).getBody();
        } catch (FeignException exception) {
            if (exception.status() == HttpStatus.NOT_FOUND.value()) {
                throw new EmployeeNotFoundException("Error while searching for Employee, resource not found!");
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }

        return employee;
    }

    private void checkTimeClocksToday(Employee employee) {
        Integer quantityClockedToday = timeClockedQuantityToday(employee.getId());

        if (quantityClockedToday >= 4) {
            throw new MaxTimeClockedExceptionForDay();
        }
    }

    private void publishTimeClock(Long employeeId) {
        timeClockPublisher.publishNewTimeClocked(employeeId);
    }

    private Integer timeClockedQuantityToday(Long employeeId) {
        return timeClockDataProvider.findTimeClocksCountTodayForEmployee(employeeId);
    }

    private RegisterTimeClockOutput mountOutput(Long externalEmployeeId) {
        return RegisterTimeClockOutput.builder()
            .employeeId(externalEmployeeId)
            .timeClocked(new Date())
            .build();
    }
}
