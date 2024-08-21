package org.com.clockinemployees.domain.usecase.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.Itinerary;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<EnterprisePosition> enterprisePosition;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ItineraryOutput itinerary;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ManagerOutput> managers;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PersonalDataOutput personalDataOutput;

    public static EmployeeOutput toDto(Employee employee, Itinerary itinerary) {
        return EmployeeOutput.builder()
            .id(employee.getId())
            .firstName(employee.getFirstName())
            .lastName(employee.getLastName())
            .email(employee.getEmail())
            .profilePictureUrl(employee.getProfilePictureUrl())
            .managers(employee.getEmployeeManagers().stream().map(ManagerOutput::toDto).toList())
            .enterprisePosition(employee.getEmployeePositions().stream().map(position -> position.getPosition().getName()).toList())
            .personalDataOutput(PersonalDataOutput.toDto(employee.getPersonalData()))
            .itinerary(Objects.nonNull(itinerary) ? ItineraryOutput.toDto(itinerary) : null)
            .build();
    }

    public static EmployeeOutput toDto(Employee employee) {
        return EmployeeOutput.builder()
            .id(employee.getId())
            .firstName(employee.getFirstName())
            .lastName(employee.getLastName())
            .email(employee.getEmail())
            .profilePictureUrl(employee.getProfilePictureUrl())
            .managers(employee.getEmployeeManagers().stream().map(ManagerOutput::toDto).toList())
            .enterprisePosition(employee.getEmployeePositions().stream().map(position -> position.getPosition().getName()).toList())
            .personalDataOutput(PersonalDataOutput.toDto(employee.getPersonalData()))
            .build();
    }
}
