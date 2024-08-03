package org.com.clockinemployees.domain.entity.key;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class EmployeeSystemRoleKey implements Serializable {
    private Long employeeId;
    private Long systemRoleId;
}
