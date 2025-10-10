package com.questbuddy.trip;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Data Transfer Objects for Trips - unlike the User and Event class, there is no varable we need to hide (for security) when we transfer data. Creating DTO for consistency between classes.
 *
 * Similar to TripCreateDTO.java -> but more params
 *
 * @param id - id of user
 * @param ownerId - id of event
 * @param name - name of trip set by user
 * @param destination
 * @param startLocationName
 * @param startLat -> -90.0 <= startLat <= 90.0 -> implicitly true when create
 * @param startLon -> -90.0 <= startLon <= 90.0 -> implicitly true when create
 * @param startDate
 * @param endDate
 * @param createdAt
 * @param updatedAt
 */
public record TripResponseDTO(
        Long id,
        Long ownerId,
        String name,
        String destination,

        // can be null start pt
        String startLocationName,
        Double startLat,
        Double startLon,

        LocalDate startDate,
        LocalDate endDate,
        Instant createdAt,
        Instant updatedAt
) {}