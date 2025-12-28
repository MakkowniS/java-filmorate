package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;
import ru.yandex.practicum.filmorate.validation.Marker;
import ru.yandex.practicum.filmorate.validation.annotation.NotContainsWhitespaces;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
@Builder(toBuilder = true)
public class User {

    // Конструктор для InMemory
    public User(String email, String login, LocalDate birthday){
        this.email = email;
        this.login = login;
        this.birthday = birthday;
    }

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

}
