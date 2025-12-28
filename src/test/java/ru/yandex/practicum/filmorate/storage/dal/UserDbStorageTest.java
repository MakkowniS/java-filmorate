package ru.yandex.practicum.filmorate.storage.dal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorageTest.TestConfig.class)
class UserDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserDbStorage userDbStorage;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM users");

        jdbcTemplate.update("""
                    INSERT INTO users (email, login, name, birthday)
                    VALUES 
                    ('user1@mail.com', 'user1', 'User One', '1990-01-01'),
                    ('user2@mail.com', 'user2', 'User Two', '1995-05-05')
                """);
    }

    @Test
    void shouldReturnAllUsers() {
        List<User> users = userDbStorage.getUsers();

        assertThat(users)
                .hasSize(2)
                .extracting(User::getLogin)
                .containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    void shouldFindUserById() {
        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?",
                Long.class,
                "user1@mail.com"
        );

        Optional<User> userOptional = userDbStorage.getUserById(userId);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user.getId()).isEqualTo(userId)
                );
    }

    @Test
    void shouldReturnEmptyWhenUserNotFoundById() {
        Optional<User> userOptional = userDbStorage.getUserById(999L);

        assertThat(userOptional).isEmpty();
    }

    @Test
    void shouldFindUserByEmail() {
        Optional<User> userOptional = userDbStorage.getUserByEmail("user2@mail.com");

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user.getLogin()).isEqualTo("user2")
                );
    }

    @Test
    void shouldFindUserByLogin() {
        Optional<User> userOptional = userDbStorage.getUserByLogin("user1");

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user.getEmail()).isEqualTo("user1@mail.com")
                );
    }

    @Test
    void shouldAddUser() {
        User newUser = User.builder()
                .email("new@mail.com")
                .login("newlogin")
                .name("New User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User savedUser = userDbStorage.addUser(newUser);

        assertThat(savedUser.getId()).isNotNull();

        Optional<User> userFromDb = userDbStorage.getUserById(savedUser.getId());

        assertThat(userFromDb)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getEmail()).isEqualTo("new@mail.com");
                    assertThat(user.getLogin()).isEqualTo("newlogin");
                });
    }

    @Test
    void shouldUpdateUser() {
        User user = userDbStorage.getUserById(1L).orElseThrow();

        user.setName("Updated Name");
        user.setEmail("updated@mail.com");

        User updatedUser = userDbStorage.updateUser(user);

        Optional<User> userFromDb = userDbStorage.getUserById(1L);

        assertThat(userFromDb)
                .isPresent()
                .hasValueSatisfying(dbUser -> {
                    assertThat(dbUser.getName()).isEqualTo("Updated Name");
                    assertThat(dbUser.getEmail()).isEqualTo("updated@mail.com");
                });
    }

    @Test
    void shouldDeleteUser() {
        userDbStorage.deleteUser(1L);

        Optional<User> userOptional = userDbStorage.getUserById(1L);

        assertThat(userOptional).isEmpty();
    }

    // ===== Конфиг =====

    @TestConfiguration
    static class TestConfig {

        @Bean
        UserDbStorage userDbStorage(JdbcTemplate jdbcTemplate, RowMapper<User> userRowMapper) {
            return new UserDbStorage(jdbcTemplate, userRowMapper);
        }

        @Bean
        RowMapper<User> userRowMapper() {
            return (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setEmail(rs.getString("email"));
                user.setLogin(rs.getString("login"));
                user.setName(rs.getString("name"));

                Date birthday = rs.getDate("birthday");
                user.setBirthday(birthday != null ? birthday.toLocalDate() : null);

                return user;
            };
        }
    }

}
