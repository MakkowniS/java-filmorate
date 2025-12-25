package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<UserDto> getAllUsers() {
        log.info("Получение всех пользователей");
        return userService.getUsers();
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUser(@PathVariable Long userId) {
        log.info("Получение пользователя с Id:{}", userId);
        return userService.getUserById(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Validated @RequestBody NewUserRequest request) {
        log.info("Создание пользователя");
        return userService.createUserInDb(request);
    }

    @PutMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto updateUser(@PathVariable Long userId, @Validated @RequestBody UpdateUserRequest request) {
        log.info("Обновление пользователя с Id:{}", userId);
        return userService.updateUser(userId, request);
    }


    /*
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

     */

}
