package org.com.clockin.timeclock.infra.dataProvider;

import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.entity.PendingUpdateApproval;
import org.com.clockin.timeclock.infra.repository.PendingUpdateApprovalRepository;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PendingUpdateApprovalDataProvider {
    private final PendingUpdateApprovalRepository pendingUpdateApprovalRepository;

    public PendingUpdateApproval persist(PendingUpdateApproval pendingUpdateApproval) {
        return pendingUpdateApprovalRepository.save(pendingUpdateApproval);
    }
}
