package ru.yandex.practicum.filmorate.validation.validationClass;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.validation.annotation.AfterFirstFilmRelease;

import java.time.LocalDate;

public class AfterFirstFilmValidator implements ConstraintValidator<AfterFirstFilmRelease, LocalDate> {

    private final LocalDate firstFilmReleaseDate = LocalDate.of(1895, 12, 28);

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value != null) {
            return value.isAfter(firstFilmReleaseDate);
        }
        return true;
    }
}
