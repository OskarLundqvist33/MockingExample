package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingSystemTest {

    @Mock
    private TimeProvider timeProvider;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private NotificationService notificationService;

    private BookingSystem bookingSystem;
    private final LocalDateTime now = LocalDateTime.of(2025, 1, 1, 10, 0);

    @BeforeEach
    void setUp() {
        lenient().when(timeProvider.getCurrentTime()).thenReturn(now);
        bookingSystem = new BookingSystem(timeProvider, roomRepository, notificationService);
    }

    @Nested
    @DisplayName("bookRoom tester")
    class BookRoomTests {

        @Test
        @DisplayName("Ska lyckas boka ett ledigt rum")
        void shouldBookRoomSuccessfully() throws NotificationException {
            // Arrange
            String roomId = "room1";
            LocalDateTime start = now.plusHours(1);
            LocalDateTime end = now.plusHours(2);
            Room room = new Room(roomId, "Conference Room");
            
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

            // Act
            boolean result = bookingSystem.bookRoom(roomId, start, end);

            // Assert
            assertThat(result).isTrue();
            verify(roomRepository).save(room);
            verify(notificationService).sendBookingConfirmation(any(Booking.class));
        }

        @Test
        @DisplayName("Ska kasta exception om något argument är null")
        void shouldThrowExceptionWhenArgumentsAreNull() {
            assertThatThrownBy(() -> bookingSystem.bookRoom(null, now, now.plusHours(1)))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> bookingSystem.bookRoom("room1", null, now.plusHours(1)))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> bookingSystem.bookRoom("room1", now, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Ska kasta exception om bokning sker i dåtid")
        void shouldThrowExceptionWhenBookingInPast() {
            LocalDateTime pastStart = now.minusHours(1);
            LocalDateTime end = now.plusHours(1);

            assertThatThrownBy(() -> bookingSystem.bookRoom("room1", pastStart, end))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("dåtid");
        }

        @Test
        @DisplayName("Ska kasta exception om sluttid är före starttid")
        void shouldThrowExceptionWhenEndTimeBeforeStartTime() {
            LocalDateTime start = now.plusHours(2);
            LocalDateTime end = now.plusHours(1);

            assertThatThrownBy(() -> bookingSystem.bookRoom("room1", start, end))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Sluttid måste vara efter starttid");
        }

        @Test
        @DisplayName("Ska kasta exception om rummet inte finns")
        void shouldThrowExceptionWhenRoomDoesNotExist() {
            when(roomRepository.findById("non-existent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingSystem.bookRoom("non-existent", now.plusHours(1), now.plusHours(2)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Rummet existerar inte");
        }

        @Test
        @DisplayName("Ska returnera false om rummet redan är bokat")
        void shouldReturnFalseWhenRoomIsNotAvailable() {
            String roomId = "room1";
            LocalDateTime start = now.plusHours(1);
            LocalDateTime end = now.plusHours(2);
            
            Room room = new Room(roomId, "Room");
            room.addBooking(new Booking("existing", roomId, start, end));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

            boolean result = bookingSystem.bookRoom(roomId, start, end);

            assertThat(result).isFalse();
            verify(roomRepository, never()).save(any());
        }

        @Test
        @DisplayName("Ska genomföra bokning även om notifiering misslyckas")
        void shouldCompleteBookingEvenIfNotificationFails() throws NotificationException {
            String roomId = "room1";
            LocalDateTime start = now.plusHours(1);
            Room room = new Room(roomId, "Room");
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
            doThrow(new NotificationException("Failed")).when(notificationService).sendBookingConfirmation(any());

            boolean result = bookingSystem.bookRoom(roomId, start, start.plusHours(1));

            assertThat(result).isTrue();
            verify(roomRepository).save(room);
        }
    }

    @Nested
    @DisplayName("getAvailableRooms tester")
    class GetAvailableRoomsTests {

        @Test
        @DisplayName("Ska returnera lista på lediga rum")
        void shouldReturnAvailableRooms() {
            LocalDateTime start = now.plusHours(1);
            LocalDateTime end = now.plusHours(2);
            
            Room room1 = new Room("room1", "Available");
            Room room2 = new Room("room2", "Occupied");
            room2.addBooking(new Booking("b1", "room2", start, end));
            
            when(roomRepository.findAll()).thenReturn(List.of(room1, room2));

            List<Room> result = bookingSystem.getAvailableRooms(start, end);

            assertThat(result).containsExactly(room1);
        }

        @ParameterizedTest
        @MethodSource("provideInvalidTimeRanges")
        @DisplayName("Ska kasta exception för ogiltiga tidsintervall")
        void shouldThrowExceptionForInvalidTimeRanges(LocalDateTime start, LocalDateTime end) {
            assertThatThrownBy(() -> bookingSystem.getAvailableRooms(start, end))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        private static Stream<Arguments> provideInvalidTimeRanges() {
            LocalDateTime time = LocalDateTime.now();
            return Stream.of(
                    Arguments.of(null, time),
                    Arguments.of(time, null),
                    Arguments.of(time, time.minusHours(1))
            );
        }
    }

    @Nested
    @DisplayName("cancelBooking tester")
    class CancelBookingTests {

        @Test
        @DisplayName("Ska lyckas avboka en existerande bokning")
        void shouldCancelBookingSuccessfully() throws NotificationException {
            String bookingId = "book1";
            Room room = new Room("room1", "Room");
            Booking booking = new Booking(bookingId, "room1", now.plusHours(1), now.plusHours(2));
            room.addBooking(booking);
            
            when(roomRepository.findAll()).thenReturn(List.of(room));

            boolean result = bookingSystem.cancelBooking(bookingId);

            assertThat(result).isTrue();
            assertThat(room.hasBooking(bookingId)).isFalse();
            verify(roomRepository).save(room);
            verify(notificationService).sendCancellationConfirmation(booking);
        }

        @Test
        @DisplayName("Ska returnera false om bokning saknas")
        void shouldReturnFalseIfBookingNotFound() {
            when(roomRepository.findAll()).thenReturn(List.of());

            boolean result = bookingSystem.cancelBooking("non-existent");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Ska kasta exception vid null-id")
        void shouldThrowExceptionOnNullId() {
            assertThatThrownBy(() -> bookingSystem.cancelBooking(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Ska kasta exception om man avbokar påbörjad bokning")
        void shouldThrowExceptionIfCancellingStartedBooking() {
            String bookingId = "book1";
            Room room = new Room("room1", "Room");
            Booking booking = new Booking(bookingId, "room1", now.minusMinutes(1), now.plusHours(1));
            room.addBooking(booking);
            
            when(roomRepository.findAll()).thenReturn(List.of(room));

            assertThatThrownBy(() -> bookingSystem.cancelBooking(bookingId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("påbörjad eller avslutad");
        }

        @Test
        @DisplayName("Ska genomföra avbokning även om notifiering misslyckas")
        void shouldCompleteCancellationEvenIfNotificationFails() throws NotificationException {
            String bookingId = "book1";
            Room room = new Room("room1", "Room");
            Booking booking = new Booking(bookingId, "room1", now.plusHours(1), now.plusHours(2));
            room.addBooking(booking);
            
            when(roomRepository.findAll()).thenReturn(List.of(room));
            doThrow(new NotificationException("Failed")).when(notificationService).sendCancellationConfirmation(any());

            boolean result = bookingSystem.cancelBooking(bookingId);

            assertThat(result).isTrue();
            verify(roomRepository).save(room);
        }
    }
}
