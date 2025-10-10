package com.questbuddy.calendar;


import com.questbuddy.calendar.dto.*;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.util.List;


/**
 * Service Class to handle logic of events
 */
@Service
public class EventService {
    private final EventRepository repo;
    private final EventMapper mapper;


    // constructor
    public EventService (EventRepository repo, EventMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }


    // Helper mtds
    private static final Sort BY_START_ASC = Sort.by(Sort.Direction.ASC, "startAt");


    private void checkRange(Instant start, Instant end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new ValidationException("End time cannot be before start time!");
        }
    }


    @Transactional // if fail, do nothing
    public EventResponseDTO create(Long userId, EventCreateDTO dto) {
        checkRange(dto.startAt(), dto.endAt());
        Event e = mapper.toEntity(userId, dto);
        return mapper.toDto(repo.save(e));
    }


    // list (by user w/ optional range)
    public List<EventResponseDTO> list(Long userId, Instant from, Instant to) {
        if (from == null && to == null) {
            return repo.findAllByUserId(userId, BY_START_ASC).stream().map(mapper::toDto).toList();
        }
        if (from == null || to == null) {
            throw new ValidationException("Both 'from' and 'to' are required.");
        }
        checkRange(from, to);
        return repo.findAllByUserIdAndStartAtBetween(userId, from, to, BY_START_ASC).stream().map(mapper::toDto).toList();
    }


    // list ALL events by everyone
    public List<EventResponseDTO> listAll() {
        return repo.findAll(BY_START_ASC).stream().map(mapper::toDto).toList();
    }


    // list ALL events by everyone in a time t = [from, to]
    public List<EventResponseDTO> listAllBetween(Instant from, Instant to) {
        if (from == null || to == null) {
            throw new ValidationException("Both 'from' and 'to' are required.");
        }
        checkRange(from, to);
        return repo.findAllByStartAtBetween(from, to, BY_START_ASC)
                .stream().map(mapper::toDto).toList();
    }


    // events by userId (explicit)
    public List<EventResponseDTO> listByUser(Long userId) {
        return repo.findAllByUserId(userId, BY_START_ASC)
                .stream().map(mapper::toDto).toList();
    }


    // events by userId AND start..end (explicit)
    public List<EventResponseDTO> listByUserBetween(Long userId, Instant from, Instant to) {
        if (from == null || to == null) {
            throw new ValidationException("Both 'from' and 'to' are required.");
        }
        checkRange(from, to);
        return repo.findAllByUserIdAndStartAtBetween(userId, from, to, BY_START_ASC).stream().map(mapper::toDto).toList();
    }


    // New Error for if event not found
    public static class ResourceNotFound extends RuntimeException {
        public ResourceNotFound(String m) {
            super(m);
        }
    }


    public EventResponseDTO get(Long userId, Long id) {
        var opt = repo.findByIdAndUserId(id, userId);
        if (opt.isEmpty()) {
            throw new ResourceNotFound("No such event found.");
        }
        return mapper.toDto(opt.get());
    }


    @Transactional
    public EventResponseDTO update(Long userId, Long id, EventUpdateDTO dto) {
        var opt = repo.findByIdAndUserId(id, userId);
        if (opt.isEmpty()) {
            throw new ResourceNotFound("No such event found.");
        }
        Event e = opt.get();
        // Validate final time range after applying partial updates
        Instant newStart;
        Instant newEnd;
        if (dto.startAt() != null) {
            newStart = dto.startAt();
        } else {
            newStart = e.getStartAt();
        }
        if (dto.endAt() != null) {
            newEnd = dto.endAt();
        } else {
            newEnd = e.getEndAt();
        }
        checkRange(newStart, newEnd);


        mapper.applyUpdate(e, dto);
        return mapper.toDto(repo.save(e));
    }


    @Transactional
    public void delete(Long userId, Long id) {
        long n = repo.deleteByIdAndUserId(id, userId);
        if (n == 0) {
            throw new ResourceNotFound("Event not found");
        }
    }


}
