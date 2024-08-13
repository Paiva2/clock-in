package org.com.clockinemployees.domain.usecase.position.listPositionsUsecase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.entity.Position;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.usecase.employee.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.position.common.dto.PositionOutput;
import org.com.clockinemployees.domain.usecase.position.listPositionsUsecase.dto.ListPositionsOutput;
import org.com.clockinemployees.domain.usecase.position.listPositionsUsecase.exception.InsufficientPermissionPositionListException;
import org.com.clockinemployees.domain.usecase.position.listPositionsUsecase.dto.ListPositionsInput;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.com.clockinemployees.infra.providers.EmployeePositionDataProvider;
import org.com.clockinemployees.infra.providers.PositionDataProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

@AllArgsConstructor
@Builder
@Service
public class ListPositionsUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final PositionDataProvider positionDataProvider;
    private final EmployeePositionDataProvider employeePositionDataProvider;

    public ListPositionsOutput execute(String resourceServerEmployeeId, ListPositionsInput input) {
        handlePaginationDefaults(input);

        Employee employee = findEmployee(resourceServerEmployeeId);

        if (Objects.nonNull(employee.getDisabledAt())) {
            throw new EmployeeNotFoundException();
        }

        Set<EmployeePosition> employeePositions = getEmployeePositions(employee.getId());

        checkEmployeePermissions(employeePositions);

        Page<Position> availablePositions = getPositions(input);

        return mountOutput(availablePositions);
    }

    private void handlePaginationDefaults(ListPositionsInput input) {
        if (input.getPage() < 1) {
            input.setPage(1);
        }

        if (input.getSize() > 50) {
            input.setSize(50);
        }

        if (input.getSize() < 5) {
            input.setSize(5);
        }
    }

    private Employee findEmployee(String resourceServerId) {
        return employeeDataProvider.findByResourceServerId(resourceServerId).orElseThrow(EmployeeNotFoundException::new);
    }

    private Set<EmployeePosition> getEmployeePositions(Long employeeId) {
        return employeePositionDataProvider.findAllByEmployeeId(employeeId);
    }

    private void checkEmployeePermissions(Set<EmployeePosition> employeePositions) {
        employeePositions.stream().filter(ep ->
            ep.getPosition().getName().equals(EnterprisePosition.HUMAN_RESOURCES) ||
                ep.getPosition().getName().equals(EnterprisePosition.CEO)
        ).findAny().orElseThrow(InsufficientPermissionPositionListException::new);
    }

    private Page<Position> getPositions(ListPositionsInput input) {
        Pageable pageable = PageRequest.of(input.getPage() - 1, input.getSize());

        return positionDataProvider.findAll(pageable);
    }

    private ListPositionsOutput mountOutput(Page<Position> availablePositions) {
        return ListPositionsOutput.builder()
            .page(availablePositions.getNumber() + 1)
            .size(availablePositions.getSize())
            .totalItems(availablePositions.getTotalElements())
            .totalPages(availablePositions.getTotalPages())
            .items(availablePositions.getContent().stream().map(PositionOutput::toDto).toList())
            .build();
    }
}
