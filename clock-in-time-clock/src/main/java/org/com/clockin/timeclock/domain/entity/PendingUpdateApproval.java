package org.com.clockin.timeclock.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "TB_PENDING_UPDATE_APPROVALS")
public class PendingUpdateApproval {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "PUA_ID")
    private UUID id;

    @OneToOne(mappedBy = "pendingUpdateApproval")
    @JoinColumn(name = "PUA_TIME_CLOCK", nullable = false)
    private TimeClock timeClock;

    @Column(name = "PUA_TIME_CLOCK_UPDATED", nullable = false)
    private Date timeClockUpdated;

    @Column(name = "PUA_APPROVED", nullable = false)
    private Boolean approved;

    @CreationTimestamp
    @Column(name = "PUA_CREATED_AT")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "PUA_UPDATED_AT")
    private Date updatedAt;
}
