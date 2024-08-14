package org.com.clockinemployees.domain.usecase.employee.getEmployeeProfileUsecase;

import org.com.clockinemployees.domain.entity.*;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.usecase.common.dto.EmployeeOutput;
import org.com.clockinemployees.domain.usecase.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.employee.editEmployeeProfileUsecase.exception.PersonalDataNotFoundException;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.com.clockinemployees.infra.providers.EmployeeManagerDataProvider;
import org.com.clockinemployees.infra.providers.PersonalDataDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetEmployeeProfileUsecaseTest {
    @Mock
    private EmployeeDataProvider employeeDataProvider;

    @Mock
    private PersonalDataDataProvider personalDataDataProvider;

    @Mock
    private EmployeeManagerDataProvider employeeManagerDataProvider;

    private GetEmployeeProfileUsecase sut;

    private Employee mockEmployee() {
        return Employee.builder().build();
    }

    private PersonalData mockPersonalData() {
        return PersonalData.builder().build();
    }

    private EmployeeManager mockEmployeeManager() {
        return new EmployeeManager();
    }

    private EmployeePosition mockEmployeePosition() {
        return new EmployeePosition();
    }

    private Position mockPosition() {
        return Position.builder().build();
    }

    @BeforeEach
    void setUp() {
        sut = GetEmployeeProfileUsecase.builder()
            .employeeDataProvider(employeeDataProvider)
            .personalDataDataProvider(personalDataDataProvider)
            .employeeManagerDataProvider(employeeManagerDataProvider)
            .build();
    }

    @Test
    void shouldThrowExceptionIfEmployeeNotFound() {
        String mockEmployeeResourceServerId = UUID.randomUUID().toString();

        when(employeeDataProvider.findByResourceServerId(mockEmployeeResourceServerId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EmployeeNotFoundException.class, () -> {
            sut.execute(mockEmployeeResourceServerId);
        });

        assertEquals("Employee not found!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfEmployeeIsDisabled() {
        String mockEmployeeResourceServerId = UUID.randomUUID().toString();
        Employee mockEmployee = mockEmployee();
        mockEmployee.setDisabledAt(new Date());

        when(employeeDataProvider.findByResourceServerId(mockEmployeeResourceServerId)).thenReturn(Optional.of(mockEmployee));

        Exception exception = assertThrows(EmployeeNotFoundException.class, () -> {
            sut.execute(mockEmployeeResourceServerId);
        });

        assertEquals("Employee not found!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfPersonalDataNotFound() {
        String mockEmployeeResourceServerId = UUID.randomUUID().toString();

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        when(employeeDataProvider.findByResourceServerId(mockEmployeeResourceServerId)).thenReturn(Optional.of(mockEmployee));
        when(personalDataDataProvider.findByEmployeeId(mockEmployee.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(PersonalDataNotFoundException.class, () -> {
            sut.execute(mockEmployeeResourceServerId);
        });

        assertEquals("Employee personal data not found!", exception.getMessage());
    }

    @Test
    void shouldSearchForEmployeeManagers() {
        String mockEmployeeResourceServerId = UUID.randomUUID().toString();

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        Employee mockManager = mockEmployee();
        mockManager.setId(2L);
        mockManager.setFirstName("any_name");
        mockManager.setLastName("any_name");
        mockManager.setEmail("any_email");
        mockManager.setProfilePictureUrl("any_picture_url");

        PersonalData mockPersonalData = mockPersonalData();
        mockPersonalData.setId(1L);
        mockPersonalData.setPhone("any_phone");
        mockPersonalData.setCity("any_city");
        mockPersonalData.setCountry("any_country");
        mockPersonalData.setState("any_state");
        mockPersonalData.setStreet("any_street");
        mockPersonalData.setHouseNumber("any_house_number");
        mockPersonalData.setComplement("any_complement");
        mockPersonalData.setZipcode("any_zipcode");

        List<EmployeeManager> mockEmployeeManagers = List.of(mockEmployeeManager());
        mockEmployeeManagers.get(0).setEmployee(mockEmployee);
        mockEmployeeManagers.get(0).setManager(mockManager);

        List<EmployeePosition> employeePositions = List.of(mockEmployeePosition());
        employeePositions.get(0).setPosition(mockPosition());
        employeePositions.get(0).getPosition().setName(EnterprisePosition.EMPLOYEE);

        mockEmployee.setPersonalData(mockPersonalData);
        mockEmployee.setEmployeeManagers(mockEmployeeManagers);
        mockEmployee.setEmployeePositions(employeePositions);

        when(employeeDataProvider.findByResourceServerId(mockEmployeeResourceServerId)).thenReturn(Optional.of(mockEmployee));
        when(personalDataDataProvider.findByEmployeeId(mockEmployee.getId())).thenReturn(Optional.of(mockPersonalData));
        when(employeeManagerDataProvider.findEmployeeManagers(mockEmployee.getId())).thenReturn(mockEmployeeManagers);

        sut.execute(mockEmployeeResourceServerId);

        ArgumentCaptor<Long> employeeCaptorId = ArgumentCaptor.forClass(Long.class);

        verify(employeeManagerDataProvider, times(1)).findEmployeeManagers(employeeCaptorId.capture());

        assertEquals(employeeCaptorId.getValue(), mockEmployee.getId());
    }

    @Test
    void shouldReturnUsecaseOutputWithoutErrors() {
        String mockEmployeeResourceServerId = UUID.randomUUID().toString();

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);
        mockEmployee.setEmail("any_email");
        mockEmployee.setProfilePictureUrl("any_picture_url");
        mockEmployee.setFirstName("any_name");
        mockEmployee.setLastName("any_name");

        Employee mockManager = mockEmployee();
        mockManager.setId(2L);
        mockManager.setFirstName("any_name");
        mockManager.setLastName("any_name");
        mockManager.setEmail("any_email");
        mockManager.setProfilePictureUrl("any_picture_url");

        PersonalData mockPersonalData = mockPersonalData();
        mockPersonalData.setId(1L);
        mockPersonalData.setPhone("any_phone");
        mockPersonalData.setCity("any_city");
        mockPersonalData.setCountry("any_country");
        mockPersonalData.setState("any_state");
        mockPersonalData.setStreet("any_street");
        mockPersonalData.setHouseNumber("any_house_number");
        mockPersonalData.setComplement("any_complement");
        mockPersonalData.setZipcode("any_zipcode");

        List<EmployeeManager> mockEmployeeManagers = List.of(mockEmployeeManager());
        mockEmployeeManagers.get(0).setEmployee(mockEmployee);
        mockEmployeeManagers.get(0).setManager(mockManager);

        List<EmployeePosition> employeePositions = List.of(mockEmployeePosition());
        employeePositions.get(0).setPosition(mockPosition());
        employeePositions.get(0).getPosition().setName(EnterprisePosition.EMPLOYEE);

        mockEmployee.setPersonalData(mockPersonalData);
        mockEmployee.setEmployeeManagers(mockEmployeeManagers);
        mockEmployee.setEmployeePositions(employeePositions);

        when(employeeDataProvider.findByResourceServerId(mockEmployeeResourceServerId)).thenReturn(Optional.of(mockEmployee));
        when(personalDataDataProvider.findByEmployeeId(mockEmployee.getId())).thenReturn(Optional.of(mockPersonalData));
        when(employeeManagerDataProvider.findEmployeeManagers(mockEmployee.getId())).thenReturn(mockEmployeeManagers);

        EmployeeOutput output = sut.execute(mockEmployeeResourceServerId);

        assertEquals(output.getId(), mockEmployee.getId());
        assertEquals(output.getEmail(), mockEmployee.getEmail());
        assertEquals(output.getProfilePictureUrl(), mockEmployee.getProfilePictureUrl());
        assertEquals(output.getFirstName(), mockEmployee.getFirstName());
        assertEquals(output.getLastName(), mockEmployee.getLastName());
        assertEquals(output.getPersonalDataOutput().getZipcode(), mockPersonalData.getZipcode());
        assertEquals(output.getPersonalDataOutput().getState(), mockPersonalData.getState());
        assertEquals(output.getPersonalDataOutput().getCity(), mockPersonalData.getCity());
    }
}