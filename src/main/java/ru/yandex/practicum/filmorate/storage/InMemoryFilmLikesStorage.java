package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmLikesStorage implements FilmLikesStorage {

    // filmId - set<userId>
    private final Map<Long, Set<Long>> filmLikes = new HashMap<>();

    @Override
    public void addLike(long filmId, long userId) {
        filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        Set<Long> likes = filmLikes.get(filmId);
        if (likes != null) {
            likes.remove(userId);
            log.debug("Пользователь {} удалил лайк с фильма {}", userId, filmId);
            if (likes.isEmpty()) {
                filmLikes.remove(filmId);
            }
        }
    }

    @Override
    public List<Long> getLikedUserIds(long filmId) {
        return new ArrayList<>(filmLikes.getOrDefault(filmId, Collections.emptySet()));
    }

    @Override
    public int getLikesCount(long filmId) {
        return filmLikes.getOrDefault(filmId, Collections.emptySet()).size();
    }

}

