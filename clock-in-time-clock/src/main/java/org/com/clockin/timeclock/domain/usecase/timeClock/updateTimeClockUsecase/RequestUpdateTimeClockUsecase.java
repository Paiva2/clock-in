package org.com.clockin.timeclock.domain.usecase.timeClock.updateTimeClockUsecase;

import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockin.timeclock.domain.common.exception.TimeClockNotFoundException;
import org.com.clockin.timeclock.domain.entity.PendingUpdateApproval;
import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.domain.strategy.dateFormatValidator.DateFormatStrategy;
import org.com.clockin.timeclock.domain.strategy.dateFormatValidator.strategies.TimeFormatRegexValidator;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.EmployeeNotFoundException;
import org.com.clockin.timeclock.domain.usecase.timeClock.updateTimeClockUsecase.dto.RequestUpdateTimeClockOutput;
import org.com.clockin.timeclock.domain.usecase.timeClock.updateTimeClockUsecase.dto.UpdateTimeClockInput;
import org.com.clockin.timeclock.infra.dataProvider.PendingUpdateApprovalDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.TimeClockDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.external.EmployeeDataProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@Builder
@Service
public class RequestUpdateTimeClockUsecase {
    private final static String HOUR_PATTERN = "(?:[01]\\d|2[0-3]):(?:[0-5]\\d):(?:[0-5]\\d)"; // HH:MM:SS

    private final EmployeeDataProvider employeeDataProvider;
    private final TimeClockDataProvider timeClockDataProvider;
    private final PendingUpdateApprovalDataProvider pendingUpdateApprovalDataProvider;
    private final DateFormatStrategy dateFormatStrategy = new DateFormatStrategy(new TimeFormatRegexValidator());

    public RequestUpdateTimeClockOutput execute(String externalAuthorization, UUID timeClockId, UpdateTimeClockInput input) {
        validateInputNewTimeClocked(input);

        Employee employee = findEmployee(externalAuthorization);

        TimeClock timeClock = findTimeClock(employee.getId(), timeClockId);
        Date updatedTimeClockHours = updateTimeClockHours(timeClock, input.getUpdatedTimeClocked());

        PendingUpdateApproval pendingUpdateApproval = fillPendingUpdateApproval(input, updatedTimeClockHours, timeClock);
        PendingUpdateApproval createdApproval = persistPendingUpdateApproval(pendingUpdateApproval);

        return mountOutput(createdApproval, timeClock);
    }

    private void validateInputNewTimeClocked(UpdateTimeClockInput input) {
        dateFormatStrategy.execute(input.getUpdatedTimeClocked(), null);
    }

    private Employee findEmployee(String externalAuthorization) {
        try {
            return employeeDataProvider.findEmployeeByResourceServerId(externalAuthorization).getBody();
        } catch (FeignException exception) {
            if (exception.status() == HttpStatus.NOT_FOUND.value()) {
                throw new EmployeeNotFoundException("Error while searching Employee! Resource not found!");
            } else {
                throw new RuntimeException(exception.getMessage());
            }
        }
    }

    private PendingUpdateApproval fillPendingUpdateApproval(UpdateTimeClockInput input, Date updatedTimeClocked, TimeClock timeClock) {
        return PendingUpdateApproval.builder()
            .approved(false)
            .reason(input.getReason())
            .timeClockUpdated(updatedTimeClocked)
            .timeClock(timeClock)
            .build();
    }

    private PendingUpdateApproval persistPendingUpdateApproval(PendingUpdateApproval pendingUpdateApproval) {
        return pendingUpdateApprovalDataProvider.persist(pendingUpdateApproval);
    }

    private TimeClock findTimeClock(Long employeeId, UUID timeClockId) {
        return timeClockDataProvider.findByIdAndEmployeeId(employeeId, timeClockId).orElseThrow(TimeClockNotFoundException::new);
    }

    private Date updateTimeClockHours(TimeClock timeClock, String updatedTimeClocked) {
        String[] hourMinuteSecond = extractTimeFromString(updatedTimeClocked);

        Calendar timeClockCalendar = Calendar.getInstance();
        timeClockCalendar.setTime(timeClock.getTimeClocked());
        timeClockCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourMinuteSecond[0]));
        timeClockCalendar.set(Calendar.MINUTE, Integer.parseInt(hourMinuteSecond[1]));
        timeClockCalendar.set(Calendar.SECOND, Integer.parseInt(hourMinuteSecond[2]));

        return timeClockCalendar.getTime();
    }

    private String[] extractTimeFromString(String updatedTimeClocked) {
        return updatedTimeClocked.split(":");
    }

    private RequestUpdateTimeClockOutput mountOutput(PendingUpdateApproval createdApproval, TimeClock timeClock) {
        return RequestUpdateTimeClockOutput.builder()
            .timeclockId(timeClock.getId())
            .oldTimeClocked(timeClock.getTimeClocked())
            .newRequestedTimeUpdated(createdApproval.getTimeClockUpdated())
            .build();
    }
}
