package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validation.Marker;
import ru.yandex.practicum.filmorate.validation.annotation.AfterFirstFilmRelease;


import java.time.LocalDate;
import java.util.HashSet;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
public class Film {

    @Null(groups = Marker.OnCreate.class)
    @NotNull(groups = Marker.OnUpdate.class, message = "ID должен быть указан")
    private Long id;

    @NotBlank(groups = Marker.OnCreate.class, message = "Название не может быть пустым")
    private String name;

    @Size(groups = {Marker.OnUpdate.class, Marker.OnCreate.class}, max = 200, message = "Описание не может быть больше 200 символов")
    private String description;

    @AfterFirstFilmRelease(groups = {Marker.OnUpdate.class, Marker.OnCreate.class})
    @NotNull(groups = Marker.OnCreate.class, message = "Дата релиза не может быть пустой")
    private LocalDate releaseDate;

    @PositiveOrZero(groups = {Marker.OnUpdate.class, Marker.OnCreate.class}, message = "Продолжительность не может быть отрицательной")
    private Integer duration;

    private HashSet<Long> likedUserIds = new HashSet<>();
}
