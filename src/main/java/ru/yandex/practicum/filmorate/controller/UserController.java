package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.validation.Marker;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> getUsers() {
        log.info("Запрос на получение списка пользователей.");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        log.info("Запрос на получение юзера с id:{}", id);
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getUserFriends(@PathVariable Long id) {
        log.info("Запрос на получения списка друзей юзера с id:{}", id);
        return userService.getUsersFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Запрос на получение общего списка друзей пользователй с id {} и {}", id, otherId);
        return userService.findCommonFriends(id, otherId);
    }

    @PostMapping
    public User createUser(@Validated(Marker.OnCreate.class) @RequestBody User user) {
        log.info("Запрос на создание нового пользователя: {}", user);
        return userService.checkAndCreateUser(user);
    }

    @PutMapping
    public User updateUser(@Validated(Marker.OnUpdate.class) @RequestBody User newUser) {
        log.info("Запрос на обновление пользователя. Новые данные: {}", newUser);
        return userService.updateUser(newUser);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public User addUserFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        log.info("Запрос на добавление юзеру с id:{} друга с id: {}", userId, friendId);
        return userService.addFriend(userId, friendId);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        log.info("Запрос на удаление пользователя: {}", id);
        userService.deleteUserById(id);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Запрос на удаление у юзера с id:{} друга с id: {}", id, friendId);
        userService.removeFriend(id, friendId);
    }

}
