package org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase;

import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.entity.Position;
import org.com.clockinemployees.domain.entity.key.EmployeePositionKey;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EditEmployeePositionUsecaseTest {
    @Mock
    private EmployeeDataProvider employeeDataProvider;

    @Mock
    private EmployeePositionDataProvider employeePositionDataProvider;

    @Mock
    private PositionDataProvider positionDataProvider;

    @Mock
    private EmployeeKeycloakClient employeeKeycloakClient;

    private EditEmployeePositionUsecase sut;

    private Employee mockSuperior() {
        return Employee.builder().build();
    }

    private Employee mockEmployee() {
        return Employee.builder().build();
    }

    private Position mockPosition() {
        return Position.builder().build();
    }

    private EmployeePosition mockEmployeePosition() {
        return new EmployeePosition();
    }

    @BeforeEach
    void setUp() {
        sut = EditEmployeePositionUsecase.builder()
            .employeeDataProvider(employeeDataProvider)
            .employeePositionDataProvider(employeePositionDataProvider)
            .positionDataProvider(positionDataProvider)
            .employeeKeycloakClient(employeeKeycloakClient)
            .build();
    }

    @Test
    void shouldThrowErrorIfSuperiorNotFound() {
        String mockSuperiorResourceServerId = UUID.randomUUID().toString();
        Long mockEmployeeId = 1L;
        Long mockPositionId = 1L;

        when(employeeDataProvider.findByResourceServerId(mockSuperiorResourceServerId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EmployeeSuperiorNotFoundException.class, () -> {
            sut.execute(mockSuperiorResourceServerId, mockEmployeeId, mockPositionId);
        });

        assertEquals("Provided superior not found as employee superior!", exception.getMessage());
    }

    @Test
    void shouldThrowErrorIfSuperiorIsDisabled() {
        String mockSuperiorResourceServerId = UUID.randomUUID().toString();
        Long mockEmployeeId = 1L;
        Long mockPositionId = 1L;

        Employee superior = mockSuperior();
        superior.setDisabledAt(new Date());

        when(employeeDataProvider.findByResourceServerId(mockSuperiorResourceServerId)).thenReturn(Optional.of(superior));

        Exception exception = assertThrows(EmployeeSuperiorNotFoundException.class, () -> {
            sut.execute(mockSuperiorResourceServerId, mockEmployeeId, mockPositionId);
        });

        assertEquals("Provided superior not found as employee superior!", exception.getMessage());
    }

    @Test
    void shouldThrowErrorIfEmployeeNotFound() {
        String mockSuperiorResourceServerId = UUID.randomUUID().toString();
        Long mockEmployeeId = 1L;
        Long mockPositionId = 1L;

        Employee superior = mockSuperior();

        when(employeeDataProvider.findByResourceServerId(mockSuperiorResourceServerId)).thenReturn(Optional.of(superior));
        when(employeeDataProvider.findById(mockEmployeeId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EmployeeNotFoundException.class, () -> {
            sut.execute(mockSuperiorResourceServerId, mockEmployeeId, mockPositionId);
        });

        assertEquals("Employee not found!", exception.getMessage());
    }

    @Test
    void shouldThrowErrorIfEmployeeIsDisabled() {
        String mockSuperiorResourceServerId = UUID.randomUUID().toString();
        Long mockEmployeeId = 1L;
        Long mockPositionId = 1L;

        Employee superior = mockSuperior();
        Employee mockEmployee = mockEmployee();
        mockEmployee.setDisabledAt(new Date());

        when(employeeDataProvider.findByResourceServerId(mockSuperiorResourceServerId)).thenReturn(Optional.of(superior));
        when(employeeDataProvider.findById(mockEmployeeId)).thenReturn(Optional.of(mockEmployee));

        Exception exception = assertThrows(EmployeeNotFoundException.class, () -> {
            sut.execute(mockSuperiorResourceServerId, mockEmployeeId, mockPositionId);
        });

        assertEquals("Employee not found!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfPositionNotFound() {
        String mockSuperiorResourceServerId = UUID.randomUUID().toString();
        Long mockEmployeeId = 1L;
        Long mockPositionId = 1L;

        Employee superior = mockSuperior();
        Employee mockEmployee = mockEmployee();

        when(employeeDataProvider.findByResourceServerId(mockSuperiorResourceServerId)).thenReturn(Optional.of(superior));
        when(employeeDataProvider.findById(mockEmployeeId)).thenReturn(Optional.of(mockEmployee));
        when(positionDataProvider.findPositionById(mockPositionId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(PositionNotFoundException.class, () -> {
            sut.execute(mockSuperiorResourceServerId, mockEmployeeId, mockPositionId);
        });

        assertEquals("Position not found.", exception.getMessage());
    }

    @Test
    void shouldSearchEmployeePositionWithCorrectlyParams() {
        String mockSuperiorResourceServerId = UUID.randomUUID().toString();
        Long mockEmployeeId = 1L;
        Long mockPositionId = 1L;
        Long mockSuperiorPositionId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(mockSuperiorPositionId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(mockEmployeeId);

        Position mockEmployeeCurrentPosition = mockPosition();
        mockEmployeeCurrentPosition.setId(2L);
        mockEmployeeCurrentPosition.setName(EnterprisePosition.EMPLOYEE);

        Position desiredPosition = mockPosition();
        desiredPosition.setId(mockPositionId);
        desiredPosition.setName(EnterprisePosition.MANAGER);

        Position mockPositionCeo = mockPosition();
        mockPositionCeo.setId(2L);
        mockPositionCeo.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockEmployeePositionEmployee = mockEmployeePosition();
        mockEmployeePositionEmployee.setPosition(mockEmployeeCurrentPosition);
        mockEmployeePositionEmployee.setEmployee(mockEmployee);
        mockEmployeePositionEmployee.setEmployeePositionKey(new EmployeePositionKey(mockEmployeeId, mockPositionId));

        EmployeePosition mockEmployeePositionSuperior = mockEmployeePosition();
        mockEmployeePositionSuperior.setPosition(mockPositionCeo);
        mockEmployeePositionSuperior.setEmployee(mockSuperior);
        mockEmployeePositionSuperior.setEmployeePositionKey(new EmployeePositionKey(mockSuperior.getId(), mockPositionCeo.getId()));

        when(employeeDataProvider.findByResourceServerId(mockSuperiorResourceServerId)).thenReturn(Optional.of(mockSuperior));
        when(employeeDataProvider.findById(mockEmployeeId)).thenReturn(Optional.of(mockEmployee));

        when(positionDataProvider.findPositionById(mockPositionId)).thenReturn(Optional.of(desiredPosition));

        when(employeePositionDataProvider.findByEmployeeId(mockEmployeeId)).thenReturn(Optional.of(mockEmployeePositionEmployee));
        when(employeePositionDataProvider.findByEmployeeId(mockSuperior.getId())).thenReturn(Optional.of(mockEmployeePositionSuperior));

        sut.execute(mockSuperiorResourceServerId, mockEmployeeId, mockEmployeeId);

        ArgumentCaptor<Long> employeeIdCaptor = ArgumentCaptor.forClass(Long.class);

        verify(employeePositionDataProvider, atLeast(1)).findByEmployeeId(employeeIdCaptor.capture());

        List<Long> employeeIdCaptorValues = employeeIdCaptor.getAllValues();

        assertEquals(mockEmployeeId, employeeIdCaptorValues.get(0));
    }

    @Test
    void shouldThrowExceptionIfEmployeePositionNotFound() {
        String mockSuperiorResourceServerId = UUID.randomUUID().toString();
        Long mockEmployeeId = 1L;
        Long mockPositionId = 1L;

        Employee superior = mockSuperior();
        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(mockEmployeeId);

        Position mockPosition = mockPosition();

        when(employeeDataProvider.findByResourceServerId(mockSuperiorResourceServerId)).thenReturn(Optional.of(superior));
        when(employeeDataProvider.findById(mockEmployeeId)).thenReturn(Optional.of(mockEmployee));
        when(positionDataProvider.findPositionById(mockPositionId)).thenReturn(Optional.of(mockPosition));
        when(employeePositionDataProvider.findByEmployeeId(mockEmployeeId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EmployeePositionNotFoundException.class, () -> {
            sut.execute(mockSuperiorResourceServerId, mockEmployeeId, mockPositionId);
        });

        assertEquals("Employee position not found!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfSuperiorIsNotCeoAndEmployeeIs() {
        String mockSuperiorResourceServerId = UUID.randomUUID().toString();
        Long mockEmployeeId = 1L;
        Long mockPositionId = 1L;

        Long mockSuperiorId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(mockSuperiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(mockEmployeeId);

        Position mockEmployeeCurrentPosition = mockPosition();
        mockEmployeeCurrentPosition.setId(2L);
        mockEmployeeCurrentPosition.setName(EnterprisePosition.CEO);

        Position desiredPosition = mockPosition();
        desiredPosition.setId(mockPositionId);
        desiredPosition.setName(EnterprisePosition.MANAGER);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setId(2L);
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockEmployeePositionEmployee = mockEmployeePosition();
        mockEmployeePositionEmployee.setPosition(mockEmployeeCurrentPosition);
        mockEmployeePositionEmployee.setEmployee(mockEmployee);

        EmployeePosition mockEmployeePositionSuperior = mockEmployeePosition();
        mockEmployeePositionSuperior.setPosition(mockPositionSuperior);
        mockEmployeePositionSuperior.setEmployee(mockSuperior);

        when(employeeDataProvider.findByResourceServerId(mockSuperiorResourceServerId)).thenReturn(Optional.of(mockSuperior));
        when(employeeDataProvider.findById(mockEmployeeId)).thenReturn(Optional.of(mockEmployee));

        when(positionDataProvider.findPositionById(mockPositionId)).thenReturn(Optional.of(desiredPosition));

        when(employeePositionDataProvider.findByEmployeeId(mockEmployeeId)).thenReturn(Optional.of(mockEmployeePositionEmployee));
        when(employeePositionDataProvider.findByEmployeeId(mockSuperior.getId())).thenReturn(Optional.of(mockEmployeePositionSuperior));

        Exception exception = assertThrows(InsufficientPositionException.class, () -> {
            sut.execute(mockSuperiorResourceServerId, mockEmployeeId, mockEmployeeId);
        });

        assertEquals("Only CEO's can handle CEO's positions!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfNewEmployeePositionIsCeoAndSuperiorIsNotCeo() {
        String mockSuperiorResourceServerId = UUID.randomUUID().toString();
        Long mockEmployeeId = 1L;
        Long mockPositionId = 1L;

        Long mockSuperiorId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(mockSuperiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(mockEmployeeId);

        Position mockEmployeeCurrentPosition = mockPosition();
        mockEmployeeCurrentPosition.setId(2L);
        mockEmployeeCurrentPosition.setName(EnterprisePosition.MANAGER);

        Position desiredPosition = mockPosition();
        desiredPosition.setId(mockPositionId);
        desiredPosition.setName(EnterprisePosition.CEO);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setId(2L);
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockEmployeePositionEmployee = mockEmployeePosition();
        mockEmployeePositionEmployee.setPosition(mockEmployeeCurrentPosition);
        mockEmployeePositionEmployee.setEmployee(mockEmployee);

        EmployeePosition mockEmployeePositionSuperior = mockEmployeePosition();
        mockEmployeePositionSuperior.setPosition(mockPositionSuperior);
        mockEmployeePositionSuperior.setEmployee(mockSuperior);

        when(employeeDataProvider.findByResourceServerId(mockSuperiorResourceServerId)).thenReturn(Optional.of(mockSuperior));
        when(employeeDataProvider.findById(mockEmployeeId)).thenReturn(Optional.of(mockEmployee));

        when(positionDataProvider.findPositionById(mockPositionId)).thenReturn(Optional.of(desiredPosition));

        when(employeePositionDataProvider.findByEmployeeId(mockEmployeeId)).thenReturn(Optional.of(mockEmployeePositionEmployee));
        when(employeePositionDataProvider.findByEmployeeId(mockSuperior.getId())).thenReturn(Optional.of(mockEmployeePositionSuperior));

        Exception exception = assertThrows(InsufficientPositionException.class, () -> {
            sut.execute(mockSuperiorResourceServerId, mockEmployeeId, mockEmployeeId);
        });

        assertEquals("Only CEO's can handle CEO's positions!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfSuperiorIsNotCeoOrHumanResource() {
        String mockSuperiorResourceServerId = UUID.randomUUID().toString();
        Long mockEmployeeId = 1L;
        Long mockPositionId = 1L;

        Long mockSuperiorId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(mockSuperiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(mockEmployeeId);

        Position mockEmployeeCurrentPosition = mockPosition();
        mockEmployeeCurrentPosition.setId(2L);
        mockEmployeeCurrentPosition.setName(EnterprisePosition.MANAGER);

        Position desiredPosition = mockPosition();
        desiredPosition.setId(mockPositionId);
        desiredPosition.setName(EnterprisePosition.MANAGER);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setId(2L);
        mockPositionSuperior.setName(EnterprisePosition.EMPLOYEE);

        EmployeePosition mockEmployeePositionEmployee = mockEmployeePosition();
        mockEmployeePositionEmployee.setPosition(mockEmployeeCurrentPosition);
        mockEmployeePositionEmployee.setEmployee(mockEmployee);

        EmployeePosition mockEmployeePositionSuperior = mockEmployeePosition();
        mockEmployeePositionSuperior.setPosition(mockPositionSuperior);
        mockEmployeePositionSuperior.setEmployee(mockSuperior);

        when(employeeDataProvider.findByResourceServerId(mockSuperiorResourceServerId)).thenReturn(Optional.of(mockSuperior));
        when(employeeDataProvider.findById(mockEmployeeId)).thenReturn(Optional.of(mockEmployee));

        when(positionDataProvider.findPositionById(mockPositionId)).thenReturn(Optional.of(desiredPosition));

        when(employeePositionDataProvider.findByEmployeeId(mockEmployeeId)).thenReturn(Optional.of(mockEmployeePositionEmployee));
        when(employeePositionDataProvider.findByEmployeeId(mockSuperior.getId())).thenReturn(Optional.of(mockEmployeePositionSuperior));

        Exception exception = assertThrows(InsufficientPositionException.class, () -> {
            sut.execute(mockSuperiorResourceServerId, mockEmployeeId, mockEmployeeId);
        });

        assertEquals("Only CEO's and Human Resource members can handle positions!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfEmployeeAlreadyHasDesiredPosition() {
        String mockSuperiorResourceServerId = UUID.randomUUID().toString();
        Long mockEmployeeId = 1L;
        Long mockPositionId = 1L;

        Long mockSuperiorId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(mockSuperiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(mockEmployeeId);

        Position mockEmployeeCurrentPosition = mockPosition();
        mockEmployeeCurrentPosition.setId(2L);
        mockEmployeeCurrentPosition.setName(EnterprisePosition.EMPLOYEE);

        Position desiredPosition = mockPosition();
        desiredPosition.setId(mockPositionId);
        desiredPosition.setName(EnterprisePosition.EMPLOYEE);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setId(2L);
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockEmployeePositionEmployee = mockEmployeePosition();
        mockEmployeePositionEmployee.setPosition(mockEmployeeCurrentPosition);
        mockEmployeePositionEmployee.setEmployee(mockEmployee);

        EmployeePosition mockEmployeePositionSuperior = mockEmployeePosition();
        mockEmployeePositionSuperior.setPosition(mockPositionSuperior);
        mockEmployeePositionSuperior.setEmployee(mockSuperior);

        when(employeeDataProvider.findByResourceServerId(mockSuperiorResourceServerId)).thenReturn(Optional.of(mockSuperior));
        when(employeeDataProvider.findById(mockEmployeeId)).thenReturn(Optional.of(mockEmployee));

        when(positionDataProvider.findPositionById(mockPositionId)).thenReturn(Optional.of(desiredPosition));

        when(employeePositionDataProvider.findByEmployeeId(mockEmployeeId)).thenReturn(Optional.of(mockEmployeePositionEmployee));
        when(employeePositionDataProvider.findByEmployeeId(mockSuperior.getId())).thenReturn(Optional.of(mockEmployeePositionSuperior));

        Exception exception = assertThrows(EmployeeAlreadyHasPositionException.class, () -> {
            sut.execute(mockSuperiorResourceServerId, mockEmployeeId, mockEmployeeId);
        });

        assertEquals(MessageFormat.format("Can''t assign position. Employee already has desired position: {0}", desiredPosition.getName()), exception.getMessage());
    }

    @Test
    void shouldRemoveOldEmployeePosition() {
        String mockSuperiorResourceServerId = UUID.randomUUID().toString();
        Long mockEmployeeId = 1L;
        Long mockPositionId = 1L;

        Long mockSuperiorId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(mockSuperiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(mockEmployeeId);

        Position mockEmployeeCurrentPosition = mockPosition();
        mockEmployeeCurrentPosition.setId(2L);
        mockEmployeeCurrentPosition.setName(EnterprisePosition.EMPLOYEE);

        Position desiredPosition = mockPosition();
        desiredPosition.setId(mockPositionId);
        desiredPosition.setName(EnterprisePosition.MANAGER);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setId(2L);
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockEmployeePositionEmployee = mockEmployeePosition();
        mockEmployeePositionEmployee.setPosition(mockEmployeeCurrentPosition);
        mockEmployeePositionEmployee.setEmployee(mockEmployee);

        EmployeePosition mockEmployeePositionSuperior = mockEmployeePosition();
        mockEmployeePositionSuperior.setPosition(mockPositionSuperior);
        mockEmployeePositionSuperior.setEmployee(mockSuperior);

        when(employeeDataProvider.findByResourceServerId(mockSuperiorResourceServerId)).thenReturn(Optional.of(mockSuperior));
        when(employeeDataProvider.findById(mockEmployeeId)).thenReturn(Optional.of(mockEmployee));

        when(positionDataProvider.findPositionById(mockPositionId)).thenReturn(Optional.of(desiredPosition));

        when(employeePositionDataProvider.findByEmployeeId(mockEmployeeId)).thenReturn(Optional.of(mockEmployeePositionEmployee));
        when(employeePositionDataProvider.findByEmployeeId(mockSuperior.getId())).thenReturn(Optional.of(mockEmployeePositionSuperior));

        sut.execute(mockSuperiorResourceServerId, mockEmployeeId, mockEmployeeId);

        ArgumentCaptor<EmployeePosition> employeePositionArgumentCaptor = ArgumentCaptor.forClass(EmployeePosition.class);

        verify(employeePositionDataProvider, times(1)).remove(employeePositionArgumentCaptor.capture());

        EmployeePosition employeePositionRemoved = employeePositionArgumentCaptor.getValue();

        assertEquals(employeePositionRemoved.getPosition().getName(), mockEmployeeCurrentPosition.getName());
    }

    @Test
    void shouldPersistNewEmployeePosition() {
        String mockSuperiorResourceServerId = UUID.randomUUID().toString();
        Long mockEmployeeId = 1L;
        Long mockPositionId = 1L;

        Long mockSuperiorId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(mockSuperiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(mockEmployeeId);

        Position mockEmployeeCurrentPosition = mockPosition();
        mockEmployeeCurrentPosition.setId(2L);
        mockEmployeeCurrentPosition.setName(EnterprisePosition.EMPLOYEE);

        Position desiredPosition = mockPosition();
        desiredPosition.setId(mockPositionId);
        desiredPosition.setName(EnterprisePosition.MANAGER);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setId(2L);
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockEmployeePositionEmployee = mockEmployeePosition();
        mockEmployeePositionEmployee.setPosition(mockEmployeeCurrentPosition);
        mockEmployeePositionEmployee.setEmployee(mockEmployee);

        EmployeePosition mockEmployeePositionSuperior = mockEmployeePosition();
        mockEmployeePositionSuperior.setPosition(mockPositionSuperior);
        mockEmployeePositionSuperior.setEmployee(mockSuperior);

        when(employeeDataProvider.findByResourceServerId(mockSuperiorResourceServerId)).thenReturn(Optional.of(mockSuperior));
        when(employeeDataProvider.findById(mockEmployeeId)).thenReturn(Optional.of(mockEmployee));

        when(positionDataProvider.findPositionById(mockPositionId)).thenReturn(Optional.of(desiredPosition));

        when(employeePositionDataProvider.findByEmployeeId(mockEmployeeId)).thenReturn(Optional.of(mockEmployeePositionEmployee));
        when(employeePositionDataProvider.findByEmployeeId(mockSuperior.getId())).thenReturn(Optional.of(mockEmployeePositionSuperior));

        sut.execute(mockSuperiorResourceServerId, mockEmployeeId, mockEmployeeId);

        ArgumentCaptor<EmployeePosition> employeePositionArgumentCaptor = ArgumentCaptor.forClass(EmployeePosition.class);

        verify(employeePositionDataProvider, times(1)).create(employeePositionArgumentCaptor.capture());

        EmployeePosition employeePositionCreated = employeePositionArgumentCaptor.getValue();

        assertEquals(employeePositionCreated.getPosition().getName(), desiredPosition.getName());
        assertEquals(employeePositionCreated.getEmployee().getId(), mockEmployee.getId());
    }

    @Test
    void shouldPersistNewEmployeePositionOnResourceServer() {
        String mockSuperiorResourceServerId = UUID.randomUUID().toString();
        Long mockEmployeeId = 1L;
        Long mockPositionId = 1L;

        Long mockSuperiorId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(mockSuperiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(mockEmployeeId);

        Position mockEmployeeCurrentPosition = mockPosition();
        mockEmployeeCurrentPosition.setId(2L);
        mockEmployeeCurrentPosition.setName(EnterprisePosition.EMPLOYEE);

        Position desiredPosition = mockPosition();
        desiredPosition.setId(mockPositionId);
        desiredPosition.setName(EnterprisePosition.MANAGER);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setId(2L);
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockEmployeePositionEmployee = mockEmployeePosition();
        mockEmployeePositionEmployee.setPosition(mockEmployeeCurrentPosition);
        mockEmployeePositionEmployee.setEmployee(mockEmployee);

        EmployeePosition mockEmployeePositionSuperior = mockEmployeePosition();
        mockEmployeePositionSuperior.setPosition(mockPositionSuperior);
        mockEmployeePositionSuperior.setEmployee(mockSuperior);

        when(employeeDataProvider.findByResourceServerId(mockSuperiorResourceServerId)).thenReturn(Optional.of(mockSuperior));
        when(employeeDataProvider.findById(mockEmployeeId)).thenReturn(Optional.of(mockEmployee));

        when(positionDataProvider.findPositionById(mockPositionId)).thenReturn(Optional.of(desiredPosition));

        when(employeePositionDataProvider.findByEmployeeId(mockEmployeeId)).thenReturn(Optional.of(mockEmployeePositionEmployee));
        when(employeePositionDataProvider.findByEmployeeId(mockSuperior.getId())).thenReturn(Optional.of(mockEmployeePositionSuperior));

        sut.execute(mockSuperiorResourceServerId, mockEmployeeId, mockEmployeeId);

        ArgumentCaptor<Employee> employeeArgumentCaptor = ArgumentCaptor.forClass(Employee.class);
        ArgumentCaptor<Position> newPositionArgumentCaptor = ArgumentCaptor.forClass(Position.class);
        ArgumentCaptor<Position> oldPositionArgumentCaptor = ArgumentCaptor.forClass(Position.class);

        verify(employeeKeycloakClient, times(1)).updateUserRoles(employeeArgumentCaptor.capture(), newPositionArgumentCaptor.capture(), oldPositionArgumentCaptor.capture());

        Employee employeeArgumentCaptorValue = employeeArgumentCaptor.getValue();
        Position newPositionArgumentCaptorValue = newPositionArgumentCaptor.getValue();
        Position oldPositionArgumentCaptorValue = oldPositionArgumentCaptor.getValue();

        assertEquals(employeeArgumentCaptorValue.getId(), mockEmployee.getId());
        assertEquals(newPositionArgumentCaptorValue.getName(), desiredPosition.getName());
        assertEquals(oldPositionArgumentCaptorValue.getName(), mockEmployeeCurrentPosition.getName());
    }

    @Test
    void shouldReturnUsecaseOutputWithoutErrors() {
        String mockSuperiorResourceServerId = UUID.randomUUID().toString();
        Long mockEmployeeId = 1L;
        Long mockPositionId = 1L;

        Long mockSuperiorId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(mockSuperiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(mockEmployeeId);

        Position mockEmployeeCurrentPosition = mockPosition();
        mockEmployeeCurrentPosition.setId(2L);
        mockEmployeeCurrentPosition.setName(EnterprisePosition.EMPLOYEE);

        Position desiredPosition = mockPosition();
        desiredPosition.setId(mockPositionId);
        desiredPosition.setName(EnterprisePosition.MANAGER);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setId(2L);
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockEmployeePositionEmployee = mockEmployeePosition();
        mockEmployeePositionEmployee.setPosition(mockEmployeeCurrentPosition);
        mockEmployeePositionEmployee.setEmployee(mockEmployee);

        EmployeePosition mockEmployeePositionSuperior = mockEmployeePosition();
        mockEmployeePositionSuperior.setPosition(mockPositionSuperior);
        mockEmployeePositionSuperior.setEmployee(mockSuperior);

        when(employeeDataProvider.findByResourceServerId(mockSuperiorResourceServerId)).thenReturn(Optional.of(mockSuperior));
        when(employeeDataProvider.findById(mockEmployeeId)).thenReturn(Optional.of(mockEmployee));

        when(positionDataProvider.findPositionById(mockPositionId)).thenReturn(Optional.of(desiredPosition));

        when(employeePositionDataProvider.findByEmployeeId(mockEmployeeId)).thenReturn(Optional.of(mockEmployeePositionEmployee));
        when(employeePositionDataProvider.findByEmployeeId(mockSuperior.getId())).thenReturn(Optional.of(mockEmployeePositionSuperior));

        EditEmployeePositionOutput output = sut.execute(mockSuperiorResourceServerId, mockEmployeeId, mockEmployeeId);

        assertEquals(mockEmployee.getId(), output.getUserId());
        assertEquals(mockSuperior.getId(), output.getSuperiorId());
        assertEquals(desiredPosition.getName(), output.getNewPosition());
    }
}