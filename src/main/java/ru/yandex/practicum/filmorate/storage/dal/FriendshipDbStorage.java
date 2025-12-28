package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.storage.FriendshipStatusStorage;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FriendshipDbStorage implements FriendshipStorage {

    private final JdbcTemplate jdbc;
    private final FriendshipStatusStorage statusStorage;

    @Override
    public void addFriend(long userId, long friendId, FriendshipStatus status) {
        String query = """
                    INSERT INTO friendships (user_id, friend_id, status, created_at)
                    VALUES (?, ?, ?, now())
                """;

        jdbc.update(query,
                userId,
                friendId,
                statusStorage.getIdByStatus(status)
        );
    }

    @Override
    public void updateStatus(long userId, long friendId, FriendshipStatus status) {
        String query = """
                    UPDATE friendships
                    SET status = ?
                    WHERE user_id = ? AND friend_id = ?
                """;

        jdbc.update(query, statusStorage.getIdByStatus(status), userId, friendId);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        jdbc.update(
                "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?",
                userId, friendId
        );
    }

    @Override
    public void deleteAllFriends(long userId) {
        String query = """
                    DELETE FROM friendships
                    WHERE user_id = ? OR friend_id = ?
                """;

        jdbc.update(query, userId, userId);
    }

    @Override
    public List<Long> getFriendsIds(long userId) {
        String query = """
                    SELECT friend_id
                    FROM friendships
                    WHERE user_id = ?
                """;

        return jdbc.queryForList(
                query,
                Long.class,
                userId
        );
    }

    @Override
    public List<Long> getCommonFriends(long userId, long otherUserId) {
        String query = """
                    SELECT f1.friend_id
                    FROM friendships f1
                    JOIN friendships f2 ON f1.friend_id = f2.friend_id
                    WHERE f1.user_id = ?
                      AND f2.user_id = ?
                """;

        return jdbc.queryForList(
                query,
                Long.class,
                userId,
                otherUserId
        );
    }

    @Override
    public boolean existsFriendship(long userId, long friendId) {
        String sql = """
                    SELECT COUNT(*)
                    FROM friendships
                    WHERE user_id = ? AND friend_id = ?
                """;

        Integer count = jdbc.queryForObject(sql, Integer.class, userId, friendId);
        return count != null && count > 0;
    }

}
