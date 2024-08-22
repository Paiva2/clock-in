package org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.cancelPendingUpdateApprovalUsecase;

import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockin.timeclock.domain.common.exception.PendingUpdateApprovalNotFoundException;
import org.com.clockin.timeclock.domain.entity.PendingUpdateApproval;
import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.cancelPendingUpdateApprovalUsecase.exception.PendingUpdateApprovalAlreadyApprovedException;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.EmployeeNotFoundException;
import org.com.clockin.timeclock.infra.dataProvider.PendingUpdateApprovalDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.external.EmployeeDataProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@Service
@Builder
public class CancelPendingUpdateApprovalUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final PendingUpdateApprovalDataProvider pendingUpdateApprovalDataProvider;

    public void execute(String externalAuthorization, UUID pendingRequestUpdateId) {
        Employee employee = findEmployee(externalAuthorization);
        PendingUpdateApproval pendingUpdateApproval = findPendingUpdate(pendingRequestUpdateId, employee.getId());

        if (Objects.nonNull(pendingUpdateApproval.getApproved())) {
            throw new PendingUpdateApprovalAlreadyApprovedException();
        }

        removeUpdateApproval(pendingUpdateApproval);
    }

    private Employee findEmployee(String externalAuthorization) {
        Employee employee = null;

        try {
            employee = employeeDataProvider.findEmployeeByResourceServerId(externalAuthorization).getBody();
        } catch (FeignException exception) {
            if (exception.status() == HttpStatus.NOT_FOUND.value()) {
                throw new EmployeeNotFoundException("Error while searching for employee. Resource not found!");
            }

            throw new RuntimeException(exception.getMessage(), exception.getCause());
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception.getCause());
        }

        return employee;
    }

    private PendingUpdateApproval findPendingUpdate(UUID pendingRequestUpdateId, Long employeeId) {
        return pendingUpdateApprovalDataProvider.findByIdAndEmployee(pendingRequestUpdateId, employeeId).orElseThrow(PendingUpdateApprovalNotFoundException::new);
    }

    private void removeUpdateApproval(PendingUpdateApproval pendingUpdateApproval) {
        pendingUpdateApprovalDataProvider.removePendingUpdateApproval(pendingUpdateApproval.getId());
    }
}
