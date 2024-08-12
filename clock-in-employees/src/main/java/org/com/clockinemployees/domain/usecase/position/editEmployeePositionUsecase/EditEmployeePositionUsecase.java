package org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.entity.Position;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.usecase.employee.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.employee.common.exception.EmployeeSuperiorNotFoundException;
import org.com.clockinemployees.domain.usecase.employee.common.exception.PositionNotFoundException;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.dto.EditEmployeePositionOutput;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.exception.EmployeeAlreadyHasPositionException;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.exception.EmployeePositionNotFoundException;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.exception.InsufficientPositionException;
import org.com.clockinemployees.infra.keycloack.employee.EmployeeKeycloakClient;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.com.clockinemployees.infra.providers.EmployeePositionDataProvider;
import org.com.clockinemployees.infra.providers.PositionDataProvider;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Builder
@AllArgsConstructor
@Service
public class EditEmployeePositionUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final EmployeePositionDataProvider employeePositionDataProvider;
    private final PositionDataProvider positionDataProvider;
    private final EmployeeKeycloakClient employeeKeycloakClient;

    @Transactional
    public EditEmployeePositionOutput execute(String superiorResourceServerId, Long employeeId, Long positionId) {
        Optional<Employee> superior = findEmployee(superiorResourceServerId);

        if (superior.isEmpty() || Objects.nonNull(superior.get().getDisabledAt())) {
            throw new EmployeeSuperiorNotFoundException();
        }

        Optional<Employee> employee = findEmployee(employeeId);

        if (employee.isEmpty() || Objects.nonNull(employee.get().getDisabledAt())) {
            throw new EmployeeNotFoundException();
        }

        Position desiredPosition = findPosition(positionId);

        EmployeePosition getEmployeePosition = findEmployeePosition(employee.get().getId());
        Position oldEmployeePosition = getEmployeePosition.getPosition();

        checkSuperiorPermissions(superior.get(), getEmployeePosition, desiredPosition);
        checkEmployeeCurrentPosition(getEmployeePosition, desiredPosition);

        removeOldEmployeePosition(getEmployeePosition);

        createAndPersistNewEmployeePosition(employee.get(), desiredPosition);
        setAndPersistNewPositionResourceServer(employee.get(), desiredPosition, oldEmployeePosition);

        return mountOutput(employee.get().getId(), superior.get().getId(), desiredPosition);
    }

    private Optional<Employee> findEmployee(String employeeResourceServerId) {
        return employeeDataProvider.findByResourceServerId(employeeResourceServerId);
    }

    private Optional<Employee> findEmployee(Long employeeId) {
        return employeeDataProvider.findById(employeeId);
    }

    private void checkSuperiorPermissions(Employee superior, EmployeePosition employeePositions, Position desiredPosition) {
        EmployeePosition superiorPositions = findEmployeePosition(superior.getId());

        Boolean employeeIsCeo = hasCeoPosition(employeePositions);
        Boolean superiorIsCeo = hasCeoPosition(superiorPositions);

        if (employeeIsCeo && !superiorIsCeo) {
            throw new InsufficientPositionException(true);
        }

        Boolean superiorIsHumanResources = hasHumanResourcePosition(superiorPositions);

        if (desiredPosition.getName().equals(EnterprisePosition.CEO) && !superiorIsCeo) {
            throw new InsufficientPositionException(true);
        } else if (!superiorIsCeo && !superiorIsHumanResources) {
            throw new InsufficientPositionException(false);
        }
    }

    private Boolean hasCeoPosition(EmployeePosition employeePosition) {
        return employeePosition.getPosition().getName().equals(EnterprisePosition.CEO);
    }

    private Boolean hasHumanResourcePosition(EmployeePosition employeePosition) {
        return employeePosition.getPosition().getName().equals(EnterprisePosition.HUMAN_RESOURCES);
    }

    private EmployeePosition findEmployeePosition(Long employeeId) {
        return employeePositionDataProvider.findByEmployeeId(employeeId).orElseThrow(EmployeePositionNotFoundException::new);
    }

    private Position findPosition(Long positionId) {
        Optional<Position> position = positionDataProvider.findPositionById(positionId);

        if (position.isEmpty()) {
            throw new PositionNotFoundException();
        }

        return position.get();
    }

    private void checkEmployeeCurrentPosition(EmployeePosition employeePosition, Position desiredPosition) {
        Boolean employeeAlreadyHasThisPosition = employeePosition.getPosition().getName().equals(desiredPosition.getName());

        if (employeeAlreadyHasThisPosition) {
            throw new EmployeeAlreadyHasPositionException(desiredPosition.getName().toString());
        }
    }

    private void createAndPersistNewEmployeePosition(Employee employee, Position newPosition) {
        EmployeePosition employeePosition = new EmployeePosition(employee, newPosition);

        persistEmployeePosition(employeePosition);
    }

    private void removeOldEmployeePosition(EmployeePosition employeePosition) {
        employeePositionDataProvider.remove(employeePosition);
    }

    private void persistEmployeePosition(EmployeePosition employeePosition) {
        employeePositionDataProvider.create(employeePosition);
    }

    private void setAndPersistNewPositionResourceServer(Employee employee, Position newPosition, Position oldPosition) {
        employeeKeycloakClient.updateUserRoles(employee, newPosition, oldPosition);
    }

    private EditEmployeePositionOutput mountOutput(Long userId, Long superiorid, Position newPosition) {
        return EditEmployeePositionOutput.builder()
            .userId(userId)
            .superiorId(superiorid)
            .newPosition(newPosition.getName())
            .build();
    }
}
