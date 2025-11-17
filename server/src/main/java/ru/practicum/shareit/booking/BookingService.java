package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;

import java.util.List;

public interface BookingService {
    BookingOutDto createBooking(Long userId, BookingInDto bookingDto);

    BookingOutDto approveBooking(Long userId, Long bookingId, Boolean approved);

    BookingOutDto getBookingById(Long userId, Long bookingId);

    List<BookingOutDto> getAllUserBookings(Long userId, String state);

    List<BookingOutDto> getAllItemBookings(Long userId, String state);
}
