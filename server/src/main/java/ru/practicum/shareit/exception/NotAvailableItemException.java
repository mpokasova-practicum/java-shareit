package ru.practicum.shareit.exception;

public class NotAvailableItemException extends RuntimeException {
    public NotAvailableItemException(String message) {
        super(message);
    }
}
