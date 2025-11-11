package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        log.info("Запрос на добавление нового фильма: {}", film);
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Некорректное название: {}", film.getName());
            throw new ValidationException("Название не может быть пустым");
        } else if (film.getDescription().length() > 200) {
            log.warn("Некорректное описание: {}", film.getDescription());
            throw new ValidationException("Описание не может быть больше 200 символов");
        } else if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Дата релиза раньше 28.12.1895: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28.12.1895");
        } else if (film.getDuration().toMinutes() < 0) {
            log.warn("Отрицательная продолжительность: {}", film.getDuration());
            throw new ValidationException("Продолжительность не может быть отрицательной");
        }
        film.setId(getNextId());
        log.debug("Валидация пройдена. ID:{} установлен", film.getId());
        films.put(film.getId(), film);
        log.info("Фильм успешно создан ID:{}, Name:{}", film.getId(), film.getName());

        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        log.info("Запрос на обновление данных фильма:{}", newFilm);
        if (newFilm.getId() == null) {
            log.warn("ID не указан");
            throw new ValidationException("ID не может быть пустым");
        }

        Film oldFilm = films.get(newFilm.getId());
        if (oldFilm == null) {
            log.warn("Фильм с ID:{} не найден", newFilm.getId());
            throw new ValidationException("Фильма с ID: " + newFilm.getId() + " не найдено");
        }

        if (newFilm.getName() != null) {
            if (newFilm.getName().isBlank()) {
                log.warn("Некорректное название: {}", newFilm.getName());
                throw new ValidationException("Название не может быть пустым");
            }
            oldFilm.setName(newFilm.getName());
        }
        if (newFilm.getDescription() != null) {
            if (newFilm.getDescription().length() > 200) {
                log.warn("Некорректное описание: {}", newFilm.getDescription());
                throw new ValidationException("Описание не может быть больше 200 символов");
            }
            oldFilm.setDescription(newFilm.getDescription());
        }
        if (newFilm.getReleaseDate() != null) {
            if (newFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                log.warn("Дата релиза раньше 28.12.1895: {}", newFilm.getReleaseDate());
                throw new ValidationException("Дата релиза не может быть раньше 28.12.1895");
            }
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != null) {
            if (newFilm.getDuration().toMinutes() < 0) {
                log.warn("Отрицательная продолжительность: {}", newFilm.getDuration());
                throw new ValidationException("Продолжительность не может быть отрицательной");
            }
            oldFilm.setDuration(newFilm.getDuration());
        }

        return oldFilm;
    }

    private Integer getNextId() {
        int currentId = films.keySet().stream().mapToInt(id -> id).max().orElse(0);
        return ++currentId;
    }
}
