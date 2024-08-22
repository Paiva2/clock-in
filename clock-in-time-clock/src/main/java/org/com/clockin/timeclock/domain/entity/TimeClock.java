package org.com.clockin.timeclock.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.List;
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

    @Column(name = "TC_EXTERNAL_EMPLOYEE_ID", nullable = false)
    private Long externalEmployeeId;

    @Column(name = "TC_EVENT", nullable = false)
    @Enumerated(EnumType.STRING)
    private Event event;

    @CreationTimestamp
    @Column(name = "TC_CREATED_AT")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "TC_UPDATED_AT")
    private Date updatedAt;

    @OneToMany(mappedBy = "timeClock", fetch = FetchType.LAZY)
    private List<PendingUpdateApproval> pendingUpdateApprovals;

    public enum Event {
        IN,
        INTERVAL_IN,
        INTERVAL_OUT,
        OUT
    }
}
