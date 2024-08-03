package org.com.clockinemployees.domain.strategy.phoneValidation.strategies;

import org.com.clockinemployees.domain.strategy.phoneValidation.PhoneValidationStrategy;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.InvalidPhoneException;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PhoneRegexValidator implements PhoneValidationStrategy {
    private final static String PHONE_FORMAT_REGEX = "^[1-9][0-9]{1}[9]?[0-9]{8}$";

    @Override
    public void validate(String phone) {
        Pattern pattern = Pattern.compile(PHONE_FORMAT_REGEX);
        Matcher matcher = pattern.matcher(phone);

        if (!matcher.matches()) {
            throw new InvalidPhoneException();
        }
    }
}
