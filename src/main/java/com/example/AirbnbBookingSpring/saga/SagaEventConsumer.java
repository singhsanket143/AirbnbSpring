package com.example.AirbnbBookingSpring.saga;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class SagaEventConsumer {
    private final String SAGA_QUEUE = "saga:events"; // TODO: Make this configurable: https://github.com/singhsanket143/AirbnbSpring/issues/12

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;


    @Scheduled(fixedDelay = 500) // poll events every 500ms
    public void consumeEvents() {
        String eventJson = redisTemplate.opsForList().leftPop(SAGA_QUEUE, 1, TimeUnit.SECONDS);
        if(eventJson != null && !eventJson.isEmpty()) {
            
        }
    }
}
