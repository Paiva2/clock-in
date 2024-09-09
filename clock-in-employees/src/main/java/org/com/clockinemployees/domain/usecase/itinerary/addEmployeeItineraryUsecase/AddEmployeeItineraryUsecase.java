package org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeeManager;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.entity.Itinerary;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.strategy.dateValidatorStrategy.HourValidator;
import org.com.clockinemployees.domain.strategy.dateValidatorStrategy.strategies.HourStringRegexValidatorStrategy;
import org.com.clockinemployees.domain.usecase.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.common.exception.InvalidHourFormatException;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.ManagerNotFoundException;
import org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase.dto.AddEmployeeItineraryInput;
import org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase.dto.AddEmployeeItineraryOutput;
import org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase.exception.EmployeeAlreadyHasItineraryException;
import org.com.clockinemployees.domain.usecase.itinerary.makeEmployeeDayWorkHours.MakeEmployeeDayWorkHoursUsecase;
import org.com.clockinemployees.domain.usecase.itinerary.makeEmployeeDayWorkHours.dto.MakeEmployeeDayWorkHoursInput;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.exception.EmployeePositionNotFoundException;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.exception.InsufficientPositionException;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.com.clockinemployees.infra.providers.EmployeeManagerDataProvider;
import org.com.clockinemployees.infra.providers.EmployeePositionDataProvider;
import org.com.clockinemployees.infra.providers.ItineraryDataProvider;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
@Service
public class AddEmployeeItineraryUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final ItineraryDataProvider itineraryDataProvider;
    private final EmployeePositionDataProvider employeePositionDataProvider;
    private final EmployeeManagerDataProvider employeeManagerDataProvider;
    private final MakeEmployeeDayWorkHoursUsecase makeEmployeeDayWorkHoursUsecase;
    private final HourValidator hourValidator = new HourValidator(new HourStringRegexValidatorStrategy());

    public AddEmployeeItineraryOutput execute(String resourceServerManagerId, Long employeeId, AddEmployeeItineraryInput input) {
        validateInputHours(input);

        Employee manager = findManager(resourceServerManagerId);

        if (Objects.nonNull(manager.getDisabledAt())) {
            throw new ManagerNotFoundException();
        }

        Employee employee = findEmployee(employeeId);

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
        boolean isValidFormat = hourValidator.validate(inputHour);

        if (!isValidFormat) {
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
        String darkWorkHours = makeEmployeeDayWorkHoursUsecase.execute(MakeEmployeeDayWorkHoursInput.builder()
            .hourIn(input.getHourIn())
            .intervalIn(input.getIntervalIn())
            .intervalOut(input.getIntervalOut())
            .hourOut(input.getHourOut())
            .build()
        );

        return Itinerary.builder()
            .inHour(input.getHourIn())
            .outHour(input.getHourOut())
            .intervalInHour(input.getIntervalIn())
            .intervalOutHour(input.getIntervalOut())
            .employee(employee)
            .dayWorkHours(darkWorkHours)
            .build();
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
