package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final UserService userService;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final FilmStorage inMemoryStorage;
    private final FilmStorage dbStorage;
    private final FilmLikesStorage inMemoryLikesStorage;
    private final FilmLikesStorage dbLikesStorage;

    public FilmService(
            UserService userService,
            GenreStorage genreStorage,
            MpaStorage mpaStorage,
            @Qualifier("inMemoryFilmLikesStorage") FilmLikesStorage inMemoryLikesStorage,
            @Qualifier("filmLikesDbStorage") FilmLikesStorage dbLikesStorage,
            @Qualifier("inMemoryFilmStorage") FilmStorage inMemoryStorage,
            @Qualifier("filmDbStorage") FilmStorage dbStorage
    ) {
        this.userService = userService;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.inMemoryLikesStorage = inMemoryLikesStorage;
        this.dbLikesStorage = dbLikesStorage;
        this.inMemoryStorage = inMemoryStorage;
        this.dbStorage = dbStorage;
    }

    // =============== DB Storage ===============

    public FilmDto createFilmInDb(NewFilmRequest request) {

        Mpa mpa = mpaStorage.getMpaById(request.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("MPA с таким ID не найден"));

        Set<Integer> requestedGenreIds = request.getGenres().stream()
                .map(GenreDto::getId)
                .collect(Collectors.toSet());
        // Берём жанры из БД
        Set<Genre> genres = genreStorage.getManyGenresByIds(requestedGenreIds);

        // Преобразовываем найденные жанры в ID
        Set<Integer> foundGenreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        // Удаляем из запрошенных все найденные жанры. Если остались в списке, значит не найдены
        requestedGenreIds.removeAll(foundGenreIds);
        if (!requestedGenreIds.isEmpty()) {
            throw new NotFoundException("Жанры не найдены: " + requestedGenreIds);
        }

        Film film = FilmMapper.mapToFilm(request, mpa, genres);
        film = dbStorage.createFilm(film);

        return FilmMapper.mapToFilmDto(film);
    }


    public FilmDto updateFilmInDb(UpdateFilmRequest request) {
        Film film = dbStorage.getFilmById(request.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + request.getId() + " не найден"));

        Mpa mpa = null;
        if (request.hasMpa()) {
            mpa = mpaStorage.getMpaById(request.getMpaId())
                    .orElseThrow(() -> new NotFoundException("MPA с ID " + request.getMpaId() + " не найден"));
        }

        Set<Genre> genres = Collections.emptySet();
        if (request.hasGenres()) {
            genres = genreStorage.getManyGenresByIds(request.getGenreIds());
        }

        FilmMapper.updateFilmFields(film, request, mpa, genres);
        film = dbStorage.updateFilm(film);

        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto getFilmByIdInDb(Long id) {
        Film film = dbStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getAllFilmsInDb() {
        return dbStorage.getFilms().stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public void deleteFilmByIdInDb(Long id) {
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
        getFilmAndCheckNullInDb(filmId);
        userService.getUserByIdInDb(userId);
        dbLikesStorage.addLike(filmId, userId);
        log.info("Лайк поставлен.");

    }

    public List<UserDto> getLikedUsersInDb(Long filmId) {
        getFilmAndCheckNullInDb(filmId);
        List<Long> likedUserIds = dbLikesStorage.getLikedUserIds(filmId);
        return likedUserIds.stream().map(userService::getUserByIdInDb).toList();
    }

    public void removeLikeInDb(Long filmId, Long userId) {
        getFilmAndCheckNullInDb(filmId);
        userService.getUserByIdInDb(userId);
        dbLikesStorage.removeLike(filmId, userId);
        log.info("Лайк удалён.");
    }

    public void addLikeInMemory(Long filmId, Long userId) {
        userService.getUserAndCheckNullInMemory(userId);
        getFilmAndCheckNullInMemory(filmId);
        inMemoryLikesStorage.addLike(filmId, userId);
        log.info("Лайк поставлен.");
    }

    public List<User> getLikedUsersInMemory(Long filmId) {
        getFilmAndCheckNullInMemory(filmId);
        List<Long> likedUserIds = inMemoryLikesStorage.getLikedUserIds(filmId);
        return likedUserIds.stream().map(userService::getUserByIdFromMemory).toList();
    }

    public void removeLikeInMemory(Long filmId, Long userId) {
        getFilmAndCheckNullInMemory(filmId);
        inMemoryLikesStorage.removeLike(filmId, userId);
        log.info("Лайк удалён.");
    }

    // =============== Top Likes ===============

    public List<FilmDto> showTopLikedFilmsInDb(int count) {
        validationCountParameter(count);
        List<Long> topFilmsIds = dbLikesStorage.getTopLikedFilmIds(count);
        return topFilmsIds.stream().map(this::getFilmByIdInDb).toList();
    }

    public List<Film> showTopLikedFilmsInMemory(int count) {
        validationCountParameter(count);
        List<Long> topFilmIds = inMemoryLikesStorage.getTopLikedFilmIds(count);
        return topFilmIds.stream().map(this::getFilmByIdFromMemory).toList();
    }

    // ================= Хелперы =================

    private Film getFilmAndCheckNullInMemory(Long id) {
        return inMemoryStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    private Film getFilmAndCheckNullInDb(Long id) {
        return dbStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    private void validationCountParameter(int count) {
        if (count <= 0) {
            log.warn("Неверное значение параметра Count: {}", count);
            throw new IncorrectParameterException("count должен быть > 0");
        }
    }
}
