package org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegisterEmployeeInput {
    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    @Email
    private String email;
    
    private Long positionId;

    @NotEmpty
    private String password;

    private String phone;

    private Long managerId;

    @NotEmpty
    private String street;

    @NotEmpty
    private String houseNumber;

    private String complement;

    @NotEmpty
    @Pattern(regexp = "^\\d{5}-?\\d{3}$")
    private String zipcode;

    @NotEmpty
    private String city;

    @NotEmpty
    private String country;

    @NotEmpty
    private String state;
}
