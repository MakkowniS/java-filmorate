package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmControllerTest {

    private FilmController filmController;
    private Validator validator;
    private Film film;

    @BeforeEach
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        filmController = new FilmController();
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

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void validationFilmThrowsWhenDurationNegative() {
        film.setName("Star");
        film.setDescription("Lorem ipsum dolor sit amet");
        film.setReleaseDate(LocalDate.of(2000, 12, 2));
        film.setDuration(-1);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("duration")));
    }

    @Test
    void validationFilmThrowsWhenNameIsBlank() {
        film.setName("");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void validationFilmThrowsWhenRequestEmpty() {
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
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
    void createFilmThrowsWhenDateBefore1895() {
        film.setName("Star");
        film.setDescription("Lorem ipsum dolor sit amet");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
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
    void updateFilmThrowsWhenIdIsNull() {
        assertThrows(ValidationException.class, () -> filmController.updateFilm(film));
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

    @Test
    void updateFilmThrowsWhenDescriptionIsNotNullButMoreThan200() {
        film.setId(1);
        film.setDescription("a".repeat(201));

        assertThrows(ValidationException.class, () -> filmController.updateFilm(film));
    }

    @Test
    void updateFilmThrowsWhenReleaseDateIsNotNullButBefore1895() {
        film.setId(1);
        film.setReleaseDate(LocalDate.of(1894, 1, 1));

        assertThrows(ValidationException.class, () -> filmController.updateFilm(film));
    }

    @Test
    void updateFilmThrowsWhenDurationIsNotNullButNegative() {
        film.setId(1);
        film.setDuration((-1));

        assertThrows(ValidationException.class, () -> filmController.updateFilm(film));
    }
}
