package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public Collection<User> getUsers(){
        return users.values();
    }

    @Override
    public User createUser(User user) {
        user.setId(getNextId());

        // Проверка на повторение Login и Email
        isEmailExists(user);
        isLoginExists(user);
        // Проверка на наличие имени для отображения
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Name не обнаружен. Использование Login для Name: {}", user.getName());
        }

        users.put(user.getId(), user);
        log.info("Пользователь успешно создан: id={}, email={}, login={}", user.getId(), user.getEmail(), user.getLogin());
        return user;
    }

    @Override
    public User updateUser(User newUser) {

        // Поиск ID в списке
        User oldUser = users.get(newUser.getId());
        if (oldUser == null) {
            log.warn("Пользователь с ID:{} не найден", newUser.getId());
            throw new ValidationException("User с указанным ID: " + newUser.getId() + " не найден");
        }

        // Поиск Email в запросе
        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
            // Проверка Email на дубль
            isEmailExists(newUser);
            oldUser.setEmail(newUser.getEmail());
            log.debug("Email:{} обновлён.", newUser.getEmail());
        }

        // Поиск Login в запросе
        if (newUser.getLogin() != null && !newUser.getLogin().isBlank()) {
            // Проверка Login на дубль
            isLoginExists(newUser);
            oldUser.setLogin(newUser.getLogin());
            log.debug("Login:{} обновлён", newUser.getLogin());
        }

        // Поиск Birthday в запросе
        if (newUser.getBirthday() != null) {
            oldUser.setBirthday(newUser.getBirthday());
            log.debug("дата рождения:{} обновлена", newUser.getBirthday());
        }

        // Поиск Name в запросе
        if (newUser.getName() != null) {
            oldUser.setName(newUser.getName().isBlank() ? oldUser.getLogin() : newUser.getName());
            log.debug("Name:{} обновлено", oldUser.getName());
        }

        log.info("Пользователь ID:{} успешно обновлён", oldUser.getId());
        return oldUser;
    }

    @Override
    public void deleteUser(int id) {
        if (users.containsKey(id)) {
            users.remove(id);
            log.debug("Пользователь с id: {} удалён.", id);
        } else {
            log.warn("Пользователь с id: {} не найден.", id);
            throw new IncorrectParameterException(Integer.toString(id), "Пользователь с указанным id не найден.");
        }
    }

    private void isEmailExists(User user) {
        boolean emailExists = users.values().stream()
                .anyMatch(u -> !u.getId().equals(user.getId()) && u.getEmail().equals(user.getEmail()));
        if (emailExists) {
            log.warn("Использование зарегистрированного email: {}", user.getEmail());
            throw new ValidationException("Данная электронная почта уже используется");
        }
    }

    private void isLoginExists(User user) {
        boolean loginExists = users.values().stream()
                .anyMatch(u -> !u.getId().equals(user.getId()) && u.getLogin().equals(user.getLogin()));
        if (loginExists) {
            log.warn("Использование зарегистрированного login: {}", user.getLogin());
            throw new ValidationException("Данный Login уже используется");
        }
    }

    private Integer getNextId() {
        int currentId = users.keySet().stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentId;
    }

}
