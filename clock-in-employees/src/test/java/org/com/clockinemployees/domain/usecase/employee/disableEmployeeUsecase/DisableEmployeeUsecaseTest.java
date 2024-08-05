package org.com.clockinemployees.domain.usecase.employee.disableEmployeeUsecase;

import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeeManager;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.usecase.employee.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.employee.disableEmployeeUsecase.dto.DisableEmployeeOutput;
import org.com.clockinemployees.domain.usecase.employee.disableEmployeeUsecase.exception.EmployeeSuperiorNotFoundException;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.com.clockinemployees.infra.providers.EmployeeManagerDataProvider;
import org.com.clockinemployees.infra.providers.EmployeePositionDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisableEmployeeUsecaseTest {
    @Mock
    private EmployeeDataProvider employeeDataProvider;
    @Mock
    private EmployeeManagerDataProvider employeeManagerDataProvider;
    @Mock
    private EmployeePositionDataProvider employeePositionDataProvider;

    private DisableEmployeeUsecase sut;

    @BeforeEach
    void setUp() {
        this.sut = DisableEmployeeUsecase.builder()
            .employeeDataProvider(employeeDataProvider)
            .employeeManagerDataProvider(employeeManagerDataProvider)
            .employeePositionDataProvider(employeePositionDataProvider)
            .build();
    }

    private Employee mockSuperior() {
        return Employee.builder()
            .firstName("test_name")
            .lastName("test_last_name")
            .email("test_email")
            .build();
    }

    private Employee mockEmployee() {
        return Employee.builder()
            .firstName("test_name")
            .lastName("test_last_name")
            .email("test_email")
            .build();
    }

    private EmployeePosition mockEmployeePosition() {
        return new EmployeePosition();
    }

    private EmployeeManager mockEmployeeManager() {
        return new EmployeeManager();
    }

    @Test
    void shouldCallSutWithCorrectlyProvidedParams() {
        Long superiorId = 1L;
        Long employeeId = 2L;

        DisableEmployeeUsecase mockSut = mock(DisableEmployeeUsecase.class);
        mockSut.execute(superiorId, employeeId);

        ArgumentCaptor<Long> superiorIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> employeeIdCaptor = ArgumentCaptor.forClass(Long.class);

        verify(mockSut, times(1)).execute(superiorIdCaptor.capture(), employeeIdCaptor.capture());

        Long superiorIdCaptured = superiorIdCaptor.getValue();
        Long employeeIdCaptured = employeeIdCaptor.getValue();

        assertEquals(superiorIdCaptured, 1L);
        assertEquals(employeeIdCaptured, 2L);
    }

    @Test
    void shouldThrowExceptionIfSuperiorNotFound() {
        Long superiorId = 1L;
        Long employeeId = 2L;

        when(employeeDataProvider.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EmployeeSuperiorNotFoundException.class, () -> {
            sut.execute(superiorId, employeeId);
        });

        assertEquals("Provided superior not found as employee superior!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfSuperiorIsDisabled() {
        Long superiorId = 1L;
        Long employeeId = 2L;

        Employee superiorMock = mockSuperior();
        superiorMock.setDisabledAt(new Date());

        when(employeeDataProvider.findById(Mockito.anyLong())).thenReturn(Optional.of(superiorMock));

        Exception exception = assertThrows(EmployeeSuperiorNotFoundException.class, () -> {
            sut.execute(superiorId, employeeId);
        });

        assertEquals("Provided superior not found as employee superior!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfEmployeeNotFound() {
        Long superiorId = 1L;
        Long employeeId = 2L;

        Employee mockSuperior = mockSuperior();

        doReturn(Optional.of(mockSuperior)).doReturn(Optional.empty()).when(employeeDataProvider).findById(Mockito.anyLong());

        Exception exception = assertThrows(EmployeeNotFoundException.class, () -> {
            sut.execute(superiorId, employeeId);
        });

        assertEquals("Employee not found!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfEmployeeIsDisabled() {
        Long superiorId = 1L;
        Long employeeId = 2L;

        Employee mockSuperior = mockSuperior();
        Employee mockEmployee = mockEmployee();

        mockEmployee.setDisabledAt(new Date());

        doReturn(Optional.of(mockSuperior)).doReturn(Optional.of(mockEmployee)).when(employeeDataProvider).findById(Mockito.anyLong());

        Exception exception = assertThrows(EmployeeNotFoundException.class, () -> {
            sut.execute(superiorId, employeeId);
        });

        assertEquals("Employee not found!", exception.getMessage());
    }

    @Test
    void shouldCallDataProviderToFindHumanResourceSuperiorWithCorrectlyParams() {
        Long superiorId = 1L;
        Long employeeId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(superiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(employeeId);

        doReturn(Optional.of(mockSuperior)).doReturn(Optional.of(mockEmployee)).when(employeeDataProvider).findById(Mockito.anyLong());
        doReturn(Optional.of(mockEmployeePosition())).when(employeePositionDataProvider).findHrByEmployeeId(Mockito.anyLong());
        when(employeeDataProvider.save(Mockito.any())).thenReturn(mockEmployee);

        sut.execute(superiorId, employeeId);

        ArgumentCaptor<Long> superiorCaptor = ArgumentCaptor.forClass(Long.class);

        verify(employeePositionDataProvider, times(1)).findHrByEmployeeId(superiorCaptor.capture());

        Long superiorIdCaptured = superiorCaptor.getValue();

        assertEquals(superiorId, superiorIdCaptured.longValue());
    }

    @Test
    void shouldSearchForEmployeeSuperiorIfSuperiorIsNotFromHumanResources() {
        Long superiorId = 1L;
        Long employeeId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(superiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(employeeId);

        doReturn(Optional.of(mockSuperior)).doReturn(Optional.of(mockEmployee)).when(employeeDataProvider).findById(Mockito.anyLong());
        doReturn(Optional.empty()).when(employeePositionDataProvider).findHrByEmployeeId(Mockito.anyLong());
        when(employeeManagerDataProvider.findEmployeeSuperior(Mockito.anyLong(), Mockito.anyLong())).thenReturn(Optional.of(mockEmployeeManager()));
        when(employeeDataProvider.save(Mockito.any())).thenReturn(mockEmployee);

        sut.execute(superiorId, employeeId);

        ArgumentCaptor<Long> superiorCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> employeeCaptor = ArgumentCaptor.forClass(Long.class);

        verify(employeeManagerDataProvider, times(1)).findEmployeeSuperior(superiorCaptor.capture(), employeeCaptor.capture());

        Long superiorIdCaptured = superiorCaptor.getValue();
        Long employeeIdCaptured = employeeCaptor.getValue();

        assertEquals(superiorId, superiorIdCaptured.longValue());
        assertEquals(employeeId, employeeIdCaptured.longValue());
    }

    @Test
    void shouldThrowExceptionIfSuperiorProvidedIsNotHrAndNotSuperiorFromEmployee() {
        Long superiorId = 1L;
        Long employeeId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(superiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(employeeId);

        doReturn(Optional.of(mockSuperior)).doReturn(Optional.of(mockEmployee)).when(employeeDataProvider).findById(Mockito.anyLong());
        doReturn(Optional.empty()).when(employeePositionDataProvider).findHrByEmployeeId(Mockito.anyLong());
        when(employeeManagerDataProvider.findEmployeeSuperior(Mockito.anyLong(), Mockito.anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EmployeeSuperiorNotFoundException.class, () -> {
            sut.execute(superiorId, employeeId);
        });

        assertEquals("Provided superior not found as employee superior!", exception.getMessage());
    }

    @Test
    void shouldPersistEmployeeWithDisabledAtSetted() {
        Long superiorId = 1L;
        Long employeeId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(superiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(employeeId);

        doReturn(Optional.of(mockSuperior)).doReturn(Optional.of(mockEmployee)).when(employeeDataProvider).findById(Mockito.anyLong());
        doReturn(Optional.of(mockSuperior)).when(employeePositionDataProvider).findHrByEmployeeId(Mockito.anyLong());
        when(employeeDataProvider.save(Mockito.any())).thenReturn(mockEmployee);

        sut.execute(superiorId, employeeId);

        ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);

        verify(employeeDataProvider, times(1)).save(employeeCaptor.capture());

        Employee employeeCaptured = employeeCaptor.getValue();

        assertEquals(employeeId, employeeCaptured.getId());
        assertNotNull(employeeCaptured.getDisabledAt());
    }

    @Test
    void shouldReturnOutputWithoutErrors() {
        Long superiorId = 1L;
        Long employeeId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(superiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(employeeId);

        doReturn(Optional.of(mockSuperior)).doReturn(Optional.of(mockEmployee)).when(employeeDataProvider).findById(Mockito.anyLong());
        doReturn(Optional.of(mockSuperior)).when(employeePositionDataProvider).findHrByEmployeeId(Mockito.anyLong());
        when(employeeDataProvider.save(Mockito.any())).thenReturn(mockEmployee);

        DisableEmployeeOutput output = sut.execute(superiorId, employeeId);

        assertEquals(mockEmployee.getId(), output.getEmployeeId());
        assertEquals(mockEmployee.getFirstName(), output.getFirstName());
        assertEquals(mockEmployee.getLastName(), output.getLastName());
        assertEquals(mockEmployee.getEmail(), output.getEmail());
        assertEquals(mockSuperior.getId(), output.getActionDoneBySuperiorId());
    }
}