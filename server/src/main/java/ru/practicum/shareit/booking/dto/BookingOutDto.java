package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.Status;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingOutDto {
    private Long id;

    @NotNull
    private LocalDateTime start;

    @NotNull
    private LocalDateTime end;

    @NotNull
    private Item item;

    private Booker booker;

    private Status status;

    @Data
    @AllArgsConstructor
    public static class Item {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    public static class Booker {
        private Long id;
        private String name;
    }
}
