package com.example.AirbnbBookingSpring.services;

import java.util.Optional;

import com.example.AirbnbBookingSpring.models.Airbnb;
import com.example.AirbnbBookingSpring.models.User;
import com.example.AirbnbBookingSpring.repositories.writes.AirbnbWriteRepository;
import com.example.AirbnbBookingSpring.repositories.writes.UserWriteRepository;
import org.springframework.stereotype.Service;

import com.example.AirbnbBookingSpring.models.Booking;
import com.example.AirbnbBookingSpring.models.readModels.BookingReadModel;
import com.example.AirbnbBookingSpring.repositories.reads.RedisReadRepository;
import com.example.AirbnbBookingSpring.repositories.writes.BookingWriteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdempotencyService implements IIdempotencyService {

    private final RedisReadRepository redisReadRepository;
    private final BookingWriteRepository bookingWriteRepository;
    private final AirbnbWriteRepository airbnbWriteRepository;
    private final UserWriteRepository userWriteRepository;
    
    @Override
    public boolean isIdempotencyKeyUsed(String idempotencyKey) {
        return false;
    }

    @Override
    public Optional<Booking> findBookingByIdempotencyKey(String idempotencyKey) {

        BookingReadModel bookingReadModel = redisReadRepository.findBookingByIdempotencyKey(idempotencyKey);

        Airbnb airbnb=airbnbWriteRepository.findById(bookingReadModel.getAirbnbId()).orElseThrow(()->new RuntimeException("No Airbnb found with id: "+bookingReadModel.getAirbnbId()));
        User user =userWriteRepository.findById(bookingReadModel.getUserId()).orElseThrow(()-> new RuntimeException("No user exist with id: "+bookingReadModel.getUserId()));

        if(bookingReadModel != null) {
            // TODO: move it to a mapper/adapter
            Booking booking = Booking.builder()
            .id(bookingReadModel.getId())
            .airbnb(airbnb)
            .user(user)
            .totalPrice(bookingReadModel.getTotalPrice())
            .bookingStatus(Booking.BookingStatus.valueOf(bookingReadModel.getBookingStatus()))
            .idempotencyKey(bookingReadModel.getIdempotencyKey())
            .checkInDate(bookingReadModel.getCheckInDate())
            .checkOutDate(bookingReadModel.getCheckOutDate())
            .build();

            return Optional.of(booking);
        }
        
        return bookingWriteRepository.findByIdempotencyKey(idempotencyKey);
    }
}
