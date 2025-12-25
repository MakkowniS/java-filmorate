package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film createFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Фильм успешно создан ID:{}, Name:{}", film.getId(), film.getName());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        films.put(film.getId(), film);
        log.info("Фильм с ID:{} успешно обновлён.", film.getId());
        return film;
    }

    @Override
    public void deleteFilm(Long id) {
        if (films.remove(id) == null) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }

        log.debug("Фильм с id:{} удалён.", id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        films.get(filmId).getLikedUserIds().add(userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        films.get(filmId).getLikedUserIds().remove(userId);
    }

    @Override
    public Set<Long> getLikedUserIds(Long filmId) {
        return films.get(filmId).getLikedUserIds();
    }

    @Override
    public List<Film> getTopLikedFilms(int count) {
        return List.of();
    }

    private Long getNextId() {
        long currentId = films.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentId;
    }
}
