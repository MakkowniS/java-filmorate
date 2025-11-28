package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long firstUserId, Long secondUserId){
        log.info("Добавление в друзья юзеров с ID: {} и {}", firstUserId,secondUserId);

        User firstUser = getUserAndValidate(firstUserId);
        User secondUser = getUserAndValidate(secondUserId);

        firstUser.getFriendsIds().add(secondUserId);
        secondUser.getFriendsIds().add(firstUserId);

        log.info("Друзья добавлены.");
    }

    public void removeFriend(Long firstUserId, Long secondUserId){
        log.info("Удаление из друзей юзеров с ID: {} и {}",  firstUserId,secondUserId);

        User firstUser = getUserAndValidate(firstUserId);
        User secondUser = getUserAndValidate(secondUserId);

        firstUser.getFriendsIds().add(secondUserId);
        secondUser.getFriendsIds().add(firstUserId);

        log.info("Друзья удалены.");
    }

    public List<User> findCommonFriends(Long firstUserId, Long secondUserId) {
        log.info("Поиск общих друзей юзеров с ID: {} и {}", firstUserId, secondUserId);

        HashSet<Long> firstUserFriendsIds = new HashSet<>(getUserAndValidate(firstUserId).getFriendsIds());
        HashSet<Long> secondUserFriendsIds = new HashSet<>(getUserAndValidate(secondUserId).getFriendsIds());

        // Ищем пересечения списков ID
        firstUserFriendsIds.retainAll(secondUserFriendsIds);

        List<User> commonFriends = new ArrayList<>();
        for (Long userId : firstUserFriendsIds) {
            User friend = userStorage.getUser(userId);
            if (friend != null) {
                commonFriends.add(friend);
            }
        }

        log .info("Найдено {} общих друзей.",  commonFriends.size());
        return commonFriends;
    }

    private User getUserAndValidate(Long userId) {
        User user = userStorage.getUser(userId);
        if (user == null) {
            log.warn("Указанного ID {} нет в списке активных юзеров.", userId);
            throw new NotFoundException("Указанный ID (" + userId + ")не найден");
        }
        return user;
    }

}
