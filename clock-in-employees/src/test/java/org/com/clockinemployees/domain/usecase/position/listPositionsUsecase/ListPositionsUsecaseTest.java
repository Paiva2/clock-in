package org.com.clockinemployees.domain.usecase.position.listPositionsUsecase;

import org.assertj.core.util.Sets;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.entity.Position;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.usecase.employee.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.position.listPositionsUsecase.dto.ListPositionsInput;
import org.com.clockinemployees.domain.usecase.position.listPositionsUsecase.dto.ListPositionsOutput;
import org.com.clockinemployees.domain.usecase.position.listPositionsUsecase.exception.InsufficientPermissionPositionListException;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.com.clockinemployees.infra.providers.EmployeePositionDataProvider;
import org.com.clockinemployees.infra.providers.PositionDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListPositionsUsecaseTest {
    @Mock
    private EmployeeDataProvider employeeDataProvider;

    @Mock
    private PositionDataProvider positionDataProvider;

    @Mock
    private EmployeePositionDataProvider employeePositionDataProvider;

    private ListPositionsUsecase sut;

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
        sut = ListPositionsUsecase.builder()
            .employeeDataProvider(employeeDataProvider)
            .positionDataProvider(positionDataProvider)
            .employeePositionDataProvider(employeePositionDataProvider)
            .build();
    }

    @Test
    void shouldHandlePaginationDefaults() {
        String mockResourceServerId = UUID.randomUUID().toString();
        ListPositionsInput input = ListPositionsInput.builder().size(3).page(0).build();

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        Position mockPositionEmployee = mockPosition();
        mockPositionEmployee.setId(3L);
        mockPositionEmployee.setName(EnterprisePosition.HUMAN_RESOURCES);

        Position mockPositionOne = mockPosition();
        mockPositionOne.setId(1L);
        mockPositionOne.setName(EnterprisePosition.EMPLOYEE);

        Position mockPositionTwo = mockPosition();
        mockPositionTwo.setId(2L);
        mockPositionTwo.setName(EnterprisePosition.MANAGER);

        EmployeePosition mockEmployeePosition = mockEmployeePosition();
        mockEmployeePosition.setPosition(mockPositionEmployee);

        Page<Position> mockPositionsPage = new PageImpl<>(List.of(mockPositionOne, mockPositionTwo), PageRequest.of(0, input.getSize()), 2L);

        when(employeeDataProvider.findByResourceServerId(mockResourceServerId)).thenReturn(Optional.of(mockEmployee));
        when(positionDataProvider.findAll(any())).thenReturn(mockPositionsPage);
        when(employeePositionDataProvider.findAllByEmployeeId(mockEmployee.getId())).thenReturn(Sets.set(mockEmployeePosition));

        sut.execute(mockResourceServerId, input);

        ArgumentCaptor<Pageable> argumentCaptorPage = ArgumentCaptor.forClass(Pageable.class);

        verify(positionDataProvider).findAll(argumentCaptorPage.capture());

        Pageable pageableCaptured = argumentCaptorPage.getValue();

        assertEquals(0, pageableCaptured.getPageNumber());
        assertEquals(5, pageableCaptured.getPageSize());
    }

    @Test
    void shouldThrowExceptionIfEmployeeDontExists() {
        String mockResourceServerId = UUID.randomUUID().toString();
        ListPositionsInput input = ListPositionsInput.builder().size(1).page(5).build();

        when(employeeDataProvider.findByResourceServerId(mockResourceServerId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EmployeeNotFoundException.class, () -> {
            sut.execute(mockResourceServerId, input);
        });

        assertEquals("Employee not found!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfEmployeeIsDisabled() {
        String mockResourceServerId = UUID.randomUUID().toString();
        ListPositionsInput input = ListPositionsInput.builder().size(1).page(5).build();

        Employee mockEmployee = mockEmployee();
        mockEmployee.setDisabledAt(new Date());

        when(employeeDataProvider.findByResourceServerId(mockResourceServerId)).thenReturn(Optional.of(mockEmployee));

        Exception exception = assertThrows(EmployeeNotFoundException.class, () -> {
            sut.execute(mockResourceServerId, input);
        });

        assertEquals("Employee not found!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfEmployeeHasNoAccessPermissions() {
        String mockResourceServerId = UUID.randomUUID().toString();
        ListPositionsInput input = ListPositionsInput.builder().size(5).page(1).build();

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        Position mockPositionEmployee = mockPosition();
        mockPositionEmployee.setId(3L);
        mockPositionEmployee.setName(EnterprisePosition.EMPLOYEE);

        EmployeePosition mockEmployeePosition = mockEmployeePosition();
        mockEmployeePosition.setPosition(mockPositionEmployee);

        when(employeeDataProvider.findByResourceServerId(mockResourceServerId)).thenReturn(Optional.of(mockEmployee));
        when(employeePositionDataProvider.findAllByEmployeeId(mockEmployee.getId())).thenReturn(Sets.set(mockEmployeePosition));

        Exception exception = assertThrows(InsufficientPermissionPositionListException.class, () -> {
            sut.execute(mockResourceServerId, input);
        });

        assertEquals("Only CEO's and Human Resource can list positions!", exception.getMessage());
    }

    @Test
    void shouldSearchForPositions() {
        String mockResourceServerId = UUID.randomUUID().toString();
        ListPositionsInput input = ListPositionsInput.builder().size(5).page(2).build();

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        Position mockPositionEmployee = mockPosition();
        mockPositionEmployee.setId(3L);
        mockPositionEmployee.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockEmployeePosition = mockEmployeePosition();
        mockEmployeePosition.setPosition(mockPositionEmployee);

        Position mockPositionOne = mockPosition();
        mockPositionOne.setId(1L);
        mockPositionOne.setName(EnterprisePosition.EMPLOYEE);

        Position mockPositionTwo = mockPosition();
        mockPositionTwo.setId(2L);
        mockPositionTwo.setName(EnterprisePosition.MANAGER);

        Page<Position> mockPositionsPage = new PageImpl<>(List.of(mockPositionOne, mockPositionTwo), PageRequest.of(0, input.getSize()), 2L);

        when(employeeDataProvider.findByResourceServerId(mockResourceServerId)).thenReturn(Optional.of(mockEmployee));
        when(positionDataProvider.findAll(any())).thenReturn(mockPositionsPage);
        when(employeePositionDataProvider.findAllByEmployeeId(mockEmployee.getId())).thenReturn(Sets.set(mockEmployeePosition));

        sut.execute(mockResourceServerId, input);

        ArgumentCaptor<Pageable> pageableArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(positionDataProvider, times(1)).findAll(pageableArgumentCaptor.capture());

        Pageable pageableCaptured = pageableArgumentCaptor.getValue();

        assertEquals(1, pageableCaptured.getPageNumber());
        assertEquals(5, pageableCaptured.getPageSize());
    }

    @Test
    void shouldReturnUsecaseOutputWithoutErrors() {
        String mockResourceServerId = UUID.randomUUID().toString();
        ListPositionsInput input = ListPositionsInput.builder().size(5).page(1).build();

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        Position mockPositionEmployee = mockPosition();
        mockPositionEmployee.setId(3L);
        mockPositionEmployee.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockEmployeePosition = mockEmployeePosition();
        mockEmployeePosition.setPosition(mockPositionEmployee);

        Position mockPositionOne = mockPosition();
        mockPositionOne.setId(1L);
        mockPositionOne.setName(EnterprisePosition.EMPLOYEE);

        Position mockPositionTwo = mockPosition();
        mockPositionTwo.setId(2L);
        mockPositionTwo.setName(EnterprisePosition.MANAGER);

        Page<Position> mockPositionsPage = new PageImpl<>(List.of(mockPositionOne, mockPositionTwo), PageRequest.of(0, input.getSize()), 2L);

        when(employeeDataProvider.findByResourceServerId(mockResourceServerId)).thenReturn(Optional.of(mockEmployee));
        when(positionDataProvider.findAll(any())).thenReturn(mockPositionsPage);
        when(employeePositionDataProvider.findAllByEmployeeId(mockEmployee.getId())).thenReturn(Sets.set(mockEmployeePosition));

        ListPositionsOutput output = sut.execute(mockResourceServerId, input);

        assertEquals(output.getTotalItems(), 2L);
        assertEquals(output.getPage(), 1);
        assertEquals(output.getTotalPages(), 1);
        assertEquals(output.getItems().get(0).getName(), mockPositionOne.getName());
        assertEquals(output.getItems().get(1).getName(), mockPositionTwo.getName());
    }
}