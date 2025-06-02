package com.flightbooking.flightbooking.Repo;
import com.flightbooking.flightbooking.Entity.Booking;
import com.flightbooking.flightbooking.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>{
    Optional<Booking> findByBookingReference(String bookingReference);
    List<Booking> findByUser(User user);
    List<Booking> findByUserOrderByBookingDateDesc(User user);
    List<Booking> findByStatus(Booking.BookingStatus status);

}
