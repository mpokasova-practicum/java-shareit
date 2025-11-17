package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    @NotBlank(message = "Необходимо указать имя пользователя")
    private String name;
    @Email(message = "Недопустимый формат email")
    @NotBlank(message = "Необходимо указать email пользователя")
    private String email;
}

