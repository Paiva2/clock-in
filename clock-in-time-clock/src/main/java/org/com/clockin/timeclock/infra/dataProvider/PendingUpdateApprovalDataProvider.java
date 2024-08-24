package org.com.clockin.timeclock.infra.dataProvider;

import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.entity.PendingUpdateApproval;
import org.com.clockin.timeclock.infra.repository.PendingUpdateApprovalRepository;
import org.springframework.stereotype.Component;

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

    public void removePendingUpdateApproval(UUID updateApprovalId) {
        pendingUpdateApprovalRepository.deleteById(updateApprovalId);
    }

    public void removeAllPendingUpdateApprovalsByTimeClockId(UUID timeClockId) {
        pendingUpdateApprovalRepository.deleteAllByTimeClockId(timeClockId);
    }
}
