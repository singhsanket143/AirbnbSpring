package com.example.AirbnbBookingSpring.repositories.writes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.AirbnbBookingSpring.models.Airbnb;

@Repository
public interface AirbnbWriteRepository extends JpaRepository<Airbnb, Long> {
    
}
