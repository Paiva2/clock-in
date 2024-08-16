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
@Table(name = "TB_TIME_CLOCKS")
public class TimeClock {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "TC_ID")
    private UUID id;

    @Column(name = "TC_TIME_CLOCKED", nullable = false)
    private Date timeClocked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TC_EMPLOYEE_ID", nullable = false)
    private EmployeeId employeeId;

    @CreationTimestamp
    @Column(name = "TC_CREATED_AT_ID")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "TC_UPDATED_AT_ID")
    private Date updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    private PendingUpdateApproval pendingUpdateApproval;
}
