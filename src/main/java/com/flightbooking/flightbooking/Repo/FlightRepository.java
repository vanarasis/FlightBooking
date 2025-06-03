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
    List<Flight> findAvailableFlights(@Param("departure") Airport departure, @Param("arrival") Airport arrival, @Param("departureDate") LocalDateTime departureDate);

    @Query("SELECT f FROM Flight f WHERE f.departureTime >= :startDate AND f.departureTime <= :endDate ORDER BY f.departureTime ASC")
    List<Flight> findFlightsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<Flight> findByAirline(String airline);

    List<Flight> findByStatus(Flight.FlightStatus status);

    // Core method used by scheduler
    List<Flight> findByStatusIn(List<Flight.FlightStatus> statuses);

    // Find flights that need cycle count reset (24 hours passed)
    @Query("SELECT f FROM Flight f WHERE f.lastCycleReset < :cutoffTime")
    List<Flight> findFlightsNeedingCycleReset(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Find completed flights ready for next cycle (after ground time)
    @Query("SELECT f FROM Flight f WHERE f.status = 'COMPLETED' AND f.arrivalTime < :cutoffTime")
    List<Flight> findCompletedFlightsReadyForNextCycle(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Find flights by cycle count
    @Query("SELECT f FROM Flight f WHERE f.cycleCount >= :minCycles")
    List<Flight> findFlightsByCycleCount(@Param("minCycles") Integer minCycles);

    // Find flights by original route
    @Query("SELECT f FROM Flight f WHERE f.originalDepartureAirport = :departure AND f.originalArrivalAirport = :arrival")
    List<Flight> findFlightsByOriginalRoute(@Param("departure") Airport departure, @Param("arrival") Airport arrival);

    // Find flights currently in reverse direction
    @Query("SELECT f FROM Flight f WHERE f.isRouteReversed = true")
    List<Flight> findReversedFlights();

    // Find active flights (not cancelled)
    @Query("SELECT f FROM Flight f WHERE f.status IN ('SCHEDULED', 'FLYING', 'COMPLETED')")
    List<Flight> findActiveFlights();

    // Statistics query - count flights by status
    @Query("SELECT f.status, COUNT(f) FROM Flight f GROUP BY f.status")
    List<Object[]> countFlightsByStatus();

    // Find flights with high cycle counts (for monitoring)
    @Query("SELECT f FROM Flight f WHERE f.cycleCount > :threshold ORDER BY f.cycleCount DESC")
    List<Flight> findHighCycleFlights(@Param("threshold") Integer threshold);

    // Find flights created today
    @Query("SELECT f FROM Flight f WHERE DATE(f.createdAt) = DATE(:today)")
    List<Flight> findFlightsCreatedToday(@Param("today") LocalDateTime today);

    // Find flights by departure time range
    @Query("SELECT f FROM Flight f WHERE f.departureTime BETWEEN :start AND :end ORDER BY f.departureTime")
    List<Flight> findFlightsByDepartureTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}