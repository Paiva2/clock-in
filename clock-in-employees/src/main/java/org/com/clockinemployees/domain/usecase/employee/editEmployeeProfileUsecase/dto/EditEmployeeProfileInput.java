package org.com.clockinemployees.domain.usecase.employee.editEmployeeProfileUsecase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EditEmployeeProfileInput {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String password;

    private String profilePictureUrl;

    private String phone;

    @NotBlank
    private String street;

    @NotBlank
    private String houseNumber;

    private String complement;

    @NotEmpty
    @Pattern(regexp = "^\\d{5}-?\\d{3}$")
    private String zipcode;

    @NotBlank
    private String city;

    @NotBlank
    private String country;

    @NotBlank
    private String state;
}
