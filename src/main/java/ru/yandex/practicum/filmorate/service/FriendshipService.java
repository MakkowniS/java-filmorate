package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FriendshipService {

    private final FriendshipStorage inMemoryStorage;
    private final FriendshipStorage dbStorage;
    private final UserStorage inMemoryUserStorage;
    private final UserStorage dbUserStorage;

    public FriendshipService(
            @Qualifier("inMemoryFriendshipStorage") FriendshipStorage inMemoryStorage,
            @Qualifier("friendshipDbStorage") FriendshipStorage dbStorage,
            @Qualifier("inMemoryUserStorage") UserStorage inMemoryUserStorage,
            @Qualifier("userDbStorage") UserStorage dbUserStorage
    ) {
        this.inMemoryStorage = inMemoryStorage;
        this.dbStorage = dbStorage;
        this.inMemoryUserStorage = inMemoryUserStorage;
        this.dbUserStorage = dbUserStorage;
    }

    // ===== DB =====

    public void addFriendInDb(long userId, long friendId) {
        validateUsersInDb(userId, friendId);
        // user - friend (NOT_CONFIRMED)
        dbStorage.addFriend(userId, friendId, FriendshipStatus.NOT_CONFIRMED);

        log.info("Добавлена дружба в БД {} <-> {}", userId, friendId);
    }

    public void deleteFriendInDb(long userId, long friendId) {
        validateUsersInDb(userId, friendId);
        dbStorage.deleteFriend(userId, friendId);

        log.info("Удалена дружба в БД {} -> {}", userId, friendId);
    }


    public void deleteAllFriendsInDb(long userId) {
        dbStorage.deleteAllFriends(userId);
        log.info("Удалены все дружбы пользователя {} в БД", userId);
    }

    public List<UserDto> getFriendsInDb(long userId) {
        dbUserStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        List<Long> friendsIds = dbStorage.getFriendsIds(userId);
        return friendsIds.stream()
                .map(dbUserStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public List<UserDto> getCommonFriendsInDb(long userId, long otherUserId) {
        validateUsersInDb(userId, otherUserId);
        List<Long> commonFriendsIds = dbStorage.getCommonFriends(userId, otherUserId);
        return commonFriendsIds.stream()
                .map(dbUserStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(UserMapper::mapToUserDto)
                .toList();

    }

    // ===== InMemory =====

    public void addFriendInMemory(long userId, long friendId) {
        validateUsersInMemory(userId, friendId);

        inMemoryStorage.addFriend(userId, friendId, FriendshipStatus.NOT_CONFIRMED);

        log.info("Добавлена дружба в памяти {} <-> {}", userId, friendId);
    }

    public void removeFriendInMemory(long userId, long friendId) {
        if (!inMemoryStorage.existsFriendship(userId, friendId)) {
            throw new NotFoundException("Дружба не найдена");
        }
        inMemoryStorage.deleteFriend(userId, friendId);

        log.info("Удалена дружба в памяти {} <-> {}", userId, friendId);
    }

    public void deleteAllFriendsInMemory(long userId) {
        inMemoryStorage.deleteAllFriends(userId);
        log.info("Удалены все дружбы пользователя {} в памяти", userId);
    }

    public List<User> getFriendsInMemory(long userId) {
        inMemoryUserStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        List<Long> friendsIds = inMemoryStorage.getFriendsIds(userId);
        return friendsIds.stream()
                .map(inMemoryUserStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public List<User> getCommonFriendsInMemory(long userId, long otherUserId) {
        validateUsersInMemory(userId, otherUserId);
        List<Long> commonFriendsIds = inMemoryStorage.getCommonFriends(userId, otherUserId);
        return commonFriendsIds.stream()
                .map(inMemoryUserStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    // ===== Хелперы =====

    private void validateUsersInDb(long userId, long friendId) {
        if (userId == friendId) {
            throw new IncorrectParameterException("Нельзя добавить себя в друзья");
        }
        dbUserStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id:" + userId + " не найден"));
        dbUserStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id:" + friendId + " не найден"));
    }

    private void validateUsersInMemory(long userId, long friendId) {
        if (userId == friendId) {
            throw new IncorrectParameterException("Нельзя добавить себя в друзья");
        }
        inMemoryUserStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id:" + userId + " не найден"));
        inMemoryUserStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id:" + friendId + " не найден"));
    }
}


