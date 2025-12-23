package com.example.AirbnbBookingSpring.repositories.reads;

import com.example.AirbnbBookingSpring.models.Booking;
import com.example.AirbnbBookingSpring.models.readModels.BookingReadModel;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import tools.jackson.databind.ObjectMapper;

@Repository
@RequiredArgsConstructor
public class RedisWriteRepository {
    
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public void writeBookingReadModel(Booking booking) {
        BookingReadModel bookingReadModel = BookingReadModel.builder()
            .id(booking.getId())
            .airbnbId(booking.getAirbnb().getId())
            .userId(booking.getUser().getId())
            .totalPrice(booking.getTotalPrice())
            .bookingStatus(booking.getBookingStatus().name())
            .idempotencyKey(booking.getIdempotencyKey())
            .checkInDate(booking.getCheckInDate())
            .checkOutDate(booking.getCheckOutDate())
            .build();

        saveBookingReadModel(bookingReadModel);
    }

    private void saveBookingReadModel(BookingReadModel bookingReadModel) {
        String key = RedisReadRepository.BOOKING_KEY_PREFIX + bookingReadModel.getId();
        String value = objectMapper.writeValueAsString(bookingReadModel);
        redisTemplate.opsForValue().set(key, value);
    }

}
