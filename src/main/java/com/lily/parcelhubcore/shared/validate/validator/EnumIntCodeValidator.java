package com.lily.parcelhubcore.shared.validate.validator;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.lily.parcelhubcore.shared.enums.IntCodeEnum;
import com.lily.parcelhubcore.shared.validate.annotation.EnumIntCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumIntCodeValidator implements ConstraintValidator<EnumIntCode, Integer> {

    private Set<Integer> allowedCodeSet;

    @Override
    public void initialize(EnumIntCode annotation) {
        allowedCodeSet = Arrays.stream(annotation.enumClass().getEnumConstants())
                .map(IntCodeEnum::getCode)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        // 把 null 视为合法；必填交给 @NotNull
        if (value == null) {
            return true;
        }
        return allowedCodeSet.contains(value);
    }
}
