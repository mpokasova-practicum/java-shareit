package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Repository
@RequiredArgsConstructor
@Slf4j
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private final ItemMapper itemMapper;

    @Override
    public List<ItemDto> getAllItems(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().equals(userId))
                .map(itemMapper::toItemDto)
                .collect(toList());
    }

    @Override
    public ItemDto getItemById(Long id) {
        return itemMapper.toItemDto(items.get(id));
    }

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.info("Дошли до репозитория");
        itemDto.setId(getNextId());
        Item item = itemMapper.toItem(itemDto);
        item.setOwner(userId);
        log.info("Создан идентификатор вещи: {}", itemDto.getId());
        items.put(item.getId(), item);
        log.info("Вещь успешно сохранено");
        return itemDto;
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item oldItem = items.get(itemId);
        if (itemDto.getName() != null) {
            oldItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            oldItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            oldItem.setAvailable(itemDto.getAvailable());
        }
        return itemMapper.toItemDto(oldItem);
    }

    @Override
    public void deleteItem(Long userId, Long id) {
        items.remove(id);
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        List<ItemDto> result = new ArrayList<>();
        if (!text.isBlank()) {
            result = items.values().stream()
                    .filter(item -> item.getAvailable())
                    .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                            || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                    .map(itemMapper::toItemDto)
                    .collect(toList());
        }
        return result;
    }

    @Override
    public boolean isExists(Long id) {
        return items.containsKey(id);
    }

    private long getNextId() {
        long currentMaxId = items.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @Override
    public Item getItemFromItemDto(Long itemId) {
        return items.get(itemId);
    }
}
