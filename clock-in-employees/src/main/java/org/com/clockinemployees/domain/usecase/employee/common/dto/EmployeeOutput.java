package org.com.clockinemployees.domain.usecase.employee.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.enums.EnterprisePosition;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
public class EmployeeOutput {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePictureUrl;
    private List<EnterprisePosition> enterprisePosition;
    private PersonalDataOutput personalDataOutput;

    public static EmployeeOutput toDto(Employee employee) {
        return EmployeeOutput.builder()
            .id(employee.getId())
            .firstName(employee.getFirstName())
            .lastName(employee.getLastName())
            .email(employee.getEmail())
            .profilePictureUrl(employee.getProfilePictureUrl())
            .enterprisePosition(employee.getEmployeePositions().stream().map(position -> position.getPosition().getName()).toList())
            .personalDataOutput(PersonalDataOutput.toDto(employee.getPersonalData()))
            .build();
    }
}
