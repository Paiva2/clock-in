package org.com.clockin.timeclock.domain.usecase.timeClock.deleteTimeClockUsecase;

import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.common.exception.TimeClockNotFoundException;
import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.domain.usecase.timeClock.deleteTimeClockUsecase.dto.DeleteTimeClockUsecaseOutput;
import org.com.clockin.timeclock.domain.usecase.timeClock.deleteTimeClockUsecase.exception.DifferentDateException;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.EmployeeNotFoundException;
import org.com.clockin.timeclock.infra.dataProvider.PendingUpdateApprovalDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.TimeClockDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.external.EmployeeDataProvider;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DeleteTimeClockUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final TimeClockDataProvider timeClockDataProvider;
    private final PendingUpdateApprovalDataProvider pendingUpdateApprovalDataProvider;

    @Transactional
    public DeleteTimeClockUsecaseOutput execute(String externalAuth, UUID timeClockId) {
        Employee employee = findEmployee(externalAuth);
        TimeClock timeClock = findTimeClock(timeClockId, employee.getId());
        checkTimeClockMonth(timeClock);

        deleteTimeClockPendingApprovals(timeClock.getId());
        deleteTimeClock(timeClock.getId());

        return mountOutput(timeClock.getId());
    }

    private Employee findEmployee(String externalAuth) {
        Employee employee = null;

        try {
            employee = employeeDataProvider.findEmployeeByResourceServerId(externalAuth).getBody();
        } catch (FeignException e) {
            if (e.status() == 400) {
                throw new EmployeeNotFoundException("Error while searching for employee. Resource not found.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return employee;
    }

    private TimeClock findTimeClock(UUID timeclockId, Long employeeId) {
        return timeClockDataProvider.findByIdAndEmployeeId(employeeId, timeclockId).orElseThrow(TimeClockNotFoundException::new);
    }

    private void checkTimeClockMonth(TimeClock timeClock) {
        Integer currentMonth = getCurrentMonth();
        Integer currentYear = getCurrentYear();

        Calendar timeClockCalendar = getCurrentCalendar();
        timeClockCalendar.setTime(timeClock.getTimeClocked());

        Integer timeClockedMonth = timeClockCalendar.get(Calendar.MONTH);
        Integer timeClockedYear = timeClockCalendar.get(Calendar.YEAR);

        if (!timeClockedMonth.equals(currentMonth) || !timeClockedYear.equals(currentYear)) {
            throw new DifferentDateException();
        }
    }

    private Calendar getCurrentCalendar() {
        return Calendar.getInstance();
    }

    private Integer getCurrentMonth() {
        Calendar calendar = getCurrentCalendar();
        return calendar.get(Calendar.MONTH);
    }

    private Integer getCurrentYear() {
        Calendar calendar = getCurrentCalendar();
        return calendar.get(Calendar.YEAR);
    }

    private void deleteTimeClockPendingApprovals(UUID timeClockId) {
        pendingUpdateApprovalDataProvider.removeAllPendingUpdateApprovalsByTimeClockId(timeClockId);
    }

    private void deleteTimeClock(UUID timeClockId) {
        timeClockDataProvider.deleteById(timeClockId);
    }

    private DeleteTimeClockUsecaseOutput mountOutput(UUID timeClockId) {
        return DeleteTimeClockUsecaseOutput.builder().id(timeClockId).build();
    }
}
