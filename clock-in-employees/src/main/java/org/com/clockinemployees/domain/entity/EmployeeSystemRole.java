package org.com.clockinemployees.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.com.clockinemployees.domain.entity.key.EmployeeSystemRoleKey;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "TB_EMPLOYEE_SYSTEM_ROLES")
public class EmployeeSystemRole {
    @EmbeddedId
    private EmployeeSystemRoleKey employeeSystemRoleKey = new EmployeeSystemRoleKey();

    @ManyToOne
    @MapsId("employeeId")
    @JoinColumn(name = "ER_EMPLOYEE_ID")
    private Employee employee;

    @ManyToOne
    @MapsId("systemRoleId")
    @JoinColumn(name = "ER_SYSTEM_ROLE_ID")
    private SystemRole systemRole;

    @CreationTimestamp
    @Column(name = "ER_CREATED_AT")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "ER_UPDATED_AT")
    private Date updatedAt;

    public EmployeeSystemRole(Employee employee, SystemRole systemRole) {
        this.employee = employee;
        this.systemRole = systemRole;
    }

    public EmployeeSystemRole() {

    }
}
