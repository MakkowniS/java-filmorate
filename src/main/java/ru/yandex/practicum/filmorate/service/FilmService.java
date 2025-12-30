package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final FilmLikesStorage likesStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final UserService userService;

    public FilmService(FilmStorage filmStorage,
                       FilmLikesStorage likesStorage,
                       GenreStorage genreStorage,
                       MpaStorage mpaStorage,
                       UserService userService) {
        this.filmStorage = filmStorage;
        this.likesStorage = likesStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.userService = userService;
    }

    // ===== Films =====

    public FilmDto createFilm(NewFilmRequest request) {

        Mpa mpa = mpaStorage.getMpaById(request.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("MPA не найден"));

        Set<Integer> genreIds = request.getGenres().stream()
                .map(GenreDto::getId)
                .collect(Collectors.toSet());

        Set<Genre> genres = genreStorage.getManyGenresByIds(genreIds);

        if (genres.size() != genreIds.size()) {
            throw new NotFoundException("Один или несколько жанров не найдены");
        }

        Film film = FilmMapper.mapToFilm(request, mpa, genres);
        film = filmStorage.createFilm(film);

        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto updateFilm(UpdateFilmRequest request) {

        Film film = filmStorage.getFilmById(request.getId())
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));

        Mpa mpa = null;
        if (request.hasMpa()) {
            mpa = mpaStorage.getMpaById(request.getMpaId())
                    .orElseThrow(() -> new NotFoundException("MPA не найден"));
        }

        Set<Genre> genres = null;
        if (request.hasGenres()) {
            genres = genreStorage.getManyGenresByIds(request.getGenreIds());
        }

        FilmMapper.updateFilmFields(film, request, mpa, genres);
        film = filmStorage.updateFilm(film);

        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto getFilmById(Long id) {
        return filmStorage.getFilmById(id)
                .map(FilmMapper::mapToFilmDto)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    public List<FilmDto> getAllFilms() {
        return filmStorage.getFilms().stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public void deleteFilm(Long id) {
        filmStorage.deleteFilm(id);
    }

    // ===== Likes =====

    public void addLike(Long filmId, Long userId) {
        getFilmOrThrowNotFound(filmId);
        userService.getUserByIdInDb(userId);
        likesStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        getFilmOrThrowNotFound(filmId);
        userService.getUserByIdInDb(userId);
        likesStorage.removeLike(filmId, userId);
    }

    public List<UserDto> getLikedUsers(Long filmId) {
        getFilmOrThrowNotFound(filmId);
        return likesStorage.getLikedUserIds(filmId).stream()
                .map(userService::getUserByIdInDb)
                .toList();
    }

    public List<FilmDto> getTopLikedFilms(int count) {
        validationCountParameter(count);

        List<Long> filmIds = likesStorage.getTopLikedFilmIds(count);

        return filmStorage.getFilmsByIds(filmIds).stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    // ================= Хелперы =================

    private void getFilmOrThrowNotFound(Long filmId) {
        filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));
    }

    private void validationCountParameter(int count) {
        if (count <= 0) {
            log.warn("Неверное значение параметра Count: {}", count);
            throw new IncorrectParameterException("count должен быть > 0");
        }
    }
}
