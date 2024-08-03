package org.com.clockinemployees.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.com.clockinemployees.domain.entity.key.EmployeeManagerKey;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Data
@Table(name = "TB_EMPLOYEES_MANAGERS")
@Entity
public class EmployeeManager {
    @EmbeddedId
    private EmployeeManagerKey employeeManagerKey = new EmployeeManagerKey();

    @ManyToOne
    @MapsId("managerId")
    @JoinColumn(name = "EM_MANAGER_ID")
    private Employee manager;

    @ManyToOne
    @MapsId("employeeId")
    @JoinColumn(name = "EM_EMPLOYEE_ID")
    private Employee employee;

    @CreationTimestamp
    @Column(name = "EM_CREATED_AT")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "EM_UPDATED_AT")
    private Date updatedAt;

    public EmployeeManager(Employee manager, Employee employee) {
        this.manager = manager;
        this.employee = employee;
    }

    public EmployeeManager() {

    }
}
