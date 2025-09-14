package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<User> getAllUsers() {

    }

    @Override
    public User getUserById(Long id) {

    }

    @Override
    public User createUser(User user) {

    }

    @Override
    public User updateUser(User user) {
        
    }
}
