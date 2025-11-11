package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        if (film.getName() == null || film.getName().isBlank()){
            throw new ValidationException("Название не может быть пустым");
        } else if (film.getDescription().length() > 200){
            throw new ValidationException("Описание не может быть больше 200 символов");
        } else if (film.getReleaseDate().isBefore(LocalDate.of(1895,12,28))){
            throw new ValidationException("Дата релиза не может быть раньше 28.12.1895");
        } else if (film.getDuration().toMinutes() < 0){
            throw new ValidationException("Продолжительность не может быть отрицательной");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);

        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        if (newFilm.getId() == null){
            throw new ValidationException("ID не может быть пустым");
        }

        Film oldFilm = films.get(newFilm.getId());
        if (oldFilm == null){
            throw new ValidationException("Фильма с ID: " + newFilm.getId() + " не найдено");
        }

        if (newFilm.getName() != null && !newFilm.getName().isBlank()){
            oldFilm.setName(newFilm.getName());
        }
        if (newFilm.getDescription() != null && newFilm.getDescription().length() < 200){
            oldFilm.setDescription(newFilm.getDescription());
        }
        if (newFilm.getReleaseDate() != null && newFilm.getReleaseDate().isAfter(LocalDate.of(1895,12,28))){
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != null && newFilm.getDuration().toMinutes() > 0){
            oldFilm.setDuration(newFilm.getDuration());
        }

        return oldFilm;
    }

    private Integer getNextId(){
        int currentId = films.keySet().stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentId;
    }
}
