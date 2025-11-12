package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class UserServiceImplIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    private UserDto userDto1;
    private UserDto userDto2;

    @BeforeEach
    void setUp() {
        // Очистка базы данных перед каждым тестом
        userRepository.deleteAll();

        // Создание тестовых данных
        userDto1 = new UserDto(null, "Alex", "alex@mail.ru");
        userDto2 = new UserDto(null, "Maria", "maria@mail.ru");
    }

    @Test
    void createUser_whenValid_thenUserSaved() {
        // When
        UserDto result = userService.createUser(userDto1);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Alex", result.getName());
        assertEquals("alex@mail.ru", result.getEmail());

        // Проверяем, что пользователь действительно сохранен в БД
        List<User> allUsers = userRepository.findAll();
        assertEquals(1, allUsers.size());
        assertEquals(result.getId(), allUsers.get(0).getId());
    }

    @Test
    void createUser_whenDuplicateEmail_thenThrowValidationException() {
        // Given
        userService.createUser(userDto1);

        // When & Then
        UserDto duplicateUser = new UserDto(null, "Another Alex", "alex@mail.ru");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.createUser(duplicateUser));

        assertEquals("Пользователь с таким email уже существует", exception.getMessage());

        // Проверяем, что в базе только один пользователь
        assertEquals(1, userRepository.findAll().size());
    }

    @Test
    void getAllUsers_whenUsersExist_thenReturnAllUsers() {
        // Given
        UserDto createdUser1 = userService.createUser(userDto1);
        UserDto createdUser2 = userService.createUser(userDto2);

        // When
        List<UserDto> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getEmail().equals("alex@mail.ru")));
        assertTrue(result.stream().anyMatch(u -> u.getEmail().equals("maria@mail.ru")));
    }

    @Test
    void getAllUsers_whenNoUsers_thenReturnEmptyList() {
        // When
        List<UserDto> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUserById_whenUserExists_thenReturnUser() {
        // Given
        UserDto createdUser = userService.createUser(userDto1);
        Long userId = createdUser.getId();

        // When
        UserDto result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Alex", result.getName());
        assertEquals("alex@mail.ru", result.getEmail());
    }

    @Test
    void getUserById_whenUserNotExists_thenThrowNotFoundException() {
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUserById(999L));

        assertEquals("Пользователь с данным id не найден", exception.getMessage());
    }

    @Test
    void updateUser_whenValidUpdate_thenUserUpdated() {
        // Given
        UserDto createdUser = userService.createUser(userDto1);
        Long userId = createdUser.getId();

        // When - обновляем имя и email
        UserDto updateDto = new UserDto(null, "Alexander", "alexander@mail.ru");
        UserDto result = userService.updateUser(userId, updateDto);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Alexander", result.getName());
        assertEquals("alexander@mail.ru", result.getEmail());

        // Проверяем, что изменения действительно сохранены в БД
        User userFromDb = userRepository.findById(userId).orElseThrow();
        assertEquals("Alexander", userFromDb.getName());
        assertEquals("alexander@mail.ru", userFromDb.getEmail());
    }

    @Test
    void updateUser_whenPartialUpdate_thenOnlyProvidedFieldsUpdated() {
        // Given
        UserDto createdUser = userService.createUser(userDto1);
        Long userId = createdUser.getId();

        // When - обновляем только имя
        UserDto updateDto = new UserDto(null, "Alexander", null);
        UserDto result = userService.updateUser(userId, updateDto);

        // Then
        assertNotNull(result);
        assertEquals("Alexander", result.getName());
        assertEquals("alex@mail.ru", result.getEmail()); // Email остался прежним

        // Проверяем в БД
        User userFromDb = userRepository.findById(userId).orElseThrow();
        assertEquals("Alexander", userFromDb.getName());
        assertEquals("alex@mail.ru", userFromDb.getEmail());
    }

    @Test
    void updateUser_whenUpdateToDuplicateEmail_thenThrowValidationException() {
        // Given
        userService.createUser(userDto1);
        UserDto createdUser2 = userService.createUser(userDto2);
        Long userId2 = createdUser2.getId();

        // When & Then - пытаемся изменить email пользователя 2 на email пользователя 1
        UserDto updateDto = new UserDto(null, "Maria", "alex@mail.ru");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.updateUser(userId2, updateDto));

        assertEquals("Пользователь с таким email уже существует", exception.getMessage());

        // Проверяем, что данные не изменились
        User userFromDb = userRepository.findById(userId2).orElseThrow();
        assertEquals("maria@mail.ru", userFromDb.getEmail());
    }

    @Test
    void updateUser_whenUserNotExists_thenThrowNotFoundException() {
        // Given
        UserDto updateDto = new UserDto(null, "New Name", "new@mail.ru");

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.updateUser(999L, updateDto));

        assertEquals("Пользователь с данным id не найден", exception.getMessage());
    }

    @Test
    void updateUser_whenNullId_thenThrowValidationException() {
        // Given
        UserDto updateDto = new UserDto(null, "New Name", "new@mail.ru");

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.updateUser(null, updateDto));

        assertEquals("Id должен быть указан", exception.getMessage());
    }

    @Test
    void deleteUser_whenUserExists_thenUserDeleted() {
        // Given
        UserDto createdUser = userService.createUser(userDto1);
        Long userId = createdUser.getId();

        // Проверяем, что пользователь существует
        assertTrue(userRepository.findById(userId).isPresent());

        // When
        userService.deleteUser(userId);

        // Then
        assertFalse(userRepository.findById(userId).isPresent());
        assertEquals(0, userRepository.findAll().size());
    }

    @Test
    void deleteUser_whenUserNotExists_thenThrowNotFoundException() {
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.deleteUser(999L));

        assertEquals("Пользователь с данным id не найден", exception.getMessage());
    }

    @Test
    void complexScenario_multipleOperations() {
        // Given - создаем нескольких пользователей
        UserDto user1 = userService.createUser(new UserDto(null, "User1", "user1@mail.ru"));
        UserDto user2 = userService.createUser(new UserDto(null, "User2", "user2@mail.ru"));
        UserDto user3 = userService.createUser(new UserDto(null, "User3", "user3@mail.ru"));

        // When 1 - получаем всех пользователей
        List<UserDto> allUsers = userService.getAllUsers();
        assertEquals(3, allUsers.size());

        // When 2 - обновляем одного пользователя
        UserDto updatedUser2 = userService.updateUser(user2.getId(),
                new UserDto(null, "Updated User2", "updated2@mail.ru"));

        // Then 2
        assertEquals("Updated User2", updatedUser2.getName());
        assertEquals("updated2@mail.ru", updatedUser2.getEmail());

        // When 3 - удаляем одного пользователя
        userService.deleteUser(user3.getId());

        // Then 3 - проверяем, что осталось 2 пользователя
        List<UserDto> remainingUsers = userService.getAllUsers();
        assertEquals(2, remainingUsers.size());
        assertTrue(remainingUsers.stream().anyMatch(u -> u.getId().equals(user1.getId())));
        assertTrue(remainingUsers.stream().anyMatch(u -> u.getId().equals(user2.getId())));
        assertFalse(remainingUsers.stream().anyMatch(u -> u.getId().equals(user3.getId())));

        // When 4 - проверяем что удаленный пользователь не найден
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUserById(user3.getId()));
        assertEquals("Пользователь с данным id не найден", exception.getMessage());
    }

    @Test
    void updateUser_whenSameEmailButSameUser_thenUpdateAllowed() {
        // Given - пользователь хочет изменить имя, но оставить тот же email
        UserDto createdUser = userService.createUser(userDto1);
        Long userId = createdUser.getId();

        // When - обновляем имя, но оставляем тот же email
        UserDto updateDto = new UserDto(null, "New Name", "alex@mail.ru");
        UserDto result = userService.updateUser(userId, updateDto);

        // Then - обновление должно пройти успешно
        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("alex@mail.ru", result.getEmail());

        // Проверяем в БД
        User userFromDb = userRepository.findById(userId).orElseThrow();
        assertEquals("New Name", userFromDb.getName());
        assertEquals("alex@mail.ru", userFromDb.getEmail());
    }
}