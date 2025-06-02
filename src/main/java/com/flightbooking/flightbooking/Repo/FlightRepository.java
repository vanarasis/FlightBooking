package com.flightbooking.flightbooking.Repo;
import com.flightbooking.flightbooking.Entity.Flight;
import com.flightbooking.flightbooking.Entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long>{
    Optional<Flight> findByFlightNumber(String flightNumber);
    boolean existsByFlightNumber(String flightNumber);

    @Query("SELECT f FROM Flight f WHERE f.departureAirport = :departure AND f.arrivalAirport = :arrival AND DATE(f.departureTime) = DATE(:departureDate) AND f.status = 'SCHEDULED' AND f.availableSeats > 0 ORDER BY f.departureTime ASC")
    List<Flight> findAvailableFlights(@Param("departure") Airport departure,
                                      @Param("arrival") Airport arrival,
                                      @Param("departureDate") LocalDateTime departureDate);

    @Query("SELECT f FROM Flight f WHERE f.departureTime >= :startDate AND f.departureTime <= :endDate ORDER BY f.departureTime ASC")
    List<Flight> findFlightsByDateRange(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    List<Flight> findByAirline(String airline);
    List<Flight> findByStatus(Flight.FlightStatus status);

}
