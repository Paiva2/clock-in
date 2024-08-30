package org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.approvePendingApprovalUsecase;

import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.common.exception.InsufficientPositionsException;
import org.com.clockin.timeclock.domain.common.exception.PendingUpdateApprovalNotFoundException;
import org.com.clockin.timeclock.domain.entity.ExtraHours;
import org.com.clockin.timeclock.domain.entity.PendingUpdateApproval;
import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.approvePendingApprovalUsecase.exception.PendingUpdateApprovalAlreadyResolvedException;
import org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.approvePendingApprovalUsecase.exception.TimeClockValidityException;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.EmployeeNotFoundException;
import org.com.clockin.timeclock.domain.utils.DateHandler;
import org.com.clockin.timeclock.infra.dataProvider.ExtraHoursDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.PendingUpdateApprovalDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.TimeClockDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.external.EmployeeDataProvider;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.util.*;

import static org.com.clockin.timeclock.domain.entity.TimeClock.Event.*;
import static org.com.clockin.timeclock.domain.entity.TimeClock.Event.INTERVAL_OUT;

@AllArgsConstructor
@Service
public class ApprovePendingApprovalUsecase {
    private final static String DAY_PERIOD_FORMAT = "dd-MM-yyyy";

    private final EmployeeDataProvider employeeDataProvider;
    private final PendingUpdateApprovalDataProvider pendingUpdateApprovalDataProvider;
    private final TimeClockDataProvider timeClockDataProvider;
    private final ExtraHoursDataProvider extraHoursDataProvider;
    private final DateHandler dateHandler;

