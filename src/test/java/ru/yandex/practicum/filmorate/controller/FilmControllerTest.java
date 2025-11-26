package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.Marker;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmControllerTest {

    @Autowired
    private Validator validator;
    @Autowired
    private FilmController filmController;
    private Film film;

    @BeforeEach
    public void setup() {
        Film film1 = new Film();
        film1.setName("Ok");
        film1.setReleaseDate(LocalDate.now());
        filmController.createFilm(film1);
        film = new Film();
    }

    @Test
    void validationFilmThrowsWhenDescriptionMore200() {
        film.setName("Star");
        film.setDescription("a".repeat(201));

        Set<ConstraintViolation<Film>> violations = validator.validate(film, Marker.OnCreate.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void validationFilmThrowsWhenDurationNegative() {
        film.setName("Star");
        film.setDescription("Lorem ipsum dolor sit amet");
        film.setReleaseDate(LocalDate.of(2000, 12, 2));
        film.setDuration(-1);

        Set<ConstraintViolation<Film>> violations = validator.validate(film, Marker.OnCreate.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("duration")));
    }

    @Test
    void validationFilmThrowsWhenNameIsBlank() {
        film.setName("");

        Set<ConstraintViolation<Film>> violations = validator.validate(film, Marker.OnCreate.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void validationFilmThrowsWhenRequestEmpty() {
        Set<ConstraintViolation<Film>> violations = validator.validate(film,  Marker.OnCreate.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void validationFilmThrowsWhenDateBefore1895() {
        film.setName("Star");
        film.setDescription("Lorem ipsum dolor sit amet");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film, Marker.OnCreate.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("releaseDate")));
    }

    @Test
    void validationUpdateFilmThrowsWhenIdIsNull() {
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Marker.OnUpdate.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("id")));
    }

    @Test
    void createFilmSucceedWhenValid() {
        film.setName("Star");
        film.setDescription("Lorem ipsum dolor sit amet");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);

        Film result = filmController.createFilm(film);
        assertNotNull(result);
        assertEquals("Star", result.getName());
    }

    @Test
    void createFilmSucceedWhenDateEquals1895() {
        film.setName("Star");
        film.setDescription("Lorem ipsum dolor sit amet");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration((120));

        Film result = filmController.createFilm(film);
        assertNotNull(result);
    }



    @Test
    void createFilmSucceedWhenDescriptionEquals200() {
        film.setName("Star");
        film.setDescription("a".repeat(200));
        film.setReleaseDate(LocalDate.of(2000, 12, 2));
        film.setDuration(120);

        Film result = filmController.createFilm(film);
        assertNotNull(result);
    }

    @Test
    void createFilmSucceedWhenDurationEquals0() {
        film.setName("Star");
        film.setDescription("Lorem ipsum dolor sit amet");
        film.setReleaseDate(LocalDate.of(2000, 12, 2));
        film.setDuration(0);

        Film result = filmController.createFilm(film);
        assertNotNull(result);
    }

    @Test
    void updateFilmThrowsWhenIdNotExists() {
        film.setId(10);

        assertThrows(ValidationException.class, () -> filmController.updateFilm(film));
    }

    @Test
    void updateFilmSucceedWhenIdExistsAndFilmValidOnBoundaries() {
        film.setId(1);
        film.setName("S");
        film.setDescription("L".repeat(200));
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(0);

        Film result = filmController.updateFilm(film);
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void updateFilmThrowsWhenNameIsNotNullButBlank() {
        film.setId(1);
        film.setName("");

        assertThrows(ValidationException.class, () -> filmController.updateFilm(film));
    }

}
