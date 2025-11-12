package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingInDto {
    @NotNull(message = "Необходимо указать дату начала бронирования")
    @FutureOrPresent(message = "Недопустимая дата начала бронирования")
    private LocalDateTime start;

    @NotNull(message = "Необходимо указать дату окончания бронирования")
    @Future(message = "Недопустимая дата окончания бронирования")
    private LocalDateTime end;

    @NotNull(message = "Необходимо указать идентификатор вещи")
    private Long itemId;
}
