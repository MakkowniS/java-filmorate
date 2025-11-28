package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Film getFilmById(Long id) {
        return new Film(films.get(id));
    }

    @Override
    public Film createFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно создан ID:{}, Name:{}", film.getId(), film.getName());

        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм с ID:{} успешно обновлён. ", newFilm.getId());

        return newFilm;
    }

    @Override
    public void deleteFilm(Long id) {
        films.remove(id);
        log.debug("Фильм с id: {} удалён.", id);
    }

    private Long getNextId() {
        long currentId = films.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentId;
    }
}
