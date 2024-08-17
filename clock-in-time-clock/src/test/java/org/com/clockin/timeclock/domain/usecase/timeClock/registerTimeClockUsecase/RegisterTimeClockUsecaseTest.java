package org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase;

import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.dto.RegisterTimeClockOutput;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.EmployeeNotFoundException;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.MaxTimeClockedExceptionForDay;
import org.com.clockin.timeclock.infra.dataProvider.TimeClockDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.external.EmployeeDataProvider;
import org.com.clockin.timeclock.infra.publishers.TimeClockPublisher;
import org.com.clockin.timeclock.infra.publishers.dto.PublishNewTimeClockedInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class RegisterTimeClockUsecaseTest {
    @Mock
    private EmployeeDataProvider employeeDataProvider;

    @Mock
    private TimeClockDataProvider timeClockDataProvider;

    @Mock
    private TimeClockPublisher timeClockPublisher;

    private RegisterTimeClockUsecase sut;

    @BeforeEach
    void setup() {
        sut = RegisterTimeClockUsecase.builder()
            .employeeDataProvider(employeeDataProvider)
            .timeClockPublisher(timeClockPublisher)
            .timeClockDataProvider(timeClockDataProvider)
            .build();
    }

    private Employee mockEmployee() {
        return Employee.builder().build();
    }

    @Test
    void shouldThrowExceptionIfExternalEmployeeNotFound() {
        String externalId = "any_id";

        when(employeeDataProvider.findEmployeeByResourceServerId(any())).thenThrow(new EmployeeNotFoundException("Error while searching for Employee, resource not found!"));

        Exception exception = assertThrows(EmployeeNotFoundException.class, () -> {
            sut.execute(externalId);
        });

        assertEquals("Error while searching for Employee, resource not found!", exception.getMessage());
    }

    @Test
    void shouldCheckForTimeClockedOnDay() {
        String externalId = "any_id";

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        when(timeClockDataProvider.findTimeClocksCountTodayForEmployee(any())).thenReturn(0);
        when(employeeDataProvider.findEmployeeByResourceServerId(any())).thenReturn(new ResponseEntity<>(mockEmployee, HttpStatus.OK));

        sut.execute(externalId);

        ArgumentCaptor<Long> externalEmployeeIdCaptor = ArgumentCaptor.forClass(Long.class);

        verify(timeClockDataProvider, times(1)).findTimeClocksCountTodayForEmployee(externalEmployeeIdCaptor.capture());

        assertEquals(mockEmployee.getId(), externalEmployeeIdCaptor.getValue());
    }

    @Test
    void shouldThrowExceptionIfUserClockedFourTimesToday() {
        String externalId = "any_id";

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        when(timeClockDataProvider.findTimeClocksCountTodayForEmployee(any())).thenReturn(4);
        when(employeeDataProvider.findEmployeeByResourceServerId(any())).thenReturn(new ResponseEntity<>(mockEmployee, HttpStatus.OK));

        Exception exception = assertThrows(MaxTimeClockedExceptionForDay.class, () -> {
            sut.execute(externalId);
        });

        assertEquals("Max time clocked exceeded for today. Limit: 4.", exception.getMessage());
    }

    @Test
    void shouldPublishNewTimeClockedOnQueuePublisher() {
        String externalId = "any_id";

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        when(timeClockDataProvider.findTimeClocksCountTodayForEmployee(any())).thenReturn(0);
        when(employeeDataProvider.findEmployeeByResourceServerId(any())).thenReturn(new ResponseEntity<>(mockEmployee, HttpStatus.OK));

        sut.execute(externalId);

        ArgumentCaptor<PublishNewTimeClockedInput> externalEmployeeIdCaptor = ArgumentCaptor.forClass(PublishNewTimeClockedInput.class);

        verify(timeClockPublisher, times(1)).publishNewTimeClocked(externalEmployeeIdCaptor.capture());

        assertEquals(mockEmployee.getId(), externalEmployeeIdCaptor.getValue().getEmployeeId());
        assertNotNull(externalEmployeeIdCaptor.getValue().getTimeClocked());
    }

    @Test
    void shouldReturnUsecaseOutputWithoutErrors() {
        String externalId = "any_id";

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        when(timeClockDataProvider.findTimeClocksCountTodayForEmployee(any())).thenReturn(0);
        when(employeeDataProvider.findEmployeeByResourceServerId(any())).thenReturn(new ResponseEntity<>(mockEmployee, HttpStatus.OK));

        RegisterTimeClockOutput output = sut.execute(externalId);

        assertEquals(output.getEmployeeId(), mockEmployee.getId());
        assertTrue((output.getTimeClocked().getTime() - new Date().getTime()) < 1000);
    }
}