package ru.practicum.shareit.request;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
public class ItemRequestServiceImplIntegrationTest {
    @Autowired
    private ItemRequestServiceImpl itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User user1;
    private User user2;
    private ItemRequestDto itemRequestDto;
    private LocalDateTime created;

    @BeforeEach
    void setUp() {
        // Очистка базы данных перед каждым тестом
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();

        // Создание тестовых пользователей
        user1 = userRepository.save(new User(null, "Alex", "alex@mail.ru"));
        user2 = userRepository.save(new User(null, "Maria", "maria@mail.ru"));

        created = LocalDateTime.now().withNano(0);
        itemRequestDto = ItemRequestDto.builder()
                .description("Нужна дрель для ремонта")
                .build();
    }

    @Test
    void createItemRequest_whenValid_thenSavedAndReturned() {
        // When
        ItemRequestDto result = itemRequestService.createItemRequest(user1.getId(), itemRequestDto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Нужна дрель для ремонта", result.getDescription());
        assertNotNull(result.getCreated());

        // Проверяем, что запрос действительно сохранен в БД
        List<ItemRequest> allRequests = itemRequestRepository.findAll();
        assertEquals(1, allRequests.size());
        assertEquals(result.getId(), allRequests.get(0).getId());
    }

    @Test
    void createItemRequest_whenUserNotExists_thenThrowNotFoundException() {
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.createItemRequest(999L, itemRequestDto));

        assertEquals("Пользователь с данным id не найден", exception.getMessage());
    }

