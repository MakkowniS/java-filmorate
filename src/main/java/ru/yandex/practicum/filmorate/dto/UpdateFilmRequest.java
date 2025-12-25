package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
public class UpdateFilmRequest {

    @NotNull
    private Long id;

    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Integer mpaId;
    private Set<Integer> genreIds;

    public boolean hasName() {
        return name != null && !name.isBlank();
    }

    public boolean hasDescription() {
        return description != null;
    }

    public boolean hasReleaseDate() {
        return releaseDate != null;
    }

    public boolean hasDuration() {
        return duration != null && duration >= 0;
    }

    public boolean hasMpa() {
        return mpaId != null;
    }

    public boolean hasGenres() {
        return genreIds != null;
    }
}
