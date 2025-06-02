package com.flightbooking.flightbooking.Util;
import com.flightbooking.flightbooking.Entity.Flight;
import com.flightbooking.flightbooking.Repo.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlightStatusScheduler {
    private final FlightRepository flightRepository;

    // Run every 2 minutes to check flight status updates
    @Scheduled(fixedRate = 120000) // 2 minutes in milliseconds
    public void updateFlightStatuses() {
        try {
            // Get current time in IST
            ZonedDateTime istNow = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            LocalDateTime currentTime = istNow.toLocalDateTime();

            log.info("Checking flight statuses at IST: {}", currentTime);

            // Find all scheduled and delayed flights
            List<Flight> activeFlights = flightRepository.findByStatusIn(
                    Arrays.asList(Flight.FlightStatus.SCHEDULED, Flight.FlightStatus.DELAYED)
            );

            int updatedCount = 0;

            for (Flight flight : activeFlights) {
                boolean statusChanged = false;

                // Check if flight should be marked as COMPLETED
                if (currentTime.isAfter(flight.getArrivalTime())) {
                    flight.setStatus(Flight.FlightStatus.COMPLETED);
                    statusChanged = true;
                    log.info("Flight {} marked as COMPLETED. Arrival time: {}, Current time: {}",
                            flight.getFlightNumber(), flight.getArrivalTime(), currentTime);
                }
                // Check if flight should be marked as DELAYED (departure time passed but not arrived)
                else if (currentTime.isAfter(flight.getDepartureTime()) &&
                        currentTime.isBefore(flight.getArrivalTime()) &&
                        flight.getStatus() == Flight.FlightStatus.SCHEDULED) {
                    flight.setStatus(Flight.FlightStatus.DELAYED);
                    statusChanged = true;
                    log.info("Flight {} marked as DELAYED. Departure time: {}, Current time: {}",
                            flight.getFlightNumber(), flight.getDepartureTime(), currentTime);
                }

                if (statusChanged) {
                    flightRepository.save(flight);
                    updatedCount++;
                }
            }

            if (updatedCount > 0) {
                log.info("Updated {} flight statuses", updatedCount);
            }

        } catch (Exception e) {
            log.error("Error updating flight statuses: {}", e.getMessage(), e);
        }
    }

    // Manual method for testing - can be called via REST endpoint
    public String updateFlightStatusesManually() {
        updateFlightStatuses();
        return "Flight status update triggered manually";
    }
}
