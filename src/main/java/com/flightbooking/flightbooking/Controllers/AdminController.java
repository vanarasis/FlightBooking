package com.flightbooking.flightbooking.Controllers;
import com.flightbooking.flightbooking.Entity.Airport;
import com.flightbooking.flightbooking.Entity.Flight;
import com.flightbooking.flightbooking.Entity.Booking;
import com.flightbooking.flightbooking.Entity.User;
import com.flightbooking.flightbooking.Services.AirportService;
import com.flightbooking.flightbooking.Services.AuthService;
import com.flightbooking.flightbooking.Services.BookingService;
import com.flightbooking.flightbooking.Services.FlightService;
import com.flightbooking.flightbooking.Util.FlightStatusScheduler;
import com.flightbooking.flightbooking.Util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final AirportService airportService;
    private final FlightService flightService;
    private final BookingService bookingService;

    // Admin Dashboard and Authentication Endpoints
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDashboard(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Welcome to Admin Dashboard",
                    "user", Map.of(
                            "email", currentUser.getEmail(),
                            "role", currentUser.getRole(),
                            "lastLogin", currentUser.getLastLogin()
                    )
            ));
        } catch (Exception e) {
            log.error("Admin dashboard error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        try {
            // Fetch all users from the repository via AuthService
            List<User> users = authService.getAllUsers();

            // Filter out admin users if needed (optional - you can remove this if you want to show all users including admins)
            List<User> customerUsers = users.stream()
                    .filter(user -> user.getRole() == User.UserRole.CUSTOMER)
                    .collect(Collectors.toList());

            // Transform user data to remove sensitive information
            List<Map<String, Object>> userData = customerUsers.stream()
                    .map(user -> Map.<String, Object>of(
                            "id", user.getId(),
                            "email", user.getEmail(),
                            "role", user.getRole().toString(),
                            "isVerified", user.getIsVerified(),
                            "createdAt", user.getCreatedAt(),
                            "lastLogin", user.getLastLogin() != null ? user.getLastLogin() : null,
                            "lastLogout", user.getLastLogout() != null ? user.getLastLogout() : null
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Users retrieved successfully",
                    "data", userData,
                    "totalUsers", userData.size()
            ));
        } catch (Exception e) {
            log.error("Get users error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            String sessionId = (String) request.getAttribute("sessionId");
            String result = authService.logout(sessionId);
            return ResponseEntity.ok(Map.of("success", true, "message", result));
        } catch (Exception e) {
            log.error("Admin logout error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Airport Management Endpoints
    @PostMapping("/airports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAirport(@RequestBody Airport airport) {
        try {
            Airport createdAirport = airportService.createAirport(airport);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Airport created successfully",
                    "data", createdAirport
            ));
        } catch (Exception e) {
            log.error("Create airport error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/airports")
    @PreAuthorize("hasRole('ADMIN')")
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

    @GetMapping("/airports/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAirportById(@PathVariable Long id) {
        try {
            return airportService.getAirportById(id)
                    .map(airport -> ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Airport retrieved successfully",
                            "data", airport
                    )))
                    .orElse(ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "Airport not found")));
        } catch (Exception e) {
            log.error("Get airport by id error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/airports/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAirport(@PathVariable Long id, @RequestBody Airport airport) {
        try {
            Airport updatedAirport = airportService.updateAirport(id, airport);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Airport updated successfully",
                    "data", updatedAirport
            ));
        } catch (Exception e) {
            log.error("Update airport error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/airports/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAirport(@PathVariable Long id) {
        try {
            airportService.deleteAirport(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Airport deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Delete airport error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Flight Management Endpoints
    @PostMapping("/flights")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createFlight(@RequestBody Flight flight) {
        try {
            Flight createdFlight = flightService.createFlight(flight);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Flight created successfully",
                    "data", createdFlight
            ));
        } catch (Exception e) {
            log.error("Create flight error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/flights")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllFlights() {
        try {
            List<Flight> flights = flightService.getAllFlights();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Flights retrieved successfully",
                    "data", flights
            ));
        } catch (Exception e) {
            log.error("Get flights error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/flights/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getFlightById(@PathVariable Long id) {
        try {
            return flightService.getFlightById(id)
                    .map(flight -> ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Flight retrieved successfully",
                            "data", flight
                    )))
                    .orElse(ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "Flight not found")));
        } catch (Exception e) {
            log.error("Get flight by id error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/flights/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateFlight(@PathVariable Long id, @RequestBody Flight flight) {
        try {
            Flight updatedFlight = flightService.updateFlight(id, flight);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Flight updated successfully",
                    "data", updatedFlight
            ));
        } catch (Exception e) {
            log.error("Update flight error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/flights/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteFlight(@PathVariable Long id) {
        try {
            flightService.deleteFlight(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Flight deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Delete flight error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Booking Management Endpoints
    @GetMapping("/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllBookings() {
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Bookings retrieved successfully",
                    "data", bookings
            ));
        } catch (Exception e) {
            log.error("Get bookings error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/bookings/{reference}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBookingByReference(@PathVariable String reference) {
        try {
            return bookingService.getBookingByReference(reference)
                    .map(booking -> ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Booking retrieved successfully",
                            "data", booking
                    )))
                    .orElse(ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "Booking not found")));
        } catch (Exception e) {
            log.error("Get booking by reference error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private final FlightStatusScheduler flightStatusScheduler; // Add this field to constructor

    // Manual flight status update endpoint
    @PostMapping("/flights/{id}/update-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateFlightStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String statusStr = request.get("status");
            Flight.FlightStatus status = Flight.FlightStatus.valueOf(statusStr.toUpperCase());

            Flight flight = flightService.getFlightById(id)
                    .orElseThrow(() -> new RuntimeException("Flight not found"));

            Flight updatedFlight = new Flight();
            updatedFlight.setStatus(status);
            // Copy other fields from existing flight
            updatedFlight.setFlightNumber(flight.getFlightNumber());
            updatedFlight.setDepartureAirport(flight.getDepartureAirport());
            updatedFlight.setArrivalAirport(flight.getArrivalAirport());
            updatedFlight.setDepartureTime(flight.getDepartureTime());
            updatedFlight.setArrivalTime(flight.getArrivalTime());
            updatedFlight.setAirline(flight.getAirline());
            updatedFlight.setPrice(flight.getPrice());
            updatedFlight.setTotalSeats(flight.getTotalSeats());
            updatedFlight.setAvailableSeats(flight.getAvailableSeats());

            Flight result = flightService.updateFlight(id, updatedFlight);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Flight status updated successfully",
                    "data", result
            ));
        } catch (Exception e) {
            log.error("Update flight status error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Update flight arrival time for testing
    @PostMapping("/flights/{id}/update-arrival-time")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateArrivalTime(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String arrivalTimeStr = request.get("arrivalTime");
            LocalDateTime arrivalTime = LocalDateTime.parse(arrivalTimeStr);

            Flight flight = flightService.getFlightById(id)
                    .orElseThrow(() -> new RuntimeException("Flight not found"));

            Flight updatedFlight = new Flight();
            // Copy all fields from existing flight
            updatedFlight.setFlightNumber(flight.getFlightNumber());
            updatedFlight.setDepartureAirport(flight.getDepartureAirport());
            updatedFlight.setArrivalAirport(flight.getArrivalAirport());
            updatedFlight.setDepartureTime(flight.getDepartureTime());
            updatedFlight.setArrivalTime(arrivalTime); // Update arrival time
            updatedFlight.setAirline(flight.getAirline());
            updatedFlight.setPrice(flight.getPrice());
            updatedFlight.setTotalSeats(flight.getTotalSeats());
            updatedFlight.setAvailableSeats(flight.getAvailableSeats());
            updatedFlight.setStatus(flight.getStatus());

            Flight result = flightService.updateFlight(id, updatedFlight);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Flight arrival time updated successfully",
                    "data", result
            ));
        } catch (Exception e) {
            log.error("Update arrival time error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Trigger manual status update
    @PostMapping("/flights/update-statuses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> triggerStatusUpdate() {
        try {
            String result = flightStatusScheduler.updateFlightStatusesManually();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", result
            ));
        } catch (Exception e) {
            log.error("Manual status update error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Get current IST time
    @GetMapping("/current-time")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCurrentTime() {
        try {
            ZonedDateTime istTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "currentTimeIST", istTime.toLocalDateTime(),
                    "timezone", "Asia/Kolkata"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/flights/cycle-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getFlightCycleStats() {
        try {
            String stats = flightStatusScheduler.getFlightCycleStats();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Flight cycle statistics retrieved",
                    "data", stats
            ));
        } catch (Exception e) {
            log.error("Get cycle stats error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Get specific flight cycle info
    @GetMapping("/flights/{id}/cycle-info")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getFlightCycleInfo(@PathVariable Long id) {
        try {
            String info = flightService.getFlightCycleInfo(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Flight cycle info retrieved",
                    "data", info
            ));
        } catch (Exception e) {
            log.error("Get flight cycle info error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Reset cycle count for specific flight
    @PostMapping("/flights/{id}/reset-cycles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetFlightCycles(@PathVariable Long id) {
        try {
            String result = flightService.resetFlightCycleCount(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", result
            ));
        } catch (Exception e) {
            log.error("Reset flight cycles error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // View active flights with detailed status
    @GetMapping("/flights/active-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getActiveFlightStatus() {
        try {
            List<Flight> flights = flightService.getAllFlights();
            ZonedDateTime istTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            LocalDateTime currentTime = istTime.toLocalDateTime();

            List<Map<String, Object>> flightStatus = flights.stream()
                    .filter(f -> f.getStatus() != Flight.FlightStatus.CANCELLED)
                    .map(f -> Map.<String, Object>of(
                            "flightNumber", f.getFlightNumber(),
                            "status", f.getStatus().toString(),
                            "currentRoute", f.getDepartureAirport().getCode() + " → " + f.getArrivalAirport().getCode(),
                            "originalRoute", f.getOriginalDepartureAirport().getCode() + " → " + f.getOriginalArrivalAirport().getCode(),
                            "isReversed", f.getIsRouteReversed(),
                            "cycleCount", f.getCycleCount(),
                            "departureTime", f.getDepartureTime(),
                            "arrivalTime", f.getArrivalTime(),
                            "nextDepartureTime", f.getNextDepartureTime(),
                            "lastCycleReset", f.getLastCycleReset()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "currentTime", currentTime,
                    "timezone", "Asia/Kolkata",
                    "totalActiveFlights", flightStatus.size(),
                    "data", flightStatus
            ));
        } catch (Exception e) {
            log.error("Get active flight status error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }

    }
}
