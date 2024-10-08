package org.com.clockinemployees.domain.usecase.employee.disableEmployeeUsecase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeeManager;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.usecase.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.common.exception.EmployeeSuperiorNotFoundException;
import org.com.clockinemployees.domain.usecase.employee.disableEmployeeUsecase.dto.DisableEmployeeOutput;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.exception.InsufficientPositionException;
import org.com.clockinemployees.infra.keycloack.employee.EmployeeKeycloakClient;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.com.clockinemployees.infra.providers.EmployeeManagerDataProvider;
import org.com.clockinemployees.infra.providers.EmployeePositionDataProvider;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
@Builder
@Service
public class DisableEmployeeUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final EmployeeManagerDataProvider employeeManagerDataProvider;
    private final EmployeePositionDataProvider employeePositionDataProvider;
    private final EmployeeKeycloakClient employeeKeycloakClient;

    public DisableEmployeeOutput execute(String superiorId, Long employeeId) {
        Employee superior = findSuperior(superiorId);

        if (Objects.nonNull(superior.getDisabledAt())) {
            throw new EmployeeSuperiorNotFoundException();
        }

        Employee employee = findEmployee(employeeId);

        if (Objects.nonNull(employee.getDisabledAt())) {
            throw new EmployeeNotFoundException();
        }

        handleSuperiorPermissions(superior, employee);

        employee.setDisabledAt(new Date());

        Employee disabledEmployee = persistEmployeeDisabled(employee);
        disableUserResourceServer(disabledEmployee.getKeycloakId());

        return mountOutput(disabledEmployee, superior.getId());
    }

    private Employee findSuperior(String superiorId) {
        return employeeDataProvider.findByResourceServerId(superiorId).orElseThrow(EmployeeSuperiorNotFoundException::new);
    }

    private Employee findEmployee(Long employeeId) {
        return employeeDataProvider.findById(employeeId).orElseThrow(EmployeeNotFoundException::new);
    }

    private void handleSuperiorPermissions(Employee superior, Employee employee) {
        Set<EmployeePosition> superiorPositions = getSuperiorPositions(superior.getId());

        boolean isCeoOrHr = superiorPositions.stream().anyMatch(superiorPosition -> superiorPosition.getPosition().getName().equals(EnterprisePosition.CEO) || superiorPosition.getPosition().getName().equals(EnterprisePosition.HUMAN_RESOURCES));

        if (!isCeoOrHr) {
            checkEmployeeManager(superior.getId(), employee.getId());
        }
    }

    private void checkEmployeeManager(Long managerId, Long employeeId) {
        Optional<EmployeeManager> employeeManager = employeeManagerDataProvider.findEmployeeManager(managerId, employeeId);

        if (employeeManager.isEmpty()) {
            throw new InsufficientPositionException(false);
        }
    }

    private Set<EmployeePosition> getSuperiorPositions(Long superiorId) {
        return employeePositionDataProvider.findAllByEmployeeId(superiorId);
    }

    private void disableUserResourceServer(String userResourceServerId) {
        employeeKeycloakClient.handleUserEnabled(userResourceServerId, false);
    }

    private Employee persistEmployeeDisabled(Employee employee) {
        return employeeDataProvider.save(employee);
    }

    private DisableEmployeeOutput mountOutput(Employee disabledEmployee, Long superiorId) {
        return DisableEmployeeOutput.builder()
            .employeeId(disabledEmployee.getId())
            .firstName(disabledEmployee.getFirstName())
            .lastName(disabledEmployee.getLastName())
            .email(disabledEmployee.getEmail())
            .actionDoneBySuperiorId(superiorId)
            .build();
    }
}
