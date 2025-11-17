package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDatesDto;

import java.util.List;

public interface ItemService {
    List<ItemWithDatesDto> getAllItems(Long userId);

    ItemWithDatesDto getItemById(Long id);

    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    void deleteItem(Long userId, Long id);

    List<ItemDto> searchItems(String text);

    CommentDto createComment(Long userId, Long itemId, CommentDto commentDto);
}
