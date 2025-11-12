package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Autowired
    private JacksonTester<ItemRequestWithItemsDto> jsonLong;

    @Test
    void testItemRequestDto() throws Exception {
        LocalDateTime created = LocalDateTime.now();
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("item request description")
                .created(created)
                .build();

        JsonContent<ItemRequestDto> result = json.write(itemRequestDto);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(itemRequestDto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(itemRequestDto.getDescription());
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(itemRequestDto.getCreated().format(formatter));

        ItemRequestDto itemRequestDtoForTest = json.parseObject(result.getJson());

        assertThat(itemRequestDtoForTest).isEqualTo(itemRequestDto);
    }

    @Test
    void testItemRequestWithItemsDto() throws Exception {
        LocalDateTime created = LocalDateTime.of(2025, 10, 15, 14, 30);
        ItemForRequestDto itemDto = ItemForRequestDto.builder()
                .itemId(10L)
                .name("Дрель")
                .ownerId(2L)
                .build();

        ItemRequestWithItemsDto dto = ItemRequestWithItemsDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(created)
                .items(List.of(itemDto))
                .build();

        JsonContent<ItemRequestWithItemsDto> result = jsonLong.write(dto);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(dto.getCreated().format(formatter));
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo(itemDto.getName());

        // ИСПРАВЛЕННАЯ СТРОКА - используем extractingJsonPathNumberValue для чисел
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(itemDto.getOwnerId().intValue());
        // Или также для itemId
        assertThat(result).extractingJsonPathNumberValue("$.items[0].itemId").isEqualTo(itemDto.getItemId().intValue());

        ItemRequestWithItemsDto itemRequestDtoForTest = jsonLong.parseObject(result.getJson());

        assertThat(itemRequestDtoForTest).isEqualTo(dto);
    }
}
