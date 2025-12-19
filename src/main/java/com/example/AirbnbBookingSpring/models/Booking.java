package com.example.AirbnbBookingSpring.models;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Booking extends BaseModel{

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String airbnbId;

    @Column(nullable = false)
    private String totalPrice;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus bookingStatus = BookingStatus.PENDING;

    @Column(unique = true)
    private String idempotencyKey;


    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED;
    }

}
