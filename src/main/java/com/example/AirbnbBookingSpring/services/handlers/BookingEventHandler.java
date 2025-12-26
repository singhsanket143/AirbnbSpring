package com.example.AirbnbBookingSpring.services.handlers;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.AirbnbBookingSpring.models.Booking;
import com.example.AirbnbBookingSpring.repositories.writes.BookingWriteRepository;
import com.example.AirbnbBookingSpring.saga.SagaEvent;
import com.example.AirbnbBookingSpring.saga.SagaEventPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingEventHandler {
    
    private final BookingWriteRepository bookingWriteRepository;
    private final SagaEventPublisher sagaEventPublisher;


    public void handleBookingConfirmRequested(SagaEvent sagaEvent) {
        try {
            Map<String, Object> payload = sagaEvent.getPayload();
            Long bookingId = Long.valueOf(payload.get("bookingId").toString());
            Long airbnbId = Long.valueOf(payload.get("airbnbId").toString());
            LocalDate checkInDate = LocalDate.parse(payload.get("checkInDate").toString());
            LocalDate checkOutDate = LocalDate.parse(payload.get("checkOutDate").toString());


            Booking booking = bookingWriteRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found")); // READ

            booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
            bookingWriteRepository.save(booking);

            sagaEventPublisher.publishEvent("BOOKING_CONFIRMED","CONFIRM_BOOKING", 
                Map.of("bookingId", bookingId, "airbnbId", airbnbId, "checkInDate", checkInDate.toString(), "checkOutDate", checkOutDate.toString())
            );
        } catch (Exception e) {
            Map<String, Object> payload = sagaEvent.getPayload();
            sagaEventPublisher.publishEvent("BOOKING_COMPENSATED", "COMPENSATE_BOOKING", payload);
            throw new RuntimeException("Failed to confirm booking", e);
        }
        
    }

    public void handleBookingCancelRequested(SagaEvent sagaEvent) {
        try {
            Map<String, Object> payload = sagaEvent.getPayload();
            Long bookingId = Long.valueOf(payload.get("bookingId").toString());
            Long airbnbId = Long.valueOf(payload.get("airbnbId").toString());
            LocalDate checkInDate = LocalDate.parse(payload.get("checkInDate").toString());
            LocalDate checkOutDate = LocalDate.parse(payload.get("checkOutDate").toString());

            Booking booking = bookingWriteRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
            booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
            bookingWriteRepository.save(booking);

            sagaEventPublisher.publishEvent("BOOKING_CANCELLED","CANCEL_BOOKING", 
                Map.of("bookingId", bookingId, "airbnbId", airbnbId, "checkInDate", checkInDate.toString(), "checkOutDate", checkOutDate.toString())
            );

        } catch (Exception e) {
            Map<String, Object> payload = sagaEvent.getPayload();
            sagaEventPublisher.publishEvent("BOOKING_COMPENSATED", "COMPENSATE_BOOKING", payload);
            throw new RuntimeException("Failed to cancel booking", e);
        }
    }
}
