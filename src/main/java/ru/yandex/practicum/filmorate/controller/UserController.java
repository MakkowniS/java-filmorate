package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.Marker;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(@Validated({Marker.OnCreate.class}) @RequestBody User user) {
        log.info("Запрос на создание нового пользователя: {}", user);

        // Проверка на дубль Email и Login
        isEmailExists(user);
        isLoginExists(user);

        user.setId(getNextId());
        log.debug("Валидация пройдена. ID:{} установлен", user.getId());

        // Проверка на наличие имени для отображения
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Name не обнаружен. Использование Login для Name: {}", user.getName());
        }

        users.put(user.getId(), user);
        log.info("Пользователь успешно создан: id={}, email={}, login={}", user.getId(), user.getEmail(), user.getLogin());

        return user;
    }

    @PutMapping
    public User updateUser(@Validated({Marker.OnUpdate.class}) @RequestBody User newUser) {
        log.info("Запрос на обновление пользователя. Новые данные: {}", newUser);

        // Поиск ID в списке
        User oldUser = users.get(newUser.getId());
        if (oldUser == null) {
            log.warn("Пользователь с ID:{} не найден", newUser.getId());
            throw new ValidationException("User с указанным ID: " + newUser.getId() + " не найден");
        }

        // Поиск Email в запросе
        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
            // Проверка Email на дубль
            isEmailExists(newUser);
            oldUser.setEmail(newUser.getEmail());
            log.debug("Email:{} обновлён.", newUser.getEmail());
        }

        // Поиск Login в запросе
        if (newUser.getLogin() != null && !newUser.getLogin().isBlank()) {
            // Проверка Login на дубль
            isLoginExists(newUser);
            oldUser.setLogin(newUser.getLogin());
            log.debug("Login:{} обновлён", newUser.getLogin());
        }

        // Поиск Birthday в запросе
        if (newUser.getBirthday() != null) {
            oldUser.setBirthday(newUser.getBirthday());
            log.debug("дата рождения:{} обновлена", newUser.getBirthday());
        }

        // Поиск Name в запросе
        if (newUser.getName() != null) {
            oldUser.setName(newUser.getName().isBlank() ? oldUser.getLogin() : newUser.getName());
            log.debug("Name:{} обновлено", oldUser.getName());
        }

        log.info("Пользователь ID:{} успешно обновлён", oldUser.getId());
        return oldUser;
    }

    private Integer getNextId() {
        int currentId = users.keySet().stream().mapToInt(id -> id).max().orElse(0);
        return ++currentId;
    }

    private void isEmailExists(User user) {
        boolean emailExists = users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()));
        if (emailExists) {
            log.warn("Использование зарегистрированного email: {}", user.getEmail());
            throw new ValidationException("Данная электронная почта уже используется");
        }
    }

    private void isLoginExists(User user) {
        boolean loginExists = users.values().stream().anyMatch(u -> u.getLogin().equals(user.getLogin()));
        if (loginExists) {
            log.warn("Использование зарегистрированного login: {}", user.getLogin());
            throw new ValidationException("Данный Login уже используется");
        }
    }
}
