
 package com.example.AirbnbBookingSpring.services.concurrency;

import java.time.LocalDate;
import java.util.List;

import com.example.AirbnbBookingSpring.models.Availability;

public interface ConcurrencyControlStrategy {

    void releaseLock(Long airbnbId, LocalDate checkInDate, LocalDate checkOutDate);

    List<Availability>lockAndCheckAvailability(Long airbnbId, LocalDate checkInDate, LocalDate checkOutDate);
}