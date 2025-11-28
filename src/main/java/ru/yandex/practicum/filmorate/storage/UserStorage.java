package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Set;

public interface UserStorage {

    // Получение списка юзеров
    Collection<User> getUsers();

    // Получение юзера
    User getUser(Long userId);

    // Создание юзера
    User addUser(User user);

    // Обновление юзера
    User updateUser(User newUser);

    // Удаление юзера
    void deleteUser(Long id);
}
