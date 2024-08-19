package org.com.clockin.timeclock.domain.usecase.timeClock.updateTimeClockUsecase;

import org.com.clockin.timeclock.domain.common.exception.TimeClockNotFoundException;
import org.com.clockin.timeclock.domain.entity.PendingUpdateApproval;
import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.domain.strategy.dateFormatValidator.exception.InvalidHourTimeFormatException;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.EmployeeNotFoundException;
import org.com.clockin.timeclock.domain.usecase.timeClock.updateTimeClockUsecase.dto.RequestUpdateTimeClockOutput;
import org.com.clockin.timeclock.domain.usecase.timeClock.updateTimeClockUsecase.dto.UpdateTimeClockInput;
import org.com.clockin.timeclock.infra.dataProvider.PendingUpdateApprovalDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.TimeClockDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.external.EmployeeDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestUpdateTimeClockUsecaseTest {
    @Mock
    private EmployeeDataProvider employeeDataProvider;

    @Mock
    private TimeClockDataProvider timeClockDataProvider;

    @Mock
    private PendingUpdateApprovalDataProvider pendingUpdateApprovalDataProvider;

    private RequestUpdateTimeClockUsecase sut;

    @BeforeEach
    void setUp() {
        sut = RequestUpdateTimeClockUsecase.builder()
            .employeeDataProvider(employeeDataProvider)
            .timeClockDataProvider(timeClockDataProvider)
            .pendingUpdateApprovalDataProvider(pendingUpdateApprovalDataProvider)
            .build();
    }

    private Employee mockEmployee() {
        return Employee.builder().build();
    }

    private TimeClock mockTimeClock() {
        return TimeClock.builder().build();
    }

    private PendingUpdateApproval mockPendingUpdateApproval() {
        return PendingUpdateApproval.builder().build();
    }

    @Test
    void shouldThrowExceptionIfTimeFormatIsInvalid() {
        String mockExternalAuth = "any_token";
        UUID mockTimeClockId = UUID.randomUUID();
        UpdateTimeClockInput mockInput = UpdateTimeClockInput.builder().updatedTimeClocked("invalid_hour").reason("test").build();

        Exception exception = assertThrows(InvalidHourTimeFormatException.class, () -> {
            sut.execute(mockExternalAuth, mockTimeClockId, mockInput);
        });

        assertEquals("Invalid updated time clock hour format. Valid format ex: 00:00:00", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfExternalEmployeeNotFound() {
        String mockExternalAuth = "any_token";
        UUID mockTimeClockId = UUID.randomUUID();
        UpdateTimeClockInput mockInput = UpdateTimeClockInput.builder().updatedTimeClocked("23:59:00").reason("test").build();

        when(employeeDataProvider.findEmployeeByResourceServerId(any())).thenThrow(new EmployeeNotFoundException("Error while searching for Employee, resource not found!"));

        Exception exception = assertThrows(EmployeeNotFoundException.class, () -> {
            sut.execute(mockExternalAuth, mockTimeClockId, mockInput);
        });

        assertEquals("Error while searching for Employee, resource not found!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfTimeClockeNotFound() {
        String mockExternalAuth = "any_token";
        UUID mockTimeClockId = UUID.randomUUID();
        UpdateTimeClockInput mockInput = UpdateTimeClockInput.builder().updatedTimeClocked("23:59:00").reason("test").build();

        Employee mockEmployee = mockEmployee();

        when(employeeDataProvider.findEmployeeByResourceServerId(any())).thenReturn(new ResponseEntity<>(mockEmployee, HttpStatus.OK));
        when(timeClockDataProvider.findByIdAndEmployeeId(any(), any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(TimeClockNotFoundException.class, () -> {
            sut.execute(mockExternalAuth, mockTimeClockId, mockInput);
        });

        assertEquals("Time Clock not found!", exception.getMessage());
    }

    @Test
    void shouldFillAndPersistUpdateApprovalWithHourUpdatedCorrectly() {
        String mockExternalAuth = "any_token";
        UUID mockTimeClockId = UUID.randomUUID();
        UpdateTimeClockInput mockInput = UpdateTimeClockInput.builder().updatedTimeClocked("23:59:00").reason("test").build();

        Date mockDate = new Date(2024, Calendar.JANUARY, 30, 23, 50, 0);

        String[] hourMinuteSecond = mockInput.getUpdatedTimeClocked().split(":");

        Calendar timeClockCalendarMock = Calendar.getInstance();
        timeClockCalendarMock.setTime(mockDate);
        timeClockCalendarMock.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourMinuteSecond[0]));
        timeClockCalendarMock.set(Calendar.MINUTE, Integer.parseInt(hourMinuteSecond[1]));
        timeClockCalendarMock.set(Calendar.SECOND, Integer.parseInt(hourMinuteSecond[2]));

        Employee mockEmployee = mockEmployee();
        TimeClock mockTimeClock = mockTimeClock();
        mockTimeClock.setId(mockTimeClockId);
        mockTimeClock.setTimeClocked(mockDate);

        PendingUpdateApproval mockPendingUpdateApproval = mockPendingUpdateApproval();
        mockPendingUpdateApproval.setTimeClock(mockTimeClock);
        mockPendingUpdateApproval.setTimeClockUpdated(timeClockCalendarMock.getTime());

        when(employeeDataProvider.findEmployeeByResourceServerId(any())).thenReturn(new ResponseEntity<>(mockEmployee, HttpStatus.OK));
        when(timeClockDataProvider.findByIdAndEmployeeId(any(), any())).thenReturn(Optional.of(mockTimeClock));
        when(pendingUpdateApprovalDataProvider.persist(any())).thenReturn(mockPendingUpdateApproval);

        sut.execute(mockExternalAuth, mockTimeClockId, mockInput);

        ArgumentCaptor<PendingUpdateApproval> captorPendingApproval = ArgumentCaptor.forClass(PendingUpdateApproval.class);

        verify(pendingUpdateApprovalDataProvider, times(1)).persist(captorPendingApproval.capture());

        PendingUpdateApproval pendingUpdateApprovalValue = captorPendingApproval.getValue();

        assertEquals(timeClockCalendarMock.getTime(), pendingUpdateApprovalValue.getTimeClockUpdated());
        assertEquals(mockTimeClock.getId(), pendingUpdateApprovalValue.getTimeClock().getId());
        assertEquals(mockInput.getReason(), pendingUpdateApprovalValue.getReason());
        assertFalse(pendingUpdateApprovalValue.getApproved());
    }

    @Test
    void shouldReturnUsecaseOutputWithoutErrors() {
        String mockExternalAuth = "any_token";
        UUID mockTimeClockId = UUID.randomUUID();
        UpdateTimeClockInput mockInput = UpdateTimeClockInput.builder().updatedTimeClocked("23:59:00").reason("test").build();

        Date mockDate = new Date(2024, Calendar.JANUARY, 30, 23, 50, 0);

        String[] hourMinuteSecond = mockInput.getUpdatedTimeClocked().split(":");

        Calendar timeClockCalendarMock = Calendar.getInstance();
        timeClockCalendarMock.setTime(mockDate);
        timeClockCalendarMock.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourMinuteSecond[0]));
        timeClockCalendarMock.set(Calendar.MINUTE, Integer.parseInt(hourMinuteSecond[1]));
        timeClockCalendarMock.set(Calendar.SECOND, Integer.parseInt(hourMinuteSecond[2]));

        Employee mockEmployee = mockEmployee();
        TimeClock mockTimeClock = mockTimeClock();
        mockTimeClock.setId(mockTimeClockId);
        mockTimeClock.setTimeClocked(mockDate);

        PendingUpdateApproval mockPendingUpdateApproval = mockPendingUpdateApproval();
        mockPendingUpdateApproval.setTimeClock(mockTimeClock);
        mockPendingUpdateApproval.setTimeClockUpdated(timeClockCalendarMock.getTime());

        when(employeeDataProvider.findEmployeeByResourceServerId(any())).thenReturn(new ResponseEntity<>(mockEmployee, HttpStatus.OK));
        when(timeClockDataProvider.findByIdAndEmployeeId(any(), any())).thenReturn(Optional.of(mockTimeClock));
        when(pendingUpdateApprovalDataProvider.persist(any())).thenReturn(mockPendingUpdateApproval);

        RequestUpdateTimeClockOutput output = sut.execute(mockExternalAuth, mockTimeClockId, mockInput);

        assertEquals(mockTimeClock.getTimeClocked(), output.getOldTimeClocked());
        assertEquals(mockTimeClock.getId(), output.getTimeclockId());
        assertEquals(mockPendingUpdateApproval.getTimeClockUpdated(), output.getNewRequestedTimeUpdated());
    }
}