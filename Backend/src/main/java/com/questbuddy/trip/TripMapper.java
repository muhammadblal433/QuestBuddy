package com.questbuddy.trip;

import org.springframework.stereotype.Component;

/**
 * This class is mainly for two things
 *
 * 1) Create a trip based on a CreateDTO
 * 2) Create a responseDTO based on a trip
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
}
