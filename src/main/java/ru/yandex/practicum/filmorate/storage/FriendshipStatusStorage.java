package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.util.Optional;

public interface FriendshipStatusStorage {

    int getIdByStatus(FriendshipStatus status);

    Optional<FriendshipStatus> getStatusById(int id);
}

