package org.com.clockinemployees.domain.usecase.employeeManager.removeEmployeeManagerUsecase;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeeManager;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.usecase.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.ManagerNotFoundException;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.SuperiorNotFoundException;
import org.com.clockinemployees.domain.usecase.employeeManager.removeEmployeeManagerUsecase.exception.EmployeeHasNoManagerException;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.exception.InsufficientPositionException;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.com.clockinemployees.infra.providers.EmployeeManagerDataProvider;
import org.com.clockinemployees.infra.providers.EmployeePositionDataProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
@Service
public class RemoveEmployeeManagerUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final EmployeeManagerDataProvider employeeManagerDataProvider;
    private final EmployeePositionDataProvider employeePositionDataProvider;

    @Transactional
    public void execute(String superiorIdResourceServer, Long managerId, Long employeeId) {
        Employee superior = findEmployee(superiorIdResourceServer);

        if (Objects.nonNull(superior.getDisabledAt())) {
            throw new SuperiorNotFoundException();
        }

        checkSuperiorPermissions(superior);

        Optional<Employee> employee = findEmployee(employeeId);

        if (employee.isEmpty() || Objects.nonNull(employee.get().getDisabledAt())) {
            throw new EmployeeNotFoundException();
        }

        Optional<Employee> manager = findEmployee(managerId);

        if (manager.isEmpty() || Objects.nonNull(manager.get().getDisabledAt())) {
            throw new ManagerNotFoundException();
        }

        List<EmployeeManager> findEmployeeManagers = findManagers(employee.get().getId());
        checkEmployeeHasManager(findEmployeeManagers, manager.get());

        removeEmployeeManager(employee.get(), manager.get());
    }

    private Employee findEmployee(String superiorIdResourceServer) {
        return employeeDataProvider.findByResourceServerId(superiorIdResourceServer).orElseThrow(SuperiorNotFoundException::new);
    }

    private Optional<Employee> findEmployee(Long employeeId) {
        return employeeDataProvider.findById(employeeId);
    }

    private void checkSuperiorPermissions(Employee superior) {
        Set<EmployeePosition> superiorPositions = findEmployeePositions(superior.getId());

        if (superiorPositions.isEmpty()) {
            throw new InsufficientPositionException(false);
        }

        boolean isCeoOrHr = checkIsCeoOrHr(superiorPositions);

        if (!isCeoOrHr) {
            throw new InsufficientPositionException("Only CEO or Human Resource positions can handle this resource!");
        }
    }

    private boolean checkIsCeoOrHr(Set<EmployeePosition> employeePositions) {
        return employeePositions.stream().anyMatch(employeePosition -> employeePosition.getPosition().getName().equals(EnterprisePosition.CEO) || employeePosition.getPosition().getName().equals(EnterprisePosition.HUMAN_RESOURCES));
    }

    private Set<EmployeePosition> findEmployeePositions(Long employeeId) {
        return employeePositionDataProvider.findAllByEmployeeId(employeeId);
    }

    private List<EmployeeManager> findManagers(Long employeeId) {
        return employeeManagerDataProvider.findEmployeeManagers(employeeId);
    }

    private void checkEmployeeHasManager(List<EmployeeManager> employeeManagers, Employee manager) {
        if (employeeManagers.isEmpty()) {
            throw new EmployeeHasNoManagerException(manager.getId());
        }

        employeeManagers.stream().filter(employeeManager -> employeeManager.getManager().getId().equals(manager.getId()))
            .findAny().orElseThrow(() -> new EmployeeHasNoManagerException(manager.getId()));
    }

    private void removeEmployeeManager(Employee employee, Employee manager) {
        employeeManagerDataProvider.remove(employee, manager);
    }
}
