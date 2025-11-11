package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;

import static org.mockito.ArgumentMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemRequestService itemRequestService;

    private final Long userId = 1L;
    private final Long requestId = 1L;
    private final Long itemId = 10L;
    private final Long ownerId = 2L;
    private final LocalDateTime created = LocalDateTime.now();

    private final ItemRequestDto itemRequestDto = ItemRequestDto.builder()
            .id(requestId)
            .description("description")
            .created(created)
            .build();

    private final ItemForRequestDto itemForRequestDto = ItemForRequestDto.builder()
            .itemId(itemId)
            .name("name")
            .ownerId(ownerId)
            .build();

    private final ItemRequestWithItemsDto itemRequestWithItemsDto = ItemRequestWithItemsDto.builder()
            .id(requestId)
            .description("description")
            .created(created)
            .items(List.of(itemForRequestDto))
            .build();

    @Test
    void createItemRequest() throws Exception {
        Mockito.when(itemRequestService.createItemRequest(anyLong(), any(ItemRequestDto.class)))
                .thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    void getRequests() throws Exception {
        Mockito.when(itemRequestService.getRequests(anyLong()))
                .thenReturn(List.of(itemRequestWithItemsDto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestWithItemsDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestWithItemsDto.getDescription())))
                .andExpect(jsonPath("$[0].items[0].itemId", is(itemForRequestDto.getItemId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].name", is(itemForRequestDto.getName())))
                .andExpect(jsonPath("$[0].items[0].ownerId", is(itemForRequestDto.getOwnerId()), Long.class));
    }

    @Test
    void getRequestsAll() throws Exception {
        Mockito.when(itemRequestService.getRequestsAll(anyLong()))
                .thenReturn(List.of(itemRequestDto));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$[0].created").exists());
    }

    @Test
    void getRequestById() throws Exception {
        Mockito.when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenReturn(itemRequestWithItemsDto);

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestWithItemsDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestWithItemsDto.getDescription())))
                .andExpect(jsonPath("$.items[0].itemId", is(itemForRequestDto.getItemId()), Long.class))
                .andExpect(jsonPath("$.items[0].name", is(itemForRequestDto.getName())))
                .andExpect(jsonPath("$.items[0].ownerId", is(itemForRequestDto.getOwnerId()), Long.class));
    }
}
