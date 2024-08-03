package org.com.clockinemployees.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.com.clockinemployees.domain.enums.Role;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "TB_SYSTEM_ROLES")
public class SystemRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SR_ID")
    private Long id;

    @Column(name = "SR_ROLE", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private Role role;

    @CreationTimestamp
    @Column(name = "SR_CREATED_AT", nullable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "SR_UPDATED_AT", nullable = false)
    private Date updatedAt;

    @OneToMany(mappedBy = "systemRole")
    private List<EmployeeSystemRole> employeeSystemRoles;
}
