package com.example.AirbnbBookingSpring.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.AirbnbBookingSpring.dtos.CreateBookingRequest;
import com.example.AirbnbBookingSpring.dtos.UpdateBookingRequest;
import com.example.AirbnbBookingSpring.models.Airbnb;
import com.example.AirbnbBookingSpring.models.Availability;
import com.example.AirbnbBookingSpring.models.Booking;
import com.example.AirbnbBookingSpring.models.Booking.BookingStatus;
import com.example.AirbnbBookingSpring.repositories.reads.RedisWriteRepository;
import com.example.AirbnbBookingSpring.repositories.writes.AirbnbWriteRepository;
import com.example.AirbnbBookingSpring.repositories.writes.AvailabilityWriteRepository;
import com.example.AirbnbBookingSpring.repositories.writes.BookingWriteRepository;
import com.example.AirbnbBookingSpring.saga.SagaEventPublisher;
import com.example.AirbnbBookingSpring.services.concurrency.ConcurrencyControlStrategy;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService implements IBookingService {

    private final BookingWriteRepository bookingWriteRepository;
    private final AvailabilityWriteRepository availabilityWriteRepository;
    private final AirbnbWriteRepository airbnbWriteRepository;
    private final ConcurrencyControlStrategy concurrencyControlStrategy;
    private final RedisWriteRepository redisWriteRepository;
    private final IdempotencyService idempotencyService;
    private final SagaEventPublisher sagaEventPublisher;


    @Override
    @Transactional
    public Booking createBooking(CreateBookingRequest createBookingRequest) {
        
        Airbnb airbnb = airbnbWriteRepository.findById(createBookingRequest.getAirbnbId()).orElseThrow(() -> new RuntimeException("Airbnb not found"));

        if(createBookingRequest.getCheckInDate().isAfter(createBookingRequest.getCheckOutDate())) {
            throw new RuntimeException("Check-in date must be before check-out date");
        }

        if(createBookingRequest.getCheckInDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Check-in date must be today or in the future");
        }

        List<Availability> availabilities = concurrencyControlStrategy.lockAndCheckAvailability(
            airbnb.getId(), 
            createBookingRequest.getCheckInDate(), 
            createBookingRequest.getCheckOutDate(), 
            createBookingRequest.getUserId());

        long nights = ChronoUnit.DAYS.between(createBookingRequest.getCheckInDate(), createBookingRequest.getCheckOutDate());

        double pricePerNight = airbnb.getPricePerNight();

        double totalPrice = pricePerNight * nights;

        String idempotencyKey = UUID.randomUUID().toString();

        log.info("Creating booking for Airbnb {} with check-in date {} and check-out date {} and total price {} and idempotency key {}", 
        airbnb.getId(), createBookingRequest.getCheckInDate(), createBookingRequest.getCheckOutDate(), totalPrice, idempotencyKey);

        Booking booking = Booking.builder()
            .airbnbId(airbnb.getId())
            .userId(createBookingRequest.getUserId())
            .totalPrice(totalPrice)
            .idempotencyKey(idempotencyKey)
            .bookingStatus(Booking.BookingStatus.PENDING)
            .checkInDate(createBookingRequest.getCheckInDate())
            .checkOutDate(createBookingRequest.getCheckOutDate())
            .build();

        booking = bookingWriteRepository.save(booking);

        redisWriteRepository.writeBookingReadModel(booking);

        return booking;

       
    }

    @Override
    @Transactional
    public Booking updateBooking(UpdateBookingRequest updateBookingRequest) {
        log.info("Updating booking for idempotency key {}", updateBookingRequest.getIdempotencyKey());
        Booking booking = idempotencyService.findBookingByIdempotencyKey(updateBookingRequest.getIdempotencyKey())
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        log.info("Booking found for idempotency key {}", updateBookingRequest.getIdempotencyKey());
        log.info("Booking status: {}", booking.getBookingStatus());
        if(booking.getBookingStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Booking is not pending");
        }

        if(updateBookingRequest.getBookingStatus() == BookingStatus.CONFIRMED) { // TODO: This also violates a SOLID principle, please resolve: https://github.com/singhsanket143/AirbnbSpring/issues/13
            sagaEventPublisher.publishEvent("BOOKING_CONFIRM_REQUESTED", "CONFIRM_BOOKING", Map.of("bookingId", booking.getId(), "airbnbId", booking.getAirbnbId(), "checkInDate", booking.getCheckInDate(), "checkOutDate", booking.getCheckOutDate()));
        } else if(updateBookingRequest.getBookingStatus() == BookingStatus.CANCELLED) {
            sagaEventPublisher.publishEvent("BOOKING_CANCEL_REQUESTED", "CANCEL_BOOKING", Map.of("bookingId", booking.getId(), "airbnbId", booking.getAirbnbId(), "checkInDate", booking.getCheckInDate(), "checkOutDate", booking.getCheckOutDate()));
        }

        return booking;

    }
}
