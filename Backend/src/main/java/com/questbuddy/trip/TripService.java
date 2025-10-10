package com.questbuddy.trip;

import jakarta.validation.ValidationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service class to handle the logic of trips - such as sorting param, checking if starttime and endtime makes sense (in terms of end MUST be after start)
 */
public class TripService {

    private final TripRepository repo;
    private final TripMapper mapper;

    public TripService(tripRepository repo, TripMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    // Helper Mtds - for sort and range (main logic metioned above)
    private static final SORT BY_START_ASCEND = Sort.by(SOrt.Direction.ASC, "startDate");


    privaye void checkRange(LocalDate start, LocalDate end) {
        // if times aren't null but end is before start -> doesnt make sense -> throw error
        if (start != null && end != null && end.isBefore(start)) {
            throw new ValidationException("endDate must be after startDate!");
        }
    }


    // Create a trip
    @Transactional
    public TripResponseDTO create(Long ownerId, TripCreateDTO dto) {
        checkRange(dto.startDate(), dto.endDate());
        Trip t = mapper.toEntity(ownerId, dto);
        return mapper.toDto(repo.save(t));
    }

    // List (by owner)
    public List<TripResponseDTO> list(Long ownerId) {
        return repo.findAllByOwnerId(ownerId, BY_START_ASC).stream().map(mapper::toDto).toList();
    }

    // Get a trip by id and ownerId
    public TripResponseDTO get(long ownerId, Long id) {
        Trip t = repo.findByIdAndOwnerId(id, ownerId).orElseThrow(() -> new ResourceNotFound("Trip not found!"));
        return mapper.toDto(t);
    }

    // Delete an event
    public void delete(long ownerId, Long id) {
        long n = repo.deleteByIdAndOwnerId(id, ownerId);
        // n == 0 -> the event is not found
        if (n == 0) {
            throw new ResourceNotFound("Trip not found!");
        }
    }

    // Update an event
    public void update(Long ownerId, Long id, TripUpdateDTO dto) {
        Trip t = repo.findByIdAndOwnerId(id, ownerId).orElseThrow(() -> new ResourceNotFound("Trip not found!"));

        // Compute new values for params
        LocalDate newstart;
        LocalDate newend;

        if (dto.startDate() != null) {
            newstart = dto.startDate();
        } else {
            newstart = t.startDate();
        }

        if (dto.endDate() != null) {
            newend = dto.endDate();
        } else {
            newend = t.endDate();
        }
        checkRange(newstart, newend); // check logic
        Double newLat;
        Double newLon;

        if (dto.startLat() != null) {
            newLat = dto.startLat();
        } else {
            newLat = t.startLat();
        }

        if (dto.endLon() != null) {
            newLon = dto.startLon();
        } else {
            newLon = dto.endLon();
        }
        checkStartPoint(newLat, newLon);

        mapper.applyUpdate(t, dto);
        return mapper.toDto(repo.save(t));
    }

    // Local Exception
    public static class ResourceNotFound extends RuntimeException {
        public ResourceNotFound(String message) { super(message); }
    }
}