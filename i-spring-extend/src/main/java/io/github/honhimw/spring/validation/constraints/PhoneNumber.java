package io.github.honhimw.spring.validation.constraints;

import io.github.honhimw.spring.validation.constraintvalidators.PhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

/**
 * @author hon_him
 * @since 2023-04-21
 */

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
public @interface PhoneNumber {

    /**
     * Supported phone number type
     */
    PhoneType phoneType() default PhoneType.ALL;

    String message() default "{cn.validation.message.phoneNumber}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    enum PhoneType {

        /**
         * Both mobile and fixed phone number
         */
        ALL("Phone Number Format Error", Pattern.compile("^\\d{10,12}$")),

        /**
         * Mobile phone number only
         */
        MOBILE("Mobile Phone Number Format Error", Pattern.compile("^1\\d{10}$")),

        /**
         * Fixed phone number only
         */
        FIXED("Fixed Phone Number Format Error", Pattern.compile("^0\\d{9,11}$"));

        private final String zh_CN;

        private final Pattern pattern;

        PhoneType(String zh_CN, Pattern pattern) {
            this.zh_CN = zh_CN;
            this.pattern = pattern;
        }

        public Pattern pattern() {
            return this.pattern;
        }

        @Override
        public String toString() {
            return zh_CN;
        }
    }
}
