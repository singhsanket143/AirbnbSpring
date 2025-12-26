package com.example.AirbnbBookingSpring.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.AirbnbBookingSpring.dtos.CreateBookingRequest;
import com.example.AirbnbBookingSpring.dtos.UpdateBookingRequest;
import com.example.AirbnbBookingSpring.models.Booking;
import com.example.AirbnbBookingSpring.services.BookingService;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody CreateBookingRequest createBookingRequest) {
        return ResponseEntity.ok(bookingService.createBooking(createBookingRequest));
    }

    @PutMapping
    public ResponseEntity<Booking> updateBooking(@RequestBody UpdateBookingRequest updateBookingRequest) {
        return ResponseEntity.ok(bookingService.updateBooking(updateBookingRequest));
    }
    
}
