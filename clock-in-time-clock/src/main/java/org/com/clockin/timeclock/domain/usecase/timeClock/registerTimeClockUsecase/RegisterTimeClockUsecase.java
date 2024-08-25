package org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase;

import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockin.timeclock.domain.entity.ExtraHours;
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
import org.com.clockin.timeclock.infra.dataProvider.ExtraHoursDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.TimeClockDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.external.EmployeeDataProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.util.*;

@AllArgsConstructor
@Builder
@Service
public class RegisterTimeClockUsecase {
    private final static String INPUTTED_STRING_DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";
    private final static String INPUTTED_DAY_PERIOD_FORMAT = "dd-MM-yyyy";

    private final EmployeeDataProvider employeeDataProvider;
    private final TimeClockDataProvider timeClockDataProvider;
    private final ExtraHoursDataProvider extraHoursDataProvider;
    private final DateTimeFormatStrategy dateTimeFormatStrategy = new DateTimeFormatStrategy(new DateHourFormatRegexValidator());
    private final DateHandler dateHandler;

    @Transactional
    public RegisterTimeClockOutput execute(String externalAuthorization, RegisterTimeClockInput input) {
        validateInputClockedTime(input.getTimeClocked());

        String dayPeriod;

        try {
            dayPeriod = extractDayPeriod(input.getTimeClocked());
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        Employee employee = findEmployee(externalAuthorization);

        Date timeClockedOnDate = formatDateInputToDate(input.getTimeClocked());

        checkIfTimeClockedIsFutureOrPastMonth(timeClockedOnDate);
        checkTimeClocksInTheDay(employee.getId(), timeClockedOnDate, input.getEvent());

        TimeClock timeClock = fillTimeClock(employee.getId(), timeClockedOnDate, input.getEvent());

        persistTimeClock(timeClock);

        handleExtraHours(employee, timeClockedOnDate, dayPeriod);

        return mountOutput(employee.getId(), timeClock.getTimeClocked());
    }

    private void validateInputClockedTime(String timeClocked) {
        dateTimeFormatStrategy.execute(timeClocked, "timeClocked");
    }

    private String extractDayPeriod(String timeClocked) throws ParseException {
        Date timeClockedDate = dateHandler.parseDate(timeClocked, INPUTTED_STRING_DATE_FORMAT);
        return dateHandler.formatDate(timeClockedDate, INPUTTED_DAY_PERIOD_FORMAT);
    }

    private void createExtraHours(String dayPeriod, Long employeeId, String extraHours) {
        ExtraHours fillExtra = fillNewExtraHours(dayPeriod, employeeId, extraHours);
        persistExtraHours(fillExtra);
    }

    private ExtraHours fillNewExtraHours(String dayPeriod, Long employeeId, String extraHours) {
        return ExtraHours.builder()
            .dayPeriod(dayPeriod)
            .externalEmployeeId(employeeId)
            .extraHours(extraHours)
            .build();
    }

    private ExtraHours persistExtraHours(ExtraHours extraHours) {
        return extraHoursDataProvider.persist(extraHours);
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
        List<TimeClock> timeClockedOnDay = getTimeClockedQuantityOnDay(employeeId, inputTimeClocked);

        if (timeClockedOnDay.size() >= 4) {
            throw new MaxTimeClockedExceptionForDay();
        }

        Optional<TimeClock> timeClockWithEvent = timeClockedOnDay.stream().filter(timeClock -> timeClock.getEvent().equals(event)).findAny();

        if (timeClockWithEvent.isPresent()) {
            throw new EventAlreadyClockedOnDay(event);
        }
    }

    private List<TimeClock> getTimeClockedQuantityOnDay(Long employeeId, Date inputTimeClocked) {
        return timeClockDataProvider.findTimeClocksOnDayForEmployee(employeeId, inputTimeClocked);
    }

    private TimeClock persistTimeClock(TimeClock timeClock) {
        return timeClockDataProvider.persistTimeClock(timeClock);
    }


    private void handleExtraHours(Employee employee, Date timeClockedOnDate, String dayPeriod) {
        List<TimeClock> timeClockedsList = getTimeClockedQuantityOnDay(employee.getId(), timeClockedOnDate);

        if (Objects.isNull(timeClockedsList) || timeClockedsList.isEmpty()) return;

        Date firstTimeClockedOnDay = timeClockedsList.stream().map(TimeClock::getTimeClocked).min(Date::compareTo).get();
        Date lastTimeClockedOnDay = timeClockedsList.stream().map(TimeClock::getTimeClocked).max(Date::compareTo).get();

        Duration durationWorkedOnDay = Duration.between(firstTimeClockedOnDay.toInstant(), lastTimeClockedOnDay.toInstant());
        Duration employeeTotalDayWorkHours = getEmployeeWorkDuration(employee.getItinerary().getDayWorkHours());

        if (durationWorkedOnDay.compareTo(employeeTotalDayWorkHours) > 0) {
            Long workedHoursSeconds = durationWorkedOnDay.toSeconds();
            Long itinerarySeconds = employeeTotalDayWorkHours.toSeconds();

            Long secondsExtras = workedHoursSeconds - itinerarySeconds;

            Long hoursExtra = secondsExtras / 3600;
            Long minutes = (secondsExtras % 3600) / 60;

            createExtraHours(dayPeriod, employee.getId(), dateHandler.buildHoursFormatString(hoursExtra, minutes));
        }
    }

    private Duration getEmployeeWorkDuration(String dayWorkHours) {
        String durationHoursChar = "PT" + dayWorkHours.replace(":", "H") + "M";
        return Duration.parse(durationHoursChar);
    }

    private RegisterTimeClockOutput mountOutput(Long externalEmployeeId, Date timeClocked) {
        return RegisterTimeClockOutput.builder()
            .employeeId(externalEmployeeId)
            .timeClocked(timeClocked)
            .build();
    }
}
