package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.dal.mappers.MpaRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbc;
    private final MpaRowMapper rowMapper;

    public MpaDbStorage(JdbcTemplate jdbc, MpaRowMapper rowMapper) {
        this.jdbc = jdbc;
        this.rowMapper = rowMapper;
    }

    private static final String FIND_ALL_QUERY = "SELECT id, rating FROM mpa_ratings";
    private static final String FIND_BY_ID_QUERY = "SELECT id, rating FROM mpa_ratings WHERE id = ?";

    @Override
    public List<Mpa> getAllMpa() {
        return jdbc.query(FIND_ALL_QUERY, rowMapper);
    }

    @Override
    public Optional<Mpa> getMpaById(int id) {
        try {
            Mpa mpa = jdbc.queryForObject(FIND_BY_ID_QUERY, rowMapper, id);
            return Optional.ofNullable(mpa);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
