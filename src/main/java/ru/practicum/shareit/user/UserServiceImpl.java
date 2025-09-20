package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Autowired
    private final UserRepository repository;

    @Override
    public List<UserDto> getAllUsers() {
        return repository.getAllUsers();
    }

    @Override
    public UserDto getUserById(Long id) {
        if (!repository.isExists(id)) {
            log.warn("Пользователь с данным id: {} не найден", id);
            throw new NotFoundException("Пользователь с данным id не найден");
        }
        return repository.getUserById(id);
    }

    @Override
    public UserDto createUser(UserDto user) {
        validateEmail(user.getEmail());
        return repository.createUser(user);
    }

    @Override
    public UserDto updateUser(Long id, UserDto user) {
        if (id == null) {
            log.warn("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
        if (!repository.isExists(id)) {
            log.warn("Пользователь с данным id: {} не найден", id);
            throw new NotFoundException("Пользователь с данным id не найден");
        }
        if (user.getEmail() != null) {
            validateEmail(user.getEmail());
        }
        return repository.updateUser(id, user);
    }

    @Override
    public void deleteUser(Long id) {
        if (!repository.isExists(id)) {
            log.warn("Пользователь с данным id: {} не найден", id);
            throw new NotFoundException("Пользователь с данным id не найден");
        }
        repository.deleteUser(id);
    }

    private void validateEmail(String email) {
        List<UserDto> users = repository.getAllUsers();
        boolean hasSameEmail = users.stream().anyMatch(
                user -> user.getEmail().equals(email)
        );
        if (hasSameEmail) {
            log.warn("Пользователь с таким email: {} уже существует", email);
            throw new ValidationException("Пользователь с таким email уже существует");
        }
    }
}
