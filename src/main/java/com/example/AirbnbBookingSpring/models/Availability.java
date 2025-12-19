package com.example.AirbnbBookingSpring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


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
    @JoinColumn(name= "booking_id", nullable = true)
    private Booking booking;
}
