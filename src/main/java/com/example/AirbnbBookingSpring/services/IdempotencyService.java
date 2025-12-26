package com.example.AirbnbBookingSpring.services;

import java.util.Optional;

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
    
    @Override
    public boolean isIdempotencyKeyUsed(String idempotencyKey) {
        return this.findBookingByIdempotencyKey(idempotencyKey).isPresent();
    }

    @Override
    public Optional<Booking> findBookingByIdempotencyKey(String idempotencyKey) {

        BookingReadModel bookingReadModel = redisReadRepository.findBookingByIdempotencyKey(idempotencyKey);

        if(bookingReadModel != null) {
            // TODO: move it to a mapper/adapter
            Booking booking = Booking.builder()
            .id(bookingReadModel.getId())
            .airbnbId(bookingReadModel.getAirbnbId())
            .userId(bookingReadModel.getUserId())
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
