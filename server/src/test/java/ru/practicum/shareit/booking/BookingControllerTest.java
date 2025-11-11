package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    private final Long userId = 1L;
    private final Long bookingId = 1L;
    private final Long itemId = 10L;
    private final LocalDateTime start = LocalDateTime.now().plusDays(1);
    private final LocalDateTime end = LocalDateTime.now().plusDays(2);

    private final BookingInDto bookingInDto = new BookingInDto(start, end, itemId);

    private final BookingOutDto.Item item = new BookingOutDto.Item(itemId, "Дрель");
    private final BookingOutDto.Booker booker = new BookingOutDto.Booker(userId, "Alex");
    private final BookingOutDto bookingOutDto = BookingOutDto.builder()
            .id(bookingId)
            .start(start)
            .end(end)
            .item(item)
            .booker(booker)
            .status(Status.WAITING)
            .build();

    @Test
    void createBooking_whenValid_thenReturnBookingOutDto() throws Exception {
        when(bookingService.createBooking(anyLong(), any(BookingInDto.class)))
                .thenReturn(bookingOutDto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(bookingInDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.start").exists())
                .andExpect(jsonPath("$.end").exists())
                .andExpect(jsonPath("$.item.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(item.getName())))
                .andExpect(jsonPath("$.booker.id", is(booker.getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(booker.getName())))
                .andExpect(jsonPath("$.status", is(bookingOutDto.getStatus().toString())));
    }

    @Test
    void createBooking_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingInDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_whenInvalidDates_thenReturnBadRequest() throws Exception {
        // End before start
        BookingInDto invalidBooking = new BookingInDto(end, start, itemId);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(invalidBooking))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_whenValidApprovedTrue_thenReturnApprovedBooking() throws Exception {
        BookingOutDto approvedBooking = BookingOutDto.builder()
                .id(bookingId)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(Status.APPROVED)
                .build();

        when(bookingService.approveBooking(userId, bookingId, true))
                .thenReturn(approvedBooking);

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void approveBooking_whenValidApprovedFalse_thenReturnRejectedBooking() throws Exception {
        BookingOutDto rejectedBooking = BookingOutDto.builder()
                .id(bookingId)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(Status.REJECTED)
                .build();

        when(bookingService.approveBooking(userId, bookingId, false))
                .thenReturn(rejectedBooking);

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "false")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")));
    }

    @Test
    void approveBooking_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_whenMissingApprovedParam_thenReturnBadRequest() throws Exception {
        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingById_whenValid_thenReturnBookingOutDto() throws Exception {
        when(bookingService.getBookingById(userId, bookingId))
                .thenReturn(bookingOutDto);

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(booker.getId()), Long.class));
    }

    @Test
    void getBookingById_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mvc.perform(get("/bookings/{bookingId}", bookingId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUserBookings_whenValidWithState_thenReturnListOfBookings() throws Exception {
        when(bookingService.getAllUserBookings(userId, "WAITING"))
                .thenReturn(List.of(bookingOutDto));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "WAITING")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is("WAITING")));
    }

    @Test
    void getAllUserBookings_whenValidWithDefaultState_thenReturnListOfBookings() throws Exception {
        when(bookingService.getAllUserBookings(userId, "ALL"))
                .thenReturn(List.of(bookingOutDto));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingOutDto.getId()), Long.class));
    }

    @Test
    void getAllUserBookings_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUserBookings_whenEmptyList_thenReturnEmptyArray() throws Exception {
        when(bookingService.getAllUserBookings(userId, "ALL"))
                .thenReturn(List.of());

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    void getAllItemBookings_whenValidWithState_thenReturnListOfBookings() throws Exception {
        when(bookingService.getAllItemBookings(userId, "CURRENT"))
                .thenReturn(List.of(bookingOutDto));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "CURRENT")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingOutDto.getId()), Long.class));
    }

    @Test
    void getAllItemBookings_whenValidWithDefaultState_thenReturnListOfBookings() throws Exception {
        when(bookingService.getAllItemBookings(userId, "ALL"))
                .thenReturn(List.of(bookingOutDto));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingOutDto.getId()), Long.class));
    }

    @Test
    void getAllItemBookings_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllItemBookings_whenEmptyList_thenReturnEmptyArray() throws Exception {
        when(bookingService.getAllItemBookings(userId, "ALL"))
                .thenReturn(List.of());

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    void getAllUserBookings_whenDifferentStates_thenReturnFilteredBookings() throws Exception {
        // Test for PAST state
        BookingOutDto pastBooking = BookingOutDto.builder()
                .id(2L)
                .start(start.minusDays(10))
                .end(end.minusDays(5))
                .item(item)
                .booker(booker)
                .status(Status.APPROVED)
                .build();

        when(bookingService.getAllUserBookings(userId, "PAST"))
                .thenReturn(List.of(pastBooking));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "PAST")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(2L), Long.class));
    }

    @Test
    void getAllItemBookings_whenFutureState_thenReturnFutureBookings() throws Exception {
        BookingOutDto futureBooking = BookingOutDto.builder()
                .id(3L)
                .start(start.plusDays(5))
                .end(end.plusDays(10))
                .item(item)
                .booker(booker)
                .status(Status.APPROVED)
                .build();

        when(bookingService.getAllItemBookings(userId, "FUTURE"))
                .thenReturn(List.of(futureBooking));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "FUTURE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(3L), Long.class));
    }

    @Test
    void createBooking_whenNullFields_thenReturnBadRequest() throws Exception {
        String invalidJson = """
            {
                "start": null,
                "end": null,
                "itemId": null
            }
            """;

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(invalidJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}