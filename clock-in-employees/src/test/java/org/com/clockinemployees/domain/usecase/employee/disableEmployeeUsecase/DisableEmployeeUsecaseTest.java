package org.com.clockinemployees.domain.usecase.employee.disableEmployeeUsecase;

import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeeManager;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.entity.Position;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.usecase.employee.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.employee.disableEmployeeUsecase.dto.DisableEmployeeOutput;
import org.com.clockinemployees.domain.usecase.employee.common.exception.EmployeeSuperiorNotFoundException;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.exception.InsufficientPositionException;
import org.com.clockinemployees.infra.keycloack.employee.EmployeeKeycloakClient;
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
import java.util.UUID;

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

    @Mock
    private EmployeeKeycloakClient employeeKeycloakClient;

    private DisableEmployeeUsecase sut;

    @BeforeEach
    void setUp() {
        this.sut = DisableEmployeeUsecase.builder()
            .employeeDataProvider(employeeDataProvider)
            .employeeManagerDataProvider(employeeManagerDataProvider)
            .employeePositionDataProvider(employeePositionDataProvider)
            .employeeKeycloakClient(employeeKeycloakClient)
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

    private Position mockPosition() {
        return Position.builder().build();
    }

    private EmployeePosition mockEmployeePosition() {
        return new EmployeePosition();
    }

    private EmployeeManager mockEmployeeManager() {
        return new EmployeeManager();
    }

    @Test
    void shouldThrowExceptionIfSuperiorNotFound() {
        String superiorIdResourceServer = UUID.randomUUID().toString();
        Long employeeId = 2L;

        when(employeeDataProvider.findByResourceServerId(superiorIdResourceServer)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EmployeeSuperiorNotFoundException.class, () -> {
            sut.execute(superiorIdResourceServer, employeeId);
        });

        assertEquals("Provided superior not found as employee superior!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfSuperiorIsDisabled() {
        String superiorIdResourceServer = UUID.randomUUID().toString();
        Long employeeId = 2L;

        Employee superiorMock = mockSuperior();
        superiorMock.setDisabledAt(new Date());

        when(employeeDataProvider.findByResourceServerId(superiorIdResourceServer)).thenReturn(Optional.of(superiorMock));

        Exception exception = assertThrows(EmployeeSuperiorNotFoundException.class, () -> {
            sut.execute(superiorIdResourceServer, employeeId);
        });

        assertEquals("Provided superior not found as employee superior!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfEmployeeNotFound() {
        String superiorIdResourceServer = UUID.randomUUID().toString();
        Long superiorId = 1L;
        Long employeeId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(superiorId);

        when(employeeDataProvider.findByResourceServerId(superiorIdResourceServer)).thenReturn(Optional.of(mockSuperior));

        Exception exception = assertThrows(EmployeeNotFoundException.class, () -> {
            sut.execute(superiorIdResourceServer, employeeId);
        });

        assertEquals("Employee not found!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfEmployeeIsDisabled() {
        String superiorIdResourceServer = UUID.randomUUID().toString();
        Long superiorId = 1L;
        Long employeeId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(superiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setDisabledAt(new Date());

        when(employeeDataProvider.findByResourceServerId(superiorIdResourceServer)).thenReturn(Optional.of(mockSuperior));
        when(employeeDataProvider.findById(employeeId)).thenReturn(Optional.of(mockEmployee));

        Exception exception = assertThrows(EmployeeNotFoundException.class, () -> {
            sut.execute(superiorIdResourceServer, employeeId);
        });

        assertEquals("Employee not found!", exception.getMessage());
    }

    @Test
    void shouldCallDataProviderToFindSuperiorPositionWithCorrectlyParams() {
        String superiorIdResourceServer = UUID.randomUUID().toString();
        Long superiorId = 1L;
        Long employeeId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(superiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(employeeId);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.MANAGER);

        EmployeePosition superiorPosition = mockEmployeePosition();
        superiorPosition.setPosition(mockPositionSuperior);

        EmployeeManager mockEmployeeManager = mockEmployeeManager();

        when(employeeDataProvider.findByResourceServerId(superiorIdResourceServer)).thenReturn(Optional.of(mockSuperior));
        when(employeeDataProvider.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        doReturn(Optional.of(superiorPosition)).when(employeePositionDataProvider).findByEmployeeId(Mockito.anyLong());
        when(employeeDataProvider.save(Mockito.any())).thenReturn(mockEmployee);
        when(employeeManagerDataProvider.findEmployeeManager(mockSuperior.getId(), mockEmployee.getId())).thenReturn(Optional.of(mockEmployeeManager));

        sut.execute(superiorIdResourceServer, employeeId);

        ArgumentCaptor<Long> superiorCaptor = ArgumentCaptor.forClass(Long.class);

        verify(employeePositionDataProvider, times(1)).findByEmployeeId(superiorCaptor.capture());

        Long superiorIdCaptured = superiorCaptor.getValue();

        assertEquals(superiorId, superiorIdCaptured.longValue());
    }

    @Test
    void shouldSearchForEmployeeSuperiorIfSuperiorIsNotFromHumanResourcesOrCeo() {
        String superiorIdResourceServer = UUID.randomUUID().toString();
        Long superiorId = 1L;
        Long employeeId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(superiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(employeeId);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.MANAGER);

        EmployeePosition superiorPosition = mockEmployeePosition();
        superiorPosition.setPosition(mockPositionSuperior);

        EmployeeManager mockEmployeeManager = mockEmployeeManager();

        when(employeeDataProvider.findByResourceServerId(superiorIdResourceServer)).thenReturn(Optional.of(mockSuperior));
        when(employeeDataProvider.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        doReturn(Optional.empty()).when(employeePositionDataProvider).findByEmployeeId(Mockito.anyLong());
        when(employeeDataProvider.save(Mockito.any())).thenReturn(mockEmployee);
        when(employeePositionDataProvider.findByEmployeeId(mockSuperior.getId())).thenReturn(Optional.of(superiorPosition));
        when(employeeManagerDataProvider.findEmployeeManager(mockSuperior.getId(), mockEmployee.getId())).thenReturn(Optional.of(mockEmployeeManager));

        sut.execute(superiorIdResourceServer, employeeId);

        ArgumentCaptor<Long> superiorCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> employeeCaptor = ArgumentCaptor.forClass(Long.class);

        verify(employeeManagerDataProvider, times(1)).findEmployeeManager(superiorCaptor.capture(), employeeCaptor.capture());

        Long superiorIdCaptured = superiorCaptor.getValue();
        Long employeeIdCaptured = employeeCaptor.getValue();

        assertEquals(superiorId, superiorIdCaptured.longValue());
        assertEquals(employeeId, employeeIdCaptured.longValue());
    }

    @Test
    void shouldThrowExceptionIfSuperiorProvidedIsNotHrAndNotSuperiorFromEmployee() {
        String superiorIdResourceServer = UUID.randomUUID().toString();
        Long superiorId = 1L;
        Long employeeId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(superiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(employeeId);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.MANAGER);

        EmployeePosition superiorPosition = mockEmployeePosition();
        superiorPosition.setPosition(mockPositionSuperior);

        when(employeeDataProvider.findByResourceServerId(superiorIdResourceServer)).thenReturn(Optional.of(mockSuperior));
        when(employeeDataProvider.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        doReturn(Optional.empty()).when(employeePositionDataProvider).findByEmployeeId(Mockito.anyLong());
        when(employeeManagerDataProvider.findEmployeeManager(Mockito.anyLong(), Mockito.anyLong())).thenReturn(Optional.empty());
        when(employeePositionDataProvider.findByEmployeeId(mockSuperior.getId())).thenReturn(Optional.of(superiorPosition));
        when(employeeManagerDataProvider.findEmployeeManager(mockSuperior.getId(), mockEmployee.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EmployeeSuperiorNotFoundException.class, () -> {
            sut.execute(superiorIdResourceServer, employeeId);
        });

        assertEquals("Provided superior not found as employee superior!", exception.getMessage());
    }

    @Test
    void shouldPersistEmployeeWithDisabledAtSetted() {
        String superiorIdResourceServer = UUID.randomUUID().toString();
        Long superiorId = 1L;
        Long employeeId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(superiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(employeeId);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.MANAGER);

        EmployeePosition superiorPosition = mockEmployeePosition();
        superiorPosition.setPosition(mockPositionSuperior);

        EmployeeManager mockEmployeeManager = mockEmployeeManager();

        when(employeeDataProvider.findByResourceServerId(superiorIdResourceServer)).thenReturn(Optional.of(mockSuperior));
        when(employeeDataProvider.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        doReturn(Optional.of(mockSuperior)).when(employeePositionDataProvider).findByEmployeeId(Mockito.anyLong());
        when(employeeDataProvider.save(Mockito.any())).thenReturn(mockEmployee);
        when(employeePositionDataProvider.findByEmployeeId(mockSuperior.getId())).thenReturn(Optional.of(superiorPosition));
        when(employeeManagerDataProvider.findEmployeeManager(mockSuperior.getId(), mockEmployee.getId())).thenReturn(Optional.of(mockEmployeeManager));

        sut.execute(superiorIdResourceServer, employeeId);

        ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);

        verify(employeeDataProvider, times(1)).save(employeeCaptor.capture());

        Employee employeeCaptured = employeeCaptor.getValue();

        assertEquals(employeeId, employeeCaptured.getId());
        assertNotNull(employeeCaptured.getDisabledAt());
    }

    @Test
    void shouldThrowExceptionIfSuperiorProvidedIsAnEmployee() {
        String superiorIdResourceServer = UUID.randomUUID().toString();
        Long superiorId = 1L;
        Long employeeId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(superiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(employeeId);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.EMPLOYEE);

        EmployeePosition superiorPosition = mockEmployeePosition();
        superiorPosition.setPosition(mockPositionSuperior);

        when(employeeDataProvider.findByResourceServerId(superiorIdResourceServer)).thenReturn(Optional.of(mockSuperior));
        when(employeeDataProvider.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        doReturn(Optional.of(mockSuperior)).when(employeePositionDataProvider).findByEmployeeId(Mockito.anyLong());
        when(employeePositionDataProvider.findByEmployeeId(mockSuperior.getId())).thenReturn(Optional.of(superiorPosition));

        Exception exception = assertThrows(InsufficientPositionException.class, () -> {
            sut.execute(superiorIdResourceServer, employeeId);
        });

        assertEquals("Only CEO's and Human Resource members or managers can handle employees!", exception.getMessage());
    }

    @Test
    void shouldDisableEmployeeResourceServer() {
        String superiorIdResourceServer = UUID.randomUUID().toString();
        String mockDisabledEmployeeResourceServerId = UUID.randomUUID().toString();
        Long superiorId = 1L;
        Long employeeId = 2L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(superiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(employeeId);
        mockEmployee.setKeycloakId(mockDisabledEmployeeResourceServerId);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.MANAGER);

        EmployeePosition superiorPosition = mockEmployeePosition();
        superiorPosition.setPosition(mockPositionSuperior);

        EmployeeManager mockEmployeeManager = mockEmployeeManager();

        when(employeeDataProvider.findByResourceServerId(superiorIdResourceServer)).thenReturn(Optional.of(mockSuperior));
        when(employeeDataProvider.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        doReturn(Optional.of(mockSuperior)).when(employeePositionDataProvider).findByEmployeeId(Mockito.anyLong());
        when(employeeDataProvider.save(Mockito.any())).thenReturn(mockEmployee);
        when(employeePositionDataProvider.findByEmployeeId(mockSuperior.getId())).thenReturn(Optional.of(superiorPosition));
        when(employeeManagerDataProvider.findEmployeeManager(mockSuperior.getId(), mockEmployee.getId())).thenReturn(Optional.of(mockEmployeeManager));

        sut.execute(superiorIdResourceServer, employeeId);

        ArgumentCaptor<String> employeeResourceServerIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> enabledArgumentCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(employeeKeycloakClient, times(1)).handleUserEnabled(employeeResourceServerIdCaptor.capture(), enabledArgumentCaptor.capture());

        String employeeIdCaptured = employeeResourceServerIdCaptor.getValue();
        Boolean enabledCaptured = enabledArgumentCaptor.getValue();

        assertEquals(mockDisabledEmployeeResourceServerId, employeeIdCaptured);
        assertFalse(enabledCaptured);
    }

    @Test
    void shouldReturnOutputWithoutErrors() {
        String superiorIdResourceServer = UUID.randomUUID().toString();
        Long employeeId = 2L;
        Long mockSuperiorId = 1L;

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(mockSuperiorId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(employeeId);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.MANAGER);

        EmployeePosition superiorPosition = mockEmployeePosition();
        superiorPosition.setPosition(mockPositionSuperior);

        EmployeeManager mockEmployeeManager = mockEmployeeManager();

        when(employeeDataProvider.findByResourceServerId(superiorIdResourceServer)).thenReturn(Optional.of(mockSuperior));
        when(employeeDataProvider.findById(employeeId)).thenReturn(Optional.of(mockEmployee));
        doReturn(Optional.of(mockSuperior)).when(employeePositionDataProvider).findByEmployeeId(Mockito.anyLong());
        when(employeeDataProvider.save(Mockito.any())).thenReturn(mockEmployee);
        when(employeePositionDataProvider.findByEmployeeId(mockSuperior.getId())).thenReturn(Optional.of(superiorPosition));
        when(employeeManagerDataProvider.findEmployeeManager(mockSuperior.getId(), mockEmployee.getId())).thenReturn(Optional.of(mockEmployeeManager));

        DisableEmployeeOutput output = sut.execute(superiorIdResourceServer, employeeId);

        assertEquals(mockEmployee.getId(), output.getEmployeeId());
        assertEquals(mockEmployee.getFirstName(), output.getFirstName());
        assertEquals(mockEmployee.getLastName(), output.getLastName());
        assertEquals(mockEmployee.getEmail(), output.getEmail());
        assertEquals(mockSuperior.getId(), output.getActionDoneBySuperiorId());
    }
}