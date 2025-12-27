package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.service.FriendshipService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FriendshipService friendshipService;

    // ===== Users =====

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<UserDto> getAllUsers() {
        log.info("Получение всех пользователей");
        return userService.getUsersFromDb();
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUser(@PathVariable Long userId) {
        log.info("Получение пользователя с Id:{}", userId);
        return userService.getUserByIdInDb(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody NewUserRequest request) {
        log.info("Создание пользователя");
        return userService.createUserInDb(request);
    }

    @PutMapping()
    @ResponseStatus(HttpStatus.OK)
    public UserDto updateUser(@Valid @RequestBody UpdateUserRequest request) {
        log.info("Обновление пользователя");
        return userService.updateUserInDb(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        log.info("Запрос на удаление пользователя: {}", id);
        userService.deleteUserInDb(id);
    }

    // ===== Friends =====

    @GetMapping("/{userId}/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUserFriends(@PathVariable Long userId) {
        log.info("Запрос на получения списка друзей юзера с id:{}", userId);
        return friendshipService.getFriendsInDb(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherUserId}")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getCommonFriends(@PathVariable Long userId, @PathVariable Long otherUserId) {
        log.info("Запрос на получение общего списка друзей пользователей с id {} и {}", userId, otherUserId);
        return friendshipService.getCommonFriendsInDb(userId, otherUserId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Добавление друга: пользователь {} -> друг {}", id, friendId);
        friendshipService.addFriendInDb(id, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        log.info("Удаление друга: пользователь {} -> друг {}", userId, friendId);
        friendshipService.deleteFriendInDb(userId, friendId);
    }
}
