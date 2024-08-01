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
   * 支持的号码类型{@link PhoneType}
   */
  PhoneType phoneType() default PhoneType.ALL;
  
  String message() default "{cn.validation.message.phoneNumber}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  enum PhoneType {
    
    /**
     * 支持所有号码
     */
    ALL("号码格式错误", Pattern.compile("^\\d{10,12}$")),
    
    /**
     * 仅支持手机号
     */
    MOBILE("手机号码格式错误", Pattern.compile("^1\\d{10}$")),
    
    /**
     * 仅支持固话号码
     */
    FIXED("固话号码格式错误", Pattern.compile("^0\\d{9,11}$"));

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
