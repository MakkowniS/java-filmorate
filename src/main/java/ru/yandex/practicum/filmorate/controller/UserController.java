package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.validation.Marker;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final InMemoryUserStorage inMemoryUserStorage;

    public UserController(InMemoryUserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    @GetMapping
    public Collection<User> getUsers() {
        log.info("Запрос на получение списка пользователей.");
        return inMemoryUserStorage.getUsers();
    }

    @PostMapping
    public User createUser(@Validated(Marker.OnCreate.class) @RequestBody User user) {
        log.info("Запрос на создание нового пользователя: {}", user);
        return inMemoryUserStorage.createUser(user);
    }

    @PutMapping
    public User updateUser(@Validated(Marker.OnUpdate.class) @RequestBody User newUser) {
        log.info("Запрос на обновление пользователя. Новые данные: {}", newUser);
        return inMemoryUserStorage.updateUser(newUser);
    }

}
