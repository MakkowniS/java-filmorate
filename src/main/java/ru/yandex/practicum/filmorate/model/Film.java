package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.validation.Marker;
import ru.yandex.practicum.filmorate.validation.annotation.AfterFirstFilmRelease;


import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false, of = "id")
public class Film {

    @Null(groups = Marker.OnCreate.class)
    @NotNull(groups = Marker.OnUpdate.class, message = "ID должен быть указан")
    private Integer id;

    @NotBlank(groups = Marker.OnCreate.class, message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не может быть больше 200 символов")
    private String description;

    @AfterFirstFilmRelease
    @NotNull(groups = Marker.OnCreate.class, message = "Дата релиза не может быть пустой")
    private LocalDate releaseDate;

    @PositiveOrZero(message = "Продолжительность не может быть отрицательной")
    private Integer duration;
}
