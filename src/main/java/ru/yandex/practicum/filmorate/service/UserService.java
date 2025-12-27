package ru.yandex.practicum.filmorate.service;

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
public class UserService {

    private final UserStorage inMemoryStorage;
    private final UserStorage dbStorage;
    private final FriendshipService friendshipService;

    public UserService(
            FriendshipService friendshipService,
            @Qualifier("inMemoryUserStorage") UserStorage inMemoryStorage,
            @Qualifier("userDbStorage") UserStorage dbStorage) {
        this.inMemoryStorage = inMemoryStorage;
        this.dbStorage = dbStorage;
        this.friendshipService = friendshipService;
    }

    // ===== DB Storage =====

    // Добавление пользователя в БД
    public UserDto createUserInDb(NewUserRequest request) {
        Optional<User> alreadyExistEmail = dbStorage.getUserByEmail(request.getEmail());
        if (alreadyExistEmail.isPresent()) {
            throw new DuplicatedDataException("Данный Email уже используется");
        }
        Optional<User> alreadyExistLogin = dbStorage.getUserByLogin(request.getLogin());
        if (alreadyExistLogin.isPresent()) {
            throw new DuplicatedDataException("Данный Login уже используется");
        }
        User user = UserMapper.mapToUser(request);
        user = dbStorage.addUser(user);

        return UserMapper.mapToUserDto(user);
    }

    // Получение пользователя по Id и списка всех пользователей
    public UserDto getUserByIdInDb(long userId) {
        return dbStorage.getUserById(userId)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public List<UserDto> getUsersFromDb() {
        return dbStorage.getUsers().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    // Обновление пользователя в БД
    public UserDto updateUserInDb(UpdateUserRequest request) {
        User updatedUser = dbStorage.getUserById(request.getId())
                .map(user -> UserMapper.updateUserFields(user, request))
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        updatedUser = dbStorage.updateUser(updatedUser);
        return UserMapper.mapToUserDto(updatedUser);
    }

    // Удаление пользователя из БД
    public void deleteUserInDb(long userId) {
        getUserByIdInDb(userId);
        friendshipService.deleteAllFriendsInDb(userId);
        dbStorage.deleteUser(userId);

        log.info("Пользователь {} и все его дружбы удалены из БД", userId);
    }

    // ===== InMemory Storage =====

    public Collection<User> getAllUsersFromMemory() {
        return inMemoryStorage.getUsers();
    }

    public User getUserByIdFromMemory(Long id) {
        return getUserAndCheckNullInMemory(id);
    }

    public User checkAndCreateUserInMemory(User user) {
        // Проверка на повторение Login и Email
        isEmailExistsInMemory(user);
        isLoginExistsInMemory(user);

        // Проверка на наличие имени для отображения
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Name не обнаружен. Использование Login для Name: {}", user.getName());
        }

        return inMemoryStorage.addUser(user);
    }

    public User updateUserInMemory(User newUser) {

        // Поиск ID в списке активных пользователей
        User storedUser = getUserAndCheckNullInMemory(newUser.getId());
        // Поиск Email в запросе
        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
            // Проверка Email на дубль
            isEmailExistsInMemory(newUser);
            storedUser.setEmail(newUser.getEmail());
            log.debug("Email:{} обновлён.", newUser.getEmail());
        }
        // Поиск Login в запросе
        if (newUser.getLogin() != null && !newUser.getLogin().isBlank()) {
            // Проверка Login на дубль
            isLoginExistsInMemory(newUser);
            storedUser.setLogin(newUser.getLogin());
            log.debug("Login:{} обновлён", newUser.getLogin());
        }
        // Поиск Birthday в запросе
        if (newUser.getBirthday() != null) {
            storedUser.setBirthday(newUser.getBirthday());
            log.debug("Дата рождения:{} обновлена", newUser.getBirthday());
        }
        // Поиск Name в запросе
        if (newUser.getName() != null) {
            storedUser.setName(newUser.getName().isBlank() ? storedUser.getLogin() : newUser.getName());
            log.debug("Name:{} обновлено", storedUser.getName());
        }

        return inMemoryStorage.updateUser(storedUser);
    }

    public void deleteUserByIdInMemory(Long userId) {
        getUserAndCheckNullInMemory(userId);
        friendshipService.deleteAllFriendsInMemory(userId);
        inMemoryStorage.deleteUser(userId);

        log.info("Пользователь {} и все его дружбы удалены из памяти", userId);
    }

    private void isEmailExistsInMemory(User user) {
        boolean emailExists = inMemoryStorage.getUsers().stream()
                .anyMatch(u -> !u.getId().equals(user.getId()) && u.getEmail().equals(user.getEmail()));
        if (emailExists) {
            log.warn("Использование зарегистрированного email: {}", user.getEmail());
            throw new ValidationException("Данная электронная почта уже используется");
        }
    }

    private void isLoginExistsInMemory(User user) {
        boolean loginExists = inMemoryStorage.getUsers().stream()
                .anyMatch(u -> !u.getId().equals(user.getId()) && u.getLogin().equals(user.getLogin()));
        if (loginExists) {
            log.warn("Использование зарегистрированного login: {}", user.getLogin());
            throw new ValidationException("Данный Login уже используется");
        }
    }

    protected User getUserAndCheckNullInMemory(Long userId) {
        return inMemoryStorage.getUserById(userId)
                .orElseThrow(() -> {
                    log.warn("Указанного ID {} нет в списке активных юзеров.", userId);
                    return new NotFoundException("Указанный ID (" + userId + ")не найден");
                });
    }
}
