package org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase;

import org.com.clockinemployees.domain.entity.*;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.enums.Role;
import org.com.clockinemployees.domain.strategy.passwordValidator.PasswordValidatorStrategy;
import org.com.clockinemployees.domain.strategy.phoneValidation.PhoneValidationStrategy;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.dto.RegisterEmployeeInput;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.dto.RegisterEmployeeOutput;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.*;
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
            .build();
    }

    private SystemRole mountRole() {
        return SystemRole.builder().build();
    }

    private Position mountPosition() {
        return Position.builder().build();
    }

    private Employee mountEmployee() {
        return Employee.builder().build();
    }

    private RegisterEmployeeInput mountSutInput() {
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
    void shouldCallSutWithCorrectlyProvidedParams() {
        RegisterEmployeeInput input = mountSutInput();
        RegisterEmployeeUsecase sutMock = Mockito.mock(RegisterEmployeeUsecase.class);

        sutMock.execute(input);

        ArgumentCaptor<RegisterEmployeeInput> inputArgumentCaptor = ArgumentCaptor.forClass(RegisterEmployeeInput.class);

        verify(sutMock, times(1)).execute(inputArgumentCaptor.capture());

        RegisterEmployeeInput capturedInput = inputArgumentCaptor.getValue();

        assertEquals(input.getEmail(), capturedInput.getEmail());
        assertEquals(input.getFirstName(), capturedInput.getFirstName());
        assertEquals(input.getLastName(), capturedInput.getLastName());
        assertEquals(input.getPassword(), capturedInput.getPassword());
    }

    @Test
    void shouldThrowErrorIfPasswordHasNotStrengthEnough() {
        RegisterEmployeeInput input = mountSutInput();
        input.setPassword("invalid-password");

        doThrow(new WeakPasswordException()).when(passwordValidatorStrategy).validate(Mockito.anyString());

        Exception exception = assertThrows(WeakPasswordException.class, () -> {
            sut.execute(input);
        });

        assertEquals("Weak password. Password must have at least 6 characters, one upper letter, and a special character.", exception.getMessage());
    }

    @Test
    void shouldThrowErrorIfPhoneIsInvalid() {
        RegisterEmployeeInput input = mountSutInput();
        input.setPhone("invalid-phone");

        doThrow(new InvalidPhoneException()).when(phoneValidationStrategy).validate(Mockito.anyString());

        Exception exception = assertThrows(InvalidPhoneException.class, () -> {
            sut.execute(input);
        });

        assertEquals("Invalid phone format.", exception.getMessage());
    }

    @Test
    void shouldThrowErrorIfEmailAlreadyBeingUsed() {
        RegisterEmployeeInput input = mountSutInput();
        input.setEmail("already-used-email@email.com");

        when(employeeDataProvider.findByEmail(Mockito.anyString())).thenReturn(
            Optional.of(Employee.builder().build())
        );

        Exception exception = assertThrows(EmailAlreadyUsedException.class, () -> {
            sut.execute(input);
        });

        assertEquals("E-mail already being used!", exception.getMessage());
    }

    @Test
    void shouldThrowErrorIfFullNameAlreadyBeingUsed() {
        RegisterEmployeeInput input = mountSutInput();
        input.setFirstName("test-name");
        input.setLastName("test-lastName");

        when(employeeDataProvider.findByFullName(Mockito.anyString(), Mockito.anyString())).thenReturn(
            Optional.of(Employee.builder().build())
        );

        Exception exception = assertThrows(FullNameAlreadyUsedException.class, () -> {
            sut.execute(input);
        });

        assertEquals("Provided first name and last name is already being used!", exception.getMessage());
    }

    @Test
    void shouldHashNewPassword() {
        String password = "123456";
        RegisterEmployeeInput input = mountSutInput();
        input.setPassword(password);

        when(systemRoleDataProvider.findByRole(Mockito.any())).thenReturn(Optional.of(mountRole()));
        when(positionDataProvider.findPositionById(Mockito.any())).thenReturn(Optional.of(mountPosition()));
        when(employeeDataProvider.save(Mockito.any())).thenReturn(mountEmployee());

        when(passwordEncoder.encode(Mockito.any())).thenReturn("hashed-password");

        sut.execute(input);

        ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);

        verify(employeeDataProvider, times(1)).save(employeeCaptor.capture());
        Employee employeeSaved = employeeCaptor.getValue();

        assertEquals(employeeSaved.getPassword(), "hashed-password");
    }

    @Test
    void shouldCallAndSaveNewEmployeeWithCorrectlyProvidedParams() {
        RegisterEmployeeInput input = mountSutInput();

        when(systemRoleDataProvider.findByRole(Mockito.any())).thenReturn(Optional.of(mountRole()));
        when(positionDataProvider.findPositionById(Mockito.any())).thenReturn(Optional.of(mountPosition()));
        when(employeeDataProvider.save(Mockito.any())).thenReturn(mountEmployee());
        when(passwordEncoder.encode(Mockito.anyString())).thenReturn("hashed-password");

        sut.execute(input);

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
        RegisterEmployeeInput input = mountSutInput();

        when(systemRoleDataProvider.findByRole(Mockito.any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RoleNotFoundException.class, () -> {
            sut.execute(input);
        });

        assertEquals("Role not found.", exception.getMessage());
    }

    @Test
    void shouldThrowErrorIfPositionNotFound() {
        RegisterEmployeeInput input = mountSutInput();

        when(systemRoleDataProvider.findByRole(Mockito.any())).thenReturn(Optional.of(mountRole()));
        when(positionDataProvider.findPositionById(Mockito.any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(PositionNotFoundException.class, () -> {
            sut.execute(input);
        });

        assertEquals("Position not found.", exception.getMessage());
    }

    @Test
    void shouldSaveEmployeeRoleAsUser() {
        SystemRole systemRole = mountRole();
        systemRole.setRole(Role.USER);

        Employee employee = mountEmployee();
        employee.setEmail("test-email");

        when(systemRoleDataProvider.findByRole(Mockito.any())).thenReturn(Optional.of(systemRole));
        when(positionDataProvider.findPositionById(Mockito.any())).thenReturn(Optional.of(mountPosition()));
        when(employeeDataProvider.save(Mockito.any())).thenReturn(employee);

        RegisterEmployeeInput input = mountSutInput();

        sut.execute(input);

        ArgumentCaptor<EmployeeSystemRole> employeeSystemRoleCaptor = ArgumentCaptor.forClass(EmployeeSystemRole.class);

        verify(employeeSystemRoleDataProvider, times(1)).create(employeeSystemRoleCaptor.capture());

        EmployeeSystemRole employeeSystemRoleCreated = employeeSystemRoleCaptor.getValue();

        assertEquals(Role.USER, employeeSystemRoleCreated.getSystemRole().getRole());
        assertEquals(input.getEmail(), employeeSystemRoleCreated.getEmployee().getEmail());
    }

    @Test
    void shouldSaveEmployeePositionAsEmployee() {
        Position position = mountPosition();
        position.setName(EnterprisePosition.EMPLOYEE);

        Employee employee = mountEmployee();
        employee.setEmail("test-email");

        when(positionDataProvider.findPositionById(Mockito.any())).thenReturn(Optional.of(position));
        when(systemRoleDataProvider.findByRole(Mockito.any())).thenReturn(Optional.of(mountRole()));
        when(employeeDataProvider.save(Mockito.any())).thenReturn(employee);

        RegisterEmployeeInput input = mountSutInput();

        sut.execute(input);

        ArgumentCaptor<EmployeePosition> employeePositionArgumentCaptor = ArgumentCaptor.forClass(EmployeePosition.class);

        verify(employeePositionDataProvider, times(1)).create(employeePositionArgumentCaptor.capture());

        EmployeePosition employeeSystemRoleCreated = employeePositionArgumentCaptor.getValue();

        assertEquals(EnterprisePosition.EMPLOYEE, employeeSystemRoleCreated.getPosition().getName());
        assertEquals(input.getEmail(), employeeSystemRoleCreated.getEmployee().getEmail());
    }

    @Test
    void shouldPersistPersonalDataWithCorrectlyProvidedParams() {
        RegisterEmployeeInput input = mountSutInput();

        when(positionDataProvider.findPositionById(Mockito.any())).thenReturn(Optional.of(mountPosition()));
        when(systemRoleDataProvider.findByRole(Mockito.any())).thenReturn(Optional.of(mountRole()));
        when(employeeDataProvider.save(Mockito.any())).thenReturn(mountEmployee());

        sut.execute(input);

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
        RegisterEmployeeInput input = mountSutInput();

        Date mockCreationDate = new Date();
        Long mockId = 1L;

        Employee mockEmployee = mountEmployee();
        mockEmployee.setId(mockId);
        mockEmployee.setEmail(input.getEmail());
        mockEmployee.setCreatedAt(mockCreationDate);

        when(positionDataProvider.findPositionById(Mockito.any())).thenReturn(Optional.of(mountPosition()));
        when(systemRoleDataProvider.findByRole(Mockito.any())).thenReturn(Optional.of(mountRole()));
        when(employeeDataProvider.save(Mockito.any())).thenReturn(mockEmployee);

        RegisterEmployeeOutput sutOutput = sut.execute(input);

        assertEquals(sutOutput.getEmployeeEmail(), input.getEmail());
        assertEquals(sutOutput.getEmployeeId(), mockId);
        assertEquals(sutOutput.getCreatedAt(), mockCreationDate);
    }
}