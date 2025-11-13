package ru.yandex.practicum.filmorate.validation.validationClass;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.validation.annotation.NotContainsWhitespaces;

public class NotContainsWhitespacesValidator implements ConstraintValidator<NotContainsWhitespaces, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value != null) {
            return !value.contains(" ");
        }
        return true;
    }

}
