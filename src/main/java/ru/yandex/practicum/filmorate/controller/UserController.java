package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Запрос на создание нового пользователя: {}", user);

        if (user.getLogin().contains(" ")) {
            log.warn("Login не может содержать пробелы: {}", user.getLogin());
            throw new ValidationException("Login не может содержать пробелы");
        }

        // Проверка на дубль Email
        boolean emailExists = users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()));
        if (emailExists) {
            log.warn("Использование зарегистрированного email: {}", user.getEmail());
            throw new ValidationException("Данная электронная почта или Логин уже используются");
        }

        // Проверка на дубль Login
        boolean loginExists = users.values().stream().anyMatch(u -> u.getLogin().equals(user.getLogin()));
        if (loginExists) {
            log.warn("Использование зарегистрированного login: {}", user.getLogin());
            throw new ValidationException("Данный Login уже используется");
        }

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
    public User updateUser(@RequestBody User newUser) {
        log.info("Запрос на обновление пользователя. Новые данные: {}", newUser);

        // Поиск ID в запросе
        if (newUser.getId() == null) {
            log.warn("ID не указан в запросе");
            throw new ValidationException("ID не может быть пустым");
        }

        // Поиск ID в списке
        User oldUser = users.get(newUser.getId());
        if (oldUser == null) {
            log.warn("Пользователь с ID:{} не найден", newUser.getId());
            throw new ValidationException("User с указанным ID: " + newUser.getId() + " не найден");
        }

        // Поиск Email в запросе
        if (newUser.getEmail() != null) {
            if (newUser.getEmail().isBlank() || !newUser.getEmail().contains("@")) {
                log.warn("Некорректный email: {}", newUser.getEmail());
                throw new ValidationException("Email не может быть пустым или не содержать символ @");
            }
            // Проверка Email на дубль
            boolean emailExists = users.values().stream().anyMatch(u -> u.getEmail().equals(newUser.getEmail()));
            if (emailExists) {
                log.warn("Использование зарегистрированного email: {}", newUser.getEmail());
                throw new ValidationException("Данная электронная почта уже используется");
            }
            oldUser.setEmail(newUser.getEmail());
            log.debug("Email:{} обновлён.", newUser.getEmail());
        }

        // Поиск Login в запросе
        if (newUser.getLogin() != null) {
            if (newUser.getLogin().isBlank() || newUser.getLogin().contains(" ")) {
                log.warn("Некорректный login: {}", newUser.getLogin());
                throw new ValidationException("Login не может быть пустым или содержать пробелы");
            }
            // Проверка Login на дубль
            boolean loginExists = users.values().stream().anyMatch(u -> u.getLogin().equals(newUser.getLogin()));
            if (loginExists) {
                log.warn("Использование зарегистрированного login: {}", newUser.getLogin());
                throw new ValidationException("Данный Login уже используется");
            }
            oldUser.setLogin(newUser.getLogin());
            log.debug("Login:{} обновлён", newUser.getLogin());
        }

        // Поиск Birthday в запросе
        if (newUser.getBirthday() != null) {
            if (newUser.getBirthday().isAfter(LocalDate.now())) {
                log.warn("Дата рождения указана в будущем: {}", newUser.getBirthday());
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
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
}
