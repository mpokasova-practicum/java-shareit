package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDatesDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeItemDto() throws Exception {
        ItemDto dto = new ItemDto(1L, "Дрель", "Простая дрель", true, 10L);

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Дрель\"");
        assertThat(json).contains("\"description\":\"Простая дрель\"");
        assertThat(json).contains("\"available\":true");
        assertThat(json).contains("\"requestId\":10");
    }

    @Test
    void shouldDeserializeItemDto() throws Exception {
        String json = "{\"id\": 1, \"name\": \"Дрель\", \"description\": \"Простая дрель\", \"available\": true, \"requestId\": 10}";

        ItemDto dto = objectMapper.readValue(json, ItemDto.class);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Простая дрель");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(10L);
    }

    @Test
    void shouldSerializeItemWithDatesDto() throws Exception {
        BookingItemDto lastBooking = BookingItemDto.builder().id(1L).bookerId(2L).build();
        BookingItemDto nextBooking = BookingItemDto.builder().id(2L).bookerId(3L).build();
        CommentDto comment = CommentDto.builder()
                .id(1L)
                .text("Отличный товар!")
                .authorName("Alex")
                .created(LocalDateTime.of(2023, 10, 15, 14, 30))
                .build();

        ItemWithDatesDto dto = ItemWithDatesDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(List.of(comment))
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Дрель\"");
        assertThat(json).contains("\"lastBooking\":");
        assertThat(json).contains("\"nextBooking\":");
        assertThat(json).contains("\"comments\":[");
        assertThat(json).contains("\"text\":\"Отличный товар!\"");
    }

    @Test
    void shouldSerializeCommentDto() throws Exception {
        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("Отличный товар!")
                .authorName("Alex")
                .created(LocalDateTime.of(2023, 10, 15, 14, 30))
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"text\":\"Отличный товар!\"");
        assertThat(json).contains("\"authorName\":\"Alex\"");
        assertThat(json).contains("\"created\":\"2023-10-15T14:30:00\"");
    }

    @Test
    void shouldDeserializeCommentDto() throws Exception {
        String json = """
            {
                "id": 1,
                "text": "Отличный товар!",
                "authorName": "Alex",
                "created": "2023-10-15T14:30:00"
            }
            """;

        CommentDto dto = objectMapper.readValue(json, CommentDto.class);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("Отличный товар!");
        assertThat(dto.getAuthorName()).isEqualTo("Alex");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2023, 10, 15, 14, 30));
    }
}
