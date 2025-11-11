package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemForRequestDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ItemRequestWithItemsDto {
    private Long id;

    @NotBlank
    private String description;

    private LocalDateTime created;

    private List<ItemForRequestDto> items;
}
