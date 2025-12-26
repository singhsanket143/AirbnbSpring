package com.example.AirbnbBookingSpring.repositories.writes;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.AirbnbBookingSpring.models.Booking;

import jakarta.persistence.LockModeType;

@Repository
public interface BookingWriteRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findById(Long id);
    
    List<Booking> findByAirbnbId(String airbnbId);

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);

    // @Lock(LockModeType.PESSIMISTIC_WRITE)
    // @Query("SELECT b FROM Booking b WHERE b.id = :id")
    // Optional<Booking> findByIdWithLock( @Param("id") Long id);

}
