package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

@Repository
public class FilmDbStorage {

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Film getFilmById(Long id) {
        return films.get(id);
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
}
