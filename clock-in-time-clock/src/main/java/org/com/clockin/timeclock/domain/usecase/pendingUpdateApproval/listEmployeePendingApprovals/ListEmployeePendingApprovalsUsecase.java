package org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.listEmployeePendingApprovals;

import feign.FeignException;
import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.common.exception.InsufficientPositionsException;
import org.com.clockin.timeclock.domain.entity.PendingUpdateApproval;
import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.listEmployeePendingApprovals.dto.ListEmployeePendingApprovalOutput;
import org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.listEmployeePendingApprovals.dto.ListEmployeePendingApprovalsInput;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.EmployeeNotFoundException;
import org.com.clockin.timeclock.infra.dataProvider.PendingUpdateApprovalDataProvider;
import org.com.clockin.timeclock.infra.dataProvider.external.EmployeeDataProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Service
public class ListEmployeePendingApprovalsUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final PendingUpdateApprovalDataProvider pendingUpdateApprovalDataProvider;

    public ListEmployeePendingApprovalOutput execute(String externalAuth, Long employeeId, ListEmployeePendingApprovalsInput input) {
        Employee superior = findEmployee(externalAuth, null);
        Employee employee = findEmployee(externalAuth, employeeId);

        handleInputDefault(input);
        checkSuperiorPermissions(superior, employee);

        Page<PendingUpdateApproval> pendingUpdateApprovals = findEmployeePendingApprovals(employee.getId(), input);

        return mountOutput(pendingUpdateApprovals);
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
            if (exception.status() == 400) {
                throw new EmployeeNotFoundException("Error while fetching employee. Resource not found!");
            }

            throw new RuntimeException(exception.getMessage(), exception);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }

        return employee;
    }

    private void handleInputDefault(ListEmployeePendingApprovalsInput input) {
        if (input.getPage() < 1) {
            input.setPage(1);
        }

        if (input.getSize() < 5) {
            input.setSize(5);
        } else if (input.getSize() > 50) {
            input.setSize(50);
        }
    }

    private void checkSuperiorPermissions(Employee superior, Employee employee) {
        boolean superiorIsCeoOrHr = superiorIsCeoOrHumanResources(superior.getEnterprisePosition());
        boolean superiorIsManager = superiorIsManager(superior.getEnterprisePosition());

        if (superiorIsCeoOrHr) return;

        if (superiorIsManager) {
            checkSuperiorFromEmployee(superior, employee);
        } else {
            throw new InsufficientPositionsException("Only Managers can list pending approvals from an employee!");
        }
    }

    private void checkSuperiorFromEmployee(Employee superior, Employee employee) {
        employee.getManagers().stream().filter(employeeManager -> employeeManager.getId().equals(superior.getId()))
            .findAny().orElseThrow(() -> new InsufficientPositionsException("Only the Employee Manager can list Employee pending approvals!"));
    }

    private Page<PendingUpdateApproval> findEmployeePendingApprovals(Long employeeId, ListEmployeePendingApprovalsInput input) {
        Pageable pageable = PageRequest.of(input.getPage() - 1, input.getSize());
        return pendingUpdateApprovalDataProvider.findEmployeePendingApprovals(employeeId, pageable);
    }

    private boolean superiorIsCeoOrHumanResources(List<Employee.EnterprisePosition> positions) {
        return positions.stream().anyMatch(position -> position.equals(Employee.EnterprisePosition.CEO) || position.equals(Employee.EnterprisePosition.HUMAN_RESOURCES));
    }

    private boolean superiorIsManager(List<Employee.EnterprisePosition> positions) {
        return positions.stream().anyMatch(position -> position.equals(Employee.EnterprisePosition.MANAGER));
    }

    private ListEmployeePendingApprovalOutput mountOutput(Page<PendingUpdateApproval> pendingUpdateApprovals) {
        return ListEmployeePendingApprovalOutput.builder()
            .page(pendingUpdateApprovals.getNumber() + 1)
            .size(pendingUpdateApprovals.getSize())
            .totalElements(pendingUpdateApprovals.getTotalElements())
            .totalPages(pendingUpdateApprovals.getTotalPages())
            .pendingApprovals(pendingUpdateApprovals.getContent().stream().map(ListEmployeePendingApprovalOutput.EmployeePendingApprovalOutput::toDto).toList())
            .build();
    }
}
