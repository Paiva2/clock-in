package org.com.clockin.timeclock.domain.usecase.timeClock.createTimeClockUsecase;

import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.MaxTimeClockedExceptionForDay;
import org.com.clockin.timeclock.infra.dataProvider.TimeClockDataProvider;
import org.com.clockin.timeclock.infra.publishers.dto.PublishNewTimeClockedInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTimeClockUsecaseTest {
    @Mock
    private TimeClockDataProvider timeClockDataProvider;

    private CreateTimeClockUsecase sut;

    @BeforeEach
    void setup() {
        sut = CreateTimeClockUsecase.builder()
            .timeClockDataProvider(timeClockDataProvider)
            .build();
    }

    private Employee mockEmployee() {
        return Employee.builder().build();
    }

    private PublishNewTimeClockedInput mockInput() {
        return PublishNewTimeClockedInput.builder().build();
    }

    @Test
    void shouldCheckForTimeClockedOnDay() {
        Long externaEmployeeId = 1L;
        Date mockDate = new Date();
        PublishNewTimeClockedInput mockInput = mockInput();
        mockInput.setTimeClocked(mockDate);
        mockInput.setEmployeeId(externaEmployeeId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        when(timeClockDataProvider.findTimeClocksCountTodayForEmployee(any())).thenReturn(0);

        sut.execute(mockInput);

        ArgumentCaptor<Long> externalEmployeeIdCaptor = ArgumentCaptor.forClass(Long.class);

        verify(timeClockDataProvider, times(1)).findTimeClocksCountTodayForEmployee(externalEmployeeIdCaptor.capture());

        assertEquals(mockEmployee.getId(), externalEmployeeIdCaptor.getValue());
    }

    @Test
    void shouldThrowExceptionIfUserClockedFourTimesToday() {
        Long externaEmployeeId = 1L;
        Date mockDate = new Date();
        PublishNewTimeClockedInput mockInput = mockInput();
        mockInput.setTimeClocked(mockDate);
        mockInput.setEmployeeId(externaEmployeeId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        when(timeClockDataProvider.findTimeClocksCountTodayForEmployee(any())).thenReturn(4);

        Exception exception = assertThrows(MaxTimeClockedExceptionForDay.class, () -> {
            sut.execute(mockInput);
        });

        assertEquals("Max time clocked exceeded for today. Limit: 4.", exception.getMessage());
    }

    @Test
    void shouldPersistNewTimeClocked() {
        Long externaEmployeeId = 1L;
        Date mockDate = new Date();
        PublishNewTimeClockedInput mockInput = mockInput();
        mockInput.setTimeClocked(mockDate);
        mockInput.setEmployeeId(externaEmployeeId);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        when(timeClockDataProvider.findTimeClocksCountTodayForEmployee(any())).thenReturn(0);

        sut.execute(mockInput);

        ArgumentCaptor<TimeClock> timeClockArgumentCaptor = ArgumentCaptor.forClass(TimeClock.class);

        verify(timeClockDataProvider, times(1)).persistTimeClock(timeClockArgumentCaptor.capture());

        assertEquals(mockEmployee.getId(), timeClockArgumentCaptor.getValue().getExternalEmployeeId());
        assertEquals(mockDate, timeClockArgumentCaptor.getValue().getTimeClocked());
    }

}