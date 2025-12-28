package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.FilmLikesStorage;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FilmLikesDbStorage implements FilmLikesStorage {

    private final JdbcTemplate jdbc;

    @Override
    public void addLike(long filmId, long userId) {
        String query = "INSERT INTO film_likes (film_id, user_id, created_at) VALUES (?, ?, now())";
        jdbc.update(query, filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        String query = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbc.update(query, filmId, userId);
    }

    @Override
    public List<Long> getLikedUserIds(long filmId) {
        String query = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return jdbc.queryForList(query, Long.class, filmId);
    }



    @Override
    public List<Long> getTopLikedFilmIds(int count) {
        String sql = """
            SELECT f.id
            FROM films f
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            GROUP BY f.id
            ORDER BY COUNT(fl.user_id) DESC
            LIMIT ?
        """;
        return jdbc.queryForList(sql, Long.class, count);
    }
}

