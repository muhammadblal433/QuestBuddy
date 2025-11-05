package com.questbuddy.trip;

import jakarta.validation.ValidationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

// v12 membership
import com.questbuddy.tripmember.service.TripMembershipService;

/**
 * Service class to handle the logic of trips - such as sorting param, checking if starttime and endtime makes sense (in terms of end MUST be after start)
 */
@Service
public class TripService {

    private final TripRepository repo;
    private final TripMapper mapper;
    // Trip membership seeding for v12
    private final TripMembershipService tripMembershipService;

    public TripService(TripRepository repo, TripMapper mapper, TripMembershipService tripMembershipService) {
        this.repo = repo;
        this.mapper = mapper;
        this.tripMembershipService = tripMembershipService;
    }

    // Helper Mtds - for sort and range (main logic metioned above)
    private static final Sort BY_START_ASC = Sort.by(Sort.Direction.ASC, "startDate");

    private void checkRange(LocalDate start, LocalDate end) {
        // if times aren't null but end is before start -> doesnt make sense -> throw error
        if (start != null && end != null && end.isBefore(start)) {
            throw new ValidationException("endDate must be after startDate!");
        }
    }

    // ensure both or neither of startLat/startLon are provided
    private void checkStartPoint(Double lat, Double lon) {
        if ((lat == null) ^ (lon == null)) {
            throw new ValidationException("Provide both startLat and startLon together, or leave both null.");
        }
    }

    // Create a trip
    @Transactional
    public TripResponseDTO create(Long ownerId, TripCreateDTO dto) {
        checkRange(dto.startDate(), dto.endDate());
        // if your TripCreateDTO has startLat/startLon, validate the pair:
        try {
            var latField = TripCreateDTO.class.getDeclaredMethod("startLat");
            var lonField = TripCreateDTO.class.getDeclaredMethod("startLon");
            Double lat = (Double) latField.invoke(dto);
            Double lon = (Double) lonField.invoke(dto);
            checkStartPoint(lat, lon);
        } catch (Exception ignore) {
            /* if fields don't exist, skip */
        }

        Trip t = mapper.toEntity(ownerId, dto);

        // save and then seed owner membership so v12 gate doesn't 403 the owner
        Trip saved = repo.save(t);
        tripMembershipService.seedOwner(ownerId, saved.getId());

        return mapper.toDto(saved);
    }

    // List (by owner)
    public List<TripResponseDTO> list(Long ownerId) {
        return repo.findAllByOwnerId(ownerId, BY_START_ASC).stream().map(mapper::toDto).toList();
    }

    // Get a trip by id and ownerId
    public TripResponseDTO get(Long ownerId, Long id) {
        Trip t = repo.findByIdAndOwnerId(id, ownerId).orElseThrow(() -> new ResourceNotFound("Trip not found!"));
        return mapper.toDto(t);
    }

    // Delete an event
    @Transactional
    public void delete(Long ownerId, Long id) {
        long n = repo.deleteByIdAndOwnerId(id, ownerId);
        // n == 0 -> the event is not found
        if (n == 0) {
            throw new ResourceNotFound("Trip not found!");
        }
    }

    // Update an event
    @Transactional
    public TripResponseDTO update(Long ownerId, Long id, TripUpdateDTO dto) {
        Trip t = repo.findByIdAndOwnerId(id, ownerId).orElseThrow(() -> new ResourceNotFound("Trip not found!"));

        // Compute new values for params
        LocalDate newstart = (dto.startDate() != null) ? dto.startDate() : t.getStartDate();
        LocalDate newend   = (dto.endDate()   != null) ? dto.endDate()   : t.getEndDate();
        checkRange(newstart, newend); // check logic

        // merge potential coordinates if your DTO supports them
        // im going to start using ternary operatory ? for in line if-else
        Double newLat = (dto.startLat() != null) ? dto.startLat() : t.getStartLat();
        Double newLon = (dto.startLon() != null) ? dto.startLon() : t.getStartLon();
        checkStartPoint(newLat, newLon);

        mapper.applyUpdate(t, dto);
        return mapper.toDto(repo.save(t));
    }

    // Local Exception
    public static class ResourceNotFound extends RuntimeException {
        public ResourceNotFound(String message) { super(message); }
    }
}
