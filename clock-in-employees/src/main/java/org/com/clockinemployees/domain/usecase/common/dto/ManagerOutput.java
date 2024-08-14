package org.com.clockinemployees.domain.usecase.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeeManager;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class ManagerOutput {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePictureUrl;

    public static ManagerOutput toDto(EmployeeManager employeeManager) {
        Employee manager = employeeManager.getManager();

        return ManagerOutput.builder()
            .id(manager.getId())
            .firstName(manager.getFirstName())
            .lastName(manager.getLastName())
            .email(manager.getEmail())
            .profilePictureUrl(manager.getProfilePictureUrl())
            .build();
    }
}
