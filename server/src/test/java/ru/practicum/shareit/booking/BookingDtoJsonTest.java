package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeBookingInDto() throws Exception {
        LocalDateTime start = LocalDateTime.of(2023, 10, 15, 14, 30);
        LocalDateTime end = LocalDateTime.of(2023, 10, 16, 14, 30);
        BookingInDto dto = new BookingInDto(start, end, 1L);

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"start\":\"2023-10-15T14:30:00\"");
        assertThat(json).contains("\"end\":\"2023-10-16T14:30:00\"");
        assertThat(json).contains("\"itemId\":1");
    }

    @Test
    void shouldDeserializeBookingInDto() throws Exception {
        String json = """
            {
                "start": "2023-10-15T14:30:00",
                "end": "2023-10-16T14:30:00",
                "itemId": 1
            }
            """;

        BookingInDto dto = objectMapper.readValue(json, BookingInDto.class);

        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2023, 10, 15, 14, 30));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2023, 10, 16, 14, 30));
        assertThat(dto.getItemId()).isEqualTo(1L);
    }

    @Test
    void shouldSerializeBookingOutDto() throws Exception {
        LocalDateTime start = LocalDateTime.of(2023, 10, 15, 14, 30);
        LocalDateTime end = LocalDateTime.of(2023, 10, 16, 14, 30);

        BookingOutDto.Item item = new BookingOutDto.Item(1L, "Дрель");
        BookingOutDto.Booker booker = new BookingOutDto.Booker(2L, "Alex");

        BookingOutDto dto = BookingOutDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(Status.WAITING)
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"start\":\"2023-10-15T14:30:00\"");
        assertThat(json).contains("\"end\":\"2023-10-16T14:30:00\"");
        assertThat(json).contains("\"item\":");
        assertThat(json).contains("\"booker\":");
        assertThat(json).contains("\"status\":\"WAITING\"");
        assertThat(json).contains("\"name\":\"Дрель\"");
        assertThat(json).contains("\"name\":\"Alex\"");
    }

    @Test
    void shouldDeserializeBookingOutDto() throws Exception {
        String json = """
            {
                "id": 1,
                "start": "2023-10-15T14:30:00",
                "end": "2023-10-16T14:30:00",
                "item": {
                    "id": 10,
                    "name": "Дрель"
                },
                "booker": {
                    "id": 20,
                    "name": "Alex"
                },
                "status": "APPROVED"
            }
            """;

        BookingOutDto dto = objectMapper.readValue(json, BookingOutDto.class);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2023, 10, 15, 14, 30));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2023, 10, 16, 14, 30));
        assertThat(dto.getItem().getId()).isEqualTo(10L);
        assertThat(dto.getItem().getName()).isEqualTo("Дрель");
        assertThat(dto.getBooker().getId()).isEqualTo(20L);
        assertThat(dto.getBooker().getName()).isEqualTo("Alex");
        assertThat(dto.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    void itemInnerClassShouldWork() {
        BookingOutDto.Item item = new BookingOutDto.Item(1L, "Test Item");

        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getName()).isEqualTo("Test Item");
    }

    @Test
    void bookerInnerClassShouldWork() {
        BookingOutDto.Booker booker = new BookingOutDto.Booker(1L, "Test Booker");

        assertThat(booker.getId()).isEqualTo(1L);
        assertThat(booker.getName()).isEqualTo("Test Booker");
    }
}