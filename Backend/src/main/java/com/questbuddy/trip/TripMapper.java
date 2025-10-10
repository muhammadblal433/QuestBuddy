package com.questbuddy.trip;

import org.springframework.stereotype.Component;

/**
 * This class is mainly for two things
 *
 * 1) Create a trip based on a CreateDTO
 * 2) Create a responseDTO based on a trip
 * 3) Update an event based on a UpdateDTO
 */
@Component
public class TripMapper {

    // 1) Create a trip based on a CreateDTO
    public Trip toEntity(Long ownerId, TripCreateDTO dto) {
        Trip t = new Trip();
        t.setOwnerId(ownerId);
        t.setName(dto.name());
        t.setDestination(dto.destination());

        // starting point (optional)
        t.setStartLocationName(dto.startLocationName());
        t.setStartLat(dto.startLat());
        t.setStartLon(dto.startLon());

        t.setStartDate(dto.startDate());
        t.setEndDate(dto.endDate());
        return t;
    }

    // 2) Create a responseDTO based on a trip
    public TripResponseDTO toDto(Trip t) {
        return new TripResponseDTO(
                t.getId(),
                t.getOwnerId(),
                t.getName(),
                t.getDestination(),
                t.getStartLocationName(),
                t.getStartLat(),
                t.getStartLon(),
                t.getStartDate(),
                t.getEndDate(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }

    // 3) Update an event based on a UpdateDTO
    public void applyUpdate(Trip t, TripUpdateDTO dto) {
        if (dto.name() != null)               t.setName(dto.name());
        if (dto.destination() != null)        t.setDestination(dto.destination());
        if (dto.startLocationName() != null)  t.setStartLocationName(dto.startLocationName());
        if (dto.startLat() != null)           t.setStartLat(dto.startLat());
        if (dto.startLon() != null)           t.setStartLon(dto.startLon());
        if (dto.startDate() != null)          t.setStartDate(dto.startDate());
        if (dto.endDate() != null)            t.setEndDate(dto.endDate());
    }
}
