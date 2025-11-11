package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Film.
 */
@Data
@EqualsAndHashCode(callSuper = false, of = "id")
public class Film {
    private Integer id;
    private String name;
    private String description;
    @JsonFormat(shape =  JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate releaseDate;
    private Duration duration;
}
