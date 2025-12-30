package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users where id = ?";
    private static final String FIND_BY_EMAIL_QUERY = "SELECT * FROM users where email = ?";
    private static final String FIND_BY_LOGIN_QUERY = "SELECT * FROM users where login = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM users WHERE id = ?";

    public UserDbStorage(JdbcTemplate jdbcTemplate, RowMapper<User> rowMapper) {
        super(jdbcTemplate, rowMapper);
    }

    @Override
    public List<User> getUsers() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return findOne(FIND_BY_ID_QUERY, userId);
    }

    @Override
    public List<User> getUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        String FIND_BY_ID_LIST_QUERY = """
                SELECT *
                FROM users
                WHERE id IN (%s)
                """.formatted(
                String.join(",", Collections.nCopies(ids.size(), "?"))
        );

        return jdbc.query(FIND_BY_ID_LIST_QUERY, rowMapper, ids.toArray());
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return findOne(FIND_BY_EMAIL_QUERY, email);
    }

    @Override
    public Optional<User> getUserByLogin(String login) {
        return findOne(FIND_BY_LOGIN_QUERY, login);
    }

    @Override
    public User addUser(User user) {
        try {
            long id = insert(
                    INSERT_QUERY,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday()
            );
            user.setId(id);
            return user;
        } catch (DuplicateKeyException e) {
            throw new DuplicatedDataException("Данный Login или Email уже используются");
        }
    }

    @Override
    public User updateUser(User user) {
        try {
            update(
                    UPDATE_QUERY,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday(),
                    user.getId()
            );
            return user;
        } catch (DuplicateKeyException e) {
            throw new DuplicatedDataException("Данный Login или Email уже используются");
        }
    }

    @Override
    public void deleteUser(Long id) {
        delete(DELETE_QUERY, id);
    }

    @Override
    public boolean isUserExistsById(Long userId) {
        String IS_USER_EXISTS_QUERY = "SELECT EXISTS (SELECT 1 FROM users WHERE id = ?)";
        return jdbc.queryForObject(IS_USER_EXISTS_QUERY, Boolean.class, userId);
    }


}
