package com.flightbooking.flightbooking.Repo;
import com.flightbooking.flightbooking.Entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long>{
    Optional<Airport> findByCode(String code);
    boolean existsByCode(String code);
    List<Airport> findByCity(String city);
    List<Airport> findByCountry(String country);

    @Query("SELECT a FROM Airport a ORDER BY a.name ASC")
    List<Airport> findAllOrderByName();
}
