package com.example.AirbnbBookingSpring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Airbnb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private User user;

    @OneToMany(mappedBy = "airbnb",orphanRemoval = true,cascade = CascadeType.ALL)
    private List<Booking> bookings;

    @OneToMany(mappedBy = "airbnb",orphanRemoval = true,cascade = CascadeType.ALL)
    private List<Availability> availabilities;

    @Column(nullable = false,precision = 10,scale = 2)
    private Long pricePerNight;
}
