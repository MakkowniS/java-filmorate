package ru.yandex.practicum.filmorate.storage;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.Marker;

import java.util.Collection;

public interface FilmStorage {

    // Получение списка фильмов
    public Collection<Film> getFilms();

    // Получение фильма по id
    public Film getFilmById(Long id);

    // Добавление фильма
    public Film createFilm(Film film);

    // Изменение добавленного фильма
    public Film updateFilm(Film newFilm);

    // Удаление фильма
    public void deleteFilm(Long id);
}
