package com.questbuddy.events.trip.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.questbuddy.events.trip.dto.TripEventCreateDTO;
import com.questbuddy.events.trip.dto.TripEventEditDTO;
import com.questbuddy.events.trip.dto.TripEventResponseDTO;
import com.questbuddy.events.trip.model.TripEvent;

import java.util.Collections;
import java.util.List;

/**
 * This class does 3 things:
 *
 * 1) Creates an entity based on a CreateDTO
 *
 * 2) Create a ResponseDTO based on an entity
 *
 * 3) Apply an edit
 */
public final class TripEventMapper {
    private static final ObjectMapper M = new ObjectMapper();

    private TripEventMapper() {}

    // 1
    public static TripEvent fromCreate(Long tripId, Long creatorId, TripEventCreateDTO in) {
        TripEvent e = new TripEvent();
        e.setTripId(tripId);
        e.setCreatorId(creatorId);
        e.setName(in.name());
        e.setStartsAt(in.startsAt());
        e.setEndsAt(in.endsAt());
        e.setLocation(in.location());
        e.setNotes(in.notes());
        e.setPosition(in.position());
        e.setAttachmentRefsJson(toJson(in.attachmentRefs()));
        return e;
    }

    // 2
    public static TripEventResponseDTO toDTO(TripEvent e) {
        return new TripEventResponseDTO(
                e.getId(),
                e.getTripId(),
                e.getCreatorId(),
                e.getName(),
                e.getStartsAt(),
                e.getEndsAt(),
                e.getLocation(),
                e.getNotes(),
                e.getPosition(),
                fromJson(e.getAttachmentRefsJson()),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getDeletedAt() != null
        );
    }

    // 3
    public static void applyEdit(TripEvent e, TripEventEditDTO in) {
        if (in.name() != null) e.setName(in.name());
        if (in.startsAt() != null) e.setStartsAt(in.startsAt());
        if (in.endsAt() != null) e.setEndsAt(in.endsAt());
        if (in.location() != null) e.setLocation(in.location());
        if (in.notes() != null) e.setNotes(in.notes());
        if (in.position() != null) e.setPosition(in.position());
        if (in.attachmentRefs() != null) e.setAttachmentRefsJson(toJson(in.attachmentRefs()));
    }

    // ...Helpers,,,
    public static String toJson(List<String> refs) {
        try {
            return (refs == null || refs.isEmpty()) ? null : M.writeValueAsString(refs);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to serialize attachmentRefs", ex);
        }
    }

    public static List<String> fromJson(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return M.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception ex) {
            throw new RuntimeException("Failed to parse attachmentRefs", ex);
        }
    }
}
