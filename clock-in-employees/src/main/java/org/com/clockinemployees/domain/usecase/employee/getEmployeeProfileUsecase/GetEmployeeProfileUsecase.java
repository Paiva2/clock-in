package org.com.clockinemployees.domain.usecase.employee.getEmployeeProfileUsecase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockinemployees.domain.entity.*;
import org.com.clockinemployees.domain.usecase.common.dto.EmployeeOutput;
import org.com.clockinemployees.domain.usecase.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.employee.editEmployeeProfileUsecase.exception.PersonalDataNotFoundException;
import org.com.clockinemployees.infra.providers.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
@Builder
public class GetEmployeeProfileUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final PersonalDataDataProvider personalDataDataProvider;
    private final EmployeeManagerDataProvider employeeManagerDataProvider;
    private final ItineraryDataProvider itineraryDataProvider;
    private final EmployeePositionDataProvider employeePositionDataProvider;

    public EmployeeOutput execute(String employeeResourceServerId, Long employeeId) {
        Employee employee;

        if (Objects.isNull(employeeId)) {
            employee = findEmployeeResourceServer(employeeResourceServerId);
        } else {
            Employee internalEmployee = findEmployee(employeeId);
            employee = findEmployeeResourceServer(internalEmployee.getKeycloakId());
        }

        if (Objects.nonNull(employee.getDisabledAt())) {
            throw new EmployeeNotFoundException();
        }

        PersonalData personalData = findPersonalData(employee.getId());

        List<EmployeeManager> employeeManagers = findManagers(employee.getId());

        Itinerary itinerary = findEmployeeItinerary(employee.getId());

        Set<EmployeePosition> employeePositions = findEmployeePositions(employee.getId());

        return mountOutput(employee, personalData, employeeManagers, itinerary, employeePositions);
    }

    private Employee findEmployeeResourceServer(String employeeResourceServerId) {
        return employeeDataProvider.findByResourceServerId(employeeResourceServerId).orElseThrow(EmployeeNotFoundException::new);
    }

    private Employee findEmployee(Long employeeId) {
        return employeeDataProvider.findById(employeeId).orElseThrow(EmployeeNotFoundException::new);
    }

    private PersonalData findPersonalData(Long employeeId) {
        return personalDataDataProvider.findByEmployeeId(employeeId).orElseThrow(PersonalDataNotFoundException::new);
    }

    private List<EmployeeManager> findManagers(Long employeeId) {
        return employeeManagerDataProvider.findEmployeeManagers(employeeId);
    }

    private Itinerary findEmployeeItinerary(Long employeeId) {
        Optional<Itinerary> itinerary = itineraryDataProvider.findByEmployee(employeeId);

        return itinerary.orElse(null);
    }

    private Set<EmployeePosition> findEmployeePositions(Long employeeId) {
        return employeePositionDataProvider.findAllByEmployeeId(employeeId);
    }

    private EmployeeOutput mountOutput(Employee employee, PersonalData personalData, List<EmployeeManager> managers, Itinerary itinerary, Set<EmployeePosition> employeePositions) {
        employee.setEmployeeManagers(managers);
        employee.setPersonalData(personalData);
        employee.setEmployeePositions(new ArrayList<>(employeePositions));

        return EmployeeOutput.toDto(employee, itinerary);
    }
}
