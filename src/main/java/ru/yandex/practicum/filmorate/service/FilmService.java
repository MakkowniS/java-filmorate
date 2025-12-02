package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final UserService userService;
    private final FilmStorage filmStorage;

    public Collection<Film> getAllFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(Long id) {
        return getFilmAndCheckNull(id);
    }

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film newFilm) {
        Film storedFilm = getFilmAndCheckNull(newFilm.getId());

        // Проверка наличия Name в запросе
        if (newFilm.getName() != null) {
            if (!newFilm.getName().isBlank()) {
                storedFilm.setName(newFilm.getName());
                log.debug("Название обновлено. Name:{}", newFilm.getName());
            } else { // Если передан пустой Name (" ")
                throw new ValidationException("Название не может быть пустым");
            }
        }

        // Проверка наличия Description в запросе
        if (newFilm.getDescription() != null) {
            storedFilm.setDescription(newFilm.getDescription());
            log.debug("Описание обновлено. Description:{}", newFilm.getDescription());
        }

        // Проверка наличия ReleaseDate в запросе
        if (newFilm.getReleaseDate() != null) {
            storedFilm.setReleaseDate(newFilm.getReleaseDate());
            log.debug("Дата релиза обновлена. ReleaseDate:{}", newFilm.getReleaseDate());
        }

        // Проверка наличия Duration в запросе
        if (newFilm.getDuration() != null) {
            storedFilm.setDuration(newFilm.getDuration());
            log.debug("Продолжительность обновлена. Duration:{}", newFilm.getDuration());
        }

        return storedFilm;
    }

    public void deleteFilmById(Long id) {
        getFilmAndCheckNull(id);

        filmStorage.deleteFilm(id);
    }

    public Film addLike(Long filmId, Long userId) {
        log.info("Добавление лайка от юзера: {} фильму: {}", userId, filmId);

        userService.getUserAndCheckNull(userId);
        Film film = getFilmAndCheckNull(filmId);
        film.getLikedUserIds().add(userId);

        log.info("Лайк добавлен");
        return film;
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка от юзера: {} у фильма: {}", userId, filmId);

        Film film = getFilmAndCheckNull(filmId);
        Set<Long> filmLikedUserIds = film.getLikedUserIds();
        if (!filmLikedUserIds.contains(userId)) {
            log.warn("Юзера с id: {} в списке лайков не найдено.", userId);
            throw new NotFoundException("Юзера с ID: " + userId + " нет в списке лайков.");
        }

        filmLikedUserIds.remove(userId);
        log.info("Лайк удалён.");
    }

    public List<Film> showTopLikedFilms(int count) {
        if (count <= 0) {
            log.warn("Неверное значение параметра Count: {}", count);
            throw new IncorrectParameterException("Значение параметра count не может быть меньше или равно нулю.");
        }
        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparingLong((Film f) -> f.getLikedUserIds().size()).reversed())
                .limit(count)
                .toList();
    }

    private Film getFilmAndCheckNull(Long filmId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            log.warn("Фильм с ID:{} не найден", filmId);
            throw new NotFoundException("Указанный ID (" + filmId + ")не найден");
        }
        return film;
    }

}
