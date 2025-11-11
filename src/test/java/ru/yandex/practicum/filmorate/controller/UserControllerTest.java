package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserControllerTest {

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
    void createUserSucceedIfUserValid() {
        user.setEmail("usermail@yandex.ru");
        user.setLogin("userLogin");

        User result = userController.createUser(user);
        assertNotNull(result);
        assertEquals("userLogin", result.getLogin());
    }

    @Test
    void createUserThrowsWhenEmailIsBlank() {
        user.setEmail("");

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUserThrowsWhenEmailNotContainsMailCharacter() {
        user.setEmail("usermail.yandex.ru");

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUserThrowsWhenLoginIsBlank() {
        user.setEmail("usermail@ru");
        user.setLogin("");

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUserThrowsWhenLoginContainsWhitespace() {
        user.setEmail("usermail@ru");
        user.setLogin("user Login");

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUserThrowsWhenBirthdayIsAfterNow() {
        user.setEmail("usermail@ru");
        user.setLogin("userLogin");
        user.setBirthday(LocalDate.now().plusDays(1));

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