    @Transactional
    public void execute(String externalAuth, UUID pendingApprovalId) {
        Employee superior = findExternalEmployee(externalAuth, null);

        PendingUpdateApproval pendingUpdateApproval = findPendingUpdateApproval(pendingApprovalId);

        if (Objects.nonNull(pendingUpdateApproval.getApproved())) {
            throw new PendingUpdateApprovalAlreadyResolvedException(pendingApprovalId);
        }

        TimeClock timeClock = pendingUpdateApproval.getTimeClock();

        Employee employee = findExternalEmployee(externalAuth, timeClock.getExternalEmployeeId());

        checkSuperiorPermission(superior, employee);

        checkPendingApprovalIsFromSamePeriod(timeClock, pendingUpdateApproval);

        updateTimeClockWithPendingApproval(timeClock, pendingUpdateApproval);
        updatePendingApproval(pendingUpdateApproval);

        String timeClockPeriod = null;

        try {
            timeClockPeriod = extractDayPeriod(timeClock.getTimeClocked());
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        handleExtraHours(employee, timeClock.getTimeClocked(), timeClockPeriod);
    }

    private void checkPendingApprovalIsFromSamePeriod(TimeClock timeClock, PendingUpdateApproval pendingUpdateApproval) {
        String timeClockDay = DateHandler.extractDayNumberFromDate(timeClock.getTimeClocked());
        String timeClockMonth = DateHandler.extractMonthFromDate(timeClock.getTimeClocked());
        String timeClockYear = DateHandler.extractYearFromDate(timeClock.getTimeClocked());

        String pendingApprovalDay = DateHandler.extractDayNumberFromDate(pendingUpdateApproval.getTimeClockUpdated());
        String pendingApprovalMonth = DateHandler.extractMonthFromDate(pendingUpdateApproval.getTimeClockUpdated());
        String pendingApprovalYear = DateHandler.extractYearFromDate(pendingUpdateApproval.getTimeClockUpdated());

        String currentMonth = DateHandler.extractMonthFromDate(new Date());
        String currentYear = DateHandler.extractMonthFromDate(new Date());

        boolean pendingAndTimeClockedSameDay = timeClockDay.equals(pendingApprovalDay);
        boolean pendingAndTimeClockedSameMonth = timeClockMonth.equals(pendingApprovalMonth);
        boolean pendingAndTimeClockedSameYear = timeClockYear.equals(pendingApprovalYear);

        boolean timeClockSameMonthAsCurrent = currentMonth.equals(timeClockMonth);
        boolean timeClockSameYearAsCurrent = currentYear.equals(timeClockYear);

        if (!timeClockSameMonthAsCurrent || !timeClockSameYearAsCurrent) {
            throw new TimeClockValidityException("Only time clock's from current month and year can be updated!");
        }

        if (!pendingAndTimeClockedSameDay || !pendingAndTimeClockedSameMonth || !pendingAndTimeClockedSameYear) {
            throw new TimeClockValidityException("Pending update approval must be at the same period (day, month, year) as Time clocked!");
        }
    }

    private void updateTimeClockWithPendingApproval(TimeClock timeClock, PendingUpdateApproval pendingUpdateApproval) {
        timeClock.setTimeClocked(pendingUpdateApproval.getTimeClockUpdated());
        timeClockDataProvider.persistTimeClock(timeClock);
    }

    private void updatePendingApproval(PendingUpdateApproval pendingUpdateApproval) {
        pendingUpdateApproval.setApproved(true);
        pendingUpdateApprovalDataProvider.persist(pendingUpdateApproval);
    }

    private String extractDayPeriod(Date timeClocked) throws ParseException {
        return dateHandler.formatDate(timeClocked, DAY_PERIOD_FORMAT);
    }

    private void handleExtraHours(Employee employee, Date timeClockedOnDate, String timeClockPeriod) {
        List<TimeClock> timeClockedsList = timeClockDataProvider.findTimeClocksOnDayForEmployee(employee.getId(), timeClockedOnDate);

        if (Objects.isNull(timeClockedsList) || timeClockedsList.isEmpty()) return;

        Optional<TimeClock> inClocked = findTimeClockEventOnList(timeClockedsList, IN);
        if (inClocked.isEmpty()) return;

        Optional<TimeClock> outClocked = findTimeClockEventOnList(timeClockedsList, OUT);
        if (outClocked.isEmpty()) return;

        Optional<TimeClock> intervalInClocked = findTimeClockEventOnList(timeClockedsList, INTERVAL_IN);

        Long secondsOfHoursWorked = 0L;

        if (intervalInClocked.isPresent()) {
            Optional<TimeClock> intervalOutClocked = findTimeClockEventOnList(timeClockedsList, INTERVAL_OUT);
            if (intervalOutClocked.isEmpty()) return;

            Duration diffBetweenInAndIntervalIn = Duration.between(inClocked.get().getTimeClocked().toInstant(), intervalInClocked.get().getTimeClocked().toInstant());
            secondsOfHoursWorked += diffBetweenInAndIntervalIn.toSeconds();

            Duration diffBetweenIntervalOutAndOut = Duration.between(intervalOutClocked.get().getTimeClocked().toInstant(), outClocked.get().getTimeClocked().toInstant());
            secondsOfHoursWorked += diffBetweenIntervalOutAndOut.toSeconds();
        } else {
            Duration diffBetweenInAndOut = Duration.between(inClocked.get().getTimeClocked().toInstant(), outClocked.get().getTimeClocked().toInstant());
            secondsOfHoursWorked += diffBetweenInAndOut.toSeconds();
        }

        Duration employeeTotalItinerary = getEmployeeWorkDuration(employee.getItinerary().getDayWorkHours());

        Optional<ExtraHours> timeClockHasExtraHours = extraHoursDataProvider.findByDayPeriod(timeClockPeriod, employee.getId());

        if (secondsOfHoursWorked > employeeTotalItinerary.toSeconds()) {
            Long extraWorkedOnSeconds = secondsOfHoursWorked - employeeTotalItinerary.toSeconds();
            Long hoursExtra = extraWorkedOnSeconds / 3600;
            Long minutes = (extraWorkedOnSeconds % 3600) / 60;

            if (timeClockHasExtraHours.isPresent()) {
                updateExtraHours(timeClockHasExtraHours.get(), dateHandler.buildHoursFormatString(hoursExtra, minutes));
            } else {
                createExtraHours(timeClockPeriod, employee.getId(), dateHandler.buildHoursFormatString(hoursExtra, minutes));
            }
        } else if (timeClockHasExtraHours.isPresent()) {
            deleteExtraHours(timeClockHasExtraHours.get().getId());
        }
    }

    private Optional<TimeClock> findTimeClockEventOnList(List<TimeClock> timeClockedsList, TimeClock.Event necessaryEvent) {
        return timeClockedsList.stream().filter(timeClock -> timeClock.getEvent().equals(necessaryEvent)).findAny();
    }

    private Duration getEmployeeWorkDuration(String dayWorkHours) {
        String durationHoursChar = "PT" + dayWorkHours.replace(":", "H") + "M";
        return Duration.parse(durationHoursChar);
    }

    private void createExtraHours(String period, Long employeeId, String extraHoursString) {
        ExtraHours extraHours = ExtraHours.builder()
            .extraHours(extraHoursString)
            .dayPeriod(period)
            .externalEmployeeId(employeeId)
            .build();

        extraHoursDataProvider.persist(extraHours);
    }

    private void updateExtraHours(ExtraHours extraHours, String extraHoursString) {
        extraHours.setExtraHours(extraHoursString);
        extraHoursDataProvider.persist(extraHours);
    }

    private void deleteExtraHours(UUID extraHoursId) {
        extraHoursDataProvider.removeById(extraHoursId);
    }

    private Employee findExternalEmployee(String externalAuth, Long employeeId) {
        Employee employee = null;

        try {
            if (Objects.isNull(employeeId)) {
                employee = employeeDataProvider.findEmployeeByResourceServerId(externalAuth).getBody();
            } else {
                employee = employeeDataProvider.findEmployeeByBasicId(externalAuth, employeeId).getBody();
            }
        } catch (FeignException exception) {
            if (exception.status() == 404) {
                throw new EmployeeNotFoundException("Error while searching Employee. Resource not found!");
            }

            throw new RuntimeException(exception.getMessage(), exception);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return employee;
    }

    private void checkSuperiorPermission(Employee superior, Employee employee) {
        Boolean isCeo = superiorHasProvidedPermission(Employee.EnterprisePosition.CEO, superior);
        Boolean isHr = superiorHasProvidedPermission(Employee.EnterprisePosition.HUMAN_RESOURCES, superior);
        Boolean isManager = superiorHasProvidedPermission(Employee.EnterprisePosition.MANAGER, superior);

        if (isCeo || isHr) return;

        if (!isManager) {
            throw new InsufficientPositionsException("Only CEO, Human Resource or Managers can approve pending approvals!");
        }

        checkSuperiorIsEmployeeManager(superior, employee);
    }

    private Boolean superiorHasProvidedPermission(Employee.EnterprisePosition position, Employee employee) {
        return employee.getEnterprisePositions().contains(position);
    }

    private void checkSuperiorIsEmployeeManager(Employee superior, Employee employee) {
        Long superiorId = superior.getId();
        boolean employeeHasSuperiorAsManager = employee.getManagers().stream().anyMatch(employeeManager -> employeeManager.getId().equals(superiorId));

        if (!employeeHasSuperiorAsManager) {
            throw new InsufficientPositionsException("Superior provided not found as employee manager!");
        }
    }

    private PendingUpdateApproval findPendingUpdateApproval(UUID pendingApprovalId) {
        return pendingUpdateApprovalDataProvider.findById(pendingApprovalId).orElseThrow(PendingUpdateApprovalNotFoundException::new);
    }
}
