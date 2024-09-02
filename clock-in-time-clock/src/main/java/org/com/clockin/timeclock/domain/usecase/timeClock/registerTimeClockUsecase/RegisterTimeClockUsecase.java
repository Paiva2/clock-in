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
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.ItineraryNotFoundException;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.MaxTimeClockedExceptionForDay;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.NecessaryEventMissingException;
import org.com.clockin.timeclock.domain.utils.DateHandler;
import org.com.clockin.timeclock.infra.dataProvider.ExtraHoursDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.TimeClockDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.external.EmployeeDataProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.util.*;

import static org.com.clockin.timeclock.domain.entity.TimeClock.Event.*;

@AllArgsConstructor
@Builder
@Service
public class RegisterTimeClockUsecase {
    private final static String INPUTTED_STRING_DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";
    private final static String INPUTTED_DAY_PERIOD_FORMAT = "dd-MM-yyyy";
    private final static Long ONE_HOUR_IN_SECONDS = 3600L;

    private final EmployeeDataProvider employeeDataProvider;
    private final TimeClockDataProvider timeClockDataProvider;
    private final ExtraHoursDataProvider extraHoursDataProvider;
    private final DateTimeFormatStrategy dateTimeFormatStrategy = new DateTimeFormatStrategy(new DateHourFormatRegexValidator());
    private final DateHandler dateHandler;

