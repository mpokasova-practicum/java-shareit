package ru.practicum.shareit.booking.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.model.Item;

@Component
public class BookingMapper {
    public BookingOutDto toBookingOutDto(Booking booking) {
        return BookingOutDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(new BookingOutDto.Booker(booking.getBooker().getId(), booking.getBooker().getName()))
                .item(new BookingOutDto.Item(booking.getItem().getId(), booking.getItem().getName()))
                .build();
    }

    public BookingItemDto toBookingItemDto(Booking booking) {
        return BookingItemDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .bookerId(booking.getBooker().getId())
                .build();
    }

    public Booking toBooking(BookingInDto bookingInDto) {
        Booking booking = new Booking();
        booking.setStart(bookingInDto.getStart());
        booking.setEnd(bookingInDto.getEnd());
        Item item = new Item();
        item.setId(bookingInDto.getItemId());
        booking.setItem(item);
        return booking;
    }
}
