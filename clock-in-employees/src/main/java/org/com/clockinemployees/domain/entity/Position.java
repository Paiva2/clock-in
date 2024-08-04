package org.com.clockinemployees.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "TB_POSITIONS")
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PS_ID")
    private Long id;

    @Column(name = "PS_NAME", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private EnterprisePosition name;

    @CreationTimestamp
    @Column(name = "PS_CREATED_AT", nullable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "PS_UPDATED_AT", nullable = false)
    private Date updatedAt;

    @OneToMany(mappedBy = "position")
    List<EmployeePosition> employeePositions;
}
