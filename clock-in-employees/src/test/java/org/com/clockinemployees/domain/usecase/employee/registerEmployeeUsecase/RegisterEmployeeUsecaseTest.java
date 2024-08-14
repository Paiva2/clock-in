package org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterEmployeeUsecaseTest {
    @Mock
    private PasswordValidatorStrategy passwordValidatorStrategy;

    @Mock
    private PhoneValidationStrategy phoneValidationStrategy;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmployeeDataProvider employeeDataProvider;

    @Mock
    private SystemRoleDataProvider systemRoleDataProvider;

    @Mock
    private EmployeeSystemRoleDataProvider employeeSystemRoleDataProvider;

    @Mock
    private PersonalDataDataProvider personalDataDataProvider;

    @Mock
    private PositionDataProvider positionDataProvider;

    @Mock
    private EmployeePositionDataProvider employeePositionDataProvider;

    @Mock
    private EmployeeKeycloakClient employeeKeycloakClient;

    private RegisterEmployeeUsecase sut;

    @BeforeEach
    void setup() {
        this.sut = RegisterEmployeeUsecase.builder()
            .passwordValidatorStrategy(passwordValidatorStrategy)
            .phoneValidationStrategy(phoneValidationStrategy)
            .passwordEncoder(passwordEncoder)
            .employeeDataProvider(employeeDataProvider)
            .systemRoleDataProvider(systemRoleDataProvider)
            .employeeSystemRoleDataProvider(employeeSystemRoleDataProvider)
            .personalDataDataProvider(personalDataDataProvider)
            .positionDataProvider(positionDataProvider)
            .employeePositionDataProvider(employeePositionDataProvider)
            .employeeKeycloakClient(employeeKeycloakClient)
            .build();
    }

    private SystemRole mockRole() {
        return SystemRole.builder().build();
    }

    private Position mockPosition() {
        return Position.builder().build();
    }

    private Employee mockEmployee() {
        return Employee.builder().build();
    }

    private Employee mockSuperior() {
        return Employee.builder().build();
    }

    private EmployeePosition mockEmployeePosition() {
        return new EmployeePosition();
    }

    private RegisterEmployeeInput mockSutInput() {
        return RegisterEmployeeInput.builder()
            .email("test-email")
            .city("test-city")
            .complement("test-complement")
            .country("test-country")
            .firstName("test-first-name")
            .houseNumber("test-house-number")
            .state("test-state")
            .lastName("test-last-name")
            .phone("test-phone")
            .positionId(1L)
            .password("test-password")
            .street("test-street")
            .zipcode("test-zipcode")
            .build();
    }

    @Test
    void shouldThrowExceptionIfSuperiorNotFound() {
        String mockResourceServerSuperiorId = UUID.randomUUID().toString();
        RegisterEmployeeInput input = mockSutInput();
        input.setPassword("invalid-password");

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(1L);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockSuperiorEmployeePosition = mockEmployeePosition();
        mockSuperiorEmployeePosition.setEmployee(mockSuperior);
        mockSuperiorEmployeePosition.setPosition(mockPositionSuperior);

        Exception exception = assertThrows(SuperiorNotFoundException.class, () -> {
            sut.execute(mockResourceServerSuperiorId, input);
        });

        assertEquals("Superior not found!", exception.getMessage());
    }

    @Test
    void shouldThrowErrorIfSuperiorHasNoPermissionToRegisterEmployees() {
        String mockResourceServerSuperiorId = UUID.randomUUID().toString();
        RegisterEmployeeInput input = mockSutInput();
        input.setPassword("invalid-password");

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(1L);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.EMPLOYEE);

        EmployeePosition mockSuperiorEmployeePosition = mockEmployeePosition();
        mockSuperiorEmployeePosition.setEmployee(mockSuperior);
        mockSuperiorEmployeePosition.setPosition(mockPositionSuperior);

        when(employeeDataProvider.findByResourceServerId(mockResourceServerSuperiorId)).thenReturn(Optional.of(mockSuperior));
        when(employeePositionDataProvider.findAllByEmployeeId(mockSuperior.getId())).thenReturn(Set.of(mockSuperiorEmployeePosition));

        Exception exception = assertThrows(InsufficientPermissionsToRegisterException.class, () -> {
            sut.execute(mockResourceServerSuperiorId, input);
        });

        assertEquals("Only CEO's or Human Resource members can create employees!", exception.getMessage());
    }

    @Test
    void shouldThrowErrorIfPasswordHasNotStrengthEnough() {
        String mockResourceServerSuperiorId = UUID.randomUUID().toString();
        RegisterEmployeeInput input = mockSutInput();
        input.setPassword("invalid-password");

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(1L);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockSuperiorEmployeePosition = mockEmployeePosition();
        mockSuperiorEmployeePosition.setEmployee(mockSuperior);
        mockSuperiorEmployeePosition.setPosition(mockPositionSuperior);

        doThrow(new WeakPasswordException()).when(passwordValidatorStrategy).validate(Mockito.anyString());
        when(employeeDataProvider.findByResourceServerId(mockResourceServerSuperiorId)).thenReturn(Optional.of(mockSuperior));
        when(employeePositionDataProvider.findAllByEmployeeId(mockSuperior.getId())).thenReturn(Set.of(mockSuperiorEmployeePosition));

        Exception exception = assertThrows(WeakPasswordException.class, () -> {
            sut.execute(mockResourceServerSuperiorId, input);
        });

        assertEquals("Weak password. Password must have at least 6 characters, one upper letter, and a special character.", exception.getMessage());
    }

    @Test
    void shouldThrowErrorIfPhoneIsInvalid() {
        String mockResourceServerSuperiorId = UUID.randomUUID().toString();
        RegisterEmployeeInput input = mockSutInput();
        input.setPhone("invalid-phone");

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(1L);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockSuperiorEmployeePosition = mockEmployeePosition();
        mockSuperiorEmployeePosition.setEmployee(mockSuperior);
        mockSuperiorEmployeePosition.setPosition(mockPositionSuperior);

        doThrow(new InvalidPhoneException()).when(phoneValidationStrategy).validate(Mockito.anyString());
        when(employeeDataProvider.findByResourceServerId(mockResourceServerSuperiorId)).thenReturn(Optional.of(mockSuperior));
        when(employeePositionDataProvider.findAllByEmployeeId(mockSuperior.getId())).thenReturn(Set.of(mockSuperiorEmployeePosition));

        Exception exception = assertThrows(InvalidPhoneException.class, () -> {
            sut.execute(mockResourceServerSuperiorId, input);
        });

        assertEquals("Invalid phone format.", exception.getMessage());
    }

    @Test
    void shouldThrowErrorIfEmailAlreadyBeingUsed() {
        String mockResourceServerSuperiorId = UUID.randomUUID().toString();
        RegisterEmployeeInput input = mockSutInput();
        input.setEmail("already-used-email@email.com");

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(1L);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockSuperiorEmployeePosition = mockEmployeePosition();
        mockSuperiorEmployeePosition.setEmployee(mockSuperior);
        mockSuperiorEmployeePosition.setPosition(mockPositionSuperior);

        when(employeeDataProvider.findByEmail(Mockito.anyString())).thenReturn(Optional.of(Employee.builder().build()));
        when(employeeDataProvider.findByResourceServerId(mockResourceServerSuperiorId)).thenReturn(Optional.of(mockSuperior));
        when(employeePositionDataProvider.findAllByEmployeeId(mockSuperior.getId())).thenReturn(Set.of(mockSuperiorEmployeePosition));

        Exception exception = assertThrows(EmailAlreadyUsedException.class, () -> {
            sut.execute(mockResourceServerSuperiorId, input);
        });

        assertEquals("E-mail already being used!", exception.getMessage());
    }

    @Test
    void shouldThrowErrorIfFullNameAlreadyBeingUsed() {
        String mockResourceServerSuperiorId = UUID.randomUUID().toString();
        RegisterEmployeeInput input = mockSutInput();
        input.setFirstName("test-name");
        input.setLastName("test-lastName");

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(1L);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockSuperiorEmployeePosition = mockEmployeePosition();
        mockSuperiorEmployeePosition.setEmployee(mockSuperior);
        mockSuperiorEmployeePosition.setPosition(mockPositionSuperior);

        when(employeeDataProvider.findByFullName(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(Employee.builder().build()));
        when(employeeDataProvider.findByResourceServerId(mockResourceServerSuperiorId)).thenReturn(Optional.of(mockSuperior));
        when(employeePositionDataProvider.findAllByEmployeeId(mockSuperior.getId())).thenReturn(Set.of(mockSuperiorEmployeePosition));

        Exception exception = assertThrows(FullNameAlreadyUsedException.class, () -> {
            sut.execute(mockResourceServerSuperiorId, input);
        });

        assertEquals("Provided first name and last name is already being used!", exception.getMessage());
    }

    @Test
    void shouldHashNewPassword() {
        String password = "123456";
        String mockResourceServerSuperiorId = UUID.randomUUID().toString();
        RegisterEmployeeInput input = mockSutInput();
        input.setPassword(password);

        String mockEmployeeResourceServerId = UUID.randomUUID().toString();
        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(1L);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockSuperiorEmployeePosition = mockEmployeePosition();
        mockSuperiorEmployeePosition.setEmployee(mockSuperior);
        mockSuperiorEmployeePosition.setPosition(mockPositionSuperior);

        when(systemRoleDataProvider.findByRole(Mockito.any())).thenReturn(Optional.of(mockRole()));
        when(positionDataProvider.findPositionById(Mockito.any())).thenReturn(Optional.of(mockPosition()));
        when(employeeDataProvider.save(Mockito.any())).thenReturn(mockEmployee());
        when(employeeKeycloakClient.registerUser(Mockito.any(), Mockito.any())).thenReturn(mockEmployeeResourceServerId);
        when(employeeDataProvider.findByResourceServerId(mockResourceServerSuperiorId)).thenReturn(Optional.of(mockSuperior));
        when(employeePositionDataProvider.findAllByEmployeeId(mockSuperior.getId())).thenReturn(Set.of(mockSuperiorEmployeePosition));

        when(passwordEncoder.encode(Mockito.any())).thenReturn("hashed-password");

        sut.execute(mockResourceServerSuperiorId, input);

        ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);

        verify(employeeDataProvider, times(1)).save(employeeCaptor.capture());
        Employee employeeSaved = employeeCaptor.getValue();

        assertEquals(employeeSaved.getPassword(), "hashed-password");
    }

    @Test
    void shouldCallAndSaveNewEmployeeWithCorrectlyProvidedParams() {
        String mockResourceServerSuperiorId = UUID.randomUUID().toString();
        RegisterEmployeeInput input = mockSutInput();

        String mockEmployeeResourceServerId = UUID.randomUUID().toString();
        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(1L);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockSuperiorEmployeePosition = mockEmployeePosition();
        mockSuperiorEmployeePosition.setEmployee(mockSuperior);
        mockSuperiorEmployeePosition.setPosition(mockPositionSuperior);

        when(systemRoleDataProvider.findByRole(Mockito.any())).thenReturn(Optional.of(mockRole()));
        when(positionDataProvider.findPositionById(Mockito.any())).thenReturn(Optional.of(mockPosition()));
        when(employeeDataProvider.save(Mockito.any())).thenReturn(mockEmployee());
        when(passwordEncoder.encode(Mockito.anyString())).thenReturn("hashed-password");
        when(employeeKeycloakClient.registerUser(Mockito.any(), Mockito.any())).thenReturn(mockEmployeeResourceServerId);
        when(employeeDataProvider.findByResourceServerId(mockResourceServerSuperiorId)).thenReturn(Optional.of(mockSuperior));
        when(employeePositionDataProvider.findAllByEmployeeId(mockSuperior.getId())).thenReturn(Set.of(mockSuperiorEmployeePosition));

        sut.execute(mockResourceServerSuperiorId, input);

        ArgumentCaptor<Employee> argumentCaptorEmployee = ArgumentCaptor.forClass(Employee.class);

        verify(employeeDataProvider, times(1)).save(argumentCaptorEmployee.capture());

        Employee employeeSaved = argumentCaptorEmployee.getValue();

        assertEquals("hashed-password", employeeSaved.getPassword());
        assertEquals(input.getEmail(), employeeSaved.getEmail());
        assertEquals(input.getFirstName(), employeeSaved.getFirstName());
        assertEquals(input.getLastName(), employeeSaved.getLastName());
    }

    @Test
    void shouldThrowErrorIfRoleNotFound() {
        String mockResourceServerSuperiorId = UUID.randomUUID().toString();
        RegisterEmployeeInput input = mockSutInput();

        String mockEmployeeResourceServerId = UUID.randomUUID().toString();

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(1L);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockSuperiorEmployeePosition = mockEmployeePosition();
        mockSuperiorEmployeePosition.setEmployee(mockSuperior);
        mockSuperiorEmployeePosition.setPosition(mockPositionSuperior);

        when(systemRoleDataProvider.findByRole(Mockito.any())).thenReturn(Optional.empty());
        when(employeeKeycloakClient.registerUser(Mockito.any(), Mockito.any())).thenReturn(mockEmployeeResourceServerId);
        when(employeeDataProvider.findByResourceServerId(mockResourceServerSuperiorId)).thenReturn(Optional.of(mockSuperior));
        when(employeePositionDataProvider.findAllByEmployeeId(mockSuperior.getId())).thenReturn(Set.of(mockSuperiorEmployeePosition));

        Exception exception = assertThrows(RoleNotFoundException.class, () -> {
            sut.execute(mockResourceServerSuperiorId, input);
        });

        assertEquals("Role not found.", exception.getMessage());
    }

    @Test
    void shouldThrowErrorIfPositionNotFound() {
        String mockResourceServerSuperiorId = UUID.randomUUID().toString();
        RegisterEmployeeInput input = mockSutInput();

        String mockEmployeeResourceServerId = UUID.randomUUID().toString();

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(1L);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockSuperiorEmployeePosition = mockEmployeePosition();
        mockSuperiorEmployeePosition.setEmployee(mockSuperior);
        mockSuperiorEmployeePosition.setPosition(mockPositionSuperior);

        when(systemRoleDataProvider.findByRole(Mockito.any())).thenReturn(Optional.of(mockRole()));
        when(positionDataProvider.findPositionById(Mockito.any())).thenReturn(Optional.empty());
        when(employeeKeycloakClient.registerUser(Mockito.any(), Mockito.any())).thenReturn(mockEmployeeResourceServerId);
        when(employeeDataProvider.findByResourceServerId(mockResourceServerSuperiorId)).thenReturn(Optional.of(mockSuperior));
        when(employeePositionDataProvider.findAllByEmployeeId(mockSuperior.getId())).thenReturn(Set.of(mockSuperiorEmployeePosition));

        Exception exception = assertThrows(PositionNotFoundException.class, () -> {
            sut.execute(mockResourceServerSuperiorId, input);
        });

        assertEquals("Position not found.", exception.getMessage());
    }

    @Test
    void shouldSaveEmployeeRoleAsUser() {
        String mockResourceServerSuperiorId = UUID.randomUUID().toString();
        SystemRole systemRole = mockRole();
        systemRole.setRole(Role.USER);

        Employee employee = mockEmployee();
        employee.setEmail("test-email");

        String mockEmployeeResourceServerId = UUID.randomUUID().toString();

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(1L);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockSuperiorEmployeePosition = mockEmployeePosition();
        mockSuperiorEmployeePosition.setEmployee(mockSuperior);
        mockSuperiorEmployeePosition.setPosition(mockPositionSuperior);

        when(systemRoleDataProvider.findByRole(Mockito.any())).thenReturn(Optional.of(systemRole));
        when(positionDataProvider.findPositionById(Mockito.any())).thenReturn(Optional.of(mockPosition()));
        when(employeeDataProvider.save(Mockito.any())).thenReturn(employee);
        when(employeeKeycloakClient.registerUser(Mockito.any(), Mockito.any())).thenReturn(mockEmployeeResourceServerId);
        when(employeeDataProvider.findByResourceServerId(mockResourceServerSuperiorId)).thenReturn(Optional.of(mockSuperior));
        when(employeePositionDataProvider.findAllByEmployeeId(mockSuperior.getId())).thenReturn(Set.of(mockSuperiorEmployeePosition));

        RegisterEmployeeInput input = mockSutInput();

        sut.execute(mockResourceServerSuperiorId, input);

        ArgumentCaptor<EmployeeSystemRole> employeeSystemRoleCaptor = ArgumentCaptor.forClass(EmployeeSystemRole.class);

        verify(employeeSystemRoleDataProvider, times(1)).create(employeeSystemRoleCaptor.capture());

        EmployeeSystemRole employeeSystemRoleCreated = employeeSystemRoleCaptor.getValue();

        assertEquals(Role.USER, employeeSystemRoleCreated.getSystemRole().getRole());
        assertEquals(input.getEmail(), employeeSystemRoleCreated.getEmployee().getEmail());
    }

    @Test
    void shouldSaveEmployeePositionAsEmployee() {
        String mockResourceServerSuperiorId = UUID.randomUUID().toString();
        Position position = mockPosition();
        position.setName(EnterprisePosition.EMPLOYEE);

        Employee employee = mockEmployee();
        employee.setEmail("test-email");

        String mockEmployeeResourceServerId = UUID.randomUUID().toString();

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(1L);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockSuperiorEmployeePosition = mockEmployeePosition();
        mockSuperiorEmployeePosition.setEmployee(mockSuperior);
        mockSuperiorEmployeePosition.setPosition(mockPositionSuperior);

        when(positionDataProvider.findPositionById(Mockito.any())).thenReturn(Optional.of(position));
        when(systemRoleDataProvider.findByRole(Mockito.any())).thenReturn(Optional.of(mockRole()));
        when(employeeDataProvider.save(Mockito.any())).thenReturn(employee);
        when(employeeKeycloakClient.registerUser(Mockito.any(), Mockito.any())).thenReturn(mockEmployeeResourceServerId);
        when(employeeDataProvider.findByResourceServerId(mockResourceServerSuperiorId)).thenReturn(Optional.of(mockSuperior));
        when(employeePositionDataProvider.findAllByEmployeeId(mockSuperior.getId())).thenReturn(Set.of(mockSuperiorEmployeePosition));

        RegisterEmployeeInput input = mockSutInput();

        sut.execute(mockResourceServerSuperiorId, input);

        ArgumentCaptor<EmployeePosition> employeePositionArgumentCaptor = ArgumentCaptor.forClass(EmployeePosition.class);

        verify(employeePositionDataProvider, times(1)).create(employeePositionArgumentCaptor.capture());

        EmployeePosition employeeSystemRoleCreated = employeePositionArgumentCaptor.getValue();

        assertEquals(EnterprisePosition.EMPLOYEE, employeeSystemRoleCreated.getPosition().getName());
        assertEquals(input.getEmail(), employeeSystemRoleCreated.getEmployee().getEmail());
    }

    @Test
    void shouldPersistPersonalDataWithCorrectlyProvidedParams() {
        String mockResourceServerSuperiorId = UUID.randomUUID().toString();
        RegisterEmployeeInput input = mockSutInput();

        String mockEmployeeResourceServerId = UUID.randomUUID().toString();

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(1L);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockSuperiorEmployeePosition = mockEmployeePosition();
        mockSuperiorEmployeePosition.setEmployee(mockSuperior);
        mockSuperiorEmployeePosition.setPosition(mockPositionSuperior);

        when(positionDataProvider.findPositionById(Mockito.any())).thenReturn(Optional.of(mockPosition()));
        when(systemRoleDataProvider.findByRole(Mockito.any())).thenReturn(Optional.of(mockRole()));
        when(employeeDataProvider.save(Mockito.any())).thenReturn(mockEmployee());
        when(employeeKeycloakClient.registerUser(Mockito.any(), Mockito.any())).thenReturn(mockEmployeeResourceServerId);
        when(employeeDataProvider.findByResourceServerId(mockResourceServerSuperiorId)).thenReturn(Optional.of(mockSuperior));
        when(employeePositionDataProvider.findAllByEmployeeId(mockSuperior.getId())).thenReturn(Set.of(mockSuperiorEmployeePosition));

        sut.execute(mockResourceServerSuperiorId, input);

        ArgumentCaptor<PersonalData> personalDataArgumentCaptor = ArgumentCaptor.forClass(PersonalData.class);

        verify(personalDataDataProvider, times(1)).create(personalDataArgumentCaptor.capture());
        PersonalData personalData = personalDataArgumentCaptor.getValue();

        assertEquals(input.getCity(), personalData.getCity());
        assertEquals(input.getCountry(), personalData.getCountry());
        assertEquals(input.getState(), personalData.getState());
        assertEquals(input.getPhone(), personalData.getPhone());
        assertEquals(input.getComplement(), personalData.getComplement());
        assertEquals(input.getStreet(), personalData.getStreet());
        assertEquals(input.getHouseNumber(), personalData.getHouseNumber());
        assertEquals(input.getZipcode(), personalData.getZipcode());
    }

    @Test
    void shouldReturnSutOutputWithoutErrors() {
        String mockResourceServerSuperiorId = UUID.randomUUID().toString();
        RegisterEmployeeInput input = mockSutInput();

        Date mockCreationDate = new Date();
        Long mockId = 1L;

        String mockEmployeeResourceServerId = UUID.randomUUID().toString();

        Employee mockSuperior = mockSuperior();
        mockSuperior.setId(1L);

        Position mockPositionSuperior = mockPosition();
        mockPositionSuperior.setName(EnterprisePosition.HUMAN_RESOURCES);

        EmployeePosition mockSuperiorEmployeePosition = mockEmployeePosition();
        mockSuperiorEmployeePosition.setEmployee(mockSuperior);
        mockSuperiorEmployeePosition.setPosition(mockPositionSuperior);

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(mockId);
        mockEmployee.setEmail(input.getEmail());
        mockEmployee.setCreatedAt(mockCreationDate);

        when(positionDataProvider.findPositionById(Mockito.any())).thenReturn(Optional.of(mockPosition()));
        when(systemRoleDataProvider.findByRole(Mockito.any())).thenReturn(Optional.of(mockRole()));
        when(employeeDataProvider.save(Mockito.any())).thenReturn(mockEmployee);
        when(employeeKeycloakClient.registerUser(Mockito.any(), Mockito.any())).thenReturn(mockEmployeeResourceServerId);
        when(employeeDataProvider.findByResourceServerId(mockResourceServerSuperiorId)).thenReturn(Optional.of(mockSuperior));
        when(employeePositionDataProvider.findAllByEmployeeId(mockSuperior.getId())).thenReturn(Set.of(mockSuperiorEmployeePosition));

        RegisterEmployeeOutput sutOutput = sut.execute(mockResourceServerSuperiorId, input);

        assertEquals(sutOutput.getEmployeeEmail(), input.getEmail());
        assertEquals(sutOutput.getEmployeeId(), mockId);
        assertEquals(sutOutput.getCreatedAt(), mockCreationDate);
    }
}