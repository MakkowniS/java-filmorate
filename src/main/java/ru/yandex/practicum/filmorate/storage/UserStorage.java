package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    // Получение списка юзеров
    Collection<User> getUsers();

    // Получение юзера
    Optional<User> getUserById(Long userId);

    Optional<User> getUserByEmail(String email);

    Optional<User> getUserByLogin(String login);

    // Создание юзера
    User addUser(User user);

    // Обновление юзера
    User updateUser(User user);

    // Удаление юзера
    void deleteUser(Long id);
}
