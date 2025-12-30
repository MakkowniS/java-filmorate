package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.util.List;

public interface FriendshipStorage {

    void addFriend(long userId, long friendId, FriendshipStatus status);

    void updateStatus(long userId, long friendId, FriendshipStatus status);

    void deleteFriend(long userId, long friendId);

    void deleteAllFriends(long userId);

    List<Long> getFriendsIds(long userId);

    List<Long> getCommonFriends(long userId, long otherUserId);

    boolean existsFriendship(long userId, long friendId);

}