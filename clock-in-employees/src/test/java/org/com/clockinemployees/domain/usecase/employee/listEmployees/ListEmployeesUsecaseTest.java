package org.com.clockinemployees.domain.usecase.employee.listEmployees;

import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.PersonalData;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.usecase.employee.common.dto.EmployeeOutput;
import org.com.clockinemployees.domain.usecase.employee.listEmployees.dto.ListEmployeesInput;
import org.com.clockinemployees.domain.usecase.employee.listEmployees.dto.ListEmployeesOutput;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListEmployeesUsecaseTest {
    @Mock
    private EmployeeDataProvider employeeDataProvider;

    private ListEmployeesUsecase sut;

    @BeforeEach
    void setUp() {
        this.sut = ListEmployeesUsecase.builder()
            .employeeDataProvider(employeeDataProvider)
            .build();
    }

    private ListEmployeesInput mountInput() {
        return ListEmployeesInput.builder().build();
    }

    private Employee mountEmployee() {
        return Employee.builder().build();
    }

    private PersonalData mountPersonalData() {
        return PersonalData.builder().build();
    }

    @Test
    void shouldCallSutWithCorrectlyProvidedParameters() {
        ListEmployeesInput input = mountInput();
        input.setPage(1);
        input.setPerPage(10);
        input.setName("test_name");
        input.setEmail("test_email");
        input.setPosition(EnterprisePosition.DIRECTOR);

        ListEmployeesUsecase sutMock = mock(ListEmployeesUsecase.class);
        sutMock.execute(input);

        ArgumentCaptor<ListEmployeesInput> argumentCaptor = ArgumentCaptor.forClass(ListEmployeesInput.class);

        verify(sutMock, times(1)).execute(argumentCaptor.capture());

        ListEmployeesInput inputCaptorValue = argumentCaptor.getValue();

        assertEquals(input.getPage(), inputCaptorValue.getPage());
        assertEquals(input.getPerPage(), inputCaptorValue.getPerPage());
        assertEquals(input.getPosition(), inputCaptorValue.getPosition());
        assertEquals(input.getEmail(), inputCaptorValue.getEmail());
        assertEquals(input.getName(), inputCaptorValue.getName());
    }

    @Test
    void shouldCallDataProviderWithPageZeroIfZeroIsProvided() {
        ListEmployeesInput input = mountInput();
        input.setPage(0);
        input.setPerPage(10);
        input.setName("test_name");
        input.setEmail("test_email");
        input.setPosition(EnterprisePosition.DIRECTOR);

        Pageable pageable = PageRequest.of(1, 10);
        Page<Employee> pageEmployee = new PageImpl<>(Collections.emptyList(), pageable, 1);
        when(employeeDataProvider.listAllEmployees(any(), any(), any(), any())).thenReturn(pageEmployee);

        sut.execute(input);

        ArgumentCaptor<PageRequest> argumentCaptor = ArgumentCaptor.forClass(PageRequest.class);

        verify(employeeDataProvider, times(1)).listAllEmployees(argumentCaptor.capture(), any(), any(), any());

        PageRequest inputCaptorValue = argumentCaptor.getValue();

        assertEquals(inputCaptorValue.getPageNumber(), 0);
    }

    @Test
    void shouldCallDataProviderWithCorrectlyProvidedParams() {
        ListEmployeesInput input = mountInput();
        input.setPage(1);
        input.setPerPage(10);
        input.setName("test_name");
        input.setEmail("test_email");
        input.setPosition(EnterprisePosition.DIRECTOR);

        Pageable pageable = PageRequest.of(input.getPage(), input.getPerPage());
        Page<Employee> pageEmployee = new PageImpl<>(Collections.emptyList(), pageable, 1);
        when(employeeDataProvider.listAllEmployees(any(), any(), any(), any())).thenReturn(pageEmployee);

        sut.execute(input);

        ArgumentCaptor<PageRequest> argumentCaptorPage = ArgumentCaptor.forClass(PageRequest.class);
        ArgumentCaptor<String> argumentCaptorName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentCaptorEmail = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<EnterprisePosition> argumentCaptorPosition = ArgumentCaptor.forClass(EnterprisePosition.class);

        verify(employeeDataProvider, times(1)).listAllEmployees(argumentCaptorPage.capture(), argumentCaptorName.capture(), argumentCaptorEmail.capture(), argumentCaptorPosition.capture());

        PageRequest pageCaptorValue = argumentCaptorPage.getValue();
        String nameCaptorValue = argumentCaptorName.getValue();
        String emailCaptorValue = argumentCaptorEmail.getValue();
        EnterprisePosition positionCaptorValue = argumentCaptorPosition.getValue();

        assertEquals(pageCaptorValue.getPageNumber(), 0);
        assertEquals(pageCaptorValue.getPageSize(), input.getPerPage());
        assertEquals(nameCaptorValue, input.getName());
        assertEquals(emailCaptorValue, input.getEmail());
        assertEquals(positionCaptorValue, input.getPosition());
    }

    @Test
    void shouldReturnOutputWithoutErrors() {
        ListEmployeesInput input = mountInput();
        input.setPage(1);
        input.setPerPage(10);
        input.setName("test_name");
        input.setEmail("test_email");
        input.setPosition(EnterprisePosition.EMPLOYEE);

        Date mockDate = new Date();

        Employee mockEmployee = mountEmployee();
        mockEmployee.setId(1L);
        mockEmployee.setEmail("email@test.com");
        mockEmployee.setFirstName("first_name_test");
        mockEmployee.setLastName("last_name_test");
        mockEmployee.setDeletedAt(null);
        mockEmployee.setPassword("hashed_password");
        mockEmployee.setProfilePictureUrl(null);
        mockEmployee.setPersonalData(mountPersonalData());
        mockEmployee.setUpdatedAt(mockDate);
        mockEmployee.setCreatedAt(mockDate);
        mockEmployee.setEmployeeManagers(null);
        mockEmployee.setEmployeeSystemRoles(Collections.emptyList());
        mockEmployee.setEmployeePositions(Collections.emptyList());

        Pageable pageable = PageRequest.of(input.getPage() - 1, input.getPerPage());
        Page<Employee> pageEmployee = new PageImpl<>(List.of(mockEmployee), pageable, 1);
        when(employeeDataProvider.listAllEmployees(any(), any(), any(), any())).thenReturn(pageEmployee);

        ListEmployeesOutput output = sut.execute(input);

        List<EmployeeOutput> outputEmployees = output.getEmployees();

        assertEquals(input.getPage(), output.getPage());
        assertEquals(input.getPerPage(), output.getSize());
        assertEquals(outputEmployees.size(), output.getTotalItems());
        assertEquals(1, output.getTotalPages());
        assertEquals(1, outputEmployees.size());
        assertEquals(mockEmployee.getEmail(), outputEmployees.get(0).getEmail());
        assertEquals(mockEmployee.getFirstName(), outputEmployees.get(0).getFirstName());
        assertEquals(mockEmployee.getLastName(), outputEmployees.get(0).getLastName());
        assertEquals(mockEmployee.getId(), outputEmployees.get(0).getId());
    }
}