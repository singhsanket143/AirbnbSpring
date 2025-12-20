package com.example.AirbnbBookingSpring.repositories.reads;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.AirbnbBookingSpring.models.readModels.AirbnbReadModel;
import com.example.AirbnbBookingSpring.models.readModels.AvailabilityReadModel;
import com.example.AirbnbBookingSpring.models.readModels.BookingReadModel;

import lombok.RequiredArgsConstructor;

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

    public List<AirbnbReadModel> findAllAirbnbs() {
        Set<String> keys = redisTemplate.keys(AIRBNB_KEY_PREFIX + "*");

        if (keys.isEmpty() || keys == null) {
            return List.of(); // empty list
        }

        return keys.stream()
        .map(key -> {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            try {
                return objectMapper.readValue(value, AirbnbReadModel.class);
            } catch (JacksonException e) {
                throw new RuntimeException("Failed to parse Airbnb read model from Redis", e);
            }
        })
        .filter(airbnb -> airbnb != null)
        .collect(Collectors.toList());
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
