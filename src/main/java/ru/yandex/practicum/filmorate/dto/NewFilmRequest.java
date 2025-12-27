package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validation.annotation.MinReleaseDate;
import ru.yandex.practicum.filmorate.validation.annotation.NotBlankSpaces;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class NewFilmRequest {

    @NotBlankSpaces
    private String name;

    @Size(max = 200)
    private String description;

    @NotNull
    @MinReleaseDate
    private LocalDate releaseDate;

    @Positive
    private Integer duration;

    @NotNull
    private MpaDto mpa;

    private Set<GenreDto> genres = new HashSet<>();
}

