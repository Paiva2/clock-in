package org.com.clockinemployees.domain.usecase.employeeManager.InsertEmployeeManagerUsecase;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeeManager;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.usecase.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.employeeManager.InsertEmployeeManagerUsecase.exception.EmployeeAlreadyHasManagerException;
import org.com.clockinemployees.domain.usecase.employeeManager.InsertEmployeeManagerUsecase.exception.OnlyCeoOrHrException;
import org.com.clockinemployees.domain.usecase.employeeManager.InsertEmployeeManagerUsecase.exception.OnlyManagerException;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.ManagerNotFoundException;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.SuperiorNotFoundException;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.exception.EmployeePositionNotFoundException;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.com.clockinemployees.infra.providers.EmployeeManagerDataProvider;
import org.com.clockinemployees.infra.providers.EmployeePositionDataProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@AllArgsConstructor
public class InsertEmployeeManagerUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final EmployeeManagerDataProvider employeeManagerDataProvider;
    private final EmployeePositionDataProvider employeePositionDataProvider;

    public void execute(String resourceServerSuperiorId, Long managerId, Long employeeId) {
        Employee superior = findSuperior(resourceServerSuperiorId);

        if (Objects.nonNull(superior.getDisabledAt())) {
            throw new SuperiorNotFoundException();
        }

        checkSuperiorIsCeoOrHr(superior);

        Employee employee = findEmployee(employeeId);

        if (Objects.nonNull(employee.getDisabledAt())) {
            throw new EmployeeNotFoundException();
        }

        Employee manager = findEmployee(managerId);

        if (Objects.nonNull(manager.getDisabledAt())) {
            throw new ManagerNotFoundException();
        }

        checkManagerHasPermission(manager);
        checkEmployeeAlreadyHasManager(employee, manager);

        EmployeeManager employeeManager = fillEmployeeManager(employee, manager);
        persistEmployeeManager(employeeManager);
    }

    private Employee findEmployee(Long employeeId) {
        return employeeDataProvider.findById(employeeId).orElseThrow(EmployeeNotFoundException::new);
    }

    private Employee findSuperior(String resourceServerId) {
        return employeeDataProvider.findByResourceServerId(resourceServerId).orElseThrow(SuperiorNotFoundException::new);
    }

    private void checkSuperiorIsCeoOrHr(Employee superior) {
        Set<EmployeePosition> superiorPositions = findEmployeePositions(superior.getId());

        if (Objects.isNull(superiorPositions) || superiorPositions.isEmpty()) {
            throw new EmployeePositionNotFoundException();
        }

        boolean isCeoOrHr = findCeoOrHrPositionOnList(superiorPositions);

        if (!isCeoOrHr) {
            throw new OnlyCeoOrHrException();
        }
    }

    private boolean findCeoOrHrPositionOnList(Set<EmployeePosition> positions) {
        return positions.stream().anyMatch(employeePosition -> employeePosition.getPosition().getName().equals(EnterprisePosition.CEO) || employeePosition.getPosition().getName().equals(EnterprisePosition.HUMAN_RESOURCES));
    }

    private Set<EmployeePosition> findEmployeePositions(Long employeeId) {
        return employeePositionDataProvider.findAllByEmployeeId(employeeId);
    }

    private void checkManagerHasPermission(Employee manager) {
        boolean isManager = manager.getEmployeePositions().stream().anyMatch(employeePosition -> employeePosition.getPosition().getName().equals(EnterprisePosition.MANAGER));

        if (!isManager) {
            throw new OnlyManagerException();
        }
    }

    private void checkEmployeeAlreadyHasManager(Employee employee, Employee manager) {
        List<EmployeeManager> employeeManagers = employeeManagerDataProvider.findEmployeeManagers(employee.getId());

        if (employeeManagers.isEmpty()) return;

        boolean hasManagerAlready = employeeManagers.stream().anyMatch(employeeManager -> employeeManager.getManager().getId().equals(manager.getId()));

        if (hasManagerAlready) {
            throw new EmployeeAlreadyHasManagerException(employee.getId(), manager.getId());
        }
    }

    private EmployeeManager fillEmployeeManager(Employee employee, Employee manager) {
        EmployeeManager employeeManager = new EmployeeManager();
        employeeManager.setEmployee(employee);
        employeeManager.setManager(manager);

        return employeeManager;
    }

    private void persistEmployeeManager(EmployeeManager employeeManager) {
        employeeManagerDataProvider.save(employeeManager);
    }
}
