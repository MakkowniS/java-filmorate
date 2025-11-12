package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false, of = "id")
public class User {
    private Integer id;

    @Email(message = "Некорректный формат Email")
    @NotBlank(message = "Поле Email не может быть пустым")
    private String email;

    @NotBlank(message = "Поле Login не может быть пустым")
    private String login;

    private String name;

    @PastOrPresent(message = "Некорректная дата рождения")
    private LocalDate birthday;
}
