package org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.denyPendingUpdateApprovalUsecase;

import feign.FeignException;
import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.common.exception.InsufficientPositionsException;
import org.com.clockin.timeclock.domain.common.exception.PendingUpdateApprovalNotFoundException;
import org.com.clockin.timeclock.domain.entity.PendingUpdateApproval;
import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.approvePendingApprovalUsecase.exception.PendingUpdateApprovalAlreadyResolvedException;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.EmployeeNotFoundException;
import org.com.clockin.timeclock.infra.dataProvider.PendingUpdateApprovalDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.external.EmployeeDataProvider;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DenyPendingUpdateApprovalUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final PendingUpdateApprovalDataProvider pendingUpdateApprovalDataProvider;

    public void execute(String externalAuth, UUID pendingUpdateApprovalId) {
        Employee superior = findExternalEmployee(externalAuth, null);
        checkSuperiorPermissions(superior);

        PendingUpdateApproval pendingUpdateApproval = findPendingUpdate(pendingUpdateApprovalId);

        if (Objects.nonNull(pendingUpdateApproval.getApproved())) {
            throw new PendingUpdateApprovalAlreadyResolvedException(pendingUpdateApproval.getId());
        }

        TimeClock timeClock = pendingUpdateApproval.getTimeClock();

        Employee employee = findExternalEmployee(externalAuth, timeClock.getExternalEmployeeId());

        checkEmployeeSuperior(superior, employee);

        updatePendingApproval(pendingUpdateApproval);
    }

    private Employee findExternalEmployee(String externalAuth, Long employeeId) {
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

    private PendingUpdateApproval findPendingUpdate(UUID pendingUpdateApprovalId) {
        return pendingUpdateApprovalDataProvider.findById(pendingUpdateApprovalId).orElseThrow(PendingUpdateApprovalNotFoundException::new);
    }

    private void checkSuperiorPermissions(Employee superior) {
        boolean isHr = findPositionProvided(superior, Employee.EnterprisePosition.HUMAN_RESOURCES);
        boolean isCeo = findPositionProvided(superior, Employee.EnterprisePosition.CEO);

        if (isHr || isCeo) return;

        boolean isManager = findPositionProvided(superior, Employee.EnterprisePosition.MANAGER);

        if (!isManager) {
            throw new InsufficientPositionsException("Only CEO, Human Resources or Managers are allowed to handle approvals!");
        }
    }

    private boolean findPositionProvided(Employee superior, Employee.EnterprisePosition position) {
        return superior.getEnterprisePosition().contains(position);
    }

    private void checkEmployeeSuperior(Employee superior, Employee employee) {
        boolean isEmployeeSuperior = employee.getManagers().stream().anyMatch(employeeManager -> employeeManager.getId().equals(superior.getId()));

        if (!isEmployeeSuperior) {
            throw new InsufficientPositionsException("Only Employee Manager are allowed to handle his approvals!");
        }
    }

    private void updatePendingApproval(PendingUpdateApproval pendingUpdateApproval) {
        pendingUpdateApproval.setApproved(false);
        pendingUpdateApprovalDataProvider.persist(pendingUpdateApproval);
    }
}
