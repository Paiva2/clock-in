package org.com.clockin.timeclock.domain.usecase.extraHours;

import feign.FeignException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.com.clockin.timeclock.domain.entity.ExtraHours;
import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.domain.strategy.dateFormatValidator.DateTimeFormatStrategy;
import org.com.clockin.timeclock.domain.strategy.dateFormatValidator.strategies.DateFormatRegexValidator;
import org.com.clockin.timeclock.domain.usecase.extraHours.dto.FilterEmployeeExtraHourOutput;
import org.com.clockin.timeclock.domain.usecase.extraHours.dto.FindEmployeeExtraHourInput;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.EmployeeNotFoundException;
import org.com.clockin.timeclock.domain.utils.DateHandler;
import org.com.clockin.timeclock.infra.dataProvider.ExtraHoursDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.external.EmployeeDataProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Objects;

@AllArgsConstructor
@Service
public class FilterEmployeExtraHourUsecase {
    private final DateHandler dateHandler;
    private final EmployeeDataProvider employeeDataProvider;
    private final ExtraHoursDataProvider extraHoursDataProvider;
    private final DateTimeFormatStrategy dateValidatorStrategy = new DateTimeFormatStrategy(new DateFormatRegexValidator());

    public FilterEmployeeExtraHourOutput execute(String externalAuth, FindEmployeeExtraHourInput input) {
        handleInput(input);

        Employee employee = findEmployee(externalAuth);
        Page<ExtraHours> extraHours = findAllEmployeeExtraHours(employee.getId(), input);
        Long findTotalExtraHours = findTotalExtra(employee.getId(), input);

        String convertSecondsToHours = "0";

        if (Objects.nonNull(findTotalExtraHours)) {
            convertSecondsToHours = convertSeconds(findTotalExtraHours);
        }

        return mountOutput(extraHours, convertSecondsToHours, input);
    }

    private void handleInput(FindEmployeeExtraHourInput input) {
        if (input.getPage() < 1) {
            input.setPage(1);
        }

        if (input.getPerPage() > 50) {
            input.setPerPage(50);
        } else if (input.getPerPage() < 5) {
            input.setPerPage(5);
        }

        if (StringUtils.isNotBlank(input.getPeriod())) {
            validatePeriodFormat(input.getPeriod());
        }
    }

    private void validatePeriodFormat(String period) {
        dateValidatorStrategy.execute(period, "period");
    }

    private Employee findEmployee(String externalAuth) {
        Employee employee = null;

        try {
            employee = employeeDataProvider.findEmployeeByResourceServerId(externalAuth).getBody();
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

    private Page<ExtraHours> findAllEmployeeExtraHours(Long employeeId, FindEmployeeExtraHourInput input) {
        Pageable pageable = PageRequest.of(input.getPage() - 1, input.getPerPage(), Sort.Direction.ASC, "EH_CREATED_AT");

        return extraHoursDataProvider.findAllByEmployee(employeeId, input.getPeriod(), pageable);
    }

    private Long findTotalExtra(Long employeeId, FindEmployeeExtraHourInput input) {
        return extraHoursDataProvider.findTotalByEmployee(employeeId, input.getPeriod());
    }

    private String convertSeconds(Long seconds) {
        Long hoursExtra = seconds / 3600;
        Long minutes = (seconds % 3600) / 60;

        return dateHandler.buildHoursFormatString(hoursExtra, minutes);
    }

    private FilterEmployeeExtraHourOutput mountOutput(Page<ExtraHours> extraHours, String totalExtraHours, FindEmployeeExtraHourInput input) {
        return FilterEmployeeExtraHourOutput.builder()
            .page(extraHours.getNumber() + 1)
            .perPage(extraHours.getSize())
            .totalItems(extraHours.getTotalElements())
            .totalPages(extraHours.getTotalPages())
            .extraHours(extraHours.getContent())
            .dayPeriod(Objects.nonNull(input.getPeriod()) ? input.getPeriod() : null)
            .totalExtra(totalExtraHours)
            .build();
    }
}
