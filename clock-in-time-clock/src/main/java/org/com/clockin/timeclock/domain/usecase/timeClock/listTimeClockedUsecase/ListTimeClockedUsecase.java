package org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase;

import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.domain.strategy.dateFormatValidator.DateTimeFormatStrategy;
import org.com.clockin.timeclock.domain.strategy.dateFormatValidator.strategies.DateFormatRegexValidator;
import org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.dto.ListTimeClockedInputFilters;
import org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.dto.ListTimeClockedOutput;
import org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.dto.TimeClockFilterOutput;
import org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.dto.TimeClockListDTO;
import org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.exception.DateRangeInvalidException;
import org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.exception.InvalidDateFiltersException;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.EmployeeNotFoundException;
import org.com.clockin.timeclock.domain.utils.DateHandler;
import org.com.clockin.timeclock.infra.dataProvider.TimeClockDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.external.EmployeeDataProvider;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.util.*;

@Builder
@Service
@AllArgsConstructor
public class ListTimeClockedUsecase {
    private static final Integer MAX_DAYS_FROM_NOW = 90;
    private static final String DATE_PATTERN = "dd-MM-yyyy";

    private final TimeClockDataProvider timeClockDataProvider;
    private final EmployeeDataProvider employeeDataProvider;
    private final DateHandler dateHandler;
    private final DateTimeFormatStrategy dateTimeFormatStrategy = new DateTimeFormatStrategy(new DateFormatRegexValidator());

    public ListTimeClockedOutput execute(String externalAuthorization, ListTimeClockedInputFilters filters) {
        validateDateInputs(filters);

        Employee employee = findEmployee(externalAuthorization);

        Date startDate;
        Date endDate;

        try {
            startDate = formatInputDate(filters.getStartDate());
            endDate = formatInputDate(filters.getEndDate());
        } catch (ParseException exception) {
            throw new RuntimeException(exception.getMessage());
        }

        validateDateRange(startDate, endDate);

        Page<TimeClock> timeClocks = listTimeClocks(employee.getId(), startDate, endDate);

        return mountOutput(timeClocks, filters.getStartDate(), filters.getEndDate(), employee);
    }

    private void validateDateInputs(ListTimeClockedInputFilters filters) {
        if (Objects.isNull(filters.getStartDate()) || Objects.isNull(filters.getEndDate())) {
            throw new InvalidDateFiltersException();
        }

        dateTimeFormatStrategy.execute(filters.getStartDate(), "startDate");
        dateTimeFormatStrategy.execute(filters.getEndDate(), "endDate");
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

    private Date formatInputDate(String inputDate) throws ParseException {
        return dateHandler.parseDate(inputDate, DATE_PATTERN);
    }

    private void validateDateRange(Date startDate, Date endDate) {
        if (Objects.nonNull(startDate)) {
            Date ninetyDaysAgo = dateHandler.getTodayMinusDays(MAX_DAYS_FROM_NOW);

            if (Objects.nonNull(endDate) && startDate.after(endDate)) {
                throw new DateRangeInvalidException("Start date must be before end date!");
            }

            if (startDate.before(ninetyDaysAgo)) {
                throw new DateRangeInvalidException("Start date can't have more than 90 days from now!");
            }
        }
    }

    private Page<TimeClock> listTimeClocks(Long employeeId, Date startDate, Date endDate) {
        return timeClockDataProvider.listAllByEmployee(employeeId, startDate, endDate);
    }

    private String convertStringDuration(Duration duration) {
        return duration.toString().replace("PT", "");
    }

    private void calculateAndSetWorkHoursOfDay(LinkedHashMap<String, TimeClockListDTO> timeClockeds, Employee employee) {
        String employeeItineraryFormat = "PT" + employee.getItinerary().getDayWorkHours().replace(":", "H") + "M";
        Duration employeeItineraryDuration = Duration.parse(employeeItineraryFormat);

        timeClockeds.keySet().forEach(key -> {
            TimeClockListDTO timeClockListDto = timeClockeds.get(key);

            if (timeClockListDto.getTimeClockeds().size() < 2) return;

            Optional<TimeClockFilterOutput> eventIn = timeClockListDto.getTimeClockeds().stream().filter(timeClock -> timeClock.getEventType().equals(TimeClock.Event.IN)).findFirst();
            if (eventIn.isEmpty()) return;

            Date outDate = timeClockListDto.getTimeClockeds().stream().map(TimeClockFilterOutput::getTimeClocked).max(Date::compareTo).get();

            Duration hoursWorkedOnDay = Duration.between(eventIn.get().getTimeClocked().toInstant(), outDate.toInstant());

            if (hoursWorkedOnDay.compareTo(employeeItineraryDuration) > 0) {
                Long workedHoursSeconds = hoursWorkedOnDay.toSeconds();
                Long itinerarySeconds = employeeItineraryDuration.toSeconds();

                Long secondsExtras = workedHoursSeconds - itinerarySeconds;

                Long hoursExtra = secondsExtras / 3600;
                Long minutes = (secondsExtras % 3600) / 60;


                timeClockListDto.setTotalExtraHoursDay(dateHandler.buildHoursFormatString(hoursExtra, minutes));
            } else {
                timeClockListDto.setTotalExtraHoursDay("0");
            }

            timeClockListDto.setTotalHoursWorkDay(convertStringDuration(hoursWorkedOnDay));
        });
    }

    private LinkedHashMap<String, TimeClockListDTO> aggregateTimeClockedByDay(List<TimeClock> timeClocks, Employee employee) {
        LinkedHashMap<String, TimeClockListDTO> timeClockeds = new LinkedHashMap<>();

        for (TimeClock timeClock : timeClocks) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(timeClock.getTimeClocked());

            String day = DateHandler.extractDayNumberFromDate(timeClock.getTimeClocked());
            String month = DateHandler.extractMonthFromDate(timeClock.getTimeClocked());
            String year = DateHandler.extractYearFromDate(timeClock.getTimeClocked());

            String key = day + "-" + month + "-" + year;

            TimeClockListDTO timeClockList = timeClockeds.get(key);

            if (Objects.isNull(timeClockList)) {
                TimeClockListDTO timeClockListDtos = TimeClockListDTO.builder()
                    .timeClockeds(
                        new LinkedList<>() {{
                            add(TimeClockFilterOutput.toDto(timeClock));
                        }}
                    )
                    .totalHoursWorkDay(null)
                    .build();

                timeClockeds.put(key, timeClockListDtos);
            } else {
                timeClockList.getTimeClockeds().add(TimeClockFilterOutput.toDto(timeClock));
            }
        }

        calculateAndSetWorkHoursOfDay(timeClockeds, employee);

        return timeClockeds;
    }

    private ListTimeClockedOutput mountOutput(Page<TimeClock> timeClocks, String startDate, String endDate, Employee employee) {
        LinkedHashMap<String, TimeClockListDTO> timeClockeds = aggregateTimeClockedByDay(timeClocks.getContent(), employee);

        return ListTimeClockedOutput.builder()
            .totalItems(timeClocks.getTotalElements())
            .fromDate(startDate)
            .toDate(endDate)
            .items(timeClockeds)
            .build();
    }
}
