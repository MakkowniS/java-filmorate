package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.annotation.NotContainsWhitespaces;

import java.time.LocalDate;

@Data
public class NewUserRequest {

    @Email(message = "Неверный формат электронной почты")
    @NotBlank(message = "Поле Email не может быть пустым")
    private String email;

    @NotBlank(message = "Поле Login не может быть пустым")
    @NotContainsWhitespaces
    private String login;

    private String name;

    @PastOrPresent(message = "Некорректная дата рождения")
    private LocalDate birthday;
}
