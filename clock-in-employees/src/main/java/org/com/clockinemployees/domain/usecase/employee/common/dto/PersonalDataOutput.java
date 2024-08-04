package org.com.clockinemployees.domain.usecase.employee.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.com.clockinemployees.domain.entity.PersonalData;

@AllArgsConstructor
@Data
@Builder
public class PersonalDataOutput {
    private Long id;
    private String phone;
    private String street;
    private String houseNumber;
    private String complement;
    private String zipcode;
    private String city;
    private String country;
    private String state;

    public static PersonalDataOutput toDto(PersonalData personalData) {
        return PersonalDataOutput.builder()
            .id(personalData.getId())
            .phone(personalData.getPhone())
            .street(personalData.getStreet())
            .houseNumber(personalData.getHouseNumber())
            .complement(personalData.getComplement())
            .zipcode(personalData.getZipcode())
            .city(personalData.getCity())
            .country(personalData.getCountry())
            .state(personalData.getState())
            .build();
    }
}
