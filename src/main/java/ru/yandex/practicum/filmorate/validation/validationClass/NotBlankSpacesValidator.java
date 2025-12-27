package ru.yandex.practicum.filmorate.validation.validationClass;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.validation.annotation.NotBlankSpaces;

public class NotBlankSpacesValidator implements ConstraintValidator<NotBlankSpaces, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && value.trim().length() > 0;
    }
}
