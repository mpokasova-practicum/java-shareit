package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDatesDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
        import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mvc;

    private final Long userId = 1L;
    private final Long itemId = 1L;
    private final LocalDateTime created = LocalDateTime.now();

    private final ItemDto itemDto = new ItemDto(
            itemId,
            "Дрель",
            "Простая дрель",
            true,
            null
    );

    private final BookingItemDto lastBooking = BookingItemDto.builder()
            .id(1L)
            .bookerId(2L)
            .build();

    private final BookingItemDto nextBooking = BookingItemDto.builder()
            .id(2L)
            .bookerId(3L)
            .build();

    private final CommentDto commentDto = CommentDto.builder()
            .id(1L)
            .text("Отличная дрель!")
            .authorName("Alex")
            .created(created)
            .build();

    private final ItemWithDatesDto itemWithDatesDto = ItemWithDatesDto.builder()
            .id(itemId)
            .name("Дрель")
            .description("Простая дрель")
            .available(true)
            .lastBooking(lastBooking)
            .nextBooking(nextBooking)
            .comments(List.of(commentDto))
            .build();

    @Test
    void getAllItems_whenValid_thenReturnListOfItemWithDatesDto() throws Exception {
        when(itemService.getAllItems(userId))
                .thenReturn(List.of(itemWithDatesDto));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemWithDatesDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemWithDatesDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemWithDatesDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemWithDatesDto.getAvailable())))
                .andExpect(jsonPath("$[0].lastBooking.id", is(lastBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.id", is(nextBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].comments[0].id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].comments[0].text", is(commentDto.getText())));
    }

    @Test
    void getAllItems_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mvc.perform(get("/items"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemById_whenValid_thenReturnItemWithDatesDto() throws Exception {
        when(itemService.getItemById(itemId))
                .thenReturn(itemWithDatesDto);

        mvc.perform(get("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemWithDatesDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemWithDatesDto.getName())))
                .andExpect(jsonPath("$.description", is(itemWithDatesDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemWithDatesDto.getAvailable())))
                .andExpect(jsonPath("$.lastBooking.id", is(lastBooking.getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.id", is(nextBooking.getId()), Long.class))
                .andExpect(jsonPath("$.comments[0].text", is(commentDto.getText())));
    }

    @Test
    void getItemById_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mvc.perform(get("/items/{id}", itemId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItem_whenValid_thenReturnItemDto() throws Exception {
        when(itemService.createItem(anyLong(), any(ItemDto.class)))
                .thenReturn(itemDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void createItem_whenWithRequestId_thenReturnItemDtoWithRequestId() throws Exception {
        ItemDto itemWithRequest = new ItemDto(itemId, "Дрель", "Простая дрель", true, 10L);
        when(itemService.createItem(anyLong(), any(ItemDto.class)))
                .thenReturn(itemWithRequest);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(itemWithRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId", is(10), Long.class));
    }

    @Test
    void createItem_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_whenValid_thenReturnUpdatedItemDto() throws Exception {
        ItemDto updatedItem = new ItemDto(itemId, "Новая дрель", "Обновленное описание", false, null);
        when(itemService.updateItem(anyLong(), anyLong(), any(ItemDto.class)))
                .thenReturn(updatedItem);

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(updatedItem))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Новая дрель")))
                .andExpect(jsonPath("$.description", is("Обновленное описание")))
                .andExpect(jsonPath("$.available", is(false)));
    }

    @Test
    void updateItem_whenPartialUpdate_thenReturnUpdatedItemDto() throws Exception {
        // Обновляем только имя
        String partialUpdateJson = "{\"name\": \"Новое имя\"}";
        ItemDto updatedItem = new ItemDto(itemId, "Новое имя", "Простая дрель", true, null);
        when(itemService.updateItem(anyLong(), anyLong(), any(ItemDto.class)))
                .thenReturn(updatedItem);

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .content(partialUpdateJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Новое имя")));
    }

    @Test
    void updateItem_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mvc.perform(patch("/items/{itemId}", itemId)
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteItem_whenValid_thenReturnOk() throws Exception {
        mvc.perform(delete("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @Test
    void deleteItem_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mvc.perform(delete("/items/{id}", itemId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchItems_whenValidText_thenReturnListOfItemDto() throws Exception {
        when(itemService.searchItems("дрель"))
                .thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", "дрель")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())));
    }

    @Test
    void searchItems_whenEmptyText_thenReturnEmptyList() throws Exception {
        when(itemService.searchItems(""))
                .thenReturn(List.of());

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", "")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    void searchItems_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchItems_whenMissingTextParam_thenReturnBadRequest() throws Exception {
        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_whenValid_thenReturnCommentDto() throws Exception {
        when(itemService.createComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenReturn(commentDto);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    void createComment_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .content(mapper.writeValueAsString(commentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllItems_whenEmptyList_thenReturnEmptyArray() throws Exception {
        when(itemService.getAllItems(userId))
                .thenReturn(List.of());

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    void searchItems_whenNoResults_thenReturnEmptyList() throws Exception {
        when(itemService.searchItems("несуществующий"))
                .thenReturn(List.of());

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", "несуществующий")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    void getItemById_whenItemWithoutBookingsAndComments_thenReturnItemWithNulls() throws Exception {
        ItemWithDatesDto itemWithoutDetails = ItemWithDatesDto.builder()
                .id(itemId)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .lastBooking(null)
                .nextBooking(null)
                .comments(List.of())
                .build();

        when(itemService.getItemById(itemId))
                .thenReturn(itemWithoutDetails);

        mvc.perform(get("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastBooking").doesNotExist())
                .andExpect(jsonPath("$.nextBooking").doesNotExist())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()", is(0)));
    }
}
