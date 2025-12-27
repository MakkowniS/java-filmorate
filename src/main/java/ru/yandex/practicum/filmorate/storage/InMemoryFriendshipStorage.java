package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.util.*;

@Slf4j
@Component
public class InMemoryFriendshipStorage implements FriendshipStorage {

    // userID - (FriendId - Status)
    private final Map<Long, Map<Long, FriendshipStatus>> friendships = new HashMap<>();

    @Override
    public void addFriend(long userId, long friendId, FriendshipStatus status) {
        friendships
                .computeIfAbsent(userId, id -> new HashMap<>())
                .put(friendId, status);

        log.debug("Добавлена дружба {} -> {} со статусом {}", userId, friendId, status);
    }

    @Override
    public void updateStatus(long userId, long friendId, FriendshipStatus status) {
        Map<Long, FriendshipStatus> userFriends = friendships.get(userId);

        if (userFriends == null || !userFriends.containsKey(friendId)) {
            throw new NotFoundException("Дружба не найдена");
        }

        userFriends.put(friendId, status);
        log.debug("Обновлён статус дружбы {} -> {} на {}", userId, friendId, status);
    }

    // Удаление связи user - friend
    @Override
    public void deleteFriend(long userId, long friendId) {
        Map<Long, FriendshipStatus> userFriends = friendships.get(userId);

        if (userFriends != null) {
            userFriends.remove(friendId);
            log.debug("Удалена дружба {} -> {}", userId, friendId);

            if (userFriends.isEmpty()) {
                friendships.remove(userId);
            }
        }
    }

    @Override
    public void deleteAllFriends(long userId) {

        // Удаляем все связи user - *
        friendships.remove(userId);
        // Удаляем все связи * - user
        friendships.values()
                .forEach(friendsMap -> friendsMap.remove(userId));

        log.debug("Удалены все дружеские связи пользователя {}", userId);
    }


    // Список ID друзей пользователя
    @Override
    public List<Long> getFriendsIds(long userId) {
        return friendships.getOrDefault(userId, Map.of()).entrySet().stream()
                .filter(e -> e.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .toList();
    }

    // Пересечение двух множеств
    @Override
    public List<Long> getCommonFriends(long userId, long otherUserId) {
        Set<Long> first = friendships.getOrDefault(userId, Map.of()).keySet();
        Set<Long> second = friendships.getOrDefault(otherUserId, Map.of()).keySet();

        return first.stream()
                .filter(second::contains)
                .toList();
    }

    @Override
    public boolean existsFriendship(long userId, long friendId) {
        return friendships.containsKey(userId)
                && friendships.get(userId).containsKey(friendId);
    }
}

