package org.com.clockin.timeclock.domain.usecase.timeClock.filterEmployeeTimeClockUsecase;

import feign.FeignException;
import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.common.exception.InsufficientPositionsException;
import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.domain.usecase.timeClock.filterEmployeeTimeClockUsecase.dto.FilterEmployeeTimeClockInput;
import org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.dto.TimeClockListDTO;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.EmployeeNotFoundException;
import org.com.clockin.timeclock.domain.utils.AggregateTimeClockList;
import org.com.clockin.timeclock.infra.dataProvider.TimeClockDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.external.EmployeeDataProvider;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.*;

@AllArgsConstructor
@Service
public class FilterEmployeeTimeClockUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final TimeClockDataProvider timeClockDataProvider;
    private final AggregateTimeClockList aggregateTimeClockList;

    public LinkedHashMap<String, TimeClockListDTO> execute(String externalAuth, Long employeeId, FilterEmployeeTimeClockInput input) {
        Employee superior = findEmployee(externalAuth, null);
        Employee employee = findEmployee(externalAuth, employeeId);

        checkSuperiorPermissions(superior, employee);

        if (input.getMonth() < 0) {
            input.setMonth(1);
        }

        Page<TimeClock> getTimeClocks = findEmployeeTimeClocks(employeeId, input);

        LinkedHashMap<String, TimeClockListDTO> timeClockedsAggregated = aggregateTimeClockList.execute(getTimeClocks.getContent(), employee);

        return timeClockedsAggregated;
    }

    private Employee findEmployee(String externalAuth, Long employeeId) {
        Employee employee;

        try {
            if (Objects.isNull(employeeId)) {
                employee = employeeDataProvider.findEmployeeByResourceServerId(externalAuth).getBody();
            } else {
                employee = employeeDataProvider.findEmployeeByBasicId(externalAuth, employeeId).getBody();
            }
        } catch (FeignException exception) {
            if (exception.status() == 404) {
                throw new EmployeeNotFoundException("Error while fetching employee. Resource not found!");
            }

            throw new RuntimeException(exception.getMessage(), exception);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }

        return employee;
    }

    private void checkSuperiorPermissions(Employee superior, Employee employee) {
        boolean isCeoOrHr = isCeoOrHr(superior.getEnterprisePosition());

        if (isCeoOrHr) return;

        boolean isManager = isManager(superior.getEnterprisePosition());

        if (!isManager) {
            throw new InsufficientPositionsException("Only CEO, Human Resources or Manager can filter for Employee time clocked!");
        }

        checkSuperiorEmployee(superior, employee);
    }

    private Calendar getDateFromMonthYear(Integer month, Integer year, boolean isFirstDay) {
        Calendar calendarFromTo = Calendar.getInstance();
        calendarFromTo.set(Calendar.MONTH, month - 1);
        calendarFromTo.set(Calendar.YEAR, year);

        if (isFirstDay) {
            int firstDay = calendarFromTo.getActualMinimum(Calendar.DAY_OF_MONTH);
            calendarFromTo.set(Calendar.DAY_OF_MONTH, firstDay);
        } else {
            int lastDay = calendarFromTo.getActualMaximum(Calendar.DAY_OF_MONTH);
            calendarFromTo.set(Calendar.DAY_OF_MONTH, lastDay);
        }

        return calendarFromTo;
    }

    private boolean isCeoOrHr(List<Employee.EnterprisePosition> positions) {
        return positions.stream().anyMatch(position -> position.equals(Employee.EnterprisePosition.CEO) || position.equals(Employee.EnterprisePosition.HUMAN_RESOURCES));
    }

    private boolean isManager(List<Employee.EnterprisePosition> positions) {
        return positions.stream().anyMatch(position -> position.equals(Employee.EnterprisePosition.MANAGER));
    }

    private void checkSuperiorEmployee(Employee superior, Employee employee) {
        boolean isSuperiorFromEmployee = employee.getManagers().stream().anyMatch(employeeManager -> employeeManager.getId().equals(superior.getId()));

        if (!isSuperiorFromEmployee) {
            throw new InsufficientPositionsException("Only the Employee Manager can filter for Employee time clocked!");
        }
    }

    private Page<TimeClock> findEmployeeTimeClocks(Long employeeId, FilterEmployeeTimeClockInput input) {
        Calendar calendarFrom = getDateFromMonthYear(input.getMonth(), input.getYear(), true);
        Date from = calendarFrom.getTime();

        Calendar calendarTo = getDateFromMonthYear(input.getMonth(), input.getYear(), false);
        Date to = calendarTo.getTime();

        return timeClockDataProvider.listAllByEmployee(employeeId, from, to);
    }
}
