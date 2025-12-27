package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.storage.FriendshipStatusStorage;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FriendshipStatusDbStorage implements FriendshipStatusStorage {

    private final JdbcTemplate jdbc;

    @Override
    public int getIdByStatus(FriendshipStatus status) {
        String sql = """
            SELECT id
            FROM friendship_status
            WHERE name = ?
        """;

        Integer id = jdbc.queryForObject(sql, Integer.class, status.name());

        if (id == null) {
            throw new NotFoundException("Статус дружбы " + status + " не найден");
        }

        return id;
    }

    @Override
    public Optional<FriendshipStatus> getStatusById(int id) {
        String sql = """
            SELECT name
            FROM friendship_status
            WHERE id = ?
        """;

        try {
            String name = jdbc.queryForObject(sql, String.class, id);
            return Optional.of(FriendshipStatus.valueOf(name));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}

