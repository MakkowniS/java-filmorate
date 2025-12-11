package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    // Получение списка фильмов
    Collection<Film> getFilms();

    // Получение фильма по id
    Film getFilmById(Long id);

    // Добавление фильма
    Film createFilm(Film film);

    // Изменение добавленного фильма
    Film updateFilm(Film newFilm);

    // Удаление фильма
    void deleteFilm(Long id);
}
