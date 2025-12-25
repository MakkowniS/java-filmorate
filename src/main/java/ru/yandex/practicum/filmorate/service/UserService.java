package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorage inMemoryStorage;
    private final UserStorage dbStorage;

    public UserService(@Qualifier("inMemoryUserStorage") UserStorage inMemoryStorage,
                       @Qualifier("userDbStorage") UserStorage dbStorage){
        this.inMemoryStorage = inMemoryStorage;
        this.dbStorage = dbStorage;
    }

    // DbStorage Сервис

    // Добавление пользователя в БД
    public UserDto createUserInDb(NewUserRequest request){
        Optional<User> alreadyExistEmail = dbStorage.getUserByEmail(request.getEmail());
        if (alreadyExistEmail.isPresent()){
            throw new DuplicatedDataException("Данный Email уже используется");
        }
        Optional<User> alreadyExistLogin = dbStorage.getUserByLogin(request.getLogin());
        if (alreadyExistLogin.isPresent()){
            throw new DuplicatedDataException("Данный Login уже используется");
        }
        User user = UserMapper.mapToUser(request);
        user = dbStorage.addUser(user);

        return UserMapper.mapToUserDto(user);
    }

    // Получение пользователя по Id и списка всех пользователей
    public UserDto getUserById(long userId){
        return dbStorage.getUserById(userId)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public List<UserDto> getUsers(){
        return dbStorage.getUsers().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    // Обновление пользователя в БД
    public UserDto updateUser(long userId, UpdateUserRequest  request){
        User updatedUser = dbStorage.getUserById(userId)
                .map(user -> UserMapper.updateUserFields(user, request))
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        updatedUser = dbStorage.updateUser(updatedUser);
        return UserMapper.mapToUserDto(updatedUser);
    }

    // Удаление пользователя из БД
    public void deleteUser(long userId){
        dbStorage.deleteUser(userId);
    }

    // InMemoryStorage Сервис
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

    public void deleteUserByIdInMemory(Long id) {
        User user = getUserAndCheckNullInMemory(id);// Проверяем на наличие юзера с ID
        // Удаление ID юзера из списков его друзей
        user.getFriendsIds().keySet().stream()
                .map(inMemoryStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(friendUser -> friendUser.getFriendsIds().remove(id));
        inMemoryStorage.deleteUser(id);
    }

    public User addFriendInMemory(Long userId, Long friendId) {
        log.info("Добавление в друзья юзеров с ID: {} и {}", userId, friendId);
        if (userId.equals(friendId)) {
            throw new IncorrectParameterException("Нельзя добавить себя в друзья.");
        }

        User user = getUserAndCheckNullInMemory(userId);
        User friend = getUserAndCheckNullInMemory(friendId);

        user.getFriendsIds().put(friendId, FriendshipStatus.CONFIRMED);
        friend.getFriendsIds().put(userId,  FriendshipStatus.CONFIRMED);

        log.info("Друзья добавлены.");
        return user;
    }

    public void removeFriendInMemory(Long firstUserId, Long secondUserId) {
        log.info("Удаление из друзей юзеров с ID: {} и {}", firstUserId, secondUserId);

        User firstUser = getUserAndCheckNullInMemory(firstUserId);
        User secondUser = getUserAndCheckNullInMemory(secondUserId);

        firstUser.getFriendsIds().remove(secondUserId);
        secondUser.getFriendsIds().remove(firstUserId);

        log.info("Дружба удалена.");
    }

    public Collection<User> getUsersFriendsInMemory(Long id) {
        User user = getUserAndCheckNullInMemory(id);
        Set<Long> friendsIds = user.getFriendsIds().keySet();
        return friendsIds.stream()
                .map(inMemoryStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public List<User> findCommonFriendsInMemory(Long firstUserId, Long secondUserId) {
        log.info("Поиск общих друзей юзеров с ID: {} и {}", firstUserId, secondUserId);

        HashSet<Long> firstUserFriendsIds = new HashSet<>(getUserAndCheckNullInMemory(firstUserId).getFriendsIds().keySet());
        HashSet<Long> secondUserFriendsIds = new HashSet<>(getUserAndCheckNullInMemory(secondUserId).getFriendsIds().keySet());

        // Ищем пересечения списков ID
        firstUserFriendsIds.retainAll(secondUserFriendsIds);

        List<User> commonFriends = firstUserFriendsIds.stream()
                .map(inMemoryStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        log.info("Найдено {} общих друзей.", commonFriends.size());
        return commonFriends;
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
