package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return Optional.ofNullable(users.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null));
    }

    @Override
    public Optional<User> getUserByLogin(String login) {
        return Optional.ofNullable(users.values().stream()
                .filter(user -> user.getLogin().equals(login))
                .findFirst()
                .orElse(null));
    }

    @Override
    public User addUser(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);

        log.info("Пользователь успешно создан: id={}, email={}, login={}", user.getId(), user.getEmail(), user.getLogin());
        return user;
    }

    @Override
    public User updateUser(User newUser) {
        users.put(newUser.getId(), newUser);
        log.info("Пользователь ID:{} успешно обновлён", newUser.getId());
        return newUser;
    }

    @Override
    public void deleteUser(Long id) {
        users.remove(id);
    }

    private Long getNextId() {
        long currentId = users.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentId;
    }

}
