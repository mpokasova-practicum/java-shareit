package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingInDto {
    @NotNull
    @FutureOrPresent(message = "Недопустимая дата начала бронирования")
    private LocalDateTime start;

    @NotNull
    @Future(message = "Недопустимая дата окончания бронирования")
    private LocalDateTime end;

    @NotNull
    private Long itemId;
}
