package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserMapper userMapper;

    private final User user = new User(1L, "Ivanov Ivan", "ivanov@gmail.com");
    private final UserDto userDto = new UserDto(1L, "Ivanov Ivan", "ivanov@gmail.com");
    private final User user2 = new User(2L, "Petrov Petr", "petrov@gmail.com");
    private final List<User> users = List.of(user, user2);

    @Test
    void getAllUsers_shouldReturnListNotEmpty() {
        when(userRepository.findAll())
                .thenReturn(users);

        List<UserDto> expectedList = Stream.of(user, user2)
                .map(UserMapper::toUserDto).toList();
        List<UserDto> actualList = userService.getAllUsers();

        assertEquals(expectedList, actualList);
    }

    @Test
    void getUserById_shouldReturnUser() {
        when(userRepository.findById(any()))
                .thenReturn(Optional.of(user));
        UserDto actualUser = userService.getUserById(user.getId());

        assertEquals(userDto, actualUser);
    }

    @Test
    void getUserById_shouldReturnNotFoundException() {
        when(userRepository.findById(any()))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createUser_shouldCreateUser() {
        // Given
        UserDto inputDto = new UserDto(null, "Alex", "alex@mail.ru");
        User userToSave = new User(null, "Alex", "alex@mail.ru");
        User savedUser = new User(1L, "Alex", "alex@mail.ru");
        UserDto expectedDto = new UserDto(1L, "Alex", "alex@mail.ru");

        when(userRepository.findByEmail("alex@mail.ru")).thenReturn(Optional.empty());
        when(userMapper.toUser(inputDto)).thenReturn(userToSave);
        when(userRepository.save(userToSave)).thenReturn(savedUser);

        // When
        UserDto actualUser = userService.createUser(inputDto);

        // Then
        assertEquals(expectedDto, actualUser);
    }

    @Test
    void createUser_shouldReturnValidationException() {
        // Given
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> userService.createUser(userDto))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Пользователь с таким email уже существует");

        verify(userRepository).findByEmail(userDto.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldReturnNotFoundException() {
        when(userRepository.findById(any()))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateUser(1L, userDto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete() {
        User user4 = new User(4L, "Mike", "mike.d@yandex.ru");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user4));

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository, Mockito.times(1)).findById(1L);
        verify(userRepository, Mockito.times(1)).deleteById(1L);
    }
}
