package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    // Получение списка юзеров
    public Collection<User> getUsers();

    // Создание юзера
    public User createUser(User user);

    // Обновление юзера
    public User updateUser(User newUser);

    // Удаление юзера
    public void deleteUser(int id);
}
