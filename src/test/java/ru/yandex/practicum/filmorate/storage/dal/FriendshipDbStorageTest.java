package ru.yandex.practicum.filmorate.storage.dal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(FriendshipDbStorageTest.TestConfig.class)
class FriendshipDbStorageTest {

    @Autowired
    private FriendshipDbStorage friendshipDbStorage;

    @Autowired
    private FriendshipStatusDbStorage statusStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private long userId1;
    private long userId2;
    private long userId3;

    @BeforeEach
    void setUp() {
        // Чистим таблицы
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM friendship_status");

        // Добавляем статусы дружбы
        jdbcTemplate.update("INSERT INTO friendship_status(name) VALUES (?)", "NOT_CONFIRMED");
        jdbcTemplate.update("INSERT INTO friendship_status(name) VALUES (?)", "CONFIRMED");

        // Добавляем пользователей
        jdbcTemplate.update("INSERT INTO users(email, login, name, birthday) VALUES ('a@mail.com','user1','User 1','1990-01-01')");
        jdbcTemplate.update("INSERT INTO users(email, login, name, birthday) VALUES ('b@mail.com','user2','User 2','1991-02-02')");
        jdbcTemplate.update("INSERT INTO users(email, login, name, birthday) VALUES ('c@mail.com','user3','User 3','1992-03-03')");

        // Получаем их id
        List<Long> ids = jdbcTemplate.queryForList("SELECT id FROM users ORDER BY id", Long.class);
        userId1 = ids.get(0);
        userId2 = ids.get(1);
        userId3 = ids.get(2);
    }

    @Test
    void shouldAddAndGetFriends() {
        friendshipDbStorage.addFriend(userId1, userId2, FriendshipStatus.NOT_CONFIRMED);
        friendshipDbStorage.addFriend(userId1, userId3, FriendshipStatus.CONFIRMED);

        List<Long> friends = friendshipDbStorage.getFriendsIds(userId1);
        assertThat(friends).containsExactlyInAnyOrder(userId2, userId3);
    }

    @Test
    void shouldUpdateStatus() {
        friendshipDbStorage.addFriend(userId1, userId2, FriendshipStatus.NOT_CONFIRMED);
        friendshipDbStorage.updateStatus(userId1, userId2, FriendshipStatus.CONFIRMED);

        // Проверяем напрямую из БД
        Integer statusId = jdbcTemplate.queryForObject(
                "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?",
                Integer.class,
                userId1, userId2
        );
        int confirmedId = statusStorage.getIdByStatus(FriendshipStatus.CONFIRMED);
        assertThat(statusId).isEqualTo(confirmedId);
    }

    @Test
    void shouldDeleteFriend() {
        friendshipDbStorage.addFriend(userId1, userId2, FriendshipStatus.NOT_CONFIRMED);
        friendshipDbStorage.deleteFriend(userId1, userId2);

        List<Long> friends = friendshipDbStorage.getFriendsIds(userId1);
        assertThat(friends).isEmpty();
    }

    @Test
    void shouldDeleteAllFriends() {
        friendshipDbStorage.addFriend(userId1, userId2, FriendshipStatus.NOT_CONFIRMED);
        friendshipDbStorage.addFriend(userId3, userId1, FriendshipStatus.CONFIRMED);

        friendshipDbStorage.deleteAllFriends(userId1);

        List<Long> friends1 = friendshipDbStorage.getFriendsIds(userId1);
        List<Long> friends2 = friendshipDbStorage.getFriendsIds(userId3);

        assertThat(friends1).isEmpty();
        assertThat(friends2).isEmpty();
    }

    @Test
    void shouldGetCommonFriends() {
        friendshipDbStorage.addFriend(userId1, userId2, FriendshipStatus.CONFIRMED);
        friendshipDbStorage.addFriend(userId1, userId3, FriendshipStatus.CONFIRMED);
        friendshipDbStorage.addFriend(userId2, userId3, FriendshipStatus.CONFIRMED);

        List<Long> common = friendshipDbStorage.getCommonFriends(userId1, userId2);
        assertThat(common).containsExactly(userId3);
    }

    @Test
    void shouldCheckExistsFriendship() {
        friendshipDbStorage.addFriend(userId1, userId2, FriendshipStatus.NOT_CONFIRMED);

        assertThat(friendshipDbStorage.existsFriendship(userId1, userId2)).isTrue();
        assertThat(friendshipDbStorage.existsFriendship(userId2, userId1)).isFalse();
    }

    // ===== Конфиг =====
    @TestConfiguration
    static class TestConfig {

        @Bean
        FriendshipStatusDbStorage friendshipStatusDbStorage(JdbcTemplate jdbcTemplate) {
            return new FriendshipStatusDbStorage(jdbcTemplate);
        }

        @Bean
        FriendshipDbStorage friendshipDbStorage(JdbcTemplate jdbcTemplate, FriendshipStatusDbStorage statusStorage) {
            return new FriendshipDbStorage(jdbcTemplate, statusStorage);
        }
    }
}
