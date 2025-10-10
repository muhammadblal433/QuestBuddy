package com.questbuddy.trip;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.time.LocalDate;

/**
 * Data Transfer Objects for Trips - unlike the User and Event class, there is no varable we need to hide when we transfer data. Creating DTO for consistency between classes.
 * @param name - name of trip set by user
 * @param destination
 * @param startLocationName
 * @param startLat -> -90.0 <= startLat <= 90.0
 * @param startLon -> -90.0 <= startLon <= 90.0
 * @param startDate
 * @param endDate
 */
public record TripUpdateDTO(
        // All fields optional for partial update
        String name,
        String destination,

        // Optional starting point updates
        String startLocationName,
        @DecimalMin(value = "-90.0",  message = "startLat must be >= -90")
        @DecimalMax(value = "90.0",   message = "startLat must be <= 90")
        Double startLat,
        @DecimalMin(value = "-180.0", message = "startLon must be >= -180")
        @DecimalMax(value = "180.0",  message = "startLon must be <= 180")
        Double startLon,

        LocalDate startDate,
        LocalDate endDate
) {}
