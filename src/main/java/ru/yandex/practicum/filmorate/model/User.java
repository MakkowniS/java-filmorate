package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
@Builder(toBuilder = true)
public class User {

    // Конструктор для InMemory
    public User(String email, String login, LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.birthday = birthday;
    }

    private Long id;

    private String email;

    private String login;

    private String name;

    private LocalDate birthday;

}
