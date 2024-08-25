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
@Table(name = "TB_EXTRA_HOURS")
public class ExtraHours {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "EH_ID")
    private UUID id;

    @Column(name = "EH_EXTRA_HOURS", nullable = false)
    private String extraHours;

    @Column(name = "EH_DAY_PERIOD", nullable = false)
    private String dayPeriod;

    @Column(name = "EH_EXTERNAL_EMPLOYEE_ID", nullable = false)
    private Long externalEmployeeId;

    @CreationTimestamp
    @Column(name = "EH_CREATED_AT")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "EH_UPDATED_AT")
    private Date updatedAt;
}
