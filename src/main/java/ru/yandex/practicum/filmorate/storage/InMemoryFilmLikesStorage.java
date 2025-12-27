package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

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
    public List<Long> getTopLikedFilmIds(int count) {
        return filmLikes.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .limit(count)
                .map(Map.Entry::getKey)
                .toList();
    }
}

