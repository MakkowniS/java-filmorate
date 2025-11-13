package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.validation.Marker;
import ru.yandex.practicum.filmorate.validation.annotation.NotContainsWhitespaces;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false, of = "id")
public class User {

    @Null(groups = Marker.OnCreate.class, message = "При создании пользователя ID не должен указываться")
    @NotNull(groups = Marker.OnUpdate.class, message = "ID должен быть указан")
    private Integer id;

    @NotBlank(groups = Marker.OnCreate.class, message = "Поле Email не может быть пустым")
    @Email(message = "Некорректный формат Email")
    private String email;

    @NotBlank(groups = Marker.OnCreate.class, message = "Поле Login не может быть пустым")
    @NotContainsWhitespaces
    private String login;

    private String name;

    @PastOrPresent(message = "Некорректная дата рождения")
    private LocalDate birthday;
}
