package org.com.clockinemployees.domain.usecase.employee.disableEmployeeUsecase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.usecase.employee.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.employee.disableEmployeeUsecase.dto.DisableEmployeeOutput;
import org.com.clockinemployees.domain.usecase.employee.disableEmployeeUsecase.exception.EmployeeSuperiorNotFoundException;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.com.clockinemployees.infra.providers.EmployeeManagerDataProvider;
import org.com.clockinemployees.infra.providers.EmployeePositionDataProvider;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
@Builder
@Service
public class DisableEmployeeUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final EmployeeManagerDataProvider employeeManagerDataProvider;
    private final EmployeePositionDataProvider employeePositionDataProvider;

    public DisableEmployeeOutput execute(Long superiorId, Long employeeId) {
        Employee superior = findSuperior(superiorId);

        if (Objects.nonNull(superior.getDisabledAt())) {
            throw new EmployeeSuperiorNotFoundException();
        }

        Employee employee = findEmployee(employeeId);

        if (Objects.nonNull(employee.getDisabledAt())) {
            throw new EmployeeNotFoundException();
        }

        handleSuperiorBeingHumanResources(superior, employee);

        employee.setDisabledAt(new Date());

        Employee disabledEmployee = persistEmployeeDisabled(employee);

        return mountOutput(disabledEmployee, superiorId);
    }

    private Employee findSuperior(Long superiorId) {
        return employeeDataProvider.findById(superiorId).orElseThrow(EmployeeSuperiorNotFoundException::new);
    }

    private Employee findEmployee(Long employeeId) {
        return employeeDataProvider.findById(employeeId).orElseThrow(EmployeeNotFoundException::new);
    }

    private void handleSuperiorBeingHumanResources(Employee superior, Employee employee) {
        Optional<EmployeePosition> superiorHumanResources = checkIfSuperiorIsFromHumanResources(superior.getId());

        if (superiorHumanResources.isPresent()) return;

        checkEmployeeSuperior(superior.getId(), employee.getId());
    }

    private void checkEmployeeSuperior(Long superiorId, Long employeeId) {
        employeeManagerDataProvider.findEmployeeSuperior(superiorId, employeeId).orElseThrow(EmployeeSuperiorNotFoundException::new);
    }

    private Optional<EmployeePosition> checkIfSuperiorIsFromHumanResources(Long superiorId) {
        return employeePositionDataProvider.findHrByEmployeeId(superiorId);
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
