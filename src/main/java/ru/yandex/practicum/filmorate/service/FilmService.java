package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.*;

@Slf4j
@Service
public class FilmService {

    private final UserService userService;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final FilmStorage inMemoryStorage;
    private final FilmStorage dbStorage;

    public FilmService(UserService userService,  GenreStorage genreStorage, MpaStorage mpaStorage,
                       @Qualifier("inMemoryFilmStorage") FilmStorage inMemoryStorage,
                       @Qualifier("filmDbStorage") FilmStorage dbStorage) {
        this.userService = userService;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.inMemoryStorage = inMemoryStorage;
        this.dbStorage = dbStorage;
    }

    // =============== DB Storage ===============

    public FilmDto createFilmInDb(NewFilmRequest request) {
        Mpa mpa = mpaStorage.getMpaById(request.getMpaId())
                .orElseThrow(() -> new NotFoundException("MPA с таким ID не найден"));

        Set<Genre> genres = genreStorage.findManyByIds(request.getGenreIds());
        Film film = FilmMapper.mapToFilm(request, mpa, genres);

        film = dbStorage.createFilm(film);

        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto updateFilmInDb(Long filmId, UpdateFilmRequest request) {
        Film film = dbStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));

        Mpa mpa = null;
        if (request.hasMpa()) {
            mpa = mpaStorage.getMpaById(request.getMpaId())
                    .orElseThrow(() -> new NotFoundException("MPA с ID " + request.getMpaId() + " не найден"));
        }

        Set<Genre> genres = Collections.emptySet();
        if (request.hasGenres()) {
            genres = genreStorage.findManyByIds(request.getGenreIds());
        }

        FilmMapper.updateFilmFields(film, request, mpa, genres);
        film = dbStorage.updateFilm(film);

        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto getFilmByIdFromDb(Long id) {
        Film film = dbStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getAllFilmsFromDb() {
        return dbStorage.getFilms().stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public void deleteFilmByIdFromDb(Long id) {
        dbStorage.deleteFilm(id);
    }


    // =============== InMemory Storage ===============

    public Film createFilmInMemory(Film film) {
        return inMemoryStorage.createFilm(film);
    }

    public Collection<Film> getAllFilmsFromMemory() {
        return inMemoryStorage.getFilms();
    }

    public Film getFilmByIdFromMemory(Long id) {
        return getFilmAndCheckNullInMemory(id);
    }



    public Film updateFilmInMemory(Film newFilm) {
        Film storedFilm = getFilmAndCheckNullInMemory(newFilm.getId());

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

    public void deleteFilmByIdInMemory(Long id) {
        getFilmAndCheckNullInMemory(id);

        inMemoryStorage.deleteFilm(id);
    }

    // =============== Likes ===============

    public void addLikeInDb(Long filmId, Long userId) {
        log.info("Добавление лайка: filmId={}, userId={}", filmId, userId);

        getFilmAndCheckNullInDb(filmId);
        userService.getUserById(userId);

        dbStorage.addLike(filmId, userId);
    }

    public void removeLikeInDb(Long filmId, Long userId) {
        log.info("Удаление лайка: filmId={}, userId={}", filmId, userId);

        getFilmAndCheckNullInDb(filmId);
        userService.getUserById(userId);

        dbStorage.removeLike(filmId, userId);
    }

    public void addLikeInMemory(Long filmId, Long userId) {
        log.info("Добавление лайка от юзера: {} фильму: {}", userId, filmId);

        userService.getUserAndCheckNullInMemory(userId);
        getFilmAndCheckNullInMemory(filmId);
        inMemoryStorage.addLike(filmId, userId);
        log.info("Лайк добавлен");
    }

    public void removeLikeInMemory(Long filmId, Long userId) {
        log.info("Удаление лайка от юзера: {} у фильма: {}", userId, filmId);

        getFilmAndCheckNullInMemory(filmId);
        inMemoryStorage.removeLike(filmId, userId);

        log.info("Лайк удалён.");
    }

    // =============== Top Likes ===============

    public List<FilmDto> showTopLikedFilmsInDb(int count) {
        if (count <= 0) {
            log.warn("Неверное значение параметра Count: {}", count);
            throw new IncorrectParameterException("count должен быть > 0");
        }
        return dbStorage.getTopLikedFilms(count).stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public List<Film> showTopLikedFilmsInMemory(int count) {
        if (count <= 0) {
            log.warn("Неверное значение параметра Count: {}", count);
            throw new IncorrectParameterException("count должен быть > 0");
        }
        return inMemoryStorage.getFilms().stream()
                .sorted(Comparator.comparingLong((Film f) -> f.getLikedUserIds().size()).reversed())
                .limit(count)
                .toList();
    }

    // ================= Helpers =================

    private Film getFilmAndCheckNullInMemory(Long id) {
        return inMemoryStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    private Film getFilmAndCheckNullInDb(Long id) {
        return dbStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }
}
