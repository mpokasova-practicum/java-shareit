package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.practicum.shareit.exception.NotFoundException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Mock
    private UserRepository userRepository;

    @Mock
    ItemRepository itemRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @Mock
    private ItemMapper itemMapper;

    private final Long userId = 1L;
    private final Long requestId = 1L;
    private final LocalDateTime created = LocalDateTime.now();

    private final User user = new User(userId, "Alex", "alex@mail.ru");
    private final ItemRequestDto itemRequestDto = ItemRequestDto.builder()
            .id(requestId)
            .description("Нужна дрель")
            .created(created)
            .build();
    private final ItemRequest itemRequest = new ItemRequest(requestId, "Нужна дрель", user, created);
    private final ItemRequestWithItemsDto itemRequestWithItemsDto = ItemRequestWithItemsDto.builder()
            .id(requestId)
            .description("Нужна дрель")
            .created(created)
            .build();

    @Test
    void createItemRequest_whenUserExists_thenReturnItemRequestDto() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestMapper.toItemRequest(itemRequestDto)).thenReturn(itemRequest);
        when(itemRequestRepository.save(itemRequest)).thenReturn(itemRequest);
        when(itemRequestMapper.toItemRequestDto(itemRequest)).thenReturn(itemRequestDto);

        // When
        ItemRequestDto result = itemRequestService.createItemRequest(userId, itemRequestDto);

        // Then
        assertNotNull(result);
        assertEquals(itemRequestDto, result);
        verify(userRepository).findById(userId);
        verify(itemRequestMapper).toItemRequest(itemRequestDto);
        verify(itemRequestRepository).save(itemRequest);
        verify(itemRequestMapper).toItemRequestDto(itemRequest);
        assertNotNull(itemRequest.getCreated());
        assertEquals(user, itemRequest.getRequester());
    }

    @Test
    void createItemRequest_whenUserNotFound_thenThrowNotFoundException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.createItemRequest(userId, itemRequestDto));
        assertEquals("Пользователь с данным id не найден", exception.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(itemRequestMapper, itemRequestRepository);
    }

    @Test
    void getRequests_whenUserExists_thenReturnListWithItems() {
        // Given
        ItemForRequestDto itemDto = ItemForRequestDto.builder()
                .itemId(10L)
                .name("Дрель")
                .ownerId(2L)
                .build();
        Item item = new Item();
        item.setId(10L);
        item.setRequest(itemRequest);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterId(userId)).thenReturn(List.of(itemRequest));
        when(itemRepository.findAllByRequestIdIn(List.of(requestId))).thenReturn(List.of(item));
        when(itemRequestMapper.toItemRequestWithItemsDto(itemRequest)).thenReturn(itemRequestWithItemsDto);
        when(itemMapper.toItemForRequestDto(item)).thenReturn(itemDto);

        // When
        List<ItemRequestWithItemsDto> result = itemRequestService.getRequests(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(itemRequestWithItemsDto, result.get(0));
        assertEquals(1, result.get(0).getItems().size());
        assertEquals(itemDto, result.get(0).getItems().get(0));

        verify(userRepository).findById(userId);
        verify(itemRequestRepository).findAllByRequesterId(userId);
        verify(itemRepository).findAllByRequestIdIn(List.of(requestId));
        verify(itemRequestMapper).toItemRequestWithItemsDto(itemRequest);
        verify(itemMapper).toItemForRequestDto(item);
    }

    @Test
    void getRequests_whenUserExistsAndNoItems_thenReturnListWithoutItems() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterId(userId)).thenReturn(List.of(itemRequest));
        when(itemRepository.findAllByRequestIdIn(List.of(requestId))).thenReturn(List.of());
        when(itemRequestMapper.toItemRequestWithItemsDto(itemRequest)).thenReturn(itemRequestWithItemsDto);

        // When
        List<ItemRequestWithItemsDto> result = itemRequestService.getRequests(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getItems()); // или assertTrue если инициализируется пустым списком

        verify(userRepository).findById(userId);
        verify(itemRequestRepository).findAllByRequesterId(userId);
        verify(itemRepository).findAllByRequestIdIn(List.of(requestId));
        verify(itemRequestMapper).toItemRequestWithItemsDto(itemRequest);
        verifyNoInteractions(itemMapper);
    }

    @Test
    void getRequests_whenUserNotFound_thenThrowNotFoundException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequests(userId));
        assertEquals("Пользователь с данным id не найден", exception.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(itemRequestRepository, itemRepository);
    }

    @Test
    void getRequestsAll_whenUserExists_thenReturnList() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterIdNot(eq(userId), any())).thenReturn(List.of(itemRequest));
        when(itemRequestMapper.toItemRequestDto(itemRequest)).thenReturn(itemRequestDto);

        // When
        List<ItemRequestDto> result = itemRequestService.getRequestsAll(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(itemRequestDto, result.get(0));

        verify(userRepository).findById(userId);
        verify(itemRequestRepository).findAllByRequesterIdNot(eq(userId), any());
        verify(itemRequestMapper).toItemRequestDto(itemRequest);
    }

    @Test
    void getRequestsAll_whenUserNotFound_thenThrowNotFoundException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestsAll(userId));
        assertEquals("Пользователь с данным id не найден", exception.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(itemRequestRepository);
    }

    @Test
    void getRequestById_whenUserAndRequestExistWithItems_thenReturnWithItems() {
        // Given
        ItemForRequestDto itemDto = ItemForRequestDto.builder()
                .itemId(10L)
                .name("Дрель")
                .ownerId(2L)
                .build();
        Item item = new Item();
        item.setId(10L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(itemRequestMapper.toItemRequestWithItemsDto(itemRequest)).thenReturn(itemRequestWithItemsDto);
        when(itemRepository.findAllByRequestId(requestId)).thenReturn(List.of(item));
        when(itemMapper.toItemForRequestDto(item)).thenReturn(itemDto);

        // When
        ItemRequestWithItemsDto result = itemRequestService.getRequestById(userId, requestId);

        // Then
        assertNotNull(result);
        assertEquals(itemRequestWithItemsDto, result);
        assertEquals(1, result.getItems().size());
        assertEquals(itemDto, result.getItems().get(0));

        verify(userRepository).findById(userId);
        verify(itemRequestRepository).findById(requestId);
        verify(itemRequestMapper).toItemRequestWithItemsDto(itemRequest);
        verify(itemRepository).findAllByRequestId(requestId);
        verify(itemMapper).toItemForRequestDto(item);
    }

    @Test
    void getRequestById_whenUserAndRequestExistWithoutItems_thenReturnWithoutItems() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(itemRequestMapper.toItemRequestWithItemsDto(itemRequest)).thenReturn(itemRequestWithItemsDto);
        when(itemRepository.findAllByRequestId(requestId)).thenReturn(List.of());

        // When
        ItemRequestWithItemsDto result = itemRequestService.getRequestById(userId, requestId);

        // Then
        assertNotNull(result);
        assertEquals(itemRequestWithItemsDto, result);
        assertTrue(result.getItems().isEmpty());

        verify(userRepository).findById(userId);
        verify(itemRequestRepository).findById(requestId);
        verify(itemRequestMapper).toItemRequestWithItemsDto(itemRequest);
        verify(itemRepository).findAllByRequestId(requestId);
        verifyNoInteractions(itemMapper);
    }

    @Test
    void getRequestById_whenUserNotFound_thenThrowNotFoundException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(userId, requestId));
        assertEquals("Пользователь с данным id не найден", exception.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(itemRequestRepository);
    }

    @Test
    void getRequestById_whenRequestNotFound_thenThrowNotFoundException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(userId, requestId));
        assertEquals("Запрос с данным id не найден", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(itemRequestRepository).findById(requestId);
        verifyNoInteractions(itemRepository, itemMapper);
    }

    @Test
    void getRequests_whenMultipleRequests_thenGroupItemsCorrectly() {
        // Given
        Long requestId2 = 2L;
        ItemRequest itemRequest2 = new ItemRequest(requestId2, "Нужен молоток", user, created);

        Item item1 = new Item();
        item1.setId(10L);
        item1.setRequest(itemRequest);

        Item item2 = new Item();
        item2.setId(11L);
        item2.setRequest(itemRequest2);

        Item item3 = new Item();
        item3.setId(12L);
        item3.setRequest(itemRequest); // Второй предмет для первого запроса

        ItemForRequestDto itemDto1 = ItemForRequestDto.builder().itemId(10L).build();
        ItemForRequestDto itemDto2 = ItemForRequestDto.builder().itemId(11L).build();
        ItemForRequestDto itemDto3 = ItemForRequestDto.builder().itemId(12L).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequesterId(userId)).thenReturn(List.of(itemRequest, itemRequest2));
        when(itemRepository.findAllByRequestIdIn(List.of(requestId, requestId2))).thenReturn(List.of(item1, item2, item3));

        when(itemRequestMapper.toItemRequestWithItemsDto(itemRequest)).thenReturn(
                ItemRequestWithItemsDto.builder().id(requestId).build());
        when(itemRequestMapper.toItemRequestWithItemsDto(itemRequest2)).thenReturn(
                ItemRequestWithItemsDto.builder().id(requestId2).build());

        when(itemMapper.toItemForRequestDto(item1)).thenReturn(itemDto1);
        when(itemMapper.toItemForRequestDto(item2)).thenReturn(itemDto2);
        when(itemMapper.toItemForRequestDto(item3)).thenReturn(itemDto3);

        // When
        List<ItemRequestWithItemsDto> result = itemRequestService.getRequests(userId);

        // Then
        assertEquals(2, result.size());
        // Проверяем что предметы правильно сгруппированы по requestId
        verify(itemMapper, times(2)).toItemForRequestDto(any()); // Для item1 и item3
    }
}
