package com.example.AirbnbBookingSpring.services.handlers;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.AirbnbBookingSpring.repositories.writes.AvailabilityWriteRepository;
import com.example.AirbnbBookingSpring.saga.SagaEvent;
import com.example.AirbnbBookingSpring.saga.SagaEventPublisher;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityEventHandler {
    
    private final AvailabilityWriteRepository availabilityWriteRepository;
    private final SagaEventPublisher sagaEventPublisher;

    @Transactional
    public void handleBookingConfirmed(SagaEvent sagaEvent) {
        try {
            Map<String, Object> payload = sagaEvent.getPayload();
            Long bookingId = Long.valueOf(payload.get("bookingId").toString());
            Long airbnbId = Long.valueOf(payload.get("airbnbId").toString());
            LocalDate checkInDate = LocalDate.parse(payload.get("checkInDate").toString());
            LocalDate checkOutDate = LocalDate.parse(payload.get("checkOutDate").toString());

            Long count = availabilityWriteRepository.countByAirbnbIdAndDateBetweenAndBookingIdIsNotNull(airbnbId, checkInDate, checkOutDate);
            log.info("Count of bookings for Airbnb {}: {}", airbnbId, count);
            if(count > 0) {
                sagaEventPublisher.publishEvent("BOOKING_CANCEL_REQUESTED", "CANCEL_BOOKING", payload);
                throw new RuntimeException("Airbnb is not available for the given dates. Please try again with different dates.");
            }
            log.info("Updating availability for Airbnb {}: {}", airbnbId, checkInDate, checkOutDate);
            availabilityWriteRepository.updateBookingIdByAirbnbIdAndDateBetween(bookingId, airbnbId, checkInDate, checkOutDate);
            log.info("Availability updated for Airbnb {}: {}", airbnbId, checkInDate, checkOutDate);
            sagaEventPublisher.publishEvent("BOOKING_CONFIRMED", "CONFIRM_BOOKING", payload);
        } catch (Exception e) {
            Map<String, Object> payload = sagaEvent.getPayload();
            sagaEventPublisher.publishEvent("BOOKING_COMPENSATED", "COMPENSATE_BOOKING", payload);
            throw new RuntimeException("Failed to confirm booking", e);
        }
    }

    public void handleBookingCancelled(SagaEvent sagaEvent) {
        try {
            Map<String, Object> payload = sagaEvent.getPayload();
            Long bookingId = Long.valueOf(payload.get("bookingId").toString());
            Long airbnbId = Long.valueOf(payload.get("airbnbId").toString());
            LocalDate checkInDate = LocalDate.parse(payload.get("checkInDate").toString());
            LocalDate checkOutDate = LocalDate.parse(payload.get("checkOutDate").toString());

            availabilityWriteRepository.updateBookingIdByAirbnbIdAndDateBetween(null, airbnbId, checkInDate, checkOutDate);

            sagaEventPublisher.publishEvent("BOOKING_CANCELLED", "CANCEL_BOOKING", payload);
        } catch (Exception e) {
            Map<String, Object> payload = sagaEvent.getPayload();
            sagaEventPublisher.publishEvent("BOOKING_COMPENSATED", "COMPENSATE_BOOKING", payload);
            throw new RuntimeException("Failed to cancel booking", e);
        }
    }
}
