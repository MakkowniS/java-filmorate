package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validation.Marker;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@Validated
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Запрос на получение фильма с id:{}", id);
        return filmService.getFilmById(id);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(required = false, defaultValue = "10") String count) {
        log.info("Запрос на получение списка Топ 10 популярных фильмов");
        return filmService.showTopLikedFilms(Integer.parseInt(count));
    }

    @PostMapping
    public Film createFilm(@Validated({Marker.OnCreate.class}) @RequestBody Film film) {
        log.info("Запрос на добавление нового фильма: {}", film);
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Validated({Marker.OnUpdate.class}) @RequestBody Film newFilm) {
        log.info("Запрос на обновление данных фильма:{}", newFilm);
        return filmService.updateFilm(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film likeFilm(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос на добавление фильму с id:{} лайка от юзера: {}", id, userId);
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{filmId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFilm(@PathVariable Long filmId) {
        log.info("Запрос на удаление фильма с id:{}", filmId);
        filmService.deleteFilmById(filmId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFilmLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос на удаление у фильма с id:{} лайка от юзера: {}", id, userId);
        filmService.removeLike(id, userId);
    }
}
