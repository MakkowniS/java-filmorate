package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getFilms() {
        return films.values();
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
        Film oldFilm = films.get(newFilm.getId());

        // Проверка содержания ID в списке
        if (oldFilm == null) {
            log.warn("Фильм с ID:{} не найден", newFilm.getId());
            throw new ValidationException("Фильма с ID: " + newFilm.getId() + " не найдено");
        }

        // Проверка наличия Name в запросе
        if (newFilm.getName() != null) {
            if (!newFilm.getName().isBlank()) {
                oldFilm.setName(newFilm.getName());
                log.debug("Название обновлено. Name:{}", newFilm.getName());
            } else { // Если передан пустой Name (" ")
                throw new ValidationException("Название не может быть пустым");
            }
        }

        // Проверка наличия Description в запросе
        if (newFilm.getDescription() != null) {
            oldFilm.setDescription(newFilm.getDescription());
            log.debug("Описание обновлено. Description:{}", newFilm.getDescription());
        }

        // Проверка наличия ReleaseDate в запросе
        if (newFilm.getReleaseDate() != null) {
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            log.debug("Дата релиза обновлена. ReleaseDate:{}", newFilm.getReleaseDate());
        }

        // Проверка наличия Duration в запросе
        if (newFilm.getDuration() != null) {
            oldFilm.setDuration(newFilm.getDuration());
            log.debug("Продолжительность обновлена. Duration:{}", newFilm.getDuration());
        }

        log.info("Фильм с ID:{} успешно обновлён. ", oldFilm.getId());
        return oldFilm;
    }

    @Override
    public void deleteFilm(int id) {
        if (films.containsKey(id)) {
            films.remove(id);
            log.debug("Фильм с id: {} удалён.", id);
        } else {
            log.warn("Фильм с id: {} не найден.", id);
            throw new IncorrectParameterException(Integer.toString(id), "Фильм с указанным id не найден.");
        }
    }

    private Integer getNextId() {
        int currentId = films.keySet().stream().mapToInt(id -> id).max().orElse(0);
        return ++currentId;
    }
}
