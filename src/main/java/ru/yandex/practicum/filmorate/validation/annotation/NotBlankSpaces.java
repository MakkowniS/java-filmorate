package ru.yandex.practicum.filmorate.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.yandex.practicum.filmorate.validation.validationClass.NotBlankSpacesValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = NotBlankSpacesValidator.class)
@Documented
public @interface NotBlankSpaces {
    String message() default "Поле не может быть пустым или состоять только из пробелов";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}


