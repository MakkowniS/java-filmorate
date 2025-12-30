package ru.yandex.practicum.filmorate.storage;

import java.util.List;

public interface FilmLikesStorage {
    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);

    List<Long> getLikedUserIds(long filmId);

    int getLikesCount(long filmId);

}
