package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ItemRequestDto {
    private Long id;

    @NotBlank(message = "Необходимо указать описание запроса")
    private String description;

    private LocalDateTime created;
}
