package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createItemRequest(Long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestWithItemsDto> getRequests(Long userId);

    List<ItemRequestDto> getRequestsAll(Long userId);

    ItemRequestWithItemsDto getRequestById(Long userId, Long requestId);
}
