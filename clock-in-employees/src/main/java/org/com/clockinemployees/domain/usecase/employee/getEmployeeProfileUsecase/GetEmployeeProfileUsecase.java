package org.com.clockinemployees.domain.usecase.employee.getEmployeeProfileUsecase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeeManager;
import org.com.clockinemployees.domain.entity.Itinerary;
import org.com.clockinemployees.domain.entity.PersonalData;
import org.com.clockinemployees.domain.usecase.common.dto.EmployeeOutput;
import org.com.clockinemployees.domain.usecase.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.employee.editEmployeeProfileUsecase.exception.PersonalDataNotFoundException;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.com.clockinemployees.infra.providers.EmployeeManagerDataProvider;
import org.com.clockinemployees.infra.providers.ItineraryDataProvider;
import org.com.clockinemployees.infra.providers.PersonalDataDataProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
@Builder
public class GetEmployeeProfileUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final PersonalDataDataProvider personalDataDataProvider;
    private final EmployeeManagerDataProvider employeeManagerDataProvider;
    private final ItineraryDataProvider itineraryDataProvider;

    public EmployeeOutput execute(String employeeResourceServerId) {
        Employee employee = findEmployee(employeeResourceServerId);

        if (Objects.nonNull(employee.getDisabledAt())) {
            throw new EmployeeNotFoundException();
        }

        PersonalData personalData = findPersonalData(employee.getId());

        List<EmployeeManager> employeeManagers = findManagers(employee.getId());

        Itinerary itinerary = findEmployeeItinerary(employee.getId());

        return mountOutput(employee, personalData, employeeManagers, itinerary);
    }

    private Employee findEmployee(String employeeResourceServerId) {
        return employeeDataProvider.findByResourceServerId(employeeResourceServerId).orElseThrow(EmployeeNotFoundException::new);
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

    private EmployeeOutput mountOutput(Employee employee, PersonalData personalData, List<EmployeeManager> managers, Itinerary itinerary) {
        employee.setEmployeeManagers(managers);
        employee.setPersonalData(personalData);

        return EmployeeOutput.toDto(employee, itinerary);
    }
}
