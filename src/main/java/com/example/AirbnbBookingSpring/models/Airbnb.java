package com.example.AirbnbBookingSpring.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class Airbnb extends BaseModel {

    private String name;

    private String description;

    private String location;

    @Column(nullable = false)
    private Long pricePerNight;
}
