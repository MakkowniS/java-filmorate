package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.validation.Marker;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
@Validated
public class FilmController {

    private final InMemoryFilmStorage inMemoryFilmStorage;

    public FilmController(InMemoryFilmStorage inMemoryFilmStorage) {
        this.inMemoryFilmStorage = inMemoryFilmStorage;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return inMemoryFilmStorage.getFilms();
    }

    @PostMapping
    public Film createFilm(@Validated({Marker.OnCreate.class}) @RequestBody Film film) {
        log.info("Запрос на добавление нового фильма: {}", film);
        return inMemoryFilmStorage.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Validated({Marker.OnUpdate.class}) @RequestBody Film newFilm) {
        log.info("Запрос на обновление данных фильма:{}", newFilm);
        return  inMemoryFilmStorage.updateFilm(newFilm);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable int filmId) {
        log.info("Запрос на удаление фильма с id:{}", filmId);
        inMemoryFilmStorage.deleteFilm(filmId);
    }
}