    @Transactional
    public RegisterTimeClockOutput execute(String externalAuthorization, RegisterTimeClockInput input) {
        validateInputClockedTime(input.getTimeClocked());

        Employee employee = findEmployee(externalAuthorization);

        if (Objects.isNull(employee.getItinerary())) {
            throw new ItineraryNotFoundException(employee.getId());
        }

        String dayPeriod;

        try {
            dayPeriod = extractDayPeriod(input.getTimeClocked());
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        Date timeClockedOnDate = formatDateInputToDate(input.getTimeClocked());

        List<TimeClock> timeClockedsOnDay = getListTimeClockedOnDay(employee.getId(), timeClockedOnDate);

        setTimeClockEvent(timeClockedsOnDay, input);

        checkIfTimeClockedIsFutureOrPastMonth(timeClockedOnDate);
        checkTimeClocksInTheDay(timeClockedsOnDay, input.getEvent());

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

    private void persistExtraHours(ExtraHours extraHours) {
        extraHoursDataProvider.persist(extraHours);
    }

    private void setTimeClockEvent(List<TimeClock> timeClockedsOnDay, RegisterTimeClockInput input) {
        if (Objects.nonNull(input.getEvent())) {
            checkDesiredAndNecessaryEvents(timeClockedsOnDay, input.getEvent());
            return;
        }

        if (timeClockedsOnDay.isEmpty()) {
            input.setEvent(IN);
            return;
        }

        TimeClock lastTimeClocked = timeClockedsOnDay.stream()
            .sorted((tc1, tc2) -> tc2.getTimeClocked().compareTo(tc1.getTimeClocked()))
            .findFirst()
            .get();

        TimeClock.Event nextEvent = getNextEventOnTimeClock(lastTimeClocked.getEvent());
        input.setEvent(nextEvent);
    }

    private TimeClock.Event getNextEventOnTimeClock(TimeClock.Event lastEvent) {
        TimeClock.Event nextEvent = null;

        switch (lastEvent) {
            case IN -> nextEvent = INTERVAL_IN;
            case INTERVAL_IN -> nextEvent = INTERVAL_OUT;
            case INTERVAL_OUT -> nextEvent = OUT;
        }

        return nextEvent;
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

    private void checkTimeClocksInTheDay(List<TimeClock> timeClockedsOnDay, TimeClock.Event event) {
        if (timeClockedsOnDay.size() >= 4) {
            throw new MaxTimeClockedExceptionForDay();
        }

        Optional<TimeClock> timeClockWithEvent = timeClockedsOnDay.stream().filter(timeClock -> timeClock.getEvent().equals(event)).findAny();

        if (timeClockWithEvent.isPresent()) {
            throw new EventAlreadyClockedOnDay(event);
        }
    }

    private void checkDesiredAndNecessaryEvents(List<TimeClock> timeClockedsOnDay, TimeClock.Event event) {
        switch (event) {
            case INTERVAL_IN -> findNecessaryEventOnList(timeClockedsOnDay, IN, INTERVAL_IN);
            case INTERVAL_OUT -> findNecessaryEventOnList(timeClockedsOnDay, INTERVAL_IN, INTERVAL_OUT);
            case OUT -> {
                findNecessaryEventOnList(timeClockedsOnDay, IN, OUT);

                Boolean employeeHasIntervalIn = findTimeClockEventOnList(timeClockedsOnDay, INTERVAL_IN).isPresent();

                if (employeeHasIntervalIn) {
                    findNecessaryEventOnList(timeClockedsOnDay, INTERVAL_OUT, OUT);
                }
            }
        }
    }

    private void findNecessaryEventOnList(List<TimeClock> timeClockedsList, TimeClock.Event necessaryEvent, TimeClock.Event desiredEvent) {
        findTimeClockEventOnList(timeClockedsList, necessaryEvent).orElseThrow(() -> new NecessaryEventMissingException(necessaryEvent, desiredEvent));
    }

    private Optional<TimeClock> findTimeClockEventOnList(List<TimeClock> timeClockedsList, TimeClock.Event necessaryEvent) {
        return timeClockedsList.stream().filter(timeClock -> timeClock.getEvent().equals(necessaryEvent)).findAny();
    }

    private List<TimeClock> getListTimeClockedOnDay(Long employeeId, Date inputTimeClocked) {
        return timeClockDataProvider.findTimeClocksOnDayForEmployee(employeeId, inputTimeClocked);
    }

    private void persistTimeClock(TimeClock timeClock) {
        timeClockDataProvider.persistTimeClock(timeClock);
    }


    private void handleExtraHours(Employee employee, Date timeClockedOnDate, String dayPeriod) {
        List<TimeClock> timeClockedsList = getListTimeClockedOnDay(employee.getId(), timeClockedOnDate);

        if (Objects.isNull(timeClockedsList) || timeClockedsList.isEmpty()) return;

        Optional<TimeClock> inClocked = findTimeClockEventOnList(timeClockedsList, IN);
        if (inClocked.isEmpty()) return;

        Optional<TimeClock> outClocked = findTimeClockEventOnList(timeClockedsList, OUT);
        if (outClocked.isEmpty()) return;

        Optional<TimeClock> intervalInClocked = findTimeClockEventOnList(timeClockedsList, INTERVAL_IN);

        Long secondsOfHoursWorked = 0L;

        if (intervalInClocked.isPresent()) {
            Optional<TimeClock> intervalOutClocked = findTimeClockEventOnList(timeClockedsList, INTERVAL_OUT);
            if (intervalOutClocked.isEmpty()) return;

            Duration diffBetweenInAndIntervalIn = Duration.between(inClocked.get().getTimeClocked().toInstant(), intervalInClocked.get().getTimeClocked().toInstant());
            secondsOfHoursWorked += diffBetweenInAndIntervalIn.toSeconds();

            Duration diffBetweenIntervalOutAndOut = Duration.between(intervalOutClocked.get().getTimeClocked().toInstant(), outClocked.get().getTimeClocked().toInstant());
            secondsOfHoursWorked += diffBetweenIntervalOutAndOut.toSeconds();
        } else {
            Duration diffBetweenInAndOut = Duration.between(inClocked.get().getTimeClocked().toInstant(), outClocked.get().getTimeClocked().toInstant());
            secondsOfHoursWorked += diffBetweenInAndOut.toSeconds();
        }

        Duration employeeTotalItinerary = getEmployeeWorkDuration(employee.getItinerary().getDayWorkHours());

        if (secondsOfHoursWorked > employeeTotalItinerary.toSeconds()) {
            Long extraWorkedOnSeconds = secondsOfHoursWorked - employeeTotalItinerary.toSeconds();
            Long hoursExtra = extraWorkedOnSeconds / 3600;
            Long minutes = (extraWorkedOnSeconds % 3600) / 60;

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
