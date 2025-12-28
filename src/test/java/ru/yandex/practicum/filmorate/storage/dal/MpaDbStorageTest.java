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
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dal.mappers.MpaRowMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorageTest.TestConfig.class)
class MpaDbStorageTest {

    @Autowired
    private MpaDbStorage mpaDbStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private int mpaId1;
    private int mpaId2;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM mpa_ratings");

        jdbcTemplate.update("INSERT INTO mpa_ratings (rating) VALUES ('G')");
        jdbcTemplate.update("INSERT INTO mpa_ratings (rating) VALUES ('PG')");

        // Получаем id для проверок
        List<Integer> ids = jdbcTemplate.queryForList("SELECT id FROM mpa_ratings ORDER BY id", Integer.class);
        mpaId1 = ids.get(0);
        mpaId2 = ids.get(1);
    }

    @Test
    void shouldGetAllMpa() {
        List<Mpa> allMpa = mpaDbStorage.getAllMpa();

        assertThat(allMpa)
                .hasSize(2)
                .extracting(Mpa::getName)
                .containsExactly("G", "PG");
    }

    @Test
    void shouldGetMpaById() {
        Optional<Mpa> mpa1 = mpaDbStorage.getMpaById(mpaId1);
        Optional<Mpa> mpa2 = mpaDbStorage.getMpaById(mpaId2);

        assertThat(mpa1).isPresent().hasValueSatisfying(m -> assertThat(m.getName()).isEqualTo("G"));
        assertThat(mpa2).isPresent().hasValueSatisfying(m -> assertThat(m.getName()).isEqualTo("PG"));
    }

    // ===== Конфиг =====
    @TestConfiguration
    static class TestConfig {

        @Bean
        MpaRowMapper mpaRowMapper() {
            return new MpaRowMapper();
        }

        @Bean
        MpaDbStorage mpaDbStorage(JdbcTemplate jdbcTemplate, MpaRowMapper rowMapper) {
            return new MpaDbStorage(jdbcTemplate, rowMapper);
        }
    }
}