    @Test
    void getRequests_whenUserHasRequestsWithItems_thenReturnRequestsWithItems() {
        // Given
        ItemRequestDto createdRequest = itemRequestService.createItemRequest(user1.getId(), itemRequestDto);

        // Создаем предмет для этого запроса
        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Простая дрель");
        item.setAvailable(true);
        item.setOwner(user2);
        item.setRequest(itemRequestRepository.findById(createdRequest.getId()).get());
        itemRepository.save(item);

        // When
        List<ItemRequestWithItemsDto> result = itemRequestService.getRequests(user1.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        ItemRequestWithItemsDto requestWithItems = result.get(0);
        assertEquals(createdRequest.getId(), requestWithItems.getId());
        assertEquals("Нужна дрель для ремонта", requestWithItems.getDescription());
        assertNotNull(requestWithItems.getItems());
        assertEquals(1, requestWithItems.getItems().size());

        ItemForRequestDto itemDto = requestWithItems.getItems().get(0);
        assertEquals("Дрель", itemDto.getName());
        assertEquals(user2.getId(), itemDto.getOwnerId());
    }

    @Test
    void getRequests_whenUserHasRequestsWithoutItems_thenReturnRequestsWithEmptyItems() {
        // Given
        itemRequestService.createItemRequest(user1.getId(), itemRequestDto);

        // When
        List<ItemRequestWithItemsDto> result = itemRequestService.getRequests(user1.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getItems().isEmpty());
    }

    @Test
    void getRequests_whenUserHasNoRequests_thenReturnEmptyList() {
        // When
        List<ItemRequestWithItemsDto> result = itemRequestService.getRequests(user1.getId());

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getRequestsAll_whenOtherUsersHaveRequests_thenReturnTheirRequests() {
        // Given
        // user1 создает запрос
        itemRequestService.createItemRequest(user1.getId(), itemRequestDto);

        // user2 создает запрос
        ItemRequestDto request2 = ItemRequestDto.builder()
                .description("Нужен молоток")
                .build();
        itemRequestService.createItemRequest(user2.getId(), request2);

        // When: user1 запрашивает все запросы (кроме своих)
        List<ItemRequestDto> result = itemRequestService.getRequestsAll(user1.getId());

        // Then: должен получить только запрос user2
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Нужен молоток", result.get(0).getDescription());
    }

    @Test
    void getRequestsAll_whenNoOtherRequests_thenReturnEmptyList() {
        // Given - только запросы текущего пользователя
        itemRequestService.createItemRequest(user1.getId(), itemRequestDto);

        // When
        List<ItemRequestDto> result = itemRequestService.getRequestsAll(user1.getId());

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getRequestsAll_whenMultipleOtherUsersHaveRequests_thenReturnAllSortedByCreatedDesc() throws InterruptedException {
        // Given
        ItemRequestDto request1 = itemRequestService.createItemRequest(user1.getId(),
                ItemRequestDto.builder().description("Первый запрос").build());

        Thread.sleep(10); // Чтобы временные метки отличались

        ItemRequestDto request2 = itemRequestService.createItemRequest(user2.getId(),
                ItemRequestDto.builder().description("Второй запрос").build());

        // When: user3 запрашивает все запросы
        User user3 = userRepository.save(new User(null, "Third", "third@mail.ru"));
        List<ItemRequestDto> result = itemRequestService.getRequestsAll(user3.getId());

        // Then: должны вернуться оба запроса в порядке убывания даты создания
        assertNotNull(result);
        assertEquals(2, result.size());
        // Более поздний запрос должен быть первым
        assertEquals("Второй запрос", result.get(0).getDescription());
        assertEquals("Первый запрос", result.get(1).getDescription());
    }

    @Test
    void getRequestById_whenValid_thenReturnRequestWithItems() {
        // Given
        ItemRequestDto createdRequest = itemRequestService.createItemRequest(user1.getId(), itemRequestDto);

        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Простая дрель");
        item.setAvailable(true);
        item.setOwner(user2);
        item.setRequest(itemRequestRepository.findById(createdRequest.getId()).get());
        itemRepository.save(item);

        // When
        ItemRequestWithItemsDto result = itemRequestService.getRequestById(user2.getId(), createdRequest.getId());

        // Then
        assertNotNull(result);
        assertEquals(createdRequest.getId(), result.getId());
        assertEquals("Нужна дрель для ремонта", result.getDescription());
        assertEquals(1, result.getItems().size());
        assertEquals("Дрель", result.getItems().get(0).getName());
    }

    @Test
    void getRequestById_whenRequestNotExists_thenThrowNotFoundException() {
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(user1.getId(), 999L));

        assertEquals("Запрос с данным id не найден", exception.getMessage());
    }

    @Test
    void getRequestById_whenUserNotExists_thenThrowNotFoundException() {
        // Given
        ItemRequestDto createdRequest = itemRequestService.createItemRequest(user1.getId(), itemRequestDto);

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(999L, createdRequest.getId()));

        assertEquals("Пользователь с данным id не найден", exception.getMessage());
    }

    @Test
    void getRequestById_whenRequestHasNoItems_thenReturnRequestWithEmptyItems() {
        // Given
        ItemRequestDto createdRequest = itemRequestService.createItemRequest(user1.getId(), itemRequestDto);

        // When
        ItemRequestWithItemsDto result = itemRequestService.getRequestById(user2.getId(), createdRequest.getId());

        // Then
        assertNotNull(result);
        assertEquals(createdRequest.getId(), result.getId());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void complexScenario_multipleUsersMultipleRequestsWithItems() {
        // Given: сложный сценарий с несколькими пользователями и запросами
        User user3 = userRepository.save(new User(null, "Petr", "petr@mail.ru"));

        // user1 создает 2 запроса
        ItemRequestDto request1 = itemRequestService.createItemRequest(user1.getId(),
                ItemRequestDto.builder().description("Запрос 1 от user1").build());
        ItemRequestDto request2 = itemRequestService.createItemRequest(user1.getId(),
                ItemRequestDto.builder().description("Запрос 2 от user1").build());

        // user2 создает 1 запрос
        ItemRequestDto request3 = itemRequestService.createItemRequest(user2.getId(),
                ItemRequestDto.builder().description("Запрос от user2").build());

        // Добавляем предметы к запросам
        Item item1 = createItemForRequest("Предмет 1", user2, request1.getId());
        Item item2 = createItemForRequest("Предмет 2", user3, request1.getId()); // Два предмета для первого запроса
        Item item3 = createItemForRequest("Предмет 3", user3, request3.getId());

        // When: user3 запрашивает все запросы (кроме своих)
        List<ItemRequestDto> allRequests = itemRequestService.getRequestsAll(user3.getId());

        // When: user1 запрашивает свои запросы
        List<ItemRequestWithItemsDto> user1Requests = itemRequestService.getRequests(user1.getId());

        // Then: проверяем результаты
        // user3 должен видеть запросы user1 и user2
        assertEquals(3, allRequests.size());

        // user1 должен видеть свои 2 запроса
        assertEquals(2, user1Requests.size());

        // Первый запрос user1 должен иметь 2 предмета
        ItemRequestWithItemsDto firstRequest = user1Requests.stream()
                .filter(req -> req.getId().equals(request1.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(2, firstRequest.getItems().size());

        // Второй запрос user1 не должен иметь предметов
        ItemRequestWithItemsDto secondRequest = user1Requests.stream()
                .filter(req -> req.getId().equals(request2.getId()))
                .findFirst()
                .orElseThrow();
        assertTrue(secondRequest.getItems().isEmpty());
    }

    private Item createItemForRequest(String name, User owner, Long requestId) {
        Item item = new Item();
        item.setName(name);
        item.setDescription("Описание " + name);
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(itemRequestRepository.findById(requestId).get());
        return itemRepository.save(item);
    }
}
