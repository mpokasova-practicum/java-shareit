package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.exception.InvalidStateException;

public enum State {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static State validateState(String state) throws InvalidStateException {
        try {
            return State.valueOf(state);
        } catch (Exception e) {
            throw new InvalidStateException("Неподдерживаемый тип state");
        }
    }
}
