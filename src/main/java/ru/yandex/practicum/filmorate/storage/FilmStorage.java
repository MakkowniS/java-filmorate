package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    // Получение списка фильмов
    Collection<Film> getFilms();

    // Получение фильма по id
    Optional<Film> getFilmById(Long id);

    List<Film> getFilmsByIds(List<Long> ids);

    // Добавление фильма
    Film createFilm(Film film);

    // Изменение добавленного фильма
    Film updateFilm(Film newFilm);

    // Удаление фильма
    void deleteFilm(Long id);

    List <Film>getPopular(int count);

}
