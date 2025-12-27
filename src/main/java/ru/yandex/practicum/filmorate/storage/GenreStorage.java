package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GenreStorage {
    List<Genre> getAllGenres();
    Optional<Genre> getGenreById(Integer id);
    Set<Genre> getManyGenresByIds(Set<Integer> Ids);
}
