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
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dal.mappers.GenreRowMapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorageTest.TestConfig.class)
class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreDbStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM genres");
    }

    @Test
    void shouldSaveGenre() {
        Genre genre = new Genre();
        genre.setName("Комедия");

        Genre saved = genreDbStorage.save(genre);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Комедия");
    }

    @Test
    void shouldGetGenreById() {
        Genre genre = new Genre();
        genre.setName("Экшн");
        Genre saved = genreDbStorage.save(genre);

        Optional<Genre> found = genreDbStorage.getGenreById(saved.getId());
        assertThat(found).isPresent()
                .hasValueSatisfying(g -> assertThat(g.getName()).isEqualTo("Экшн"));
    }

    @Test
    void shouldGetAllGenres() {
        genreDbStorage.save(new Genre(null, "Хоррор"));
        genreDbStorage.save(new Genre(null, "Драма"));

        List<Genre> genres = genreDbStorage.getAllGenres();

        assertThat(genres).hasSize(2)
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder("Хоррор", "Драма");
    }

    @Test
    void shouldGetManyGenresByIds() {
        Genre g1 = genreDbStorage.save(new Genre(null, "Фантастика"));
        Genre g2 = genreDbStorage.save(new Genre(null, "Триллер"));

        Set<Genre> genres = genreDbStorage.getManyGenresByIds(Set.of(g1.getId(), g2.getId()));

        assertThat(genres).hasSize(2)
                .extracting(Genre::getName)
                .containsExactly("Фантастика", "Триллер");
    }

    @Test
    void shouldDeleteGenre() {
        Genre genre = genreDbStorage.save(new Genre(null, "Романтика"));
        genreDbStorage.delete(genre.getId());

        Optional<Genre> found = genreDbStorage.getGenreById(genre.getId());
        assertThat(found).isEmpty();
    }

    // ===== TestConfiguration =====
    @TestConfiguration
    static class TestConfig {

        @Bean
        GenreRowMapper genreRowMapper() {
            return new GenreRowMapper();
        }

        @Bean
        GenreDbStorage genreDbStorage(JdbcTemplate jdbcTemplate, GenreRowMapper rowMapper) {
            return new GenreDbStorage(jdbcTemplate, rowMapper);
        }
    }
}
