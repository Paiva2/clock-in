package org.com.clockinemployees.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.com.clockinemployees.domain.entity.key.EmployeePositionKey;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "TB_EMPLOYE_POSITION")
public class EmployeePosition {
    @EmbeddedId
    private EmployeePositionKey employeePositionKey = new EmployeePositionKey();

    @ManyToOne
    @MapsId("employeeId")
    @JoinColumn(name = "EP_EMPLOYEE_ID")
    private Employee employee;

    @ManyToOne
    @MapsId("positionId")
    @JoinColumn(name = "EP_POSITION_ID")
    private Position position;

    @CreationTimestamp
    @Column(name = "EP_CREATED_AT")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "EP_UPDATED_AT")
    private Date updatedAt;

    public EmployeePosition(Employee employee, Position position) {
        this.employee = employee;
        this.position = position;
    }

    public EmployeePosition() {

    }
}
