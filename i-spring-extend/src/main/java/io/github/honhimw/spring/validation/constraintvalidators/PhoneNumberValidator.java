package io.github.honhimw.spring.validation.constraintvalidators;

import io.github.honhimw.spring.validation.constraints.PhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * @author hon_him
 * @since 2023-04-21
 */

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    private Pattern pattern;

    @Override
    public void initialize(PhoneNumber constraintAnnotation) {
        PhoneNumber.PhoneType phoneType = constraintAnnotation.phoneType();
        this.pattern = phoneType.pattern();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return true;
        }
        return pattern.matcher(s).matches();
    }

}
