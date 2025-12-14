package com.example.AirbnbBookingSpring.repositories.reads;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.AirbnbBookingSpring.models.readModels.AirbnbReadModel;
import com.example.AirbnbBookingSpring.models.readModels.AvailabilityReadModel;
import com.example.AirbnbBookingSpring.models.readModels.BookingReadModel;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Repository
@RequiredArgsConstructor
public class RedisReadRepository {

    private static final String AIRBNB_KEY_PREFIX = "airbnb:";
    private static final String BOOKING_KEY_PREFIX = "booking:";
    private static final String AVAILABILITY_KEY_PREFIX = "availability:";
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;


    public AirbnbReadModel findAirbnbById(Long id) {
        String key = AIRBNB_KEY_PREFIX + id;

        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        try {
            return objectMapper.readValue(value, AirbnbReadModel.class);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to parse Airbnb read model from Redis", e);
        }
       
    }

    public BookingReadModel findBookingById(Long id) {
        String key = BOOKING_KEY_PREFIX + id;

        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        try {
            return objectMapper.readValue(value, BookingReadModel.class);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to parse Airbnb read model from Redis", e);
        }
       
    }

    public AvailabilityReadModel findAvailabilityById(Long id) {
        String key = AVAILABILITY_KEY_PREFIX + id;

        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        try {
            return objectMapper.readValue(value, AvailabilityReadModel.class);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to parse Availability read model from Redis", e);
        }
    }

}
