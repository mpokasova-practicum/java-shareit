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

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private final User user = new User(1L, "Ivanov Ivan", "ivanov@gmail.com");
    private final UserDto userDto = new UserDto(1L, "Ivanov Ivan", "ivanov@gmail.com");
    private final User user2 = new User(2L, "Petrov Petr", "petrov@gmail.com");
    private final List<User> users = List.of(user, user2);

    @Test
    void getAllUsers_shouldReturnListNotEmpty() {
        Mockito.when(userRepository.findAll())
                .thenReturn(users);

        List<UserDto> expectedList = Stream.of(user, user2)
                .map(UserMapper::toUserDto).toList();
        List<UserDto> actualList = userService.getAllUsers();

        assertEquals(expectedList, actualList);
    }

    @Test
    void getUserById_shouldReturnUser() {
        Mockito.when(userRepository.findById(any()))
                .thenReturn(Optional.of(user));
        UserDto actualUser = userService.getUserById(user.getId());

        assertEquals(userDto, actualUser);
    }

    @Test
    void getUserById_shouldReturnNotFoundException() {
        Mockito.when(userRepository.findById(any()))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createUser_shouldCreateUser() {
        Mockito.when(userRepository.save(any()))
                .thenReturn(user);
        UserDto actualUser = userService.createUser(userDto);
        assertEquals(userDto, actualUser);
    }

    @Test
    void createUser_shouldReturnValidationException() {
        Mockito.when(userRepository.save(any(User.class)))
                .thenThrow(ValidationException.class);
        assertThatThrownBy(() -> userService.createUser(userDto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void updateUser_shouldReturnNotFoundException() {
        Mockito.when(userRepository.findById(any()))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateUser(1L, userDto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateUser_shouldReturnValidationException() {
        Mockito.when(userRepository.save(any(User.class)))
                .thenThrow(ValidationException.class);
        assertThatThrownBy(() -> userService.updateUser(user.getId(), userDto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void updateUser_shouldUpdateUser() {
        Mockito.when(userRepository.save(any()))
                .thenReturn(user);
        UserDto actualUser = userService.updateUser(user.getId(), userDto);
        assertEquals(userDto, actualUser);
    }

    @Test
    void delete() {
        User user4 = new User(4L, "Mike", "mike.d@yandex.ru");
        userService.deleteUser(user4.getId());
        Mockito.verify(userRepository, Mockito.times(1)).deleteById(4L);
    }
}
