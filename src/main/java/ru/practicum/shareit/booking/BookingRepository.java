package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerId(Long bookerId, Sort sort);

    List<Booking> findAllByBookerIdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findAllByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime date, Sort sort);

    List<Booking> findAllByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime date, Sort sort);

    List<Booking> findAllByBookerIdAndStatus(Long bookerId, Status status, Sort sort);

    List<Booking> findAllByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfter(Long ownerId, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findAllByItemOwnerIdAndEndIsBefore(Long ownerId, LocalDateTime date, Sort sort);

    List<Booking> findAllByItemOwnerIdAndStartIsAfter(Long ownerId, LocalDateTime date, Sort sort);

    List<Booking> findAllByItemOwnerIdAndStatus(Long ownerId, Status status, Sort sort);

    Optional<Booking> findFirstByItemIdAndEndIsBeforeOrderByEndDesc(Long itemId, LocalDateTime end);

    Optional<Booking> findFirstByItemIdAndStartIsAfterOrderByStartDesc(Long itemId, LocalDateTime start);

    List<Booking> findAllByItemIdAndBookerIdAndStatusAndEndIsBefore(Long itemId, Long bookerId, Status status, LocalDateTime end);
}
