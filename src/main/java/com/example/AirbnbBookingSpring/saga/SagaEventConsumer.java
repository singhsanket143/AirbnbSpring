package com.example.AirbnbBookingSpring.saga;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaEventConsumer {
    private final String SAGA_QUEUE = "saga:events"; // TODO: Make this configurable: https://github.com/singhsanket143/AirbnbSpring/issues/12

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final SagaEventProcessor sagaEventProcessor;


    @Scheduled(fixedDelay = 500) // poll events every 500ms
    public void consumeEvents() {
        try {
            String eventJson = redisTemplate.opsForList().leftPop(SAGA_QUEUE, 1, TimeUnit.SECONDS);
            if(eventJson != null && !eventJson.isEmpty()) {
                SagaEvent sagaEvent = objectMapper.readValue(eventJson, SagaEvent.class);
                log.info("Processing saga event: {}", sagaEvent.getSagaId());
                sagaEventProcessor.processEvent(sagaEvent);
                log.info("Saga event processed successfully for saga id: {}", sagaEvent.getSagaId());
            }
        } catch (Exception e) {
            log.error("Error processing saga event: {}", e.getMessage());
            throw new RuntimeException("Failed to process saga event", e);
        }
    }
}
