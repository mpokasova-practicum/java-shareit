package ru.practicum.shareit.item;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.NoBookingFoundException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;

    @Override
    public List<ItemWithDatesDto> getAllItems(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с данным id не найден")
        );
        List<Item> items = itemRepository.findAllByOwnerId(userId);

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        Map<Long, Booking> lastBookingsMap = bookingRepository.findLastBookingsForItems(itemIds, LocalDateTime.now())
                .stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        booking -> booking
                ));
        Map<Long, Booking> nextBookingsMap = bookingRepository.findLastBookingsForItems(itemIds, LocalDateTime.now())
                .stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        booking -> booking
                ));
        Map<Long, List<Comment>> commentsMap = commentRepository.findAllByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId()));

        return items.stream()
                .map(item -> {
                    ItemWithDatesDto itemDto = itemMapper.toItemWithDatesDto(item);
                    if (lastBookingsMap.containsKey(item.getId())) {
                        BookingItemDto lastBookingDto = bookingMapper.toBookingItemDto(
                                lastBookingsMap.get(item.getId()));
                        itemDto.setLastBooking(lastBookingDto);
                    }

                    if (nextBookingsMap.containsKey(item.getId())) {
                        BookingItemDto nextBookingDto = bookingMapper.toBookingItemDto(
                                nextBookingsMap.get(item.getId()));
                        itemDto.setNextBooking(nextBookingDto);
                    }

                    if (commentsMap.containsKey(item.getId())) {
                        List<CommentDto> commentDtos = commentsMap.get(item.getId())
                                .stream()
                                .map(commentMapper::toCommentDto)
                                .toList();
                        itemDto.setComments(commentDtos);
                    }
                    return itemDto;
                })
                .toList();
    }

    @Override
    public ItemWithDatesDto getItemById(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Вещь с данным id не найдена")
        );
        ItemWithDatesDto itemDto = itemMapper.toItemWithDatesDto(item);
        List<Comment> comments = commentRepository.findAllByItemId(id);
        if (!comments.isEmpty()) {
            List<CommentDto> commentDtos = comments.stream()
                    .map(commentMapper::toCommentDto)
                    .toList();
            itemDto.setComments(commentDtos);
        }
        return itemDto;
    }

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с данным id не найден")
        );
        Item item = itemMapper.toItem(itemDto);
        item.setOwner(user);
        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(
                            () -> new NotFoundException("Запрос с данным id не найден")
                    );
            item.setRequest(itemRequest);
        }
        item = itemRepository.save(item);
        log.info("Вещь успешно сохранена");
        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        if (itemId == null || userId == null) {
            log.warn("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
        Item oldItem = itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException("Вещь с данным id не найдена")
        );
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с данным id не найден")
        );
        if (!oldItem.getOwner().getId().equals(user.getId())) {
            log.warn("Указанный идентификатор пользователя не совпадает с идентификатором владельца");
            throw new ValidationException("Указанный идентификатор пользователя не совпадает с идентификатором владельца");
        }

        if (itemDto.getName() != null) {
            oldItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            oldItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            oldItem.setAvailable(itemDto.getAvailable());
        }
        oldItem = itemRepository.save(oldItem);
        return itemMapper.toItemDto(oldItem);
    }

    @Override
    public void deleteItem(Long userId, Long id) {
        Item item = itemRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Вещь с данным id не найдена")
        );
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с данным id не найден")
        );
        if (!item.getOwner().getId().equals(user.getId())) {
            log.warn("Указанный идентификатор пользователя не совпадает с идентификатором владельца");
            throw new ValidationException("Указанный идентификатор пользователя не совпадает с идентификатором владельца");
        }
        itemRepository.deleteById(id);
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        List<Item> items = itemRepository.searchItems(text);
        return items.stream()
                .map(itemMapper::toItemDto)
                .collect(toList());
    }

    @Override
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с данным id не найден")
        );
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException("Вещь с данным id не найдена")
        );
        List<Booking> bookings = bookingRepository.findAllByItemIdAndBookerIdAndStatusAndEndIsBefore(
                itemId, userId, Status.APPROVED, LocalDateTime.now());
        if (bookings.isEmpty()) {
            throw new NoBookingFoundException("Не найдено завершенное бронирование данной вещи");
        }
        Comment comment = commentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        comment = commentRepository.save(comment);
        return commentMapper.toCommentDto(comment);
    }
}
