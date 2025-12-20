package com.example.AirbnbBookingSpring.services;

import com.example.AirbnbBookingSpring.dtos.CreateBookingRequest;
import com.example.AirbnbBookingSpring.dtos.UpdateBookingRequest;
import com.example.AirbnbBookingSpring.models.Booking;

public interface IBookingService {
    
    Booking createBooking(CreateBookingRequest createBookingRequest);

    Booking updateBooking(UpdateBookingRequest updateBookingRequest);
}
