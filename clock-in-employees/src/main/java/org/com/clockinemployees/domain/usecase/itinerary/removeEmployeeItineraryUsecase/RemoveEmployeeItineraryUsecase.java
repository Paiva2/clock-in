package org.com.clockinemployees.domain.usecase.itinerary.removeEmployeeItineraryUsecase;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeeManager;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.entity.Itinerary;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.usecase.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.common.exception.ItineraryNotFoundException;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.SuperiorNotFoundException;
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
public class RemoveEmployeeItineraryUsecase {
    private final ItineraryDataProvider itineraryDataProvider;
    private final EmployeeManagerDataProvider employeeManagerDataProvider;
    private final EmployeeDataProvider employeeDataProvider;
    private final EmployeePositionDataProvider employeePositionDataProvider;

    public void execute(String superiorResourceServerId, Long employeeId) {
        Employee superior = findSuperior(superiorResourceServerId);

        if (Objects.nonNull(superior.getDisabledAt())) {
            throw new SuperiorNotFoundException();
        }

        Employee employee = findEmployee(employeeId);

        if (Objects.nonNull(employee.getDisabledAt())) {
            throw new EmployeeNotFoundException();
        }

        Set<EmployeePosition> superiorPositions = findPositions(superior.getId());
        boolean superiorIsCeoOrHr = checkSuperiorIsCeoOrHr(superiorPositions);

        if (!superiorIsCeoOrHr) {
            boolean superiorIsManager = checkSuperiorIsManager(superiorPositions);

            if (!superiorIsManager) {
                throw new InsufficientPositionException(false);
            }

            checkSuperiorIsEmployeeManager(superior, employee);
        }

        Itinerary employeeItinerary = findItinerary(employee.getId());

        removeItinerary(employeeItinerary);
    }

    private Employee findSuperior(String superiorResourceServerId) {
        return employeeDataProvider.findByResourceServerId(superiorResourceServerId).orElseThrow(SuperiorNotFoundException::new);
    }

    private Employee findEmployee(Long employeeId) {
        return employeeDataProvider.findById(employeeId).orElseThrow(EmployeeNotFoundException::new);
    }

    private boolean checkSuperiorIsCeoOrHr(Set<EmployeePosition> superiorPositions) {
        if (superiorPositions.isEmpty()) return false;

        return superiorPositions.stream().anyMatch(superiorPosition -> superiorPosition.getPosition().getName().equals(EnterprisePosition.CEO) || superiorPosition.getPosition().getName().equals(EnterprisePosition.HUMAN_RESOURCES));
    }

    private Set<EmployeePosition> findPositions(Long employeeId) {
        return employeePositionDataProvider.findAllByEmployeeId(employeeId);
    }

    private boolean checkSuperiorIsManager(Set<EmployeePosition> superiorPositions) {
        return superiorPositions.stream().anyMatch(superiorPosition -> superiorPosition.getPosition().getName().equals(EnterprisePosition.MANAGER));
    }

    private void checkSuperiorIsEmployeeManager(Employee superior, Employee employee) {
        List<EmployeeManager> employeeManagers = employeeManagerDataProvider.findEmployeeManagers(employee.getId());

        if (employeeManagers.isEmpty()) {
            throw new InsufficientPositionException("Only employee managers can handle his itinerary!");
        }

        employeeManagers.stream().filter(employeeManager -> employeeManager.getManager().getId().equals(superior.getId()))
            .findAny().orElseThrow(() -> new InsufficientPositionException("Only employee managers can handle his itinerary!"));
    }

    private Itinerary findItinerary(Long employeeId) {
        return itineraryDataProvider.findByEmployee(employeeId).orElseThrow(ItineraryNotFoundException::new);
    }

    private void removeItinerary(Itinerary itinerary) {
        itineraryDataProvider.remove(itinerary.getId());
    }
}
