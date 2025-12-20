package com.example.AirbnbBookingSpring.models;
import lombok.*;
import jakarta.persistence.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Availability extends  BaseModel {

    /**
     * Many Availability records have one airbnb ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="airbnb_id", nullable = false)
    private Airbnb airbnb;

    @Column(nullable = false)
    private String date;


    /**
     * Many Availability records have one booking ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "booking_id")
    private Booking booking;
}
