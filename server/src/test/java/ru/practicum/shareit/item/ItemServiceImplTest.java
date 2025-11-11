package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.NoBookingFoundException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private final Long userId = 1L;
    private final Long itemId = 1L;
    private final Long requestId = 10L;
    private final LocalDateTime created = LocalDateTime.now();

    private final User user = new User(userId, "Alex", "alex@mail.ru");
    private final User booker = new User(2L, "Booker", "booker@mail.ru");
    private final ItemRequest itemRequest = new ItemRequest(requestId, "Need drill", user, created);
    private final Item item = new Item(itemId, user, "Drill", "Simple drill", true, itemRequest);
    private final ItemDto itemDto = new ItemDto(itemId, "Drill", "Simple drill", true, requestId);
    private final ItemWithDatesDto itemWithDatesDto = ItemWithDatesDto.builder()
            .id(itemId)
            .name("Drill")
            .description("Simple drill")
            .available(true)
            .build();
    private final Comment comment = new Comment(1L, "Great item!", item, user, created);
    private final CommentDto commentDto = CommentDto.builder()
            .id(1L)
            .text("Great item!")
            .authorName("Alex")
            .created(created)
            .build();
    private final Booking booking = new Booking(1L, LocalDateTime.now().minusDays(2),
            LocalDateTime.now().minusDays(1), item, booker, Status.APPROVED);

    @Test
    void getAllItems_whenUserExists_thenReturnItemsWithBookingsAndComments() {
        // Given
        BookingItemDto lastBookingDto = BookingItemDto.builder().id(1L).bookerId(2L).build();
        BookingItemDto nextBookingDto = BookingItemDto.builder().id(2L).bookerId(3L).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwnerId(userId)).thenReturn(List.of(item));
        when(bookingRepository.findLastBookingsForItems(anyList(), any())).thenReturn(List.of(booking));
        when(commentRepository.findAllByItemIdIn(anyList())).thenReturn(List.of(comment));
        when(itemMapper.toItemWithDatesDto(item)).thenReturn(itemWithDatesDto);
        when(bookingMapper.toBookingItemDto(booking)).thenReturn(lastBookingDto);
        when(commentMapper.toCommentDto(comment)).thenReturn(commentDto);

        // When
        List<ItemWithDatesDto> result = itemService.getAllItems(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findById(userId);
        verify(itemRepository).findAllByOwnerId(userId);
        verify(bookingRepository).findLastBookingsForItems(List.of(itemId), LocalDateTime.now());
        verify(commentRepository).findAllByItemIdIn(List.of(itemId));
    }

    @Test
    void getAllItems_whenUserNotFound_thenThrowNotFoundException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.getAllItems(userId));
        assertEquals("Пользователь с данным id не найден", exception.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(itemRepository);
    }

    @Test
    void getItemById_whenItemExists_thenReturnItemWithComments() {
        // Given
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemMapper.toItemWithDatesDto(item)).thenReturn(itemWithDatesDto);
        when(commentRepository.findAllByItemId(itemId)).thenReturn(List.of(comment));
        when(commentMapper.toCommentDto(comment)).thenReturn(commentDto);

        // When
        ItemWithDatesDto result = itemService.getItemById(itemId);

        // Then
        assertNotNull(result);
        assertEquals(itemWithDatesDto, result);
        verify(itemRepository).findById(itemId);
        verify(commentRepository).findAllByItemId(itemId);
        verify(commentMapper).toCommentDto(comment);
    }

    @Test
    void getItemById_whenItemNotFound_thenThrowNotFoundException() {
        // Given
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(itemId));
        assertEquals("Вещь с данным id не найдена", exception.getMessage());
        verify(itemRepository).findById(itemId);
    }

    @Test
    void getItemById_whenItemWithoutComments_thenReturnItemWithEmptyComments() {
        // Given
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemMapper.toItemWithDatesDto(item)).thenReturn(itemWithDatesDto);
        when(commentRepository.findAllByItemId(itemId)).thenReturn(List.of());

        // When
        ItemWithDatesDto result = itemService.getItemById(itemId);

        // Then
        assertNotNull(result);
        assertEquals(itemWithDatesDto, result);
        verify(commentRepository).findAllByItemId(itemId);
        verifyNoInteractions(commentMapper);
    }

    @Test
    void createItem_whenValidWithRequest_thenReturnItemDto() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemMapper.toItem(itemDto)).thenReturn(item);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        // When
        ItemDto result = itemService.createItem(userId, itemDto);

        // Then
        assertNotNull(result);
        assertEquals(itemDto, result);
        assertEquals(user, item.getOwner());
        assertEquals(itemRequest, item.getRequest());
        verify(userRepository).findById(userId);
        verify(itemRequestRepository).findById(requestId);
        verify(itemRepository).save(item);
    }

    @Test
    void createItem_whenValidWithoutRequest_thenReturnItemDto() {
        // Given
        ItemDto itemDtoWithoutRequest = new ItemDto(itemId, "Drill", "Simple drill", true, null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemMapper.toItem(itemDtoWithoutRequest)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(itemDtoWithoutRequest);

        // When
        ItemDto result = itemService.createItem(userId, itemDtoWithoutRequest);

        // Then
        assertNotNull(result);
        assertEquals(itemDtoWithoutRequest, result);
        assertEquals(user, item.getOwner());
        assertNull(item.getRequest());
        verify(itemRequestRepository, never()).findById(any());
    }

    @Test
    void createItem_whenUserNotFound_thenThrowNotFoundException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.createItem(userId, itemDto));
        assertEquals("Пользователь с данным id не найден", exception.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(itemRepository);
    }

    @Test
    void createItem_whenRequestNotFound_thenThrowNotFoundException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemMapper.toItem(itemDto)).thenReturn(item);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.createItem(userId, itemDto));
        assertEquals("Запрос с данным id не найден", exception.getMessage());
        verify(itemRequestRepository).findById(requestId);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_whenValidPartialUpdate_thenReturnUpdatedItem() {
        // Given
        ItemDto updateDto = new ItemDto(null, "New Drill", null, null, null);
        Item updatedItem = new Item(itemId, user, "New Drill", "Simple drill", true, itemRequest);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.save(item)).thenReturn(updatedItem);
        when(itemMapper.toItemDto(updatedItem)).thenReturn(
                new ItemDto(itemId, "New Drill", "Simple drill", true, requestId));

        // When
        ItemDto result = itemService.updateItem(userId, itemId, updateDto);

        // Then
        assertNotNull(result);
        assertEquals("New Drill", result.getName());
        assertEquals("Simple drill", result.getDescription()); // Старое значение сохранилось
        verify(itemRepository).findById(itemId);
        verify(userRepository).findById(userId);
        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_whenNotOwner_thenThrowValidationException() {
        // Given
        User otherUser = new User(999L, "Other", "other@mail.ru");
        ItemDto updateDto = new ItemDto(null, "New Name", null, null, null);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(otherUser));

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.updateItem(userId, itemId, updateDto));
        assertEquals("Указанный идентификатор пользователя не совпадает с идентификатором владельца",
                exception.getMessage());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_whenItemNotFound_thenThrowNotFoundException() {
        // Given
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(userId, itemId, itemDto));
        assertEquals("Вещь с данным id не найдена", exception.getMessage());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void updateItem_whenUserNotFound_thenThrowNotFoundException() {
        // Given
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(userId, itemId, itemDto));
        assertEquals("Пользователь с данным id не найден", exception.getMessage());
    }

    @Test
    void updateItem_whenNullIds_thenThrowValidationException() {
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.updateItem(null, null, itemDto));
        assertEquals("Id должен быть указан", exception.getMessage());
        verifyNoInteractions(itemRepository, userRepository);
    }

    @Test
    void deleteItem_whenValid_thenDeleteItem() {
        // Given
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        itemService.deleteItem(userId, itemId);

        // Then
        verify(itemRepository).deleteById(itemId);
        verify(itemRepository).findById(itemId);
        verify(userRepository).findById(userId);
    }

    @Test
    void deleteItem_whenNotOwner_thenThrowValidationException() {
        // Given
        User otherUser = new User(999L, "Other", "other@mail.ru");
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(otherUser));

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.deleteItem(userId, itemId));
        assertEquals("Указанный идентификатор пользователя не совпадает с идентификатором владельца",
                exception.getMessage());
        verify(itemRepository, never()).deleteById(any());
    }

    @Test
    void searchItems_whenValidText_thenReturnMatchingItems() {
        // Given
        String searchText = "drill";
        when(itemRepository.searchItems(searchText)).thenReturn(List.of(item));
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        // When
        List<ItemDto> result = itemService.searchItems(searchText);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(itemDto, result.get(0));
        verify(itemRepository).searchItems(searchText);
    }

    @Test
    void searchItems_whenEmptyText_thenReturnEmptyList() {
        // When
        List<ItemDto> result = itemService.searchItems("");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).searchItems(any());
    }

    @Test
    void searchItems_whenNullText_thenReturnEmptyList() {
        // When
        List<ItemDto> result = itemService.searchItems(null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).searchItems(any());
    }

    @Test
    void searchItems_whenBlankText_thenReturnEmptyList() {
        // When
        List<ItemDto> result = itemService.searchItems("   ");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).searchItems(any());
    }

    @Test
    void createComment_whenValid_thenReturnCommentDto() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItemIdAndBookerIdAndStatusAndEndIsBefore(
                eq(itemId), eq(userId), eq(Status.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(commentMapper.toComment(commentDto)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.toCommentDto(comment)).thenReturn(commentDto);

        // When
        CommentDto result = itemService.createComment(userId, itemId, commentDto);

        // Then
        assertNotNull(result);
        assertEquals(commentDto, result);
        assertEquals(item, comment.getItem());
        assertEquals(user, comment.getAuthor());
        assertNotNull(comment.getCreated());
        verify(bookingRepository).findAllByItemIdAndBookerIdAndStatusAndEndIsBefore(
                eq(itemId), eq(userId), eq(Status.APPROVED), any(LocalDateTime.class));
        verify(commentRepository).save(comment);
    }

    @Test
    void createComment_whenNoCompletedBooking_thenThrowNoBookingFoundException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItemIdAndBookerIdAndStatusAndEndIsBefore(
                eq(itemId), eq(userId), eq(Status.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // When & Then
        NoBookingFoundException exception = assertThrows(NoBookingFoundException.class,
                () -> itemService.createComment(userId, itemId, commentDto));
        assertEquals("Не найдено завершенное бронирование данной вещи", exception.getMessage());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_whenUserNotFound_thenThrowNotFoundException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.createComment(userId, itemId, commentDto));
        assertEquals("Пользователь с данным id не найден", exception.getMessage());
        verify(itemRepository, never()).findById(any());
    }

    @Test
    void createComment_whenItemNotFound_thenThrowNotFoundException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.createComment(userId, itemId, commentDto));
        assertEquals("Вещь с данным id не найдена", exception.getMessage());
        verify(bookingRepository, never()).findAllByItemIdAndBookerIdAndStatusAndEndIsBefore(any(), any(), any(), any());
    }

    @Test
    void getAllItems_whenMultipleItems_thenCorrectlyMapBookingsAndComments() {
        // Given
        Item item2 = new Item(2L, user,"Hammer", "Simple hammer", true, null);
        Comment comment2 = new Comment(2L, "Good hammer", item2, user, created);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwnerId(userId)).thenReturn(List.of(item, item2));
        when(bookingRepository.findLastBookingsForItems(anyList(), any())).thenReturn(List.of(booking));
        when(commentRepository.findAllByItemIdIn(anyList())).thenReturn(List.of(comment, comment2));
        when(itemMapper.toItemWithDatesDto(any())).thenReturn(itemWithDatesDto);
        when(bookingMapper.toBookingItemDto(any())).thenReturn(BookingItemDto.builder().build());
        when(commentMapper.toCommentDto(any())).thenReturn(commentDto);

        // When
        List<ItemWithDatesDto> result = itemService.getAllItems(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookingRepository).findLastBookingsForItems(List.of(itemId, 2L), LocalDateTime.now());
        verify(commentRepository).findAllByItemIdIn(List.of(itemId, 2L));
        verify(commentMapper, times(2)).toCommentDto(any());
    }
}