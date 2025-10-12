package ru.practicum.shareit.booking;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper mapper;

    static final Sort sort = Sort.by(Sort.Direction.DESC, "start");

    @Override
    public BookingOutDto createBooking(Long userId, BookingInDto bookingDto) {
        Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(
                () -> new NotFoundException("Вещь с данным id не найдена")
        );
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с данным id не найден")
        );
        if (!item.getAvailable()) {
            throw new NotAvailableItemException("Данная вещь не доступна для бронирования");
        }
        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new InvalidDatesException("Дата окончания бронирования раньше даты начала");
        }
        Booking booking = mapper.toBooking(bookingDto);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(Status.WAITING);
        booking = bookingRepository.save(booking);
        return mapper.toBookingOutDto(booking);
    }

    @Override
    public BookingOutDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new NotFoundException("Бронирование не найдено")
        );
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NoBookingFoundException("Указанный идентификатор пользователя не совпадает с владельцем вещи");
        }
        if (approved != null) {
            booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        }
        booking = bookingRepository.save(booking);
        return mapper.toBookingOutDto(booking);
    }

    @Override
    public BookingOutDto getBookingById(Long userId, Long bookingId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с данным id не найден")
        );
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new NotFoundException("Бронирование не найдено")
        );
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Указанный идентификатор пользователя не совпадает с владельцем бронирования или владельцем вещи");
        }
        return mapper.toBookingOutDto(booking);
    }

    @Override
    public List<BookingOutDto> getAllUserBookings(Long userId, String stateString) {
        State state = State.validateState(stateString);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с данным id не найден")
        );
        List<Booking> bookings = new ArrayList<>();
        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByBookerId(userId, sort);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfter(
                        userId, LocalDateTime.now(), LocalDateTime.now(), sort);
                break;
            case PAST:
                bookings = bookingRepository.findAllByBookerIdAndEndIsBefore(userId, LocalDateTime.now(), sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBookerIdAndStartIsAfter(userId, LocalDateTime.now(), sort);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByBookerIdAndStatus(userId, Status.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBookerIdAndStatus(userId, Status.REJECTED, sort);
                break;
        }
        return bookings.isEmpty() ? Collections.emptyList() : bookings.stream()
                .map(mapper::toBookingOutDto)
                .collect(toList());
    }

    @Override
    public List<BookingOutDto> getAllItemBookings(Long userId, String stateString) {
        State state = State.validateState(stateString);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с данным id не найден")
        );
        List<Booking> bookings = new ArrayList<>();
        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByItemOwnerId(userId, sort);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfter(
                        userId, LocalDateTime.now(), LocalDateTime.now(), sort);
                break;
            case PAST:
                bookings = bookingRepository.findAllByItemOwnerIdAndEndIsBefore(userId, LocalDateTime.now(), sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByItemOwnerIdAndStartIsAfter(userId, LocalDateTime.now(), sort);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByItemOwnerIdAndStatus(userId, Status.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByItemOwnerIdAndStatus(userId, Status.REJECTED, sort);
                break;
        }
        return bookings.isEmpty() ? Collections.emptyList() : bookings.stream()
                .map(mapper::toBookingOutDto)
                .collect(toList());
    }
}
