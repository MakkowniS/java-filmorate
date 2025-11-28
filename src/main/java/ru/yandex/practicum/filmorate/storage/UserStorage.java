package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Set;

public interface UserStorage {

    // Получение списка юзеров
    public Collection<User> getUsers();

    // Получение юзера
    public User getUser(Long userId);

    // Получение списка id друзей
    public Set<Long> getUsersFriendsIds(Long userId);

    // Создание юзера
    public User addUser(User user);

    // Обновление юзера
    public User updateUser(User newUser);

    // Удаление юзера
    public void deleteUser(Long id);
}
