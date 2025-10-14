package ru.practicum.shareit.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = repository.findAll();
        return users.stream()
                .map(userMapper::toUserDto)
                .collect(toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Пользователь с данным id не найден")
        );
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        repository.findByEmail(userDto.getEmail()).ifPresent(
                user -> {
                    throw new ValidationException("Пользователь с таким email уже существует");
                }
        );
        User user = userMapper.toUser(userDto);
        user = repository.save(user);
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        repository.findByEmail(userDto.getEmail()).ifPresent(
                user -> {
                    throw new ValidationException("Пользователь с таким email уже существует");
                }
        );
        if (id == null) {
            log.warn("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
        User oldUser = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Пользователь с данным id не найден")
        );
        if (userDto.getEmail() != null) {
            oldUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            oldUser.setName(userDto.getName());
        }
        oldUser = repository.save(oldUser);
        return userMapper.toUserDto(oldUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Пользователь с данным id не найден")
        );
        repository.deleteById(id);
    }
}
