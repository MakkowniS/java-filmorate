package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validation.Marker;
import ru.yandex.practicum.filmorate.validation.annotation.NotContainsWhitespaces;

import java.time.LocalDate;
import java.util.HashMap;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
public class User {

    @Null(groups = Marker.OnCreate.class, message = "При создании пользователя ID не должен указываться")
    @NotNull(groups = Marker.OnUpdate.class, message = "ID должен быть указан")
    private Long id;

    @NotBlank(groups = Marker.OnCreate.class, message = "Поле Email не может быть пустым")
    @Email(groups = {Marker.OnUpdate.class, Marker.OnCreate.class}, message = "Некорректный формат Email")
    private String email;

    @NotBlank(groups = Marker.OnCreate.class, message = "Поле Login не может быть пустым")
    @NotContainsWhitespaces(groups = {Marker.OnUpdate.class, Marker.OnCreate.class})
    private String login;

    private String name;

    @PastOrPresent(groups = {Marker.OnUpdate.class, Marker.OnCreate.class}, message = "Некорректная дата рождения")
    private LocalDate birthday;

    HashMap<Long, FriendshipStatus> friendsIds = new HashMap<>();

}
