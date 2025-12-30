package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipStorage friendshipStorage;
    private final UserStorage userStorage;

    public void addFriend(long userId, long friendId) {
        validateUsers(userId, friendId);

        friendshipStorage.addFriend(
                userId,
                friendId,
                FriendshipStatus.NOT_CONFIRMED
        );

        log.info("Добавлена дружба {} -> {}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        validateUsers(userId, friendId);

        if (!friendshipStorage.existsFriendship(userId, friendId)) {
            throw new NotFoundException("Дружба не найдена");
        }

        friendshipStorage.deleteFriend(userId, friendId);
        log.info("Удалена дружба {} -> {}", userId, friendId);
    }

    public void deleteAllFriends(long userId) {
        validateUserExists(userId);
        friendshipStorage.deleteAllFriends(userId);

        log.info("Удалены все дружбы пользователя {}", userId);
    }

    public List<UserDto> getFriends(long userId) {
        validateUserExists(userId);

        List<Long> friendIds = friendshipStorage.getFriendsIds(userId);
        if (friendIds.isEmpty()) {
            return List.of();
        }

        return userStorage.getUsersByIds(friendIds).stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public List<UserDto> getCommonFriends(long userId, long otherUserId) {
        validateUsers(userId, otherUserId);

        List<Long> commonIds =
                friendshipStorage.getCommonFriends(userId, otherUserId);

        if (commonIds.isEmpty()) {
            return List.of();
        }

        return userStorage.getUsersByIds(commonIds).stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    // ===== helpers =====

    private void validateUsers(long userId, long friendId) {
        if (userId == friendId) {
            throw new IncorrectParameterException("Нельзя добавить себя в друзья");
        }
        validateUserExists(userId);
        validateUserExists(friendId);
    }

    private void validateUserExists(long userId) {
        if (!userStorage.isUserExistsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }
}

