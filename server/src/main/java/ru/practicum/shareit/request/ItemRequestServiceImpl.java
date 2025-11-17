package ru.practicum.shareit.request;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    @Override
    public ItemRequestDto createItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с данным id не найден")
        );
        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequester(user);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest = itemRequestRepository.save(itemRequest);
        return itemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    public List<ItemRequestWithItemsDto> getRequests(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с данным id не найден")
        );
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterId(userId);
        List<Long> itemRequestIds = itemRequests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<Item>> itemsMap = itemRepository.findAllByRequestIdIn(itemRequestIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return itemRequests.stream()
                .map(request -> {
                    ItemRequestWithItemsDto requestWithItems = itemRequestMapper.toItemRequestWithItemsDto(request);
                    if (itemsMap.containsKey(request.getId())) {
                        List<ItemForRequestDto> itemForRequestDtos = itemsMap.get(request.getId())
                                .stream()
                                .map(itemMapper::toItemForRequestDto)
                                .toList();
                        requestWithItems.setItems(itemForRequestDtos);
                    }
                    return requestWithItems;
                })
                .toList();
    }

    @Override
    public List<ItemRequestDto> getRequestsAll(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с данным id не найден")
        );
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdNot(userId, sort);
        return itemRequests.stream()
                .map(itemRequestMapper::toItemRequestDto)
                .toList();
    }

    @Override
    public ItemRequestWithItemsDto getRequestById(Long userId, Long requestId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с данным id не найден")
        );
        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Запрос с данным id не найден")
        );
        ItemRequestWithItemsDto res = itemRequestMapper.toItemRequestWithItemsDto(itemRequest);
        List<Item> items = itemRepository.findAllByRequestId(requestId);
        if (!items.isEmpty()) {
            List<ItemForRequestDto> itemDtos = items.stream()
                    .map(itemMapper::toItemForRequestDto)
                    .toList();
            res.setItems(itemDtos);
        }
        return res;
    }
}
