package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Repository
@RequiredArgsConstructor
@Slf4j
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    @Autowired
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAllUsers() {
        return users.values().stream()
                .map(userMapper::toUserDto)
                .collect(toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        return userMapper.toUserDto(users.get(id));
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        userDto.setId(getNextId());
        log.info("Создан идентификатор пользователя: {}", userDto.getId());
        users.put(userDto.getId(), userMapper.toUser(userDto));
        log.info("Пользователь успешно сохранен");
        return userDto;
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User oldUser = users.get(id);
        if (userDto.getEmail() != null) {
            oldUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            oldUser.setName(userDto.getName());
        }
        return userMapper.toUserDto(oldUser);
    }

    @Override
    public void deleteUser(Long id) {
        users.remove(id);
    }

    @Override
    public boolean isExists(Long id) {
        return users.containsKey(id);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
