package org.com.clockinemployees.infra.providers;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.entity.PersonalData;
import org.com.clockinemployees.infra.repository.PersonalDataRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@AllArgsConstructor
@Component
public class PersonalDataDataProvider {
    private final PersonalDataRepository personalDataRepository;

    public PersonalData create(PersonalData personalData) {
        return personalDataRepository.save(personalData);
    }

    public Optional<PersonalData> findByEmployeeId(Long employeeId) {
        return personalDataRepository.findByEmployeeId(employeeId);
    }
}
