package com.flightbooking.flightbooking.Controllers;
import com.flightbooking.flightbooking.Entity.Airport;
import com.flightbooking.flightbooking.Entity.Flight;
import com.flightbooking.flightbooking.Entity.Booking;
import com.flightbooking.flightbooking.Entity.User;
import com.flightbooking.flightbooking.Services.*;
import com.flightbooking.flightbooking.Util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CustomerController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final AirportService airportService;
    private final FlightService flightService;
    private final BookingService bookingService;

    // Customer Dashboard and Authentication Endpoints
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getDashboard(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Welcome to Customer Dashboard",
                    "user", Map.of(
                            "email", currentUser.getEmail(),
                            "role", currentUser.getRole(),
                            "lastLogin", currentUser.getLastLogin()
                    )
            ));
        } catch (Exception e) {
            log.error("Customer dashboard error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                            "id", currentUser.getId(),
                            "email", currentUser.getEmail(),
                            "role", currentUser.getRole(),
                            "isVerified", currentUser.getIsVerified(),
                            "createdAt", currentUser.getCreatedAt(),
                            "lastLogin", currentUser.getLastLogin()
                    )
            ));
        } catch (Exception e) {
            log.error("Get profile error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            String sessionId = (String) request.getAttribute("sessionId");
            String result = authService.logout(sessionId);
            return ResponseEntity.ok(Map.of("success", true, "message", result));
        } catch (Exception e) {
            log.error("Customer logout error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Flight Search and Booking Endpoints
    @GetMapping("/airports")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getAllAirports() {
        try {
            List<Airport> airports = airportService.getAllAirports();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Airports retrieved successfully",
                    "data", airports
            ));
        } catch (Exception e) {
            log.error("Get airports error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/flights/search")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> searchFlights(
            @RequestParam String departure,
            @RequestParam String arrival,
            @RequestParam String departureDate) {
        try {
            LocalDateTime date = LocalDateTime.parse(departureDate + "T00:00:00");
            List<Flight> flights = flightService.searchFlights(departure, arrival, date);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Flights retrieved successfully",
                    "data", flights
            ));
        } catch (Exception e) {
            log.error("Search flights error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> bookFlight(@RequestBody BookingRequest bookingRequest, Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = authService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Flight flight = flightService.getFlightById(bookingRequest.getFlightId())
                    .orElseThrow(() -> new RuntimeException("Flight not found"));

            Booking booking = new Booking();
            booking.setUser(user);
            booking.setFlight(flight);
            booking.setPassengerName(bookingRequest.getPassengerName());
            booking.setPassengerEmail(bookingRequest.getPassengerEmail());
            booking.setPassengerPhone(bookingRequest.getPassengerPhone());
            booking.setNumberOfSeats(bookingRequest.getNumberOfSeats());

            Booking createdBooking = bookingService.createBooking(booking);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Booking created successfully",
                    "data", createdBooking
            ));
        } catch (Exception e) {
            log.error("Book flight error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getUserBookings(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = authService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Booking> bookings = bookingService.getUserBookings(user);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Bookings retrieved successfully",
                    "data", bookings
            ));
        } catch (Exception e) {
            log.error("Get user bookings error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/bookings/{reference}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> cancelBooking(@PathVariable String reference, Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = authService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Booking cancelledBooking = bookingService.cancelBooking(reference, user);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Booking cancelled successfully",
                    "data", cancelledBooking
            ));
        } catch (Exception e) {
            log.error("Cancel booking error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/bookings/{reference}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getBookingByReference(@PathVariable String reference, Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = authService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return bookingService.getBookingByReference(reference)
                    .filter(booking -> booking.getUser().getId().equals(user.getId()))
                    .map(booking -> ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Booking retrieved successfully",
                            "data", booking
                    )))
                    .orElse(ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "Booking not found or access denied")));
        } catch (Exception e) {
            log.error("Get booking by reference error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Inner class for booking request
    public static class BookingRequest {
        private Long flightId;
        private String passengerName;
        private String passengerEmail;
        private String passengerPhone;
        private Integer numberOfSeats;

        // Getters and setters
        public Long getFlightId() {
            return flightId;
        }

        public void setFlightId(Long flightId) {
            this.flightId = flightId;
        }

        public String getPassengerName() {
            return passengerName;
        }

        public void setPassengerName(String passengerName) {
            this.passengerName = passengerName;
        }

        public String getPassengerEmail() {
            return passengerEmail;
        }

        public void setPassengerEmail(String passengerEmail) {
            this.passengerEmail = passengerEmail;
        }

        public String getPassengerPhone() {
            return passengerPhone;
        }

        public void setPassengerPhone(String passengerPhone) {
            this.passengerPhone = passengerPhone;
        }

        public Integer getNumberOfSeats() {
            return numberOfSeats;
        }

        public void setNumberOfSeats(Integer numberOfSeats) {
            this.numberOfSeats = numberOfSeats;
        }
    }
}
