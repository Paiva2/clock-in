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
@Table(name = "TB_EMPLOYEE_IDS")
public class EmployeeId {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "EI_ID")
    private UUID id;

    @Column(name = "EI_EXTERNAL_ID", nullable = false)
    private Long externalId;

    @CreationTimestamp
    @Column(name = "EI_CREATED_AT")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "EI_UPDATED_AT")
    private Date updatedAt;
}
