package org.com.clockinemployees.domain.usecase.manager.listManagerEmployeesUsecase;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeeManager;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.usecase.common.dto.EmployeeOutput;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.SuperiorNotFoundException;
import org.com.clockinemployees.domain.usecase.manager.listManagerEmployeesUsecase.dto.ListManagerEmployeesInput;
import org.com.clockinemployees.domain.usecase.manager.listManagerEmployeesUsecase.dto.ListManagerEmployeesOutput;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.exception.InsufficientPositionException;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.com.clockinemployees.infra.providers.EmployeeManagerDataProvider;
import org.com.clockinemployees.infra.providers.EmployeePositionDataProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@AllArgsConstructor
@Service
public class ListManagerEmployeesUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final EmployeeManagerDataProvider employeeManagerDataProvider;
    private final EmployeePositionDataProvider employeePositionDataProvider;

    public ListManagerEmployeesOutput execute(String superiorId, ListManagerEmployeesInput input) {
        Employee superior = findSuperior(superiorId);

        if (Objects.nonNull(superior.getDisabledAt())) {
            throw new SuperiorNotFoundException();
        }

        handleInput(input);

        Set<EmployeePosition> superiorPositions = findSuperiorPositions(superior.getId());

        checkSuperiorHasPermissions(superiorPositions);

        Page<EmployeeManager> superiorEmployees = findSuperiorEmployees(superior.getId(), input);

        return mountOutput(superiorEmployees);
    }

    private void handleInput(ListManagerEmployeesInput input) {
        if (input.getPage() < 1) {
            input.setPage(1);
        }

        if (input.getSize() > 50) {
            input.setSize(50);
        } else if (input.getSize() < 5) {
            input.setSize(5);
        }
    }

    private Employee findSuperior(String resourceServerId) {
        return employeeDataProvider.findByResourceServerId(resourceServerId).orElseThrow(SuperiorNotFoundException::new);
    }

    private Set<EmployeePosition> findSuperiorPositions(Long superiorId) {
        return employeePositionDataProvider.findAllByEmployeeId(superiorId);
    }

    private void checkSuperiorHasPermissions(Set<EmployeePosition> superiorPositions) {
        boolean hasNecessaryPosition = findPositionNecessaryPositionOnList(superiorPositions);

        if (!hasNecessaryPosition) {
            throw new InsufficientPositionException(false);
        }
    }

    private boolean findPositionNecessaryPositionOnList(Set<EmployeePosition> superiorPositions) {
        return superiorPositions.stream().anyMatch(superiorPosition ->
            superiorPosition.getPosition().getName().equals(EnterprisePosition.MANAGER) ||
                superiorPosition.getPosition().getName().equals(EnterprisePosition.CEO) ||
                superiorPosition.getPosition().getName().equals(EnterprisePosition.HUMAN_RESOURCES)
        );
    }

    private Page<EmployeeManager> findSuperiorEmployees(Long superiorId, ListManagerEmployeesInput input) {
        Pageable pageable = PageRequest.of(input.getPage() - 1, input.getSize());

        return employeeManagerDataProvider.findManagerEmployees(superiorId, input.getName(), pageable);
    }

    private ListManagerEmployeesOutput mountOutput(Page<EmployeeManager> employeeManagers) {
        List<Employee> managerEmployees = employeeManagers.getContent().stream().map(EmployeeManager::getEmployee).toList();

        return ListManagerEmployeesOutput.builder()
            .page(employeeManagers.getNumber() + 1)
            .size(employeeManagers.getSize())
            .totalItems(employeeManagers.getTotalElements())
            .totalPages(employeeManagers.getTotalPages())
            .employees(managerEmployees.stream().map(EmployeeOutput::toDto).toList())
            .build();
    }
}
