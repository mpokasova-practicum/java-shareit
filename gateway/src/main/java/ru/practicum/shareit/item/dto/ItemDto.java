package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;

    @NotBlank(message = "Необходимо указать название вещи")
    private String name;

    @NotBlank(message = "Необходимо указать описание вещи")
    private String description;

    @NotNull(message = "Необходимо указать доступна ли вещь")
    private Boolean available;

    private Long requestId;
}
