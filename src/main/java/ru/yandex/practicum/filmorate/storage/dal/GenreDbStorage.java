package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.dal.mappers.GenreRowMapper;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class GenreDbStorage extends BaseDbStorage<Genre> implements GenreStorage {

    public GenreDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Genre>  rowMapper) {
        super(jdbcTemplate, rowMapper);
    }

    public Genre save(Genre genre) {
        long id = insert("INSERT IN TO genres(name) VALUES (?) returning id()",
                genre.getName()
                );
        genre.setId((int) id);
        return genre;
    }

    public void delete(int id) {
        delete("DELETE FROM genre WHERE id = ?", id);
    }

    public List<Genre> findAll() {
        return findMany("SELECT id, name FROM genres");
    }

    public Optional<Genre> findById(Integer id) {
        return findOne("SELECT id, name FROM genres WHERE id = ?", id);
    }

    public Set<Genre> findManyByIds(Set<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return Collections.emptySet();
        }

        String query = "SELECT id, name FROM genres WHERE id IN (" +
                       genreIds.stream().map(id -> "?").collect(Collectors.joining(",")) +
                       ")";
        List<Genre> genres = jdbc.query(query, genreIds.toArray(), rowMapper);

        return new HashSet<>(genres);
    }
}
