package org.com.clockin.timeclock.domain.usecase.extraHours.cleanEmployeeExtraHours;

import feign.FeignException;
import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.common.exception.InsufficientPositionsException;
import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.domain.usecase.extraHours.cleanEmployeeExtraHours.dto.CleanEmployeeExtraHoursInput;
import org.com.clockin.timeclock.domain.usecase.extraHours.cleanEmployeeExtraHours.exception.MissingInputPropertyException;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.EmployeeNotFoundException;
import org.com.clockin.timeclock.infra.dataProvider.ExtraHoursDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.external.EmployeeDataProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Service
public class CleanEmployeeExtraHoursUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final ExtraHoursDataProvider extraHoursDataProvider;

    @Transactional
    public void execute(String externalAuthorization, Long employeeId, CleanEmployeeExtraHoursInput input) {
        checkInput(input);

        Employee superior = findEmployeeExternal(externalAuthorization, null);
        Employee employee = findEmployeeExternal(externalAuthorization, employeeId);

        checkSuperiorPermissions(superior, employee);

        removeEmployeeExtraHours(employee, input);
    }

    private void checkInput(CleanEmployeeExtraHoursInput input) {
        if (Objects.isNull(input.getMonth())) {
            throw new MissingInputPropertyException("month");
        }

        if (Objects.isNull(input.getYear())) {
            throw new MissingInputPropertyException("year");
        }

        if (input.getMonth() < 0) {
            input.setMonth(0);
        }
    }

    private Employee findEmployeeExternal(String externalAuth, Long employeeId) {
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
        List<Employee.EnterprisePosition> superiorPositions = superior.getEnterprisePosition();
        boolean superiorIsCeoOrHr = superiorPositions.stream().anyMatch(position -> position.equals(Employee.EnterprisePosition.CEO) || position.equals(Employee.EnterprisePosition.HUMAN_RESOURCES));

        if (superiorIsCeoOrHr) return;

        boolean superiorIsManager = superiorPositions.stream().anyMatch(position -> position.equals(Employee.EnterprisePosition.MANAGER));

        if (!superiorIsManager) {
            throw new InsufficientPositionsException("Only CEO, Human Resources or Managers can handle this resource!");
        }

        checkSuperiorIsEmployeeManager(superior, employee);
    }

    private void checkSuperiorIsEmployeeManager(Employee superior, Employee employee) {
        employee.getManagers().stream().filter(employeeManager -> employeeManager.getId().equals(superior.getId()))
            .findAny().orElseThrow(() -> new InsufficientPositionsException("Only Employee Manager can handle this resource!"));
    }

    private Calendar getMonthYearCalendar(CleanEmployeeExtraHoursInput input) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, input.getMonth() - 1);
        calendar.set(Calendar.YEAR, input.getYear());

        return calendar;
    }

    private void removeEmployeeExtraHours(Employee employee, CleanEmployeeExtraHoursInput input) {
        Calendar calendar = getMonthYearCalendar(input);

        Integer firstDayMonth = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        Integer lastDayMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        calendar.set(Calendar.DAY_OF_MONTH, firstDayMonth);
        Date from = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, lastDayMonth);
        Date to = calendar.getTime();

        extraHoursDataProvider.removeAllByEmployeeId(employee.getId(), from, to);
    }
}
