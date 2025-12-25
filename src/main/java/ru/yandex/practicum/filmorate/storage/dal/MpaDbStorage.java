package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbc;

    private static final String FIND_ALL_QUERY = "SELECT id, rating FROM mpa_ratings";
    private static final String FIND_BY_ID_QUERY = "SELECT id, rating FROM mpa_ratings WHERE id = ?";

    private final RowMapper<Mpa> mpaRowMapper = new RowMapper<>() {
        @Override
        public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Mpa(rs.getInt("id"), rs.getString("rating"));
        }
    };

    @Override
    public List<Mpa> getAllMpa() {
        return jdbc.query(FIND_ALL_QUERY, mpaRowMapper);
    }

    @Override
    public Optional<Mpa> getMpaById(int id) {
        try {
            Mpa mpa = jdbc.queryForObject(FIND_BY_ID_QUERY, mpaRowMapper, id);
            return Optional.ofNullable(mpa);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
