package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final FriendshipService friendshipService;

    // =============== Users ===============

    public UserDto createUser(NewUserRequest request) {
        User user = UserMapper.mapToUser(request);

        user = userStorage.addUser(user);
        return UserMapper.mapToUserDto(user);
    }

    public UserDto getUserById(Long userId) {
        return userStorage.getUserById(userId)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public List<UserDto> getUsers() {
        return userStorage.getUsers().stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public UserDto updateUser(UpdateUserRequest request) {

        User user = userStorage.getUserById(request.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (request.getEmail() != null) {
            User storedUser = user;
            userStorage.getUserByEmail(request.getEmail())
                    .filter(u -> !u.getId().equals(storedUser.getId()))
                    .ifPresent(u -> {
                        throw new DuplicatedDataException("Данный Email уже используется");
                    });
        }

        if (request.getLogin() != null) {
            User storedUser = user;
            userStorage.getUserByLogin(request.getLogin())
                    .filter(u -> !u.getId().equals(storedUser.getId()))
                    .ifPresent(u -> {
                        throw new DuplicatedDataException("Данный Login уже используется");
                    });
        }

        UserMapper.updateUserFields(user, request);

        user = userStorage.updateUser(user);
        return UserMapper.mapToUserDto(user);
    }

    public void deleteUser(Long userId) {
        getUserById(userId);
        friendshipService.deleteAllFriends(userId);
        userStorage.deleteUser(userId);
        log.info("Пользователь {} и все его дружбы удалены", userId);
    }
}



