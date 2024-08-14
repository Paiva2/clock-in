package org.com.clockinemployees.domain.strategy.passwordValidator.strategies;

import org.com.clockinemployees.domain.strategy.passwordValidator.PasswordValidatorStrategy;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.WeakPasswordException;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PasswordRegexValidator implements PasswordValidatorStrategy {
    private static final String PASSWORD_STRENGTH_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$";

    @Override
    public void validate(String password) {
        Pattern passwordPattern = Pattern.compile(PASSWORD_STRENGTH_REGEX);
        Matcher passwordMatcher = passwordPattern.matcher(password);

        if (!passwordMatcher.matches()) {
            throw new WeakPasswordException();
        }
    }
}
