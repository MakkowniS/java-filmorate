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
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dal.mappers.GenreRowMapper;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(FilmDbStorageTest.TestConfig.class)
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Autowired
    private UserDbStorage userDbStorage;

    @Autowired
    private FilmLikesDbStorage filmLikesDbStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Integer mpaId;
    private Integer genreId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM genres");
        jdbcTemplate.update("DELETE FROM mpa_ratings");

        jdbcTemplate.update(
                "INSERT INTO mpa_ratings (rating) VALUES ('PG-13')"
        );
        mpaId = jdbcTemplate.queryForObject(
                "SELECT id FROM mpa_ratings WHERE rating = 'PG-13'",
                Integer.class
        );

        jdbcTemplate.update(
                "INSERT INTO genres (name) VALUES ('Comedy')"
        );
        genreId = jdbcTemplate.queryForObject(
                "SELECT id FROM genres WHERE name = 'Comedy'",
                Integer.class
        );
    }

    @Test
    void shouldCreateFilm() {
        Film film = createFilm();

        Film savedFilm = filmDbStorage.createFilm(film);

        assertThat(savedFilm.getId()).isNotNull();

        Optional<Film> filmFromDb = filmDbStorage.getFilmById(savedFilm.getId());

        assertThat(filmFromDb)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getName()).isEqualTo("Test Film");
                    assertThat(f.getGenres()).hasSize(1);
                });
    }

    @Test
    void shouldFindFilmById() {
        Film savedFilm = filmDbStorage.createFilm(createFilm());

        Optional<Film> filmOptional = filmDbStorage.getFilmById(savedFilm.getId());

        assertThat(filmOptional).isPresent();
    }

    @Test
    void shouldReturnAllFilms() {
        filmDbStorage.createFilm(createFilm());
        filmDbStorage.createFilm(createFilm("Film 2"));

        List<Film> films = filmDbStorage.getFilms();

        assertThat(films).hasSize(2);
    }

    @Test
    void shouldReturnTopLikedFilmIds() {
        // Создаём пользователей
        User user1 = new User();
        user1.setEmail("user1@mail.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990,1,1));
        User savedUser1 = userDbStorage.addUser(user1);
        Long userId1 = savedUser1.getId();

        User user2 = new User();
        user2.setEmail("user2@mail.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1995,5,5));
        User savedUser2 = userDbStorage.addUser(user2);
        Long userId2 = savedUser2.getId();

        // Создаём три фильма
        Film film1 = filmDbStorage.createFilm(createFilm("Film 1"));
        Film film2 = filmDbStorage.createFilm(createFilm("Film 2"));
        Film film3 = filmDbStorage.createFilm(createFilm("Film 3"));

        // Добавляем лайки (через filmLikesStorage)
        filmLikesDbStorage.addLike(film1.getId(), 1L); // 1 лайк
        filmLikesDbStorage.addLike(film1.getId(), 2L); // 2 лайка
        filmLikesDbStorage.addLike(film2.getId(), 1L); // 1 лайк
        // film3 не лайкаем (0 лайков)

        // Получаем топ-3 фильмов
        List<Film> popularFilms = filmDbStorage.getPopular(3);

        // Проверяем порядок: film1 (2 лайка), film2 (1 лайк), film3 (0 лайков)
        assertThat(popularFilms)
                .hasSize(3)
                .extracting(Film::getId)
                .containsExactly(film1.getId(), film2.getId(), film3.getId());
    }


    @Test
    void shouldUpdateFilm() {
        Film savedFilm = filmDbStorage.createFilm(createFilm());

        savedFilm.setName("Updated Film");
        savedFilm.setDuration(200);

        filmDbStorage.updateFilm(savedFilm);

        Film updatedFilm = filmDbStorage.getFilmById(savedFilm.getId()).orElseThrow();

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
        assertThat(updatedFilm.getDuration()).isEqualTo(200);
    }

    @Test
    void shouldDeleteFilm() {
        Film savedFilm = filmDbStorage.createFilm(createFilm());

        filmDbStorage.deleteFilm(savedFilm.getId());

        Optional<Film> filmOptional = filmDbStorage.getFilmById(savedFilm.getId());

        assertThat(filmOptional).isEmpty();
    }

    // ===== Хелперы =====

    private Film createFilm() {
        return createFilm("Test Film");
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(mpaId);
        film.setMpa(mpa);

        Genre genre = new Genre();
        genre.setId(genreId);
        film.setGenres(Set.of(genre));

        return film;
    }

    // ===== Конфиг =====

    @TestConfiguration
    static class TestConfig {

        @Bean
        UserDbStorage userDbStorage(JdbcTemplate jdbcTemplate, RowMapper<User> userRowMapper) {
            return new UserDbStorage(jdbcTemplate, userRowMapper);
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
        FilmLikesDbStorage filmLikesDbStorage(JdbcTemplate jdbcTemplate) {
            return new FilmLikesDbStorage(jdbcTemplate);
        }

        @Bean
        FilmDbStorage filmDbStorage(
                JdbcTemplate jdbcTemplate,
                RowMapper<Film> filmRowMapper,
                GenreRowMapper genreRowMapper
        ) {
            return new FilmDbStorage(jdbcTemplate, filmRowMapper, genreRowMapper);
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
