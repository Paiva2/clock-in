package org.com.clockinemployees.domain.usecase.employee.editEmployeeProfileUsecase;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.PersonalData;
import org.com.clockinemployees.domain.strategy.passwordValidator.PasswordValidatorStrategy;
import org.com.clockinemployees.domain.strategy.phoneValidation.PhoneValidationStrategy;
import org.com.clockinemployees.domain.usecase.common.dto.EmployeeOutput;
import org.com.clockinemployees.domain.usecase.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.common.exception.FullNameAlreadyUsedException;
import org.com.clockinemployees.domain.usecase.employee.editEmployeeProfileUsecase.dto.EditEmployeeProfileInput;
import org.com.clockinemployees.domain.usecase.employee.editEmployeeProfileUsecase.exception.PersonalDataNotFoundException;
import org.com.clockinemployees.infra.keycloack.employee.EmployeeKeycloakClient;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.com.clockinemployees.infra.providers.PersonalDataDataProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
@Service
@Builder
public class EditEmployeeProfileUsecase {
    private final EmployeeDataProvider employeeDataProvider;
    private final PersonalDataDataProvider personalDataDataProvider;
    private final PasswordValidatorStrategy passwordValidatorStrategy;
    private final PhoneValidationStrategy phoneValidationStrategy;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeKeycloakClient employeeKeycloakClient;

    @Transactional
    public EmployeeOutput execute(String employeeResourceServerId, EditEmployeeProfileInput input) {
        Employee employee = findEmployee(employeeResourceServerId);

        if (Objects.nonNull(employee.getDisabledAt())) {
            throw new EmployeeNotFoundException();
        }

        handleFullName(employee, input);

        PersonalData personalData = findPersonalData(employee.getId());

        String rawPassword = input.getPassword();

        fillEmployee(employee, input);
        Employee updatedEmployee = persistEmployee(employee);

        fillPersonalData(personalData, input);
        PersonalData updatedPersonalData = persistPersonalData(personalData);

        persistEmployeeResourceServer(employee, rawPassword);

        return mountOutput(updatedEmployee, updatedPersonalData);
    }

    private Employee findEmployee(String employeeResourceServerId) {
        return employeeDataProvider.findByResourceServerId(employeeResourceServerId).orElseThrow(EmployeeNotFoundException::new);
    }

    private PersonalData findPersonalData(Long employeeId) {
        return personalDataDataProvider.findByEmployeeId(employeeId).orElseThrow(PersonalDataNotFoundException::new);
    }

    private void handleFullName(Employee employee, EditEmployeeProfileInput input) {
        Optional<Employee> findEmployeeWithFullName = findEmployeeWithFullName(input.getFirstName(), input.getLastName());

        if (findEmployeeWithFullName.isEmpty()) return;

        if (!findEmployeeWithFullName.get().getId().equals(employee.getId())) {
            throw new FullNameAlreadyUsedException();
        }
    }

    private Optional<Employee> findEmployeeWithFullName(String firstName, String lastName) {
        return employeeDataProvider.findByFullName(firstName, lastName);
    }

    private void handlePasswordStrength(String password) {
        passwordValidatorStrategy.validate(password);
    }

    private void handlePhoneValidation(String phone) {
        phoneValidationStrategy.validate(phone);
    }

    private String hashNewPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    private void fillEmployee(Employee employee, EditEmployeeProfileInput input) {
        employee.setFirstName(input.getFirstName());
        employee.setLastName(input.getLastName());
        employee.setProfilePictureUrl(input.getProfilePictureUrl());

        if (Objects.nonNull(input.getPassword())) {
            handlePasswordStrength(input.getPassword());

            String hashedPassword = hashNewPassword(input.getPassword());
            employee.setPassword(hashedPassword);
        }
    }

    private Employee persistEmployee(Employee employee) {
        return employeeDataProvider.save(employee);
    }

    private void persistEmployeeResourceServer(Employee employee, String rawPassword) {
        employeeKeycloakClient.updateEmployee(employee, rawPassword);
    }

    private void fillPersonalData(PersonalData personalData, EditEmployeeProfileInput input) {
        personalData.setCity(input.getCity());
        personalData.setComplement(input.getComplement());
        personalData.setCountry(input.getCountry());
        personalData.setState(input.getState());
        personalData.setStreet(input.getStreet());
        personalData.setHouseNumber(input.getHouseNumber());
        personalData.setZipcode(input.getZipcode());

        if (Objects.nonNull(input.getPhone())) {
            handlePhoneValidation(input.getPhone());

            personalData.setPhone(input.getPhone());
        }
    }

    private PersonalData persistPersonalData(PersonalData personalData) {
        return personalDataDataProvider.create(personalData);
    }

    private EmployeeOutput mountOutput(Employee employee, PersonalData personalData) {
        employee.setPersonalData(personalData);
        return EmployeeOutput.toDto(employee);
    }
}

