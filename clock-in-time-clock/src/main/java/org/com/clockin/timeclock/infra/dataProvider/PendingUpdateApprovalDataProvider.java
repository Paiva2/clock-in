package org.com.clockin.timeclock.infra.dataProvider;

import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.entity.PendingUpdateApproval;
import org.com.clockin.timeclock.infra.repository.PendingUpdateApprovalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class PendingUpdateApprovalDataProvider {
    private final PendingUpdateApprovalRepository pendingUpdateApprovalRepository;

    public PendingUpdateApproval persist(PendingUpdateApproval pendingUpdateApproval) {
        return pendingUpdateApprovalRepository.save(pendingUpdateApproval);
    }

    public Optional<PendingUpdateApproval> findByIdAndEmployee(UUID pendingUpdateId, Long externalEmployeeId) {
        return pendingUpdateApprovalRepository.findByIdAndEmployeeId(pendingUpdateId, externalEmployeeId);
    }

    public Optional<PendingUpdateApproval> findById(UUID pendingUpdateId) {
        return pendingUpdateApprovalRepository.findByIdWithDeps(pendingUpdateId);
    }

    public Page<PendingUpdateApproval> findEmployeePendingApprovals(Long employeeId, Pageable pageable) {
        return pendingUpdateApprovalRepository.findByEmployeeId(employeeId, pageable);
    }

    public void removePendingUpdateApproval(UUID updateApprovalId) {
        pendingUpdateApprovalRepository.deleteById(updateApprovalId);
    }

    public void removeAllPendingUpdateApprovalsByTimeClockId(UUID timeClockId) {
        pendingUpdateApprovalRepository.deleteAllByTimeClockId(timeClockId);
    }

    public List<PendingUpdateApproval> findAllByTimeClock(UUID timeClockId) {
        return pendingUpdateApprovalRepository.findAllByTimeClockId(timeClockId);
    }
}
