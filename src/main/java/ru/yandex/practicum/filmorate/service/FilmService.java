package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film newFilm) {
        try {
            Film storedFilm = filmStorage.getFilmById(newFilm.getId());

            // Проверка содержания ID в списке
            if (storedFilm == null) {
                log.warn("Фильм с ID:{} не найден", newFilm.getId());
                throw new NotFoundException("Фильма с ID: " + newFilm.getId() + " не найдено");
            }

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

            return filmStorage.updateFilm(storedFilm);
        } catch (NullPointerException e) {
            log.warn("Фильм с ID:{} не найден", newFilm.getId());
            throw new NotFoundException("Фильма с ID: " + newFilm.getId() + " не найдено");
        }
    }

    public void deleteFilmById(Long id) {
        Film storedFilm = filmStorage.getFilmById(id);
        if (storedFilm == null) {
            log.warn("Фильм с ID:{} не найден", id);
            throw new NotFoundException("Фильма с ID: " + id + " не найдено");
        }

        filmStorage.deleteFilm(id);
    }

    public Film addLike(Long filmId, Long userId) {
        log.info("Добавление лайка от юзера: {} фильму: {}", userId, filmId);

        Film film = getFilmAndCheckNull(filmId);
        film.getLikedUserIds().add(userId);

        filmStorage.updateFilm(film);
        log.info("Лайк добавлен");
        return film;
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка от юзера: {} у фильма: {}", userId, filmId);

        Film film = getFilmAndCheckNull(filmId);
        film.getLikedUserIds().remove(userId);

        filmStorage.updateFilm(film);
        log.info("Лайк удалён.");
    }

    public List<Film> findTop10LikedFilms(int count) {
        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparingLong((Film f) -> f.getLikedUserIds().size()).reversed())
                .limit(count)
                .toList();
    }

    private Film getFilmAndCheckNull(Long filmId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            log.warn("Указанного ID {} нет в списке фильмов.", filmId);
            throw new NotFoundException("Указанный ID (" + filmId + ")не найден");
        }
        return film;
    }

}
