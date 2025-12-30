package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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

    private String name;

    private String description;

    private LocalDate releaseDate;

    private Integer duration;

    private Set<Genre> genres = new HashSet<>();

    private Mpa mpa;

    private Set<GenreList> genresInMemory = new HashSet<>();
    private MpaList mpaInMemory;
}
