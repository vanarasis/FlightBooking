package com.flightbooking.flightbooking.Services;
import com.flightbooking.flightbooking.Entity.Booking;
import com.flightbooking.flightbooking.Entity.Flight;
import com.flightbooking.flightbooking.Entity.User;
import com.flightbooking.flightbooking.Repo.BookingRepository;
import com.flightbooking.flightbooking.Repo.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final FlightService flightService;

    @Transactional
    public Booking createBooking(Booking booking) {
        Flight flight = flightRepository.findById(booking.getFlight().getId())
                .orElseThrow(() -> new RuntimeException("Flight not found"));

        // Check if enough seats are available
        if (flight.getAvailableSeats() < booking.getNumberOfSeats()) {
            throw new RuntimeException("Not enough seats available. Available: " + flight.getAvailableSeats());
        }

        // Calculate total amount
        BigDecimal totalAmount = flight.getPrice().multiply(BigDecimal.valueOf(booking.getNumberOfSeats()));
        booking.setTotalAmount(totalAmount);

        // Update flight available seats
        if (!flightService.updateAvailableSeats(flight.getId(), booking.getNumberOfSeats())) {
            throw new RuntimeException("Failed to book seats");
        }

        return bookingRepository.save(booking);
    }

    public List<Booking> getUserBookings(User user) {
        return bookingRepository.findByUserOrderByBookingDateDesc(user);
    }

    public Optional<Booking> getBookingByReference(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public Booking cancelBooking(String bookingReference, User user) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to cancel this booking");
        }

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled");
        }

        // Update booking status
        booking.setStatus(Booking.BookingStatus.CANCELLED);

        // Return seats to flight
        Flight flight = booking.getFlight();
        flight.setAvailableSeats(flight.getAvailableSeats() + booking.getNumberOfSeats());
        flightRepository.save(flight);

        return bookingRepository.save(booking);
    }
}
