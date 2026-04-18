package com.lily.parcelhubcore.shared.validate.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.lily.parcelhubcore.shared.enums.IntCodeEnum;
import com.lily.parcelhubcore.shared.validate.validator.EnumIntCodeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = EnumIntCodeValidator.class)
public @interface EnumIntCode {

    String message() default "参数值不在允许的枚举范围内";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends IntCodeEnum> enumClass();
}
