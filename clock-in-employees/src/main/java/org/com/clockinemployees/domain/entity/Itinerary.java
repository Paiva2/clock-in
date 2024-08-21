package org.com.clockinemployees.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "TB_ITINERARIES")
public class Itinerary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ITI_ID")
    private Long id;

    @Column(name = "ITI_DAY_WORK_HOURS", nullable = false)
    private String dayWorkHours;

    @Column(name = "ITI_IN_HOUR", nullable = false)
    private String inHour;

    @Column(name = "ITI_INTERVAL_IN_HOUR", nullable = false)
    private String intervalInHour;

    @Column(name = "ITI_INTERVAL_OUT_HOUR", nullable = false)
    private String intervalOutHour;

    @Column(name = "ITI_OUT_HOUR", nullable = false)
    private String outHour;

    @CreationTimestamp
    @Column(name = "ITI_CREATED_AT", nullable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "ITI_UPDATED_AT", nullable = false)
    private Date updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITI_EMPLOYEE_ID", nullable = false)
    private Employee employee;
}
