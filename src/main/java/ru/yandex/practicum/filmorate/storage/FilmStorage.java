package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {

    // Получение списка фильмов
    Collection<Film> getFilms();

    // Получение фильма по id
    Optional<Film> getFilmById(Long id);

    // Добавление фильма
    Film createFilm(Film film);

    // Изменение добавленного фильма
    Film updateFilm(Film newFilm);

    // Удаление фильма
    void deleteFilm(Long id);

    void addLike(Long filmId, Long userId);
    void removeLike(Long filmId, Long userId);
    Set<Long> getLikedUserIds(Long filmId);

    List<Film> getTopLikedFilms(int count);
}
