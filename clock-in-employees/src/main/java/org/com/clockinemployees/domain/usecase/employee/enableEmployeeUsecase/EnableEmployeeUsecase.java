package org.com.clockinemployees.domain.usecase.employee.enableEmployeeUsecase;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeeManager;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.usecase.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.employee.enableEmployeeUsecase.dto.EnableEmployeeOutput;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.SuperiorNotFoundException;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.exception.InsufficientPositionException;
import org.com.clockinemployees.infra.keycloack.employee.EmployeeKeycloakClient;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.com.clockinemployees.infra.providers.EmployeeManagerDataProvider;
import org.com.clockinemployees.infra.providers.EmployeePositionDataProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
@Service
public class EnableEmployeeUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final EmployeePositionDataProvider employeePositionDataProvider;
    private final EmployeeManagerDataProvider employeeManagerDataProvider;
    private final EmployeeKeycloakClient employeeKeycloakClient;

    @Transactional
    public EnableEmployeeOutput execute(String superiorId, Long employeeId) {
        Employee superior = findSuperior(superiorId);

        if (Objects.nonNull(superior.getDisabledAt())) {
            throw new SuperiorNotFoundException();
        }

        Employee employee = findEmployee(employeeId);

        handleSuperiorPermissions(superior, employee);

        if (Objects.isNull(employee.getDisabledAt())) {
            return mountOutput(employee, superior);
        }

        enableEmployee(employee);

        return mountOutput(employee, superior);
    }

    private Employee findSuperior(String superiorId) {
        return employeeDataProvider.findByResourceServerId(superiorId).orElseThrow(SuperiorNotFoundException::new);
    }

    private void handleSuperiorPermissions(Employee superior, Employee employee) {
        Set<EmployeePosition> superiorPositions = getSuperiorPositions(superior.getId());

        boolean isCeoOrHr = superiorPositions.stream().anyMatch(superiorPosition -> superiorPosition.getPosition().getName().equals(EnterprisePosition.CEO) || superiorPosition.getPosition().getName().equals(EnterprisePosition.HUMAN_RESOURCES));

        if (!isCeoOrHr) {
            checkEmployeeManager(superior.getId(), employee.getId());
        }
    }

    private void enableEmployee(Employee employee) {
        employee.setDisabledAt(null);
        employeeKeycloakClient.handleUserEnabled(employee.getKeycloakId(), true);
        employeeDataProvider.save(employee);
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

    private Employee findEmployee(Long employeeId) {
        return employeeDataProvider.findById(employeeId).orElseThrow(EmployeeNotFoundException::new);
    }

    private EnableEmployeeOutput mountOutput(Employee employee, Employee superior) {
        return EnableEmployeeOutput.builder()
            .firstName(employee.getFirstName())
            .lastName(employee.getLastName())
            .email(employee.getEmail())
            .employeeId(employee.getId())
            .actionDoneBySuperiorId(superior.getId())
            .build();
    }
}
