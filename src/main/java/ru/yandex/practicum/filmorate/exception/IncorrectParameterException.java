package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;

@Getter
public class IncorrectParameterException extends RuntimeException {

    String parameter;

    public IncorrectParameterException(String message) {
        super(message);
    }

    public IncorrectParameterException(String parameter, String message) {
        super(message);
        this.parameter = parameter;
    }
}
