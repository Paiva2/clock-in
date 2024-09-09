package org.com.clockinemployees.domain.usecase.itinerary.editEmployeeItinerary;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeeManager;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.entity.Itinerary;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.strategy.dateValidatorStrategy.HourValidator;
import org.com.clockinemployees.domain.strategy.dateValidatorStrategy.strategies.HourStringRegexValidatorStrategy;
import org.com.clockinemployees.domain.usecase.common.dto.ItineraryOutput;
import org.com.clockinemployees.domain.usecase.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.common.exception.InvalidHourFormatException;
import org.com.clockinemployees.domain.usecase.common.exception.ItineraryNotFoundException;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.SuperiorNotFoundException;
import org.com.clockinemployees.domain.usecase.itinerary.editEmployeeItinerary.dto.EditEmployeeItineraryInput;
import org.com.clockinemployees.domain.usecase.itinerary.makeEmployeeDayWorkHours.MakeEmployeeDayWorkHoursUsecase;
import org.com.clockinemployees.domain.usecase.itinerary.makeEmployeeDayWorkHours.dto.MakeEmployeeDayWorkHoursInput;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.exception.EmployeePositionNotFoundException;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.exception.InsufficientPositionException;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.com.clockinemployees.infra.providers.EmployeeManagerDataProvider;
import org.com.clockinemployees.infra.providers.EmployeePositionDataProvider;
import org.com.clockinemployees.infra.providers.ItineraryDataProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@AllArgsConstructor
@Service
public class EditEmployeeItineraryUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final ItineraryDataProvider itineraryDataProvider;
    private final EmployeePositionDataProvider employeePositionDataProvider;
    private final EmployeeManagerDataProvider employeeManagerDataProvider;
    private final MakeEmployeeDayWorkHoursUsecase makeEmployeeDayWorkHoursUsecase;
    private final HourValidator hourValidator = new HourValidator(new HourStringRegexValidatorStrategy());

    public ItineraryOutput execute(String resourceServerManagerId, Long employeeId, EditEmployeeItineraryInput input) {
        validateInput(input);

        Employee superior = findSuperior(resourceServerManagerId);

        if (Objects.nonNull(superior.getDisabledAt())) {
            throw new SuperiorNotFoundException();
        }

        Employee employee = findEmployee(employeeId);

        if (Objects.nonNull(employee.getDisabledAt())) {
            throw new EmployeeNotFoundException();
        }

        Set<EmployeePosition> superiorPositions = findEmployeePositions(superior.getId());

        boolean isCeoOrHr = checkSuperiorIsCeoOrHr(superiorPositions);

        if (!isCeoOrHr) {
            boolean isManager = checkSuperiorIsManager(superiorPositions);

            if (!isManager) {
                throw new InsufficientPositionException(false);
            }

            checkIsEmployeeManager(employee, superior);
        }

        Itinerary itinerary = findItinerary(employee.getId());

        boolean isItineraryTheSame = checkItineraryIsSame(itinerary, input);

        if (isItineraryTheSame) {
            return mountOutput(itinerary);
        }

        fillNewItinerary(itinerary, input);
        Itinerary itineraryEdited = persistNewItinerary(itinerary);

        return mountOutput(itineraryEdited);
    }

    private void validateInput(EditEmployeeItineraryInput input) {
        validateHourString(input.getHourIn(), "hourIn");
        validateHourString(input.getHourOut(), "hourOut");
        validateHourString(input.getIntervalIn(), "intervalIn");
        validateHourString(input.getIntervalOut(), "intervalOut");
    }

    private void validateHourString(String hour, String field) {
        boolean isValid = hourValidator.validate(hour);

        if (!isValid) {
            throw new InvalidHourFormatException(field);
        }
    }

    private Employee findSuperior(String resourceServerManagerId) {
        return employeeDataProvider.findByResourceServerId(resourceServerManagerId).orElseThrow(SuperiorNotFoundException::new);
    }

    private Employee findEmployee(Long employeeId) {
        return employeeDataProvider.findById(employeeId).orElseThrow(EmployeePositionNotFoundException::new);
    }

    private Set<EmployeePosition> findEmployeePositions(Long superiorId) {
        Set<EmployeePosition> positions = employeePositionDataProvider.findAllByEmployeeId(superiorId);

        if (positions.isEmpty()) {
            throw new InsufficientPositionException(false);
        }

        return positions;
    }

    private boolean checkSuperiorIsCeoOrHr(Set<EmployeePosition> positions) {
        return positions.stream().anyMatch(employeePosition -> employeePosition.getPosition().getName().equals(EnterprisePosition.CEO) || employeePosition.getPosition().getName().equals(EnterprisePosition.HUMAN_RESOURCES));
    }

    private boolean checkSuperiorIsManager(Set<EmployeePosition> positions) {
        return positions.stream().anyMatch(employeePosition -> employeePosition.getPosition().getName().equals(EnterprisePosition.MANAGER));
    }

    private void checkIsEmployeeManager(Employee employee, Employee superior) {
        List<EmployeeManager> employeeManagers = findEmployeeManagers(employee.getId());

        if (employeeManagers.isEmpty()) {
            throw new InsufficientPositionException("Only the employee manager can handle its itinerary!");
        }

        employeeManagers.stream().filter(employeeManager -> employeeManager.getManager().getId().equals(superior.getId()))
            .findAny().orElseThrow(() -> new InsufficientPositionException("Only the employee manager can handle its itinerary!"));
    }

    private List<EmployeeManager> findEmployeeManagers(Long employeeId) {
        return employeeManagerDataProvider.findEmployeeManagers(employeeId);
    }

    private Itinerary findItinerary(Long employeeId) {
        return itineraryDataProvider.findByEmployee(employeeId).orElseThrow(ItineraryNotFoundException::new);
    }

    private boolean checkItineraryIsSame(Itinerary itinerary, EditEmployeeItineraryInput input) {
        boolean hourInEqual = itinerary.getInHour().equals(input.getHourIn());
        boolean intervalInEqual = itinerary.getIntervalInHour().equals(input.getIntervalIn());
        boolean intervalOutEqual = itinerary.getIntervalOutHour().equals(input.getIntervalOut());
        boolean hourOutEqual = itinerary.getOutHour().equals(input.getHourOut());

        return hourInEqual && intervalInEqual && intervalOutEqual && hourOutEqual;
    }

    private void fillNewItinerary(Itinerary itinerary, EditEmployeeItineraryInput input) {
        String dayWorkHours = makeEmployeeDayWorkHoursUsecase.execute(MakeEmployeeDayWorkHoursInput.builder()
            .hourIn(input.getHourIn())
            .intervalIn(input.getIntervalIn())
            .intervalOut(input.getIntervalOut())
            .hourOut(input.getHourOut())
            .build()
        );

        itinerary.setInHour(input.getHourIn());
        itinerary.setIntervalInHour(input.getIntervalIn());
        itinerary.setIntervalOutHour(input.getIntervalOut());
        itinerary.setOutHour(input.getHourOut());
        itinerary.setDayWorkHours(dayWorkHours);
    }

    private Itinerary persistNewItinerary(Itinerary itinerary) {
        return itineraryDataProvider.persist(itinerary);
    }

    private ItineraryOutput mountOutput(Itinerary itinerary) {
        return ItineraryOutput.toDto(itinerary);
    }
}
