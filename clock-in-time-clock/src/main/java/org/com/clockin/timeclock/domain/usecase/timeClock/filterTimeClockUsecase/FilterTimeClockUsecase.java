package org.com.clockin.timeclock.domain.usecase.timeClock.filterTimeClockUsecase;

import feign.FeignException;
import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.common.exception.TimeClockNotFoundException;
import org.com.clockin.timeclock.domain.entity.PendingUpdateApproval;
import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.domain.usecase.timeClock.filterTimeClockUsecase.dto.FilterTimeClockOutput;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.EmployeeNotFoundException;
import org.com.clockin.timeclock.domain.utils.DateHandler;
import org.com.clockin.timeclock.infra.dataProvider.PendingUpdateApprovalDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.TimeClockDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.external.EmployeeDataProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class FilterTimeClockUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final TimeClockDataProvider timeClockDataProvider;
    private final PendingUpdateApprovalDataProvider pendingUpdateApprovalDataProvider;

    public FilterTimeClockOutput execute(String externalAuth, UUID timeClockId) {
        Employee employee = findEmployee(externalAuth);
        TimeClock timeClock = findTimeClock(timeClockId, employee.getId());
        List<PendingUpdateApproval> pendingUpdateApprovals = findPendingApprovals(timeClock.getId());

        return mountOutput(timeClock, pendingUpdateApprovals);
    }

    private Employee findEmployee(String externalAuth) {
        Employee employee = null;

        try {
            employee = employeeDataProvider.findEmployeeByResourceServerId(externalAuth).getBody();
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new EmployeeNotFoundException("Error while fetching employee. Resource not found!");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return employee;
    }

    private TimeClock findTimeClock(UUID timeClockId, Long employeeId) {
        return timeClockDataProvider.findByIdAndEmployeeId(employeeId, timeClockId).orElseThrow(TimeClockNotFoundException::new);
    }

    private List<PendingUpdateApproval> findPendingApprovals(UUID timeClockId) {
        return pendingUpdateApprovalDataProvider.findAllByTimeClock(timeClockId);
    }

    private FilterTimeClockOutput mountOutput(TimeClock timeClock, List<PendingUpdateApproval> pendingUpdateApprovals) {
        return FilterTimeClockOutput.builder()
            .id(timeClock.getId())
            .timeClocked(timeClock.getTimeClocked())
            .eventType(timeClock.getEvent())
            .updatedAt(timeClock.getUpdatedAt())
            .externalEmployeeId(timeClock.getExternalEmployeeId())
            .day(DateHandler.extractDayNumberFromDate(timeClock.getTimeClocked()))
            .month(DateHandler.extractMonthFromDate(timeClock.getTimeClocked()))
            .year(DateHandler.extractYearFromDate(timeClock.getTimeClocked()))
            .pendingApprovals(pendingUpdateApprovals.stream().map(FilterTimeClockOutput.UpdateApprovals::toDto).toList())
            .build();
    }
}
