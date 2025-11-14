package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.Marker;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserControllerTest {

    @Autowired
    private Validator validator;
    private UserController userController;
    private User user;

    @BeforeEach
    void setup() {
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

        Set<ConstraintViolation<User>> violations = validator.validate(user, Marker.OnCreate.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void validationUserThrowsWhenEmailNotMailFormat() {
        user.setEmail("usermail.yandex.ru");

        Set<ConstraintViolation<User>> violations = validator.validate(user, Marker.OnCreate.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void validationUserThrowsWhenLoginIsBlank() {
        user.setEmail("usermail@ru");
        user.setLogin("");

        Set<ConstraintViolation<User>> violations = validator.validate(user, Marker.OnCreate.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("login")));
    }

    @Test
    void validationUserThrowsWhenBirthdayIsAfterNow() {
        user.setEmail("usermail@ru");
        user.setLogin("userLogin");
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user, Marker.OnCreate.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("birthday")));
    }

    @Test
    void validationUserThrowsWhenLoginContainsWhitespace() {
        user.setEmail("usermail@ru");
        user.setLogin("user Login");

        Set<ConstraintViolation<User>> violations = validator.validate(user, Marker.OnCreate.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("login")));
    }

    @Test
    void validationUserThrowsWhenEmailIsNotNullButNotContainsMailCharacter() {
        user.setId(1);
        user.setEmail("userMail.ru");

        Set<ConstraintViolation<User>> violations = validator.validate(user,  Marker.OnCreate.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void updateValidationUserThrowsWhenIdIsBlank() {
        Set<ConstraintViolation<User>> violations = validator.validate(user, Marker.OnUpdate.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("id")));
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
    void updateUserThrowsWhenIdNotExists() {
        user.setId(10);

        assertThrows(ValidationException.class, () -> userController.updateUser(user));
    }
}
