package com.example.AirbnbBookingSpring.services.concurrency;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.AirbnbBookingSpring.models.Availability;
import com.example.AirbnbBookingSpring.repositories.writes.AvailabilityWriteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisLockStrategy implements ConcurrencyControlStrategy {

    private static final String LOCK_KEY_PREFIX = "lock:availability:";
    private static final Duration LOCK_TIMEOUT = Duration.ofMinutes(2); // TODO: Make this configurable

    private final RedisTemplate<String, String> redisTemplate;
    private final AvailabilityWriteRepository availabilityWriteRepository;
    
    @Override
    public void releaseLock(Long airbnbId, LocalDate checkInDate, LocalDate checkOutDate) {
        String lockKey = generateLockKey(airbnbId, checkInDate, checkOutDate);
        redisTemplate.delete(lockKey);
    }

    @Override
    public List<Availability> lockAndCheckAvailability(Long airbnbId, LocalDate checkInDate, LocalDate checkOutDate, Long userId) {
        Long bookedSlots = availabilityWriteRepository.countByAirbnbIdAndDateBetweenAndBookingIdIsNotNull(airbnbId, checkInDate, checkOutDate);

        if(bookedSlots > 0) {
            throw new RuntimeException("Airbnb is not available for all the given dates. Please try again with different dates.");
        }

        String lockKey = generateLockKey(airbnbId, checkInDate, checkOutDate);
        boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, userId.toString(), LOCK_TIMEOUT);

        if(!locked) {
            throw new IllegalStateException("Failed to acquire booking for the given dates. Please try again.");
        }

        try {
            return availabilityWriteRepository.findByAirbnbIdAndDateBetween(airbnbId, checkInDate, checkOutDate);
        } catch (Exception e) {
            releaseLock(airbnbId, checkInDate, checkOutDate);
            throw e;
        }


    }

    private String generateLockKey(Long airbnbId, LocalDate checkInDate, LocalDate checkOutDate) {
        return LOCK_KEY_PREFIX + airbnbId + ":" + checkInDate + ":" + checkOutDate;
    }
}
