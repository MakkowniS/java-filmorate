package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dal.mappers.GenreRowMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@Primary
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    private final GenreRowMapper genreRowMapper;

    private static final String FIND_ALL_QUERY = """
            SELECT
                f.id,
                f.name,
                f.description,
                f.release_date,
                f.duration,
                m.id AS mpa_id,
                m.rating AS mpa_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_rating = m.id
            """;

    private static final String FIND_BY_ID_QUERY = FIND_ALL_QUERY + " WHERE f.id = ?";

    private static final String INSERT_FILM_QUERY = "INSERT INTO films (name, description, release_date, " +
            "duration, mpa_rating) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_FILM_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?," +
            " duration = ?, mpa_rating = ? WHERE id = ?";

    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE id = ?";

    public FilmDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Film> rowMapper, GenreRowMapper genreRowMapper) {
        super(jdbcTemplate, rowMapper);
        this.genreRowMapper = genreRowMapper;
    }

    @Override
    public Film createFilm(Film film) {
        long id = insert(
                INSERT_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null
        );

        film.setId(id);
        saveGenres(film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        update(
                UPDATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId()
        );
        deleteGenres(film.getId());
        saveGenres(film);
        return film;
    }

    @Override
    public List<Film> getFilms() {
        List<Film> films = findMany(FIND_ALL_QUERY);
        films.forEach(this::loadGenres);
        return films;
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        Optional<Film> film = findOne(FIND_BY_ID_QUERY, id);
        film.ifPresent(this::loadGenres);
        return film;
    }

    @Override
    public List<Film> getFilmsByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        String FIND_BY_ID_LIST_QUERY = """
                SELECT *
                FROM films
                WHERE id IN (%s)
                """.formatted(
                String.join(",", Collections.nCopies(ids.size(), "?"))
        );

        List<Film> films = jdbc.query(FIND_BY_ID_LIST_QUERY, rowMapper, ids.toArray()); // Получаем список фильмов

        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));

        return ids.stream()
                .map(filmMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void deleteFilm(Long id) {
        deleteGenres(id);
        delete(DELETE_FILM_QUERY, id);
    }

    // ===== Genres =====

    private void saveGenres(Film film) {
        if (film.getId() == null || film.getGenres().isEmpty()) {
            return;
        }

        String query = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        jdbc.batchUpdate(query, film.getGenres(), film.getGenres().size(), (ps, genre) -> {
            ps.setLong(1, film.getId());
            ps.setInt(2, genre.getId());
        });
    }

    private void deleteGenres(Long filmId) {
        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", filmId);
    }

    private void loadGenres(Film film) {
        String query = """
                SELECT g.id, g.name
                FROM genres g
                JOIN film_genres fg ON g.id = fg.genre_id
                WHERE fg.film_id = ?
                """;

        List<Genre> genres = jdbc.query(query, genreRowMapper, film.getId());
        film.setGenres(new HashSet<>(genres));
    }
}
