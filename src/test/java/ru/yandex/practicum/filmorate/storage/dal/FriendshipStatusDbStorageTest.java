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
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import(FriendshipStatusDbStorageTest.TestConfig.class)
class FriendshipStatusDbStorageTest {

    @Autowired
    private FriendshipStatusDbStorage friendshipStatusDbStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Чистим таблицу
        jdbcTemplate.update("DELETE FROM friendship_status");

        // Добавляем тестовые статусы
        jdbcTemplate.update("INSERT INTO friendship_status(name) VALUES (?)", "NOT_CONFIRMED");
        jdbcTemplate.update("INSERT INTO friendship_status(name) VALUES (?)", "CONFIRMED");
    }

    @Test
    void shouldGetIdByStatus() {
        int pendingId = friendshipStatusDbStorage.getIdByStatus(FriendshipStatus.NOT_CONFIRMED);
        int confirmedId = friendshipStatusDbStorage.getIdByStatus(FriendshipStatus.CONFIRMED);

        assertThat(pendingId).isPositive();
        assertThat(confirmedId).isPositive();

        assertThatThrownBy(() -> friendshipStatusDbStorage.getIdByStatus(FriendshipStatus.DECLINED))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Статус дружбы DECLINED не найден");
    }

    @Test
    void shouldGetStatusById() {
        int pendingId = friendshipStatusDbStorage.getIdByStatus(FriendshipStatus.NOT_CONFIRMED);

        Optional<FriendshipStatus> status = friendshipStatusDbStorage.getStatusById(pendingId);
        assertThat(status).isPresent().contains(FriendshipStatus.NOT_CONFIRMED);

        Optional<FriendshipStatus> nonExistent = friendshipStatusDbStorage.getStatusById(999);
        assertThat(nonExistent).isEmpty();
    }

    // ===== TestConfiguration =====
    @TestConfiguration
    static class TestConfig {

        @Bean
        FriendshipStatusDbStorage friendshipStatusDbStorage(JdbcTemplate jdbcTemplate) {
            return new FriendshipStatusDbStorage(jdbcTemplate);
        }
    }
}
