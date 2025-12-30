package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> getFilmsByIds(List<Long> ids) {
        return ids.stream().map(films::get).filter(Objects::nonNull).toList();
    }

    @Override
    public Film createFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Фильм успешно создан ID:{}, Name:{}", film.getId(), film.getName());
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        if (!films.containsKey(newFilm.getId())) {
            throw new NotFoundException("Фильм с id=" + newFilm.getId() + " не найден");
        }

        Film storedFilm = films.get(newFilm.getId());

        if (newFilm.getName() != null) {
            if (!newFilm.getName().isBlank()) {
                storedFilm.setName(newFilm.getName());
                log.debug("Название обновлено. Name:{}", newFilm.getName());
            } else { // Если передан пустой Name (" ")
                throw new ValidationException("Название не может быть пустым");
            }
        }

        // Проверка наличия Description в запросе
        if (newFilm.getDescription() != null) {
            storedFilm.setDescription(newFilm.getDescription());
            log.debug("Описание обновлено. Description:{}", newFilm.getDescription());
        }

        // Проверка наличия ReleaseDate в запросе
        if (newFilm.getReleaseDate() != null) {
            storedFilm.setReleaseDate(newFilm.getReleaseDate());
            log.debug("Дата релиза обновлена. ReleaseDate:{}", newFilm.getReleaseDate());
        }

        // Проверка наличия Duration в запросе
        if (newFilm.getDuration() != null) {
            storedFilm.setDuration(newFilm.getDuration());
            log.debug("Продолжительность обновлена. Duration:{}", newFilm.getDuration());
        }

        films.put(newFilm.getId(), newFilm);
        log.info("Фильм с ID:{} успешно обновлён.", newFilm.getId());
        return newFilm;
    }

    @Override
    public void deleteFilm(Long id) {
        if (films.remove(id) == null) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }

        log.debug("Фильм с id:{} удалён.", id);
    }

    private Long getNextId() {
        long currentId = films.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentId;
    }
}
