package org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase;

import lombok.AllArgsConstructor;
import org.apache.commons.lang.time.DateUtils;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeeManager;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.entity.Itinerary;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.usecase.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.ManagerNotFoundException;
import org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase.dto.AddEmployeeItineraryInput;
import org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase.dto.AddEmployeeItineraryOutput;
import org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase.exception.EmployeeAlreadyHasItineraryException;
import org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase.exception.InvalidHourFormatException;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.exception.EmployeePositionNotFoundException;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.exception.InsufficientPositionException;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.com.clockinemployees.infra.providers.EmployeeManagerDataProvider;
import org.com.clockinemployees.infra.providers.EmployeePositionDataProvider;
import org.com.clockinemployees.infra.providers.ItineraryDataProvider;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@Service
public class AddEmployeeItineraryUsecase {
    private final static String HOUR_PATTERN = "^(0[0-9]|1[0-9]|2[0-3]):([0-5][0-9])$"; // HH:MM

    private final EmployeeDataProvider employeeDataProvider;
    private final ItineraryDataProvider itineraryDataProvider;
    private final EmployeePositionDataProvider employeePositionDataProvider;
    private final EmployeeManagerDataProvider employeeManagerDataProvider;

    public AddEmployeeItineraryOutput execute(String resourceServerManagerId, AddEmployeeItineraryInput input) {
        validateInputHours(input);

        Employee manager = findManager(resourceServerManagerId);

        if (Objects.nonNull(manager.getDisabledAt())) {
            throw new ManagerNotFoundException();
        }

        Employee employee = findEmployee(input.getEmployeeId());

        if (Objects.nonNull(employee.getDisabledAt())) {
            throw new EmployeeNotFoundException();
        }

        checkEmployeeAlreadyHasItinerary(employee.getId());
        checkManagerEmployee(manager, employee);

        Itinerary itinerary = fillItinerary(input, employee);
        Itinerary newItinerary = persistNewItinerary(itinerary);

        return mountOutput(newItinerary, employee.getId());
    }

    private void validateInputHours(AddEmployeeItineraryInput input) {
        validateHourPattern(input.getHourIn(), "hourIn");
        validateHourPattern(input.getHourOut(), "hourOut");
        validateHourPattern(input.getIntervalIn(), "intervalIn");
        validateHourPattern(input.getIntervalOut(), "intervalOut");
    }

    private void validateHourPattern(String inputHour, String field) {
        Pattern pattern = Pattern.compile(HOUR_PATTERN);
        Matcher matcher = pattern.matcher(inputHour);

        if (!matcher.matches()) {
            throw new InvalidHourFormatException(field);
        }
    }

    private Employee findEmployee(Long employeeId) {
        return employeeDataProvider.findById(employeeId).orElseThrow(EmployeeNotFoundException::new);
    }

    private Employee findManager(String employeeId) {
        return employeeDataProvider.findByResourceServerId(employeeId).orElseThrow(ManagerNotFoundException::new);
    }

    private void checkEmployeeAlreadyHasItinerary(Long employeeId) {
        Optional<Itinerary> itinerary = itineraryDataProvider.findByEmployee(employeeId);

        if (itinerary.isPresent()) {
            throw new EmployeeAlreadyHasItineraryException();
        }
    }

    private void checkManagerEmployee(Employee manager, Employee employee) {
        Optional<EmployeeManager> employeeManager = employeeManagerDataProvider.findEmployeeManager(manager.getId(), employee.getId());

        if (employeeManager.isPresent()) return;

        checkManagerIsHumanResourcesOrCeo(manager);
    }

    private Itinerary fillItinerary(AddEmployeeItineraryInput input, Employee employee) {
        String darkWorkHours = makeEmployeeDayWorkHour(input);

        return Itinerary.builder()
            .inHour(input.getHourIn())
            .outHour(input.getHourOut())
            .intervalInHour(input.getIntervalIn())
            .intervalOutHour(input.getIntervalOut())
            .employee(employee)
            .dayWorkHours(darkWorkHours)
            .build();
    }

    private String makeEmployeeDayWorkHour(AddEmployeeItineraryInput input) {
        String[] hourAndMinuteIn = input.getHourIn().split(":");
        String[] hourAndMinuteOut = input.getHourOut().split(":");

        Date timeIn = dateFromHourInput(hourAndMinuteIn, 0);
        Date timeOut = dateFromHourInput(hourAndMinuteOut, 0);

        Integer inHour = Integer.parseInt(hourAndMinuteIn[0]);
        Integer outHour = Integer.parseInt(hourAndMinuteOut[0]);

        Boolean employeeClockOutNextDay = inHour > outHour;

        if (employeeClockOutNextDay) {
            timeOut = DateUtils.addDays(timeOut, 1);
        }

        Long seconds = (timeOut.getTime() - timeIn.getTime()) / 1000;
        Long hh = TimeUnit.SECONDS.toHours(seconds) % 24;
        Long mm = TimeUnit.SECONDS.toMinutes(seconds) % 60;

        return MessageFormat.format(buildHoursFormatString(hh, mm), hh, mm);
    }

    private String buildHoursFormatString(Long hours, Long minutes) {
        StringBuilder stringBuilder = new StringBuilder();

        if (hours < 10) {
            stringBuilder.append("0");
        }

        stringBuilder.append("{0}").append(":");

        if (minutes < 10) {
            stringBuilder.append("0");
        }

        stringBuilder.append("{1}");

        return stringBuilder.toString();
    }

    private Date dateFromHourInput(String[] hourAndMinute, Integer daysToAdd) {
        Integer hours = Integer.parseInt(hourAndMinute[0]);
        Integer minutes = Integer.parseInt(hourAndMinute[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);

        if (daysToAdd > 0) {
            calendar.set(Calendar.DATE, daysToAdd);
        }

        return calendar.getTime();
    }

    private Itinerary persistNewItinerary(Itinerary itinerary) {
        return itineraryDataProvider.persist(itinerary);
    }

    private void checkManagerIsHumanResourcesOrCeo(Employee manager) {
        Set<EmployeePosition> managerPosition = employeePositionDataProvider.findAllByEmployeeId(manager.getId());

        if (Objects.isNull(managerPosition) || managerPosition.isEmpty()) {
            throw new EmployeePositionNotFoundException();
        }

        managerPosition.stream().filter(mp ->
                mp.getPosition().getName().equals(EnterprisePosition.HUMAN_RESOURCES) || mp.getPosition().getName().equals(EnterprisePosition.CEO))
            .findAny().orElseThrow(() -> new InsufficientPositionException(false));
    }

    private AddEmployeeItineraryOutput mountOutput(Itinerary itinerary, Long employeeId) {
        return AddEmployeeItineraryOutput.builder()
            .inHour(itinerary.getInHour())
            .intervalInHour(itinerary.getIntervalInHour())
            .intervalOutHour(itinerary.getIntervalOutHour())
            .outHour(itinerary.getOutHour())
            .dayWorkHours(itinerary.getDayWorkHours())
            .employeeId(employeeId)
            .build();
    }
}
