package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingMapper mapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private final Long userId = 1L;
    private final Long ownerId = 2L;
    private final Long bookingId = 1L;
    private final Long itemId = 10L;
    private final LocalDateTime start = LocalDateTime.now().plusDays(1);
    private final LocalDateTime end = LocalDateTime.now().plusDays(2);

    private final User user = new User(userId, "Alex", "alex@mail.ru");
    private final User owner = new User(ownerId, "Owner", "owner@mail.ru");
    private final Item availableItem = new Item(itemId, owner, "Drill", "Simple drill", true, null);
    private final Item unavailableItem = new Item(itemId, owner, "Drill", "Simple drill", false, null);
    private final BookingInDto bookingInDto = new BookingInDto(start, end, itemId);
    private final Booking booking = new Booking(bookingId, start, end, availableItem, user, Status.WAITING);
    private final BookingOutDto bookingOutDto = BookingOutDto.builder()
            .id(bookingId)
            .start(start)
            .end(end)
            .build();

    @Test
    void createBooking_whenValid_thenReturnBookingOutDto() {
        // Given
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(availableItem));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mapper.toBooking(bookingInDto)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(mapper.toBookingOutDto(booking)).thenReturn(bookingOutDto);

        // When
        BookingOutDto result = bookingService.createBooking(userId, bookingInDto);

        // Then
        assertNotNull(result);
        assertEquals(bookingOutDto, result);
        assertEquals(Status.WAITING, booking.getStatus());
        assertEquals(availableItem, booking.getItem());
        assertEquals(user, booking.getBooker());
        verify(itemRepository).findById(itemId);
        verify(userRepository).findById(userId);
        verify(bookingRepository).save(booking);
    }

    @Test
    void createBooking_whenItemNotFound_thenThrowNotFoundException() {
        // Given
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(userId, bookingInDto));
        assertEquals("Вещь с данным id не найдена", exception.getMessage());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void createBooking_whenUserNotFound_thenThrowNotFoundException() {
        // Given
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(availableItem));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(userId, bookingInDto));
        assertEquals("Пользователь с данным id не найден", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_whenItemNotAvailable_thenThrowNotAvailableItemException() {
        // Given
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(unavailableItem));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When & Then
        NotAvailableItemException exception = assertThrows(NotAvailableItemException.class,
                () -> bookingService.createBooking(userId, bookingInDto));
        assertEquals("Данная вещь не доступна для бронирования", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_whenEndBeforeStart_thenThrowInvalidDatesException() {
        // Given
        BookingInDto invalidBooking = new BookingInDto(end, start, itemId);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(availableItem));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When & Then
        InvalidDatesException exception = assertThrows(InvalidDatesException.class,
                () -> bookingService.createBooking(userId, invalidBooking));
        assertEquals("Дата окончания бронирования раньше даты начала", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBooking_whenValidApprovedTrue_thenReturnApprovedBooking() {
        // Given
        Booking approvedBooking = new Booking(bookingId, start, end, availableItem, user, Status.APPROVED);
        BookingOutDto approvedOutDto = BookingOutDto.builder().id(bookingId).status(Status.APPROVED).build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(approvedBooking);
        when(mapper.toBookingOutDto(approvedBooking)).thenReturn(approvedOutDto);

        // When
        BookingOutDto result = bookingService.approveBooking(ownerId, bookingId, true);

        // Then
        assertNotNull(result);
        assertEquals(Status.APPROVED, result.getStatus());
        assertEquals(Status.APPROVED, booking.getStatus());
        verify(bookingRepository).findById(bookingId);
        verify(bookingRepository).save(booking);
    }

    @Test
    void approveBooking_whenValidApprovedFalse_thenReturnRejectedBooking() {
        // Given
        Booking rejectedBooking = new Booking(bookingId, start, end, availableItem, user, Status.REJECTED);
        BookingOutDto rejectedOutDto = BookingOutDto.builder().id(bookingId).status(Status.REJECTED).build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(rejectedBooking);
        when(mapper.toBookingOutDto(rejectedBooking)).thenReturn(rejectedOutDto);

        // When
        BookingOutDto result = bookingService.approveBooking(ownerId, bookingId, false);

        // Then
        assertNotNull(result);
        assertEquals(Status.REJECTED, result.getStatus());
        assertEquals(Status.REJECTED, booking.getStatus());
    }

    @Test
    void approveBooking_whenBookingNotFound_thenThrowNotFoundException() {
        // Given
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(ownerId, bookingId, true));
        assertEquals("Бронирование не найдено", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBooking_whenNotOwner_thenThrowNoBookingFoundException() {
        // Given
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // When & Then - пользователь не владелец вещи
        NoBookingFoundException exception = assertThrows(NoBookingFoundException.class,
                () -> bookingService.approveBooking(userId, bookingId, true));
        assertEquals("Указанный идентификатор пользователя не совпадает с владельцем вещи", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBooking_whenAlreadyApproved_thenUpdateStatus() {
        // Given - бронирование уже подтверждено
        booking.setStatus(Status.APPROVED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(mapper.toBookingOutDto(booking)).thenReturn(bookingOutDto);

        // When - пытаемся отклонить уже подтвержденное
        BookingOutDto result = bookingService.approveBooking(ownerId, bookingId, false);

        // Then - статус должен измениться на REJECTED
        assertNotNull(result);
        assertEquals(Status.REJECTED, booking.getStatus());
    }

    @Test
    void getBookingById_whenValidBooker_thenReturnBooking() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(mapper.toBookingOutDto(booking)).thenReturn(bookingOutDto);

        // When
        BookingOutDto result = bookingService.getBookingById(userId, bookingId);

        // Then
        assertNotNull(result);
        assertEquals(bookingOutDto, result);
        verify(userRepository).findById(userId);
        verify(bookingRepository).findById(bookingId);
    }

    @Test
    void getBookingById_whenValidOwner_thenReturnBooking() {
        // Given
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(mapper.toBookingOutDto(booking)).thenReturn(bookingOutDto);

        // When
        BookingOutDto result = bookingService.getBookingById(ownerId, bookingId);

        // Then
        assertNotNull(result);
        assertEquals(bookingOutDto, result);
    }

    @Test
    void getBookingById_whenUserNotFound_thenThrowNotFoundException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(userId, bookingId));
        assertEquals("Пользователь с данным id не найден", exception.getMessage());
        verify(bookingRepository, never()).findById(any());
    }

    @Test
    void getBookingById_whenBookingNotFound_thenThrowNotFoundException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(userId, bookingId));
        assertEquals("Бронирование не найдено", exception.getMessage());
    }

    @Test
    void getBookingById_whenNotBookerOrOwner_thenThrowValidationException() {
        // Given
        User otherUser = new User(999L, "Other", "other@mail.ru");
        when(userRepository.findById(999L)).thenReturn(Optional.of(otherUser));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.getBookingById(999L, bookingId));
        assertEquals("Указанный идентификатор пользователя не совпадает с владельцем бронирования или владельцем вещи",
                exception.getMessage());
    }

    @Test
    void getAllUserBookings_whenStateAll_thenReturnAllBookings() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerId(userId, BookingServiceImpl.sort)).thenReturn(List.of(booking));
        when(mapper.toBookingOutDto(booking)).thenReturn(bookingOutDto);

        // When
        List<BookingOutDto> result = bookingService.getAllUserBookings(userId, "ALL");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findAllByBookerId(userId, BookingServiceImpl.sort);
    }

    @Test
    void getAllUserBookings_whenStateCurrent_thenReturnCurrentBookings() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfter(
                eq(userId), any(LocalDateTime.class), any(LocalDateTime.class), eq(BookingServiceImpl.sort)))
                .thenReturn(List.of(booking));
        when(mapper.toBookingOutDto(booking)).thenReturn(bookingOutDto);

        // When
        List<BookingOutDto> result = bookingService.getAllUserBookings(userId, "CURRENT");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findAllByBookerIdAndStartIsBeforeAndEndIsAfter(
                eq(userId), any(LocalDateTime.class), any(LocalDateTime.class), eq(BookingServiceImpl.sort));
    }

    @Test
    void getAllUserBookings_whenStatePast_thenReturnPastBookings() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerIdAndEndIsBefore(
                eq(userId), any(LocalDateTime.class), eq(BookingServiceImpl.sort)))
                .thenReturn(List.of(booking));
        when(mapper.toBookingOutDto(booking)).thenReturn(bookingOutDto);

        // When
        List<BookingOutDto> result = bookingService.getAllUserBookings(userId, "PAST");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findAllByBookerIdAndEndIsBefore(
                eq(userId), any(LocalDateTime.class), eq(BookingServiceImpl.sort));
    }

    @Test
    void getAllUserBookings_whenStateFuture_thenReturnFutureBookings() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerIdAndStartIsAfter(
                eq(userId), any(LocalDateTime.class), eq(BookingServiceImpl.sort)))
                .thenReturn(List.of(booking));
        when(mapper.toBookingOutDto(booking)).thenReturn(bookingOutDto);

        // When
        List<BookingOutDto> result = bookingService.getAllUserBookings(userId, "FUTURE");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findAllByBookerIdAndStartIsAfter(
                eq(userId), any(LocalDateTime.class), eq(BookingServiceImpl.sort));
    }

    @Test
    void getAllUserBookings_whenStateWaiting_thenReturnWaitingBookings() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerIdAndStatus(userId, Status.WAITING, BookingServiceImpl.sort))
                .thenReturn(List.of(booking));
        when(mapper.toBookingOutDto(booking)).thenReturn(bookingOutDto);

        // When
        List<BookingOutDto> result = bookingService.getAllUserBookings(userId, "WAITING");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findAllByBookerIdAndStatus(userId, Status.WAITING, BookingServiceImpl.sort);
    }

    @Test
    void getAllUserBookings_whenStateRejected_thenReturnRejectedBookings() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerIdAndStatus(userId, Status.REJECTED, BookingServiceImpl.sort))
                .thenReturn(List.of(booking));
        when(mapper.toBookingOutDto(booking)).thenReturn(bookingOutDto);

        // When
        List<BookingOutDto> result = bookingService.getAllUserBookings(userId, "REJECTED");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findAllByBookerIdAndStatus(userId, Status.REJECTED, BookingServiceImpl.sort);
    }

    @Test
    void getAllUserBookings_whenEmptyList_thenReturnEmptyList() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerId(userId, BookingServiceImpl.sort)).thenReturn(List.of());

        // When
        List<BookingOutDto> result = bookingService.getAllUserBookings(userId, "ALL");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllUserBookings_whenUserNotFound_thenThrowNotFoundException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getAllUserBookings(userId, "ALL"));
        assertEquals("Пользователь с данным id не найден", exception.getMessage());
        verify(bookingRepository, never()).findAllByBookerId(any(), any());
    }

    @Test
    void getAllItemBookings_whenStateAll_thenReturnAllBookings() {
        // Given
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerId(ownerId, BookingServiceImpl.sort)).thenReturn(List.of(booking));
        when(mapper.toBookingOutDto(booking)).thenReturn(bookingOutDto);

        // When
        List<BookingOutDto> result = bookingService.getAllItemBookings(ownerId, "ALL");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findAllByItemOwnerId(ownerId, BookingServiceImpl.sort);
    }

    @Test
    void getAllItemBookings_whenStateCurrent_thenReturnCurrentBookings() {
        // Given
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfter(
                eq(ownerId), any(LocalDateTime.class), any(LocalDateTime.class), eq(BookingServiceImpl.sort)))
                .thenReturn(List.of(booking));
        when(mapper.toBookingOutDto(booking)).thenReturn(bookingOutDto);

        // When
        List<BookingOutDto> result = bookingService.getAllItemBookings(ownerId, "CURRENT");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfter(
                eq(ownerId), any(LocalDateTime.class), any(LocalDateTime.class), eq(BookingServiceImpl.sort));
    }

    @Test
    void getAllItemBookings_whenDifferentStates_thenCallCorrectRepositoryMethods() {
        // Given
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdAndEndIsBefore(any(), any(), any())).thenReturn(List.of());
        when(bookingRepository.findAllByItemOwnerIdAndStartIsAfter(any(), any(), any())).thenReturn(List.of());
        when(bookingRepository.findAllByItemOwnerIdAndStatus(any(), any(), any())).thenReturn(List.of());

        // When - тестируем разные состояния
        bookingService.getAllItemBookings(ownerId, "PAST");
        bookingService.getAllItemBookings(ownerId, "FUTURE");
        bookingService.getAllItemBookings(ownerId, "WAITING");
        bookingService.getAllItemBookings(ownerId, "REJECTED");

        // Then - проверяем что вызывались правильные методы
        verify(bookingRepository).findAllByItemOwnerIdAndEndIsBefore(
                eq(ownerId), any(LocalDateTime.class), eq(BookingServiceImpl.sort));
        verify(bookingRepository).findAllByItemOwnerIdAndStartIsAfter(
                eq(ownerId), any(LocalDateTime.class), eq(BookingServiceImpl.sort));
        verify(bookingRepository).findAllByItemOwnerIdAndStatus(
                eq(ownerId), eq(Status.WAITING), eq(BookingServiceImpl.sort));
        verify(bookingRepository).findAllByItemOwnerIdAndStatus(
                eq(ownerId), eq(Status.REJECTED), eq(BookingServiceImpl.sort));
    }
}