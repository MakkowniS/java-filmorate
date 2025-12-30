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
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dal.mappers.GenreRowMapper;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(FilmLikesDbStorageTest.TestConfig.class)
class FilmLikesDbStorageTest {

    @Autowired
    private FilmLikesDbStorage likesStorage;

    @Autowired
    private UserDbStorage userDbStorage;

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long filmId;
    private Long userId1;
    private Long userId2;

    @BeforeEach
    void setUp() {
        // Чистим таблицы
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM mpa_ratings");

        // Создаём MPA
        jdbcTemplate.update("INSERT INTO mpa_ratings (rating) VALUES ('PG-13')");
        Integer mpaId = jdbcTemplate.queryForObject("SELECT id FROM mpa_ratings WHERE rating='PG-13'", Integer.class);

        // Создаём фильм
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2020,1,1));
        film.setDuration(120);
        Mpa mpa = new Mpa();
        mpa.setId(mpaId);
        film.setMpa(mpa);

        Film savedFilm = filmDbStorage.createFilm(film);
        filmId = savedFilm.getId();

        // Создаём пользователей
        User user1 = new User();
        user1.setEmail("user1@mail.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990,1,1));
        User savedUser1 = userDbStorage.addUser(user1);
        userId1 = savedUser1.getId();

        User user2 = new User();
        user2.setEmail("user2@mail.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1995,5,5));
        User savedUser2 = userDbStorage.addUser(user2);
        userId2 = savedUser2.getId();
    }

    @Test
    void shouldAddAndRemoveLike() {
        likesStorage.addLike(filmId, userId1);
        likesStorage.addLike(filmId, userId2);

        List<Long> likedUsers = likesStorage.getLikedUserIds(filmId);
        assertThat(likedUsers).containsExactlyInAnyOrder(userId1, userId2);

        likesStorage.removeLike(filmId, userId1);

        likedUsers = likesStorage.getLikedUserIds(filmId);
        assertThat(likedUsers).containsExactly(userId2);
    }

    @Test
    void shouldReturnAllUsersWhoLikedFilm() {
        // Добавляем лайки
        likesStorage.addLike(filmId, userId1);
        likesStorage.addLike(filmId, userId2);

        // Получаем список, кто лайкнул фильм
        List<Long> likedUserIds = likesStorage.getLikedUserIds(filmId);
        // Проверяем, что список содержит эти id
        assertThat(likedUserIds)
                .hasSize(2)
                .containsExactlyInAnyOrder(userId1, userId2);

        // Удаляем один лайк и проверяем
        likesStorage.removeLike(filmId, userId1);

        likedUserIds = likesStorage.getLikedUserIds(filmId);

        assertThat(likedUserIds)
                .hasSize(1)
                .containsExactly(userId2);
    }

    // ===== Конфиг =====
    @TestConfiguration
    static class TestConfig {

        @Bean
        FilmLikesDbStorage likesStorage(JdbcTemplate jdbcTemplate) {
            return new FilmLikesDbStorage(jdbcTemplate);
        }

        @Bean
        UserDbStorage userDbStorage(JdbcTemplate jdbcTemplate, RowMapper<User> userRowMapper) {
            return new UserDbStorage(jdbcTemplate, userRowMapper);
        }

        @Bean
        FilmDbStorage filmDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Film> filmRowMapper, GenreRowMapper genreRowMapper) {
            return new FilmDbStorage(jdbcTemplate, filmRowMapper, genreRowMapper);
        }

        @Bean
        RowMapper<User> userRowMapper() {
            return (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setEmail(rs.getString("email"));
                user.setLogin(rs.getString("login"));
                user.setName(rs.getString("name"));

                Date birthday = rs.getDate("birthday");
                user.setBirthday(birthday != null ? birthday.toLocalDate() : null);

                return user;
            };
        }

        @Bean
        RowMapper<Film> filmRowMapper() {
            return (rs, rowNum) -> {
                Film film = new Film();

                film.setId(rs.getLong("id"));
                film.setName(rs.getString("name"));
                film.setDescription(rs.getString("description"));
                film.setDuration(rs.getInt("duration"));

                Date releaseDate = rs.getDate("release_date");
                if (releaseDate != null) {
                    film.setReleaseDate(releaseDate.toLocalDate());
                }

                Integer mpaId = rs.getObject("mpa_id", Integer.class);
                if (mpaId != null) {
                    film.setMpa(new Mpa(mpaId, rs.getString("mpa_name")
                    ));
                }
                return film;
            };
        }

        @Bean
        GenreRowMapper genreRowMapper() {
            return new GenreRowMapper();
        }
    }
}
