package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;
import ru.yandex.practicum.filmorate.validation.Marker;
import ru.yandex.practicum.filmorate.validation.annotation.AfterFirstFilmRelease;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
public class Film {

    // Конструктор для InMemory
    public Film(String name,
                String description,
                LocalDate releaseDate,
                Integer duration,
                Set<GenreList> genres,
                MpaList mpa) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.genresInMemory = genres;
        this.mpaInMemory = mpa;
    }

    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не может быть больше 200 символов")
    private String description;

    @AfterFirstFilmRelease
    @NotNull(message = "Дата релиза не может быть пустой")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность не может быть отрицательной")
    private Integer duration;

    private Set<Genre> genres = new HashSet<>();

    private Mpa mpa;

    private Set<GenreList> genresInMemory = new HashSet<>();
    private MpaList mpaInMemory;
}
