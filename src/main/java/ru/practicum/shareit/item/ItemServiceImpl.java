package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public List<ItemDto> getAllItems(Long userId) {
        return itemRepository.getAllItems(userId);
    }

    @Override
    public ItemDto getItemById(Long id) {
        if (!itemRepository.isExists(id)) {
            log.warn("Вещь с данным id: {} не найдена", id);
            throw new NotFoundException("Вещь с данным id не найдена");
        }
        return itemRepository.getItemById(id);
    }

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        if (!userRepository.isExists(userId)) {
            log.warn("Пользователь с данным id: {} не найден", userId);
            throw new NotFoundException("Пользователь с данным id не найден");
        }
        return itemRepository.createItem(userId, itemDto);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        if (itemId == null || userId == null) {
            log.warn("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
        if (!itemRepository.isExists(itemId)) {
            log.warn("Вещь с данным id: {} не найдена", itemId);
            throw new NotFoundException("Вещь с данным id не найдена");
        }
        if (!userRepository.isExists(userId)) {
            log.warn("Пользователь с данным id: {} не найден", userId);
            throw new NotFoundException("Пользователь с данным id не найден");
        }
        validateOwner(userId, itemId);
        return itemRepository.updateItem(userId, itemId, itemDto);
    }

    @Override
    public void deleteItem(Long userId, Long id) {
        if (!itemRepository.isExists(id)) {
            log.warn("Вещь с данным id: {} не найдена", id);
            throw new NotFoundException("Вещь с данным id не найдена");
        }
        validateOwner(userId, id);
        itemRepository.deleteItem(userId, id);
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        return itemRepository.searchItems(text);
    }

    private void validateOwner(Long userId, Long itemId) {
        Item item = itemRepository.getItemFromItemDto(itemId);
        if (!item.getOwner().equals(userId)) {
            log.warn("Указанный идентификатор пользователя не совпадает с идентификатором владельца");
            throw new ValidationException("Указанный идентификатор пользователя не совпадает с идентификатором владельца");
        }
    }
}
