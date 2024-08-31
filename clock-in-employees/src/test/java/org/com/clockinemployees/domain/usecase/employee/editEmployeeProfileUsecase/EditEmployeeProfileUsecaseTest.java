package org.com.clockinemployees.domain.usecase.employee.editEmployeeProfileUsecase;

import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.entity.PersonalData;
import org.com.clockinemployees.domain.entity.Position;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.strategy.passwordValidator.PasswordValidatorStrategy;
import org.com.clockinemployees.domain.strategy.phoneValidation.PhoneValidationStrategy;
import org.com.clockinemployees.domain.usecase.common.dto.EmployeeOutput;
import org.com.clockinemployees.domain.usecase.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.domain.usecase.common.exception.FullNameAlreadyUsedException;
import org.com.clockinemployees.domain.usecase.employee.editEmployeeProfileUsecase.dto.EditEmployeeProfileInput;
import org.com.clockinemployees.domain.usecase.employee.editEmployeeProfileUsecase.exception.PersonalDataNotFoundException;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.InvalidPhoneException;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.WeakPasswordException;
import org.com.clockinemployees.infra.keycloack.employee.EmployeeKeycloakClient;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.com.clockinemployees.infra.providers.PersonalDataDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EditEmployeeProfileUsecaseTest {
    @Mock
    private EmployeeDataProvider employeeDataProvider;

    @Mock
    private PersonalDataDataProvider personalDataDataProvider;

    @Mock
    private PasswordValidatorStrategy passwordValidatorStrategy;

    @Mock
    private PhoneValidationStrategy phoneValidationStrategy;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmployeeKeycloakClient employeeKeycloakClient;

    private EditEmployeeProfileUsecase sut;

    @BeforeEach
    void setUp() {
        sut = EditEmployeeProfileUsecase.builder()
            .employeeDataProvider(employeeDataProvider)
            .personalDataDataProvider(personalDataDataProvider)
            .passwordValidatorStrategy(passwordValidatorStrategy)
            .phoneValidationStrategy(phoneValidationStrategy)
            .passwordEncoder(passwordEncoder)
            .employeeKeycloakClient(employeeKeycloakClient)
            .build();
    }

    private EditEmployeeProfileInput mockInput() {
        return EditEmployeeProfileInput.builder()
            .firstName("test_firstName")
            .lastName("test_lastName")
            .city("test_city")
            .state("test_state")
            .phone("test_phone")
            .complement("test_complement")
            .country("test_country")
            .profilePictureUrl("test_profilePictureUrl")
            .houseNumber("test_houseNumber")
            .password("test_password")
            .street("test_street")
            .zipcode("test_zipcode")
            .build();
    }

    private Employee mockEmployee() {
        return Employee.builder().build();
    }

    private PersonalData mockPersonalData() {
        return PersonalData.builder().build();
    }

    private EmployeePosition mockEmployeePosition() {
        return new EmployeePosition();
    }

    private Position mockPosition() {
        return Position.builder().build();
    }

    @Test
    void shouldThrowExceptionIfEmployeeNotFound() {
        String mockEmployeeResourceServerId = UUID.randomUUID().toString();
        EditEmployeeProfileInput input = mockInput();

        when(employeeDataProvider.findByResourceServerId(mockEmployeeResourceServerId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EmployeeNotFoundException.class, () -> {
            sut.execute(mockEmployeeResourceServerId, input);
        });

        assertEquals("Employee not found!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfEmployeeDisabled() {
        String mockEmployeeResourceServerId = UUID.randomUUID().toString();
        EditEmployeeProfileInput input = mockInput();

        Employee employee = mockEmployee();
        employee.setDisabledAt(new Date());

        when(employeeDataProvider.findByResourceServerId(mockEmployeeResourceServerId)).thenReturn(Optional.of(employee));

        Exception exception = assertThrows(EmployeeNotFoundException.class, () -> {
            sut.execute(mockEmployeeResourceServerId, input);
        });

        assertEquals("Employee not found!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfFullNameIsAlreadyBeingUsedOnOtherEmployee() {
        String mockEmployeeResourceServerId = UUID.randomUUID().toString();
        EditEmployeeProfileInput input = mockInput();

        Employee employee = mockEmployee();
        employee.setId(1L);

        Employee otherEmployee = mockEmployee();
        otherEmployee.setId(2L);

        when(employeeDataProvider.findByResourceServerId(mockEmployeeResourceServerId)).thenReturn(Optional.of(employee));
        when(employeeDataProvider.findByFullName(input.getFirstName(), input.getLastName())).thenReturn(Optional.of(otherEmployee));

        Exception exception = assertThrows(FullNameAlreadyUsedException.class, () -> {
            sut.execute(mockEmployeeResourceServerId, input);
        });

        assertEquals("Provided first name and last name is already being used!", exception.getMessage());
    }

    @Test
    void shouldSearchForPersonalDataWithEmployeeCorrectId() {
        String mockEmployeeResourceServerId = UUID.randomUUID().toString();
        EditEmployeeProfileInput input = mockInput();

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        PersonalData mockPersonalData = mockPersonalData();
        mockPersonalData.setId(1L);
        mockPersonalData.setPhone(input.getPhone());
        mockPersonalData.setCity(input.getCity());
        mockPersonalData.setCountry(input.getCountry());
        mockPersonalData.setState(input.getState());
        mockPersonalData.setStreet(input.getStreet());
        mockPersonalData.setHouseNumber(input.getHouseNumber());
        mockPersonalData.setComplement(input.getComplement());
        mockPersonalData.setZipcode(input.getZipcode());

        Position mockPosition = mockPosition();
        mockPosition.setName(EnterprisePosition.EMPLOYEE);

        EmployeePosition mockEmployeePosition = mockEmployeePosition();
        mockEmployeePosition.setPosition(mockPosition);
        mockEmployeePosition.setEmployee(mockEmployee);

        Employee mockEmployeeUpdated = mockEmployee();
        mockEmployeeUpdated.setId(1L);

        PersonalData mockPersonalDataUpdated = mockPersonalData();
        mockPersonalDataUpdated.setId(1L);
        mockPersonalDataUpdated.setPhone(input.getPhone());
        mockPersonalDataUpdated.setCity(input.getCity());
        mockPersonalDataUpdated.setCountry(input.getCountry());
        mockPersonalDataUpdated.setState(input.getState());
        mockPersonalDataUpdated.setStreet(input.getStreet());
        mockPersonalDataUpdated.setHouseNumber(input.getHouseNumber());
        mockPersonalDataUpdated.setComplement(input.getComplement());
        mockPersonalDataUpdated.setZipcode(input.getZipcode());

        mockEmployeeUpdated.setEmployeePositions(List.of(mockEmployeePosition));
        mockEmployeeUpdated.setPersonalData(mockPersonalData);

        when(employeeDataProvider.findByResourceServerId(mockEmployeeResourceServerId)).thenReturn(Optional.of(mockEmployee));
        when(employeeDataProvider.save(any())).thenReturn(mockEmployeeUpdated);
        when(personalDataDataProvider.create(any())).thenReturn(mockPersonalDataUpdated);
        when(personalDataDataProvider.findByEmployeeId(mockEmployee.getId())).thenReturn(Optional.of(mockPersonalData));

        ArgumentCaptor<Long> captureEmployeeId = ArgumentCaptor.forClass(Long.class);

        sut.execute(mockEmployeeResourceServerId, input);

        verify(personalDataDataProvider, times(1)).findByEmployeeId(captureEmployeeId.capture());

        Long employeeIdCaptured = captureEmployeeId.getValue();

        assertEquals(mockEmployee.getId(), employeeIdCaptured);
    }

    @Test
    void shouldThrowExceptionIfPersonalDataNotFound() {
        String mockEmployeeResourceServerId = UUID.randomUUID().toString();
        EditEmployeeProfileInput input = mockInput();

        Employee employee = mockEmployee();
        employee.setId(1L);

        when(employeeDataProvider.findByResourceServerId(mockEmployeeResourceServerId)).thenReturn(Optional.of(employee));
        when(personalDataDataProvider.findByEmployeeId(employee.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(PersonalDataNotFoundException.class, () -> {
            sut.execute(mockEmployeeResourceServerId, input);
        });

        assertEquals("Employee personal data not found!", exception.getMessage());
    }

    @Test
    void shouldFillEmployeeWithCorrectlyProvidedParams() {
        String mockEmployeeResourceServerId = UUID.randomUUID().toString();
        EditEmployeeProfileInput input = mockInput();

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        PersonalData mockPersonalData = mockPersonalData();
        mockPersonalData.setId(1L);
        mockPersonalData.setPhone(input.getPhone());
        mockPersonalData.setCity(input.getCity());
        mockPersonalData.setCountry(input.getCountry());
        mockPersonalData.setState(input.getState());
        mockPersonalData.setStreet(input.getStreet());
        mockPersonalData.setHouseNumber(input.getHouseNumber());
        mockPersonalData.setComplement(input.getComplement());
        mockPersonalData.setZipcode(input.getZipcode());

        Position mockPosition = mockPosition();
        mockPosition.setName(EnterprisePosition.EMPLOYEE);

        EmployeePosition mockEmployeePosition = mockEmployeePosition();
        mockEmployeePosition.setPosition(mockPosition);
        mockEmployeePosition.setEmployee(mockEmployee);

        Employee mockEmployeeUpdated = mockEmployee();
        mockEmployeeUpdated.setId(mockEmployee.getId());

        PersonalData mockPersonalDataUpdated = mockPersonalData();
        mockPersonalDataUpdated.setId(mockPersonalData.getId());
        mockPersonalDataUpdated.setPhone(input.getPhone());
        mockPersonalDataUpdated.setCity(input.getCity());
        mockPersonalDataUpdated.setCountry(input.getCountry());
        mockPersonalDataUpdated.setState(input.getState());
        mockPersonalDataUpdated.setStreet(input.getStreet());
        mockPersonalDataUpdated.setHouseNumber(input.getHouseNumber());
        mockPersonalDataUpdated.setComplement(input.getComplement());
        mockPersonalDataUpdated.setZipcode(input.getZipcode());

        mockEmployeeUpdated.setEmployeePositions(List.of(mockEmployeePosition));
        mockEmployeeUpdated.setPersonalData(mockPersonalData);

        when(employeeDataProvider.findByResourceServerId(mockEmployeeResourceServerId)).thenReturn(Optional.of(mockEmployee));
        when(personalDataDataProvider.findByEmployeeId(mockEmployee.getId())).thenReturn(Optional.of(mockPersonalData));

        when(employeeDataProvider.save(any())).thenReturn(mockEmployeeUpdated);
        when(personalDataDataProvider.create(any())).thenReturn(mockPersonalDataUpdated);

        ArgumentCaptor<Employee> captureEmployeeUpdate = ArgumentCaptor.forClass(Employee.class);

        sut.execute(mockEmployeeResourceServerId, input);

        verify(employeeDataProvider, times(1)).save(captureEmployeeUpdate.capture());

        Employee employeeUpdateCaptured = captureEmployeeUpdate.getValue();

        assertEquals(input.getFirstName(), employeeUpdateCaptured.getFirstName());
        assertEquals(input.getLastName(), employeeUpdateCaptured.getLastName());
        assertEquals(input.getProfilePictureUrl(), employeeUpdateCaptured.getProfilePictureUrl());
    }

    @Test
    void shouldValidatePasswordStrengthIfPasswordNonNull() {
        String mockEmployeeResourceServerId = UUID.randomUUID().toString();
        EditEmployeeProfileInput input = mockInput();
        input.setPassword("new_password");

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        PersonalData mockPersonalData = mockPersonalData();
        mockPersonalData.setId(1L);
        mockPersonalData.setPhone(input.getPhone());
        mockPersonalData.setCity(input.getCity());
        mockPersonalData.setCountry(input.getCountry());
        mockPersonalData.setState(input.getState());
        mockPersonalData.setStreet(input.getStreet());
        mockPersonalData.setHouseNumber(input.getHouseNumber());
        mockPersonalData.setComplement(input.getComplement());
        mockPersonalData.setZipcode(input.getZipcode());

        Position mockPosition = mockPosition();
        mockPosition.setName(EnterprisePosition.EMPLOYEE);

        EmployeePosition mockEmployeePosition = mockEmployeePosition();
        mockEmployeePosition.setPosition(mockPosition);
        mockEmployeePosition.setEmployee(mockEmployee);

        Employee mockEmployeeUpdated = mockEmployee();
        mockEmployeeUpdated.setId(mockEmployee.getId());

        PersonalData mockPersonalDataUpdated = mockPersonalData();
        mockPersonalDataUpdated.setId(mockPersonalData.getId());
        mockPersonalDataUpdated.setPhone(input.getPhone());
        mockPersonalDataUpdated.setCity(input.getCity());
        mockPersonalDataUpdated.setCountry(input.getCountry());
        mockPersonalDataUpdated.setState(input.getState());
        mockPersonalDataUpdated.setStreet(input.getStreet());
        mockPersonalDataUpdated.setHouseNumber(input.getHouseNumber());
        mockPersonalDataUpdated.setComplement(input.getComplement());
        mockPersonalDataUpdated.setZipcode(input.getZipcode());

        mockEmployeeUpdated.setEmployeePositions(List.of(mockEmployeePosition));
        mockEmployeeUpdated.setPersonalData(mockPersonalData);

        when(employeeDataProvider.findByResourceServerId(mockEmployeeResourceServerId)).thenReturn(Optional.of(mockEmployee));
        when(personalDataDataProvider.findByEmployeeId(mockEmployee.getId())).thenReturn(Optional.of(mockPersonalData));

        doThrow(new WeakPasswordException()).when(passwordValidatorStrategy).validate(input.getPassword());

        Exception exception = assertThrows(WeakPasswordException.class, () -> {
            sut.execute(mockEmployeeResourceServerId, input);
        });

        assertEquals("Weak password. Password must have at least 6 characters, one upper letter, and a special character.", exception.getMessage());
    }

    @Test
    void shouldSaveNewPasswordHashedIfNewProvided() {
        String mockEmployeeResourceServerId = UUID.randomUUID().toString();
        EditEmployeeProfileInput input = mockInput();
        input.setPassword("new_password");

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        PersonalData mockPersonalData = mockPersonalData();
        mockPersonalData.setId(1L);
        mockPersonalData.setPhone(input.getPhone());
        mockPersonalData.setCity(input.getCity());
        mockPersonalData.setCountry(input.getCountry());
        mockPersonalData.setState(input.getState());
        mockPersonalData.setStreet(input.getStreet());
        mockPersonalData.setHouseNumber(input.getHouseNumber());
        mockPersonalData.setComplement(input.getComplement());
        mockPersonalData.setZipcode(input.getZipcode());

        Position mockPosition = mockPosition();
        mockPosition.setName(EnterprisePosition.EMPLOYEE);

        EmployeePosition mockEmployeePosition = mockEmployeePosition();
        mockEmployeePosition.setPosition(mockPosition);
        mockEmployeePosition.setEmployee(mockEmployee);

        Employee mockEmployeeUpdated = mockEmployee();
        mockEmployeeUpdated.setId(mockEmployee.getId());

        PersonalData mockPersonalDataUpdated = mockPersonalData();
        mockPersonalDataUpdated.setId(mockPersonalData.getId());
        mockPersonalDataUpdated.setPhone(input.getPhone());
        mockPersonalDataUpdated.setCity(input.getCity());
        mockPersonalDataUpdated.setCountry(input.getCountry());
        mockPersonalDataUpdated.setState(input.getState());
        mockPersonalDataUpdated.setStreet(input.getStreet());
        mockPersonalDataUpdated.setHouseNumber(input.getHouseNumber());
        mockPersonalDataUpdated.setComplement(input.getComplement());
        mockPersonalDataUpdated.setZipcode(input.getZipcode());

        mockEmployeeUpdated.setEmployeePositions(List.of(mockEmployeePosition));
        mockEmployeeUpdated.setPersonalData(mockPersonalData);

        when(employeeDataProvider.findByResourceServerId(mockEmployeeResourceServerId)).thenReturn(Optional.of(mockEmployee));
        when(personalDataDataProvider.findByEmployeeId(mockEmployee.getId())).thenReturn(Optional.of(mockPersonalData));

        when(passwordEncoder.encode(input.getPassword())).thenReturn("hashed_new_password");

        when(employeeDataProvider.save(any())).thenReturn(mockEmployeeUpdated);
        when(personalDataDataProvider.create(any())).thenReturn(mockPersonalDataUpdated);

        ArgumentCaptor<Employee> employeeArgumentCaptor = ArgumentCaptor.forClass(Employee.class);

        sut.execute(mockEmployeeResourceServerId, input);

        verify(employeeDataProvider, times(1)).save(employeeArgumentCaptor.capture());

        assertEquals(employeeArgumentCaptor.getValue().getPassword(), "hashed_new_password");
    }

    @Test
    void shouldThrowExceptionIfNewPhoneIsInvalid() {
        String mockEmployeeResourceServerId = UUID.randomUUID().toString();
        EditEmployeeProfileInput input = mockInput();

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        PersonalData mockPersonalData = mockPersonalData();
        mockPersonalData.setId(1L);
        mockPersonalData.setPhone(input.getPhone());
        mockPersonalData.setCity(input.getCity());
        mockPersonalData.setCountry(input.getCountry());
        mockPersonalData.setState(input.getState());
        mockPersonalData.setStreet(input.getStreet());
        mockPersonalData.setHouseNumber(input.getHouseNumber());
        mockPersonalData.setComplement(input.getComplement());
        mockPersonalData.setZipcode(input.getZipcode());

        Position mockPosition = mockPosition();
        mockPosition.setName(EnterprisePosition.EMPLOYEE);

        EmployeePosition mockEmployeePosition = mockEmployeePosition();
        mockEmployeePosition.setPosition(mockPosition);
        mockEmployeePosition.setEmployee(mockEmployee);

        Employee mockEmployeeUpdated = mockEmployee();
        mockEmployeeUpdated.setId(mockEmployee.getId());

        PersonalData mockPersonalDataUpdated = mockPersonalData();
        mockPersonalDataUpdated.setId(mockPersonalData.getId());
        mockPersonalDataUpdated.setPhone(input.getPhone());
        mockPersonalDataUpdated.setCity(input.getCity());
        mockPersonalDataUpdated.setCountry(input.getCountry());
        mockPersonalDataUpdated.setState(input.getState());
        mockPersonalDataUpdated.setStreet(input.getStreet());
        mockPersonalDataUpdated.setHouseNumber(input.getHouseNumber());
        mockPersonalDataUpdated.setComplement(input.getComplement());
        mockPersonalDataUpdated.setZipcode(input.getZipcode());

        mockEmployeeUpdated.setEmployeePositions(List.of(mockEmployeePosition));
        mockEmployeeUpdated.setPersonalData(mockPersonalData);

        when(employeeDataProvider.findByResourceServerId(mockEmployeeResourceServerId)).thenReturn(Optional.of(mockEmployee));
        when(personalDataDataProvider.findByEmployeeId(mockEmployee.getId())).thenReturn(Optional.of(mockPersonalData));

        when(employeeDataProvider.save(any())).thenReturn(mockEmployeeUpdated);
        when(personalDataDataProvider.create(any())).thenReturn(mockPersonalDataUpdated);

        sut.execute(mockEmployeeResourceServerId, input);

        ArgumentCaptor<PersonalData> personalDataArgumentCaptor = ArgumentCaptor.forClass(PersonalData.class);

        verify(personalDataDataProvider, times(1)).create(personalDataArgumentCaptor.capture());

        PersonalData personalDataCapturedValue = personalDataArgumentCaptor.getValue();

        assertEquals(input.getCity(), personalDataCapturedValue.getCity());
        assertEquals(input.getCountry(), personalDataCapturedValue.getCountry());
        assertEquals(input.getComplement(), personalDataCapturedValue.getComplement());
        assertEquals(input.getHouseNumber(), personalDataCapturedValue.getHouseNumber());
        assertEquals(input.getStreet(), personalDataCapturedValue.getStreet());
        assertEquals(input.getZipcode(), personalDataCapturedValue.getZipcode());
        assertEquals(input.getState(), personalDataCapturedValue.getState());
    }

    @Test
    void shouldSavePersonalDataWithUpdatedValues() {
        String mockEmployeeResourceServerId = UUID.randomUUID().toString();
        EditEmployeeProfileInput input = mockInput();

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);

        PersonalData mockPersonalData = mockPersonalData();
        mockPersonalData.setId(1L);
        mockPersonalData.setPhone(input.getPhone());
        mockPersonalData.setCity(input.getCity());
        mockPersonalData.setCountry(input.getCountry());
        mockPersonalData.setState(input.getState());
        mockPersonalData.setStreet(input.getStreet());
        mockPersonalData.setHouseNumber(input.getHouseNumber());
        mockPersonalData.setComplement(input.getComplement());
        mockPersonalData.setZipcode(input.getZipcode());

        Position mockPosition = mockPosition();
        mockPosition.setName(EnterprisePosition.EMPLOYEE);

        EmployeePosition mockEmployeePosition = mockEmployeePosition();
        mockEmployeePosition.setPosition(mockPosition);
        mockEmployeePosition.setEmployee(mockEmployee);

        Employee mockEmployeeUpdated = mockEmployee();
        mockEmployeeUpdated.setId(mockEmployee.getId());

        PersonalData mockPersonalDataUpdated = mockPersonalData();
        mockPersonalDataUpdated.setId(mockPersonalData.getId());
        mockPersonalDataUpdated.setPhone(input.getPhone());
        mockPersonalDataUpdated.setCity(input.getCity());
        mockPersonalDataUpdated.setCountry(input.getCountry());
        mockPersonalDataUpdated.setState(input.getState());
        mockPersonalDataUpdated.setStreet(input.getStreet());
        mockPersonalDataUpdated.setHouseNumber(input.getHouseNumber());
        mockPersonalDataUpdated.setComplement(input.getComplement());
        mockPersonalDataUpdated.setZipcode(input.getZipcode());

        mockEmployeeUpdated.setEmployeePositions(List.of(mockEmployeePosition));
        mockEmployeeUpdated.setPersonalData(mockPersonalData);

        when(employeeDataProvider.findByResourceServerId(mockEmployeeResourceServerId)).thenReturn(Optional.of(mockEmployee));
        when(personalDataDataProvider.findByEmployeeId(mockEmployee.getId())).thenReturn(Optional.of(mockPersonalData));

        doThrow(new InvalidPhoneException()).when(phoneValidationStrategy).validate(input.getPhone());

        Exception exception = assertThrows(InvalidPhoneException.class, () -> {
            sut.execute(mockEmployeeResourceServerId, input);
        });

        assertEquals("Invalid phone format.", exception.getMessage());
    }

    @Test
    void shouldSaveEmployeeOnResourceServer() {
        String mockEmployeeResourceServerId = UUID.randomUUID().toString();
        EditEmployeeProfileInput input = mockInput();
        input.setPassword("new_password");

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);
        mockEmployee.setKeycloakId(mockEmployeeResourceServerId);

        PersonalData mockPersonalData = mockPersonalData();
        mockPersonalData.setId(1L);
        mockPersonalData.setPhone(input.getPhone());
        mockPersonalData.setCity(input.getCity());
        mockPersonalData.setCountry(input.getCountry());
        mockPersonalData.setState(input.getState());
        mockPersonalData.setStreet(input.getStreet());
        mockPersonalData.setHouseNumber(input.getHouseNumber());
        mockPersonalData.setComplement(input.getComplement());
        mockPersonalData.setZipcode(input.getZipcode());

        Position mockPosition = mockPosition();
        mockPosition.setName(EnterprisePosition.EMPLOYEE);

        EmployeePosition mockEmployeePosition = mockEmployeePosition();
        mockEmployeePosition.setPosition(mockPosition);
        mockEmployeePosition.setEmployee(mockEmployee);

        Employee mockEmployeeUpdated = mockEmployee();
        mockEmployeeUpdated.setId(mockEmployee.getId());

        PersonalData mockPersonalDataUpdated = mockPersonalData();
        mockPersonalDataUpdated.setId(mockPersonalData.getId());
        mockPersonalDataUpdated.setPhone(input.getPhone());
        mockPersonalDataUpdated.setCity(input.getCity());
        mockPersonalDataUpdated.setCountry(input.getCountry());
        mockPersonalDataUpdated.setState(input.getState());
        mockPersonalDataUpdated.setStreet(input.getStreet());
        mockPersonalDataUpdated.setHouseNumber(input.getHouseNumber());
        mockPersonalDataUpdated.setComplement(input.getComplement());
        mockPersonalDataUpdated.setZipcode(input.getZipcode());

        mockEmployeeUpdated.setEmployeePositions(List.of(mockEmployeePosition));
        mockEmployeeUpdated.setPersonalData(mockPersonalData);
        mockEmployeeUpdated.setKeycloakId(mockEmployee.getKeycloakId());

        when(passwordEncoder.encode(input.getPassword())).thenReturn("hashed_new_password");

        when(employeeDataProvider.findByResourceServerId(mockEmployeeResourceServerId)).thenReturn(Optional.of(mockEmployee));
        when(personalDataDataProvider.findByEmployeeId(mockEmployee.getId())).thenReturn(Optional.of(mockPersonalData));

        when(employeeDataProvider.save(any())).thenReturn(mockEmployeeUpdated);
        when(personalDataDataProvider.create(any())).thenReturn(mockPersonalDataUpdated);

        ArgumentCaptor<Employee> employeeArgumentCaptor = ArgumentCaptor.forClass(Employee.class);
        ArgumentCaptor<String> rawPasswordArgumentCaptor = ArgumentCaptor.forClass(String.class);

        sut.execute(mockEmployeeResourceServerId, input);

        verify(employeeKeycloakClient, times(1)).updateEmployee(employeeArgumentCaptor.capture(), rawPasswordArgumentCaptor.capture());

        Employee employeeCaptorValue = employeeArgumentCaptor.getValue();
        String rawPasswordCaptorValue = rawPasswordArgumentCaptor.getValue();

        assertEquals(mockEmployeeUpdated.getId(), employeeCaptorValue.getId());
        assertEquals(mockEmployeeUpdated.getKeycloakId(), employeeCaptorValue.getKeycloakId());
        assertEquals(input.getPassword(), rawPasswordCaptorValue);
    }

    @Test
    void shouldReturnUsecaseOutputWithoutErrors() {
        String mockEmployeeResourceServerId = UUID.randomUUID().toString();
        EditEmployeeProfileInput input = mockInput();
        input.setPassword("new_password");

        Employee mockEmployee = mockEmployee();
        mockEmployee.setId(1L);
        mockEmployee.setKeycloakId(mockEmployeeResourceServerId);
        mockEmployee.setEmail("test_email");

        PersonalData mockPersonalData = mockPersonalData();
        mockPersonalData.setId(1L);
        mockPersonalData.setPhone(input.getPhone());
        mockPersonalData.setCity(input.getCity());
        mockPersonalData.setCountry(input.getCountry());
        mockPersonalData.setState(input.getState());
        mockPersonalData.setStreet(input.getStreet());
        mockPersonalData.setHouseNumber(input.getHouseNumber());
        mockPersonalData.setComplement(input.getComplement());
        mockPersonalData.setZipcode(input.getZipcode());

        Position mockPosition = mockPosition();
        mockPosition.setName(EnterprisePosition.EMPLOYEE);

        EmployeePosition mockEmployeePosition = mockEmployeePosition();
        mockEmployeePosition.setPosition(mockPosition);
        mockEmployeePosition.setEmployee(mockEmployee);

        Employee mockEmployeeUpdated = mockEmployee();
        mockEmployeeUpdated.setId(mockEmployee.getId());

        PersonalData mockPersonalDataUpdated = mockPersonalData();
        mockPersonalDataUpdated.setId(mockPersonalData.getId());
        mockPersonalDataUpdated.setPhone(input.getPhone());
        mockPersonalDataUpdated.setCity(input.getCity());
        mockPersonalDataUpdated.setCountry(input.getCountry());
        mockPersonalDataUpdated.setState(input.getState());
        mockPersonalDataUpdated.setStreet(input.getStreet());
        mockPersonalDataUpdated.setHouseNumber(input.getHouseNumber());
        mockPersonalDataUpdated.setComplement(input.getComplement());
        mockPersonalDataUpdated.setZipcode(input.getZipcode());

        mockEmployeeUpdated.setEmployeePositions(List.of(mockEmployeePosition));
        mockEmployeeUpdated.setPersonalData(mockPersonalData);
        mockEmployeeUpdated.setKeycloakId(mockEmployee.getKeycloakId());
        mockEmployeeUpdated.setEmail(mockEmployee.getEmail());

        when(passwordEncoder.encode(input.getPassword())).thenReturn("hashed_new_password");

        when(employeeDataProvider.findByResourceServerId(mockEmployeeResourceServerId)).thenReturn(Optional.of(mockEmployee));
        when(personalDataDataProvider.findByEmployeeId(mockEmployee.getId())).thenReturn(Optional.of(mockPersonalData));

        when(employeeDataProvider.save(any())).thenReturn(mockEmployeeUpdated);
        when(personalDataDataProvider.create(any())).thenReturn(mockPersonalDataUpdated);

        EmployeeOutput output = sut.execute(mockEmployeeResourceServerId, input);

        assertEquals(output.getId(), mockEmployeeUpdated.getId());
        assertEquals(output.getEmail(), mockEmployeeUpdated.getEmail());
        assertEquals(output.getProfilePictureUrl(), mockEmployeeUpdated.getProfilePictureUrl());
        assertEquals(output.getFirstName(), mockEmployeeUpdated.getFirstName());
        assertEquals(output.getLastName(), mockEmployeeUpdated.getLastName());
        assertEquals(output.getPersonalData().getZipcode(), mockPersonalData.getZipcode());
        assertEquals(output.getPersonalData().getState(), mockPersonalData.getState());
        assertEquals(output.getPersonalData().getCity(), mockPersonalData.getCity());
    }
}