package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    // ===== Films =====

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto createFilm(@Valid @RequestBody NewFilmRequest request) {
        log.info("Запрос на добавление нового фильма: {}", request);
        return filmService.createFilmInDb(request);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public FilmDto updateFilm(@Valid @RequestBody UpdateFilmRequest request) {
        log.info("Запрос на обновление данных фильма");
        return filmService.updateFilmInDb(request);
    }

    @GetMapping("/{filmId}")
    @ResponseStatus(HttpStatus.OK)
    public FilmDto getFilmById(@PathVariable Long filmId) {
        log.info("Запрос на получение фильма с id:{}", filmId);
        return filmService.getFilmByIdInDb(filmId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<FilmDto> getFilms() {
        return filmService.getAllFilmsInDb();
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<FilmDto> getPopularFilms(
            @RequestParam(required = false, defaultValue = "10") String count) {
        log.info("Запрос на получение списка Топ 10 популярных фильмов");
        return filmService.showTopLikedFilmsInDb(Integer.parseInt(count));
    }

    @DeleteMapping("/{filmId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFilm(@PathVariable Long filmId) {
        log.info("Запрос на удаление фильма с id:{}", filmId);
        filmService.deleteFilmByIdInDb(filmId);
    }

    // ===== Likes =====

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void likeFilm(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос на добавление фильму с id:{} лайка от юзера: {}", id, userId);
        filmService.addLikeInDb(id, userId);
    }

    @GetMapping("/{filmId}/likes")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getLikedUsers(@PathVariable Long filmId) {
        log.info("Запрос на получение списка пользователей лайкнувших фильм с id:{}", filmId);
        return filmService.getLikedUsersInDb(filmId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeFilmLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос на удаление у фильма с id:{} лайка от юзера: {}", id, userId);
        filmService.removeLikeInDb(id, userId);
    }
}
