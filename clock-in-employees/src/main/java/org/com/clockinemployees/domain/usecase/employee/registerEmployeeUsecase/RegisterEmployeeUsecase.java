package org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockinemployees.domain.entity.*;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.enums.Role;
import org.com.clockinemployees.domain.strategy.passwordValidator.PasswordValidatorStrategy;
import org.com.clockinemployees.domain.strategy.phoneValidation.PhoneValidationStrategy;
import org.com.clockinemployees.domain.usecase.common.exception.FullNameAlreadyUsedException;
import org.com.clockinemployees.domain.usecase.common.exception.PositionNotFoundException;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.dto.RegisterEmployeeInput;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.dto.RegisterEmployeeOutput;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.*;
import org.com.clockinemployees.infra.keycloack.employee.EmployeeKeycloakClient;
import org.com.clockinemployees.infra.providers.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
@Builder
public class RegisterEmployeeUsecase {
    private final PasswordValidatorStrategy passwordValidatorStrategy;
    private final PhoneValidationStrategy phoneValidationStrategy;
    private final PasswordEncoder passwordEncoder;

    private final EmployeeDataProvider employeeDataProvider;
    private final SystemRoleDataProvider systemRoleDataProvider;
    private final EmployeeSystemRoleDataProvider employeeSystemRoleDataProvider;
    private final PersonalDataDataProvider personalDataDataProvider;
    private final PositionDataProvider positionDataProvider;
    private final EmployeePositionDataProvider employeePositionDataProvider;
    private final EmployeeKeycloakClient employeeKeycloakClient;

    @Transactional
    public RegisterEmployeeOutput execute(String superiorResourceServerId, RegisterEmployeeInput registerEmployeeInput) {
        checkSuperiorPermissions(superiorResourceServerId);

        passwordValidatorStrategy.validate(registerEmployeeInput.getPassword());

        if (Objects.nonNull(registerEmployeeInput.getPhone())) {
            phoneValidationStrategy.validate(registerEmployeeInput.getPhone());
        }

        validateEmailUsed(registerEmployeeInput.getEmail());
        validateFirstNameAndLastNameUsed(registerEmployeeInput);

        Employee fillEmployee = fillEmployee(registerEmployeeInput);
        Position position = findPosition(registerEmployeeInput.getPositionId());

        String userKcId = createEmployeeKeycloack(registerEmployeeInput, registerEmployeeInput.getPassword(), position.getName());
        fillEmployee.setKeycloakId(userKcId);

        Employee employeeCreated = persistNewEmployee(fillEmployee);

        setEmployeeRole(employeeCreated);
        setEmployeePosition(employeeCreated, position);

        handlePersonalData(employeeCreated, registerEmployeeInput);

        return mountOutput(employeeCreated);
    }

    private void checkSuperiorPermissions(String superiorResourceServerId) {
        Employee superior = findSuperior(superiorResourceServerId);

        Set<EmployeePosition> superiorPosition = employeePositionDataProvider.findAllByEmployeeId(superior.getId());

        superiorPosition.stream().filter(sp ->
                sp.getPosition().getName().equals(EnterprisePosition.HUMAN_RESOURCES) ||
                    sp.getPosition().getName().equals(EnterprisePosition.CEO))
            .findAny().orElseThrow(InsufficientPermissionsToRegisterException::new);
    }

    private Employee findSuperior(String resourceServerId) {
        return employeeDataProvider.findByResourceServerId(resourceServerId).orElseThrow(SuperiorNotFoundException::new);
    }

    private void validateEmailUsed(String email) {
        Optional<Employee> employee = employeeDataProvider.findByEmail(email);

        if (employee.isPresent()) {
            throw new EmailAlreadyUsedException();
        }
    }

    private void validateFirstNameAndLastNameUsed(RegisterEmployeeInput employee) {
        Optional<Employee> employeeFound = employeeDataProvider.findByFullName(employee.getFirstName(), employee.getLastName());

        if (employeeFound.isPresent()) {
            throw new FullNameAlreadyUsedException();
        }
    }

    private Employee fillEmployee(RegisterEmployeeInput employeeInput) {
        String rawPassword = employeeInput.getPassword();
        String hashedPassword = hashPassword(rawPassword);

        return Employee.builder()
            .firstName(employeeInput.getFirstName())
            .lastName(employeeInput.getLastName())
            .email(employeeInput.getEmail())
            .password(hashedPassword)
            .build();
    }

    private String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    private String createEmployeeKeycloack(RegisterEmployeeInput employeeInput, String rawPassword, EnterprisePosition enterprisePosition) {
        return employeeKeycloakClient.registerUser(employeeInput, rawPassword, enterprisePosition);
    }

    private void setEmployeeRole(Employee employee) {
        SystemRole userSystemRole = findRole(Role.USER);
        EmployeeSystemRole employeeSystemRole = new EmployeeSystemRole(employee, userSystemRole);

        employeeSystemRoleDataProvider.create(employeeSystemRole);
    }

    private void setEmployeePosition(Employee employee, Position position) {
        EmployeePosition employeePosition = new EmployeePosition(employee, position);

        persistEmployeePosition(employeePosition);
    }

    private Position findPosition(Long positionId) {
        return positionDataProvider.findPositionById(positionId).orElseThrow(PositionNotFoundException::new);
    }

    private SystemRole findRole(Role role) {
        return systemRoleDataProvider.findByRole(role).orElseThrow(RoleNotFoundException::new);
    }

    private Employee persistNewEmployee(Employee newEmployee) {
        return employeeDataProvider.save(newEmployee);
    }

    private void persistEmployeePosition(EmployeePosition employeePosition) {
        employeePositionDataProvider.create(employeePosition);
    }

    private void handlePersonalData(Employee employee, RegisterEmployeeInput registerEmployeeInput) {
        PersonalData personalData = fillPersonalData(registerEmployeeInput);
        personalData.setEmployee(employee);

        persistNewPersonalData(personalData);
    }

    private PersonalData fillPersonalData(RegisterEmployeeInput registerEmployeeInput) {
        return PersonalData.builder()
            .phone(Objects.nonNull(registerEmployeeInput.getPhone()) ? registerEmployeeInput.getPhone() : null)
            .street(registerEmployeeInput.getStreet())
            .houseNumber(registerEmployeeInput.getHouseNumber())
            .complement(Objects.nonNull(registerEmployeeInput.getComplement()) ? registerEmployeeInput.getComplement() : null)
            .zipcode(registerEmployeeInput.getZipcode())
            .city(registerEmployeeInput.getCity())
            .country(registerEmployeeInput.getCountry())
            .state(registerEmployeeInput.getState())
            .build();
    }

    private void persistNewPersonalData(PersonalData personalData) {
        personalDataDataProvider.create(personalData);
    }

    private RegisterEmployeeOutput mountOutput(Employee employee) {
        return RegisterEmployeeOutput.builder()
            .employeeId(employee.getId())
            .employeeEmail(employee.getEmail())
            .createdAt(employee.getCreatedAt())
            .build();
    }
}
