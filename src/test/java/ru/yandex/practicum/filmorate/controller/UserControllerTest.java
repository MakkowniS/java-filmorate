package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserControllerTest {

    private UserController userController;
    private Validator validator;
    private User user;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        userController = new UserController();
        User user1 = new User();
        user1.setEmail("email1@yandex.ru");
        user1.setLogin("login1");
        userController.createUser(user1);
        user = new User();
    }

    @Test
    void validationUserThrowsWhenEmailIsBlank() {
        user.setEmail("");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void validationUserThrowsWhenEmailNotMailFormat() {
        user.setEmail("usermail.yandex.ru");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void validationUserThrowsWhenLoginIsBlank() {
        user.setEmail("usermail@ru");
        user.setLogin("");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("login")));
    }

    @Test
    void validationUserThrowsWhenBirthdayIsAfterNow() {
        user.setEmail("usermail@ru");
        user.setLogin("userLogin");
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("birthday")));
    }

    @Test
    void createUserSucceedIfUserValid() {
        user.setEmail("usermail@yandex.ru");
        user.setLogin("userLogin");

        User result = userController.createUser(user);
        assertNotNull(result);
        assertEquals("userLogin", result.getLogin());
    }

    @Test
    void createUserThrowsWhenLoginContainsWhitespace() {
        user.setEmail("usermail@ru");
        user.setLogin("user Login");

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUserThrowsWhenUsingExistingEmail() {
        user.setEmail("email1@yandex.ru");
        user.setLogin("userLogin");

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUserThrowsWhenUsingExistingLogin() {
        user.setEmail("usermail@ru");
        user.setLogin("login1");

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUserSucceedButNameIsLoginWhenBlank() {
        user.setEmail("usermail@ru");
        user.setLogin("userLogin");
        userController.createUser(user);

        assertEquals(user.getLogin(), user.getName());
    }

    @Test
    void updateUserSucceedIfUserValid() {
        user.setId(1);
        user.setEmail("newMail@yandex.ru");
        user.setLogin("newLogin");

        User result = userController.updateUser(user);
        assertNotNull(result);
        assertEquals("newLogin", result.getLogin());
    }

    @Test
    void updateUserThrowsWhenIdIsBlank() {
        assertThrows(ValidationException.class, () -> userController.updateUser(user));
    }

    @Test
    void updateUserThrowsWhenIdNotExists() {
        user.setId(10);

        assertThrows(ValidationException.class, () -> userController.updateUser(user));
    }

    @Test
    void updateUserThrowsWhenEmailIsNotNullButNotContainsMailCharacter() {
        user.setId(1);
        user.setEmail("userMail.ru");

        assertThrows(ValidationException.class, () -> userController.updateUser(user));
    }

    @Test
    void updateUserThrowsWhenLoginIsNotNullButContainsWhitespace() {
        user.setId(1);
        user.setLogin("lo gin");

        assertThrows(ValidationException.class, () -> userController.updateUser(user));
    }

    @Test
    void updateUserThrowsWhenBirthdayIsNotNullButAfterNow() {
        user.setId(1);
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.updateUser(user));
    }
}
