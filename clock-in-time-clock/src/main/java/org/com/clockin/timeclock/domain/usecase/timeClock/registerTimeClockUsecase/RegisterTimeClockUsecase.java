package org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase;

import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.domain.strategy.dateFormatValidator.DateTimeFormatStrategy;
import org.com.clockin.timeclock.domain.strategy.dateFormatValidator.strategies.DateHourFormatRegexValidator;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.dto.EventAlreadyClockedOnDay;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.dto.FuturePastTimeClockedException;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.dto.RegisterTimeClockInput;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.dto.RegisterTimeClockOutput;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.EmployeeNotFoundException;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.MaxTimeClockedExceptionForDay;
import org.com.clockin.timeclock.domain.utils.DateHandler;
import org.com.clockin.timeclock.infra.dataProvider.TimeClockDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.external.EmployeeDataProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Builder
@Service
public class RegisterTimeClockUsecase {
    private final static String INPUTTED_STRING_DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";

    private final EmployeeDataProvider employeeDataProvider;
    private final TimeClockDataProvider timeClockDataProvider;
    private final DateTimeFormatStrategy dateTimeFormatStrategy = new DateTimeFormatStrategy(new DateHourFormatRegexValidator());
    private final DateHandler dateHandler;

    public RegisterTimeClockOutput execute(String externalAuthorization, RegisterTimeClockInput input) {
        validateInputClockedTime(input.getTimeClocked());

        Employee employee = findEmployee(externalAuthorization);

        Date timeClockedOnDate = formatDateInputToDate(input.getTimeClocked());

        checkIfTimeClockedIsFutureOrPastMonth(timeClockedOnDate);
        checkTimeClocksInTheDay(employee.getId(), timeClockedOnDate, input.getEvent());

        TimeClock timeClock = fillTimeClock(employee.getId(), timeClockedOnDate, input.getEvent());

        persistTimeClock(timeClock);

        return mountOutput(employee.getId(), timeClock.getTimeClocked());
    }

    private void validateInputClockedTime(String timeClocked) {
        dateTimeFormatStrategy.execute(timeClocked, "timeClocked");
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

    private Date formatDateInputToDate(String inputTimeClocked) {
        try {
            return dateHandler.parseDate(inputTimeClocked, INPUTTED_STRING_DATE_FORMAT);
        } catch (ParseException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    private TimeClock fillTimeClock(Long employeeId, Date inputTimeClocked, TimeClock.Event event) {
        return TimeClock.builder()
            .timeClocked(inputTimeClocked)
            .externalEmployeeId(employeeId)
            .event(event)
            .build();
    }

    private void checkIfTimeClockedIsFutureOrPastMonth(Date inputTimeClocked) {
        Date today = Date.from(dateHandler.getTodayOnMaxHour().toInstant());
        Integer currentMonth = getCurrentMonth();
        Integer currentYear = getCurrentYear();

        Integer timeClockedMonth = Calendar.getInstance().get(Calendar.MONTH);
        Integer timeClockedYear = Calendar.getInstance().get(Calendar.YEAR);

        if (inputTimeClocked.after(today) || !currentMonth.equals(timeClockedMonth) || !currentYear.equals(timeClockedYear)) {
            throw new FuturePastTimeClockedException();
        }
    }

    private Integer getCurrentMonth() {
        return Calendar.getInstance().get(Calendar.MONTH);
    }

    private Integer getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    private void checkTimeClocksInTheDay(Long employeeId, Date inputTimeClocked, TimeClock.Event event) {
        List<TimeClock> timeClockedOnDay = timeClockedQuantityOnDay(employeeId, inputTimeClocked);

        if (timeClockedOnDay.size() >= 4) {
            throw new MaxTimeClockedExceptionForDay();
        }

        Optional<TimeClock> timeClockWithEvent = timeClockedOnDay.stream().filter(timeClock -> timeClock.getEvent().equals(event)).findAny();

        if (timeClockWithEvent.isPresent()) {
            throw new EventAlreadyClockedOnDay(event);
        }
    }

    private List<TimeClock> timeClockedQuantityOnDay(Long employeeId, Date inputTimeClocked) {
        return timeClockDataProvider.findTimeClocksOnDayForEmployee(employeeId, inputTimeClocked);
    }

    private TimeClock persistTimeClock(TimeClock timeClock) {
        return timeClockDataProvider.persistTimeClock(timeClock);
    }

    private RegisterTimeClockOutput mountOutput(Long externalEmployeeId, Date timeClocked) {
        return RegisterTimeClockOutput.builder()
            .employeeId(externalEmployeeId)
            .timeClocked(timeClocked)
            .build();
    }
}
