package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(Long id) {
        return userStorage.getUser(id);
    }

    public User checkAndCreateUser(User user) {
        // Проверка на повторение Login и Email
        isEmailExists(user);
        isLoginExists(user);

        // Проверка на наличие имени для отображения
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Name не обнаружен. Использование Login для Name: {}", user.getName());
        }

        return userStorage.addUser(user);
    }

    public User updateUser(User newUser) {

        // Поиск ID в списке активных пользователей
        User storedUser = getUserAndCheckNull(newUser.getId());
        // Поиск Email в запросе
        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
            // Проверка Email на дубль
            isEmailExists(newUser);
            storedUser.setEmail(newUser.getEmail());
            log.debug("Email:{} обновлён.", newUser.getEmail());
        }
        // Поиск Login в запросе
        if (newUser.getLogin() != null && !newUser.getLogin().isBlank()) {
            // Проверка Login на дубль
            isLoginExists(newUser);
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

        return userStorage.updateUser(storedUser);
    }

    public void deleteUserById(Long id) {
        getUserAndCheckNull(id);// Проверяем на наличие юзера с ID
        userStorage.deleteUser(id);
    }

    public User addFriend(Long userId, Long friendId) {
        log.info("Добавление в друзья юзеров с ID: {} и {}", userId, friendId);

        User user = getUserAndCheckNull(userId);
        User friend = getUserAndCheckNull(friendId);

        user.getFriendsIds().add(friendId);
        friend.getFriendsIds().add(userId);

        userStorage.updateUser(user);
        userStorage.updateUser(friend);

        log.info("Друзья добавлены.");
        return user;
    }

    public void removeFriend(Long firstUserId, Long secondUserId) {
        log.info("Удаление из друзей юзеров с ID: {} и {}", firstUserId, secondUserId);

        User firstUser = getUserAndCheckNull(firstUserId);
        User secondUser = getUserAndCheckNull(secondUserId);

        firstUser.getFriendsIds().remove(secondUserId);
        secondUser.getFriendsIds().remove(firstUserId);

        userStorage.updateUser(firstUser);
        userStorage.updateUser(secondUser);

        log.info("Дружба удалена.");
    }

    public Collection<User> getUsersFriends(Long id) {
        User user = getUserAndCheckNull(id);
        Set<Long> friendsIds = user.getFriendsIds();
        return friendsIds.stream()
                .map(userStorage::getUser)
                .toList();
    }

    public List<User> findCommonFriends(Long firstUserId, Long secondUserId) {
        log.info("Поиск общих друзей юзеров с ID: {} и {}", firstUserId, secondUserId);

        HashSet<Long> firstUserFriendsIds = new HashSet<>(getUserAndCheckNull(firstUserId).getFriendsIds());
        HashSet<Long> secondUserFriendsIds = new HashSet<>(getUserAndCheckNull(secondUserId).getFriendsIds());

        // Ищем пересечения списков ID
        firstUserFriendsIds.retainAll(secondUserFriendsIds);

        List<User> commonFriends = firstUserFriendsIds.stream()
                .map(userStorage::getUser)
                .toList();

        log.info("Найдено {} общих друзей.", commonFriends.size());
        return commonFriends;
    }

    private void isEmailExists(User user) {
        boolean emailExists = userStorage.getUsers().stream()
                .anyMatch(u -> !u.getId().equals(user.getId()) && u.getEmail().equals(user.getEmail()));
        if (emailExists) {
            log.warn("Использование зарегистрированного email: {}", user.getEmail());
            throw new ValidationException("Данная электронная почта уже используется");
        }
    }

    private void isLoginExists(User user) {
        boolean loginExists = userStorage.getUsers().stream()
                .anyMatch(u -> !u.getId().equals(user.getId()) && u.getLogin().equals(user.getLogin()));
        if (loginExists) {
            log.warn("Использование зарегистрированного login: {}", user.getLogin());
            throw new ValidationException("Данный Login уже используется");
        }
    }

    private User getUserAndCheckNull(Long userId) {
        User user = userStorage.getUser(userId);
        if (user == null) {
            log.warn("Указанного ID {} нет в списке активных юзеров.", userId);
            throw new NotFoundException("Указанный ID (" + userId + ")не найден");
        }
        return user;
    }

}
