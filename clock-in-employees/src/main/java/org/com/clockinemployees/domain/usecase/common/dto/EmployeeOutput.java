package org.com.clockinemployees.domain.usecase.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.enums.EnterprisePosition;

import java.util.List;
import java.util.Objects;

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
    private ItineraryOutput itinerary;
    private List<ManagerOutput> managers;
    private PersonalDataOutput personalData;

    public static EmployeeOutput toDto(Employee employee) {
        return EmployeeOutput.builder()
            .id(employee.getId())
            .firstName(employee.getFirstName())
            .lastName(employee.getLastName())
            .email(employee.getEmail())
            .profilePictureUrl(employee.getProfilePictureUrl())
            .personalData(Objects.nonNull(employee.getPersonalData()) ? PersonalDataOutput.toDto(employee.getPersonalData()) : null)
            .enterprisePosition(Objects.nonNull(employee.getEmployeePositions()) ? employee.getEmployeePositions().stream().map(position -> position.getPosition().getName()).toList() : null)
            .itinerary(Objects.nonNull(employee.getItinerary()) ? ItineraryOutput.toDto(employee.getItinerary()) : null)
            .managers(Objects.nonNull(employee.getEmployeeManagers()) ? employee.getEmployeeManagers().stream().map(ManagerOutput::toDto).toList() : null)
            .build();
    }
}
